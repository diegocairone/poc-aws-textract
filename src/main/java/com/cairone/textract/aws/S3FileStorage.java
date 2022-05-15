package com.cairone.textract.aws;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.cairone.textract.AppException;
import com.cairone.textract.data.model.FileDescriptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorage {

    private final AmazonS3 amazonS3;

    @Value("${app.filestore.bucketname}")
    private String bucketName;

    public long saveFile(FileDescriptor descriptor, InputStream fileInputStream, long streamLengthInByte) {

        String descriptorId = descriptor.getId().toString();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(streamLengthInByte);
            amazonS3.putObject(bucketName, descriptor.getId().toString(), fileInputStream, metadata);
            return streamLengthInByte;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(e, "Error saving content for Descriptor %s!", descriptorId);
        }
    }

    public void deleteFile(String descriptorId) {
        try {
            amazonS3.deleteObject(bucketName, descriptorId);
        } catch (Exception e) {
            throw new AppException(e, "Unable to delete file %s", descriptorId);
        }
    }
    
    public InputStream getContent(String id) {
        S3Object object = amazonS3.getObject(bucketName, id);
        InputStream is = object.getObjectContent();
        return is;
    }
    
    public InputStream getContent(UUID id) {
        return getContent(id.toString());
    }
}
