package com.cairone.textract.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {

    public void doYourTrick(byte[] bytes) {
        String encodedFile = Base64.encodeBase64String(bytes);
        log.info(encodedFile);
    }
}
