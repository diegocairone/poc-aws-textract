package com.cairone.textract.ui.ctrl;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cairone.textract.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fileupload")
@RequiredArgsConstructor
public class FileUploadCtrl {

    private final FileService fileService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void handleUploadFile(@RequestParam("file") MultipartFile file) {
        try {
            
            fileService.doYourTrick(file.getInputStream());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
