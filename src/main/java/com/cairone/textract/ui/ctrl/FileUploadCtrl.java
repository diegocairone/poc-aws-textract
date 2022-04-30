package com.cairone.textract.ui.ctrl;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.cairone.textract.service.AmazonTextractService;
import com.cairone.textract.ui.exception.BadRequestException;
import com.cairone.textract.ui.response.KeyValueResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/fileupload")
@RequiredArgsConstructor
public class FileUploadCtrl {

    private final AmazonTextractService textractService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<KeyValueResponse> handleUploadFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            Map<String, String> result = textractService.analyzeDocument(file.getInputStream());
            KeyValueResponse response = new KeyValueResponse(result);
            return ResponseEntity.ok(response);
        } catch (IOException | AmazonServiceException e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }
    }
}
