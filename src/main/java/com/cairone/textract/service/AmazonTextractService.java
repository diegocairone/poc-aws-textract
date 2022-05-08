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
import com.cairone.textract.aws.TextractFormDataParser;
import com.cairone.textract.aws.TextractValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AmazonTextractService {

    public Map<String, TextractValue> analyzeDocument(InputStream is) throws IOException {
        
        ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(is));
        
        EndpointConfiguration endpoint = new EndpointConfiguration(
                "https://textract.us-east-1.amazonaws.com", "us-east-1");
        
        AmazonTextract client = AmazonTextractClientBuilder.standard()
                .withEndpointConfiguration(endpoint).build();
        
        AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                .withFeatureTypes("FORMS")
                .withDocument(new Document().withBytes(imageBytes));
        
        AnalyzeDocumentResult result = client.analyzeDocument(request);
        TextractFormDataParser parser = new TextractFormDataParser();
        
        Map<String, TextractValue> kv = parser.parse(result).getKeyValueSet();
        kv.forEach((key, value) -> log.debug("{} => {}", key, value));
        
        return kv;
    }
}
