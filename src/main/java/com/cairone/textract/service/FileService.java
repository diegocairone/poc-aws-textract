package com.cairone.textract.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.Relationship;
import com.amazonaws.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {

    public void doYourTrick(InputStream is) {
        
        try {
            ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(is));
            
            EndpointConfiguration endpoint = new EndpointConfiguration(
                    "https://textract.us-east-1.amazonaws.com", "us-east-1");
            
            AmazonTextract client = AmazonTextractClientBuilder.standard()
                    .withEndpointConfiguration(endpoint).build();
            
            AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                    .withFeatureTypes("FORMS")
                    .withDocument(new Document().withBytes(imageBytes));
            
            AnalyzeDocumentResult result = client.analyzeDocument(request);
            
            System.out.println(result.toString());
            
            final Map<String, Block> blockMap = getBlocksMap(result);
            final Map<String, Block> keyBlockMap = getKeyBlockMap(blockMap);
            final Map<String, Block> valueBlockMap = getValueBlockMap(blockMap);
            
            Map<String, String> kv = getKeyValueRelationship(
                    keyBlockMap, valueBlockMap, valueBlockMap);
            
            kv.forEach((key, value) -> {
                log.info(key + " => " + value);
            });
            
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private Map<String, Block> getBlocksMap(AnalyzeDocumentResult result) {
        return result.getBlocks().stream()
                .collect(Collectors.toMap(Block::getId, Function.identity()));    
    }
    
    private Map<String, Block> getValueBlockMap(final Map<String, Block> blockMap) {
        return blockMap.entrySet()
                .stream()
                .filter(p -> p.getValue().getBlockType().equals("KEY_VALUE_SET") 
                        && !p.getValue().getEntityTypes().contains("KEY"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Block> getKeyBlockMap(final Map<String, Block> blockMap) {
        return blockMap.entrySet()
                .stream()
                .filter(p -> p.getValue().getBlockType().equals("KEY_VALUE_SET") 
                        && p.getValue().getEntityTypes().contains("KEY"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, String> getKeyValueRelationship(
            Map<String, Block> keyBlockMap, Map<String, Block> valueBlockMap, Map<String, Block> blockMap) {
        
        Map<String, String> kv = new HashMap<>();
        for (Entry<String, Block> entry : keyBlockMap.entrySet()) {
            Block valueBlock = findValueBlock(entry.getValue(), valueBlockMap);
            String key = getText(entry.getValue(), blockMap);
            String value = getText(valueBlock, blockMap);
            kv.put(key, value);
        }
        return kv;
    }
    
    private Block findValueBlock(Block key, Map<String, Block> valueBlockMap) {
        for (Relationship relationship : key.getRelationships()) {
            if (relationship.getType().equals("VALUE")) {
                for (String id : relationship.getIds()) {
                    return valueBlockMap.get(id);
                }
            }
        }
        return null;
    }
    
    private String getText(Block result, Map<String, Block> blockMap) {
        String text = "";
        if (result.getRelationships().isEmpty()) {
            return text;
        }
        for (Relationship relationship : result.getRelationships()) {
            if (relationship.getType().equals("CHILD")) {
                for (String id : relationship.getIds()) {
                    Block word = blockMap.get(id);
                    if (word != null) {
                        if (word.getBlockType().equals("WORD")) {
                            text += word.getText() + " ";
                        } else if (word.getBlockType().equals("SELECTION_ELEMENT") && word.getSelectionStatus().equals("SELECTED")) {
                            text += "X ";
                        }
                    }
                }
            }
        }
        return text;
    }
}
