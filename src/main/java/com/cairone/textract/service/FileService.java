package com.cairone.textract.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Document;
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
            
            //System.out.println(result.toString());
            TextractFormDataParser parser = new TextractFormDataParser();
            
            Map<String, String> kv = parser.parse(result).getKeyValueSet();
            
            kv.forEach((key, value) -> {
                log.info("{} => {}", key, value);
            });
            
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
