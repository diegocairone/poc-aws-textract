package com.cairone.textract.ui.ctrl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cairone.textract.AppException;
import com.cairone.textract.aws.TextractValue;
import com.cairone.textract.data.model.FileDescriptor;
import com.cairone.textract.service.AmazonTextractService;
import com.cairone.textract.service.DataService;
import com.cairone.textract.ui.exception.BadRequestException;
import com.cairone.textract.ui.exception.NotFoundException;
import com.cairone.textract.ui.request.FileDescriptorDto;
import com.cairone.textract.ui.response.KeyValueResponse;
import com.cairone.textract.util.ImageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/fileupload")
@RequiredArgsConstructor
public class FileUploadCtrl {
    
    private static String CONTENT_TYPE_PDF = "application/pdf";

    private final AmazonTextractService textractService;
    private final DataService dataService;
    
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FileDescriptor> getFileDescriptorById(@PathVariable("id") String id) {
        return dataService.findById(id)
                .map(descriptor -> ResponseEntity.ok(descriptor))
                .orElseThrow(
                        () -> new NotFoundException("File descriptor for ID %s not found"));
    }
    
    @GetMapping("{id}/content")
    public ResponseEntity<byte[]> getContentById(
            @PathVariable("id") String id) {
        return dataService.findContentById(id)
                .map(is -> {
                    try (BufferedInputStream buf = new BufferedInputStream(is)) {
                        String contentType = ImageUtil.detectMimeType(buf);
                        byte[] bytes = ImageUtil.createByteArray(buf);
                        return ResponseEntity
                                .status(HttpStatus.OK)
                                .header("Content-Type", contentType)
                                .body(bytes);
                    } catch (IOException e) {
                        throw new BadRequestException("Error reading content for ID %s", id);
                    }
                })
                .orElseThrow(
                        () -> new NotFoundException("File descriptor for ID %s not found", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FileDescriptor> createFileDescriptor(
            @RequestBody FileDescriptorDto fileDescriptorDto) {
        FileDescriptor fileDescriptor = dataService.createFile("principal", fileDescriptorDto);
        return ResponseEntity.ok(fileDescriptor);
    }

    @PostMapping("/content")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FileDescriptor> createFileDescriptorAndUploadContent(
            FileDescriptorDto fileDescriptorDto,
            @RequestParam("file") MultipartFile file) {
        
        FileDescriptor fileDescriptor = dataService.createFile("principal", fileDescriptorDto);
        String id = fileDescriptor.getId().toString();

        try (InputStream buffered = new BufferedInputStream(file.getInputStream())) {
            
            String contentType = ImageUtil.detectMimeType(buffered, file.getName());
            
            if (contentType.startsWith("image/")) {
                dataService.uploadSingleContent(id, file.getSize(), buffered);
            } else if (contentType.equals(CONTENT_TYPE_PDF)) {
                dataService.uploadPdfContent(id, file.getSize(), buffered);
            } else {
                throw new AppException("Content type not supported");
            }
            return ResponseEntity.ok(fileDescriptor);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }
    }

    @PostMapping("{id}/content")
    @ResponseStatus(HttpStatus.CREATED)
    public void handleUploadImageFile(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file) {
        
        try (InputStream buffered = new BufferedInputStream(file.getInputStream())) {
            
            String contentType = ImageUtil.detectMimeType(buffered, file.getName());
            
            if (contentType.startsWith("image/")) {
                dataService.uploadSingleContent(id, file.getSize(), buffered);
            } else if (contentType.equals(CONTENT_TYPE_PDF)) {
                dataService.uploadPdfContent(id, file.getSize(), buffered);
            } else {
                throw new AppException("Content type not supported");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }
    }

    @PostMapping("{id}/textract")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<KeyValueResponse> runTextractOverS3Content(
            @PathVariable("id") String id) {
        
        return dataService.findContentById(id)
                .map(is -> textractService.analyzeDocument(is))
                .map(KeyValueResponse::fromResultMap)
                .map(ResponseEntity::ok)
                .orElseThrow(
                    () -> new NotFoundException("File descriptor for ID %s not found", id));
    }

    @PostMapping("textract")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<KeyValueResponse> runTextractOverIncomingFile(
            @RequestParam("file") MultipartFile file) {

        InputStream is = ImageUtil.extractInputStream(file);
        Map<String, TextractValue> result = textractService.analyzeDocument(is);
        KeyValueResponse response = KeyValueResponse.fromResultMap(result);            
        return ResponseEntity.ok(response);
    }
}
