package com.cairone.textract.aws;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextractFormDataParser {

    // source
    private AnalyzeDocumentResult result;
    
    // destination
    private Map<String, TextractValue> keyValueSet;
    
    private Map<String, Block> allBlocks;
    private Map<String, Block> keySetBlocks;
    private Map<String, Block> valueSetBlocks;
    private Map<String, Block> wordBlocks;
    
    private Map<Block, Block> keyValueBlockPairMap;
    
    public TextractFormDataParser parse(AnalyzeDocumentResult result) {
        this.result = result;
        
        // Read all blocks in the result
        readBlocksMap();
        
        // Filter blocks that are of type: KEY_VALUE_SET and EntityTypes array contains "KEY"
        filterKeys();
        
        // Filter blocks that are of type: KEY_VALUE_SET and EntityTypes array contains "VALUE"
        filterValues();
        
        // Filter blocks that are of type: WORD
        filterWords();
        
        // Create a mapping between KEY_VALUE_SET of EntityType KEY and 
        // KEY_VALUE_SET of EntityType VALUE 
        findRelationshipsBetweenKeyValueSets();
        
        // Initialize
        this.keyValueSet = new HashMap<>();
        
        // Convert block map into text map by reading WORDS
        keyValueBlockPairMap.entrySet().forEach(entry -> toText(entry));
        
        return this;
    }
    
    public Map<String, TextractValue> getKeyValueSet() {
        return keyValueSet;
    }
    
    private void readBlocksMap() {
        this.allBlocks = result.getBlocks().stream()
                .collect(Collectors.toMap(Block::getId, Function.identity()));    
    }
    
    private void filterKeys() {
        this.keySetBlocks = allBlocks.entrySet()
                .stream()
                .filter(p -> p.getValue().getBlockType().equals("KEY_VALUE_SET") 
                        && p.getValue().getEntityTypes().contains("KEY"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void filterValues() {
        this.valueSetBlocks = allBlocks.entrySet()
                .stream()
                .filter(p -> p.getValue().getBlockType().equals("KEY_VALUE_SET") 
                        && p.getValue().getEntityTypes().contains("VALUE"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void filterWords() {
        this.wordBlocks = allBlocks.entrySet()
                .stream()
                .filter(p -> p.getValue().getBlockType().equals("WORD"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private void findRelationshipsBetweenKeyValueSets() {
        
        this.keyValueBlockPairMap = new HashMap<>();
        
        keySetBlocks.forEach((blockId, block) -> {
            block.getRelationships().stream()
                .filter(p -> p.getType().equals("VALUE"))
                .findAny()
                .ifPresent(relationship -> {
                   String valueSetBlockId = relationship.getIds().get(0);
                   Block valueSetBlock = this.valueSetBlocks.get(valueSetBlockId);
                   this.keyValueBlockPairMap.put(block, valueSetBlock);
                });
        });
    }
    
    private void toText(Entry<Block, Block> entry) {
        
        Block keySetBlock = entry.getKey();
        Block valueSetBlock = entry.getValue();
        
        List<BigDecimal> accuracies = new ArrayList<>();
        
        final StringBuilder keyBuilder = new StringBuilder();
        final StringBuilder valueBuilder = new StringBuilder();
        
        keySetBlock.getRelationships()
            .stream()
            .filter(p -> p.getType().equals("CHILD")).forEach(relationship -> {
                relationship.getIds().forEach(wordId -> {
                   Block word = this.wordBlocks.get(wordId);
                   BigDecimal confidence = new BigDecimal(word.getConfidence());
                   String text = word.getText();
                   keyBuilder.append(text + " ");
                   accuracies.add(confidence);
                });
            });
        
        if (valueSetBlock.getRelationships() != null) {
            
            valueSetBlock.getRelationships()
            .stream()
            .filter(p -> p.getType().equals("CHILD")).forEach(relationship -> {
                relationship.getIds().forEach(wordId -> {
                   Block word = this.wordBlocks.get(wordId);
                   BigDecimal confidence = new BigDecimal(word.getConfidence());
                   String text = word.getText();
                   valueBuilder.append(text + " ");
                   accuracies.add(confidence);
                });
            });
        }
        
        String keyText = keyBuilder.toString().trim();
        String valueText = valueBuilder.toString().trim();
        
        BigDecimal accuracy = accuracies.stream().min(BigDecimal::compareTo).get();
        TextractValue value = new TextractValue(valueText, accuracy);
        
        this.keyValueSet.put(keyText, value);
    }
}
