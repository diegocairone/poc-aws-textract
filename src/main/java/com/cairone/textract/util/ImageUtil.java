package com.cairone.textract.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.cairone.textract.AppException;
import com.cairone.textract.ui.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtil {

    private static String FORMAT_NAME = "PNG";
    private static int FIRST_PAGE_INDEX = 0;

    public static InputStream getImageFromPage(PDFRenderer renderer, int pageIdx) {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            BufferedImage image = renderer.renderImage(pageIdx);
            ImageIO.write(image, FORMAT_NAME, os);
            InputStream output = new ByteArrayInputStream(os.toByteArray());
            return output;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }        
    }
    
    public static InputStream pdfPageToPngImage(InputStream is, int pageIdx) {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); PDDocument document = PDDocument.load(is)) {

            PDFRenderer renderer = new PDFRenderer(document);
            return getImageFromPage(renderer, pageIdx);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }        
    }
    
    public static InputStream pdfPageToPngImage(InputStream is) {
        return pdfPageToPngImage(is, FIRST_PAGE_INDEX);
    }

    public static InputStream extractInputStream(MultipartFile file) {
        try {
            if (file.getContentType().equals("application/pdf")) {
                InputStream pdfIS = file.getInputStream();
                return ImageUtil.pdfPageToPngImage(pdfIS);
            } else if (file.getContentType().startsWith("image/")) {
                return file.getInputStream();
            } else {
                throw new AppException("File is invalid!");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new AppException(e, e.getMessage());
        }
    }

    public static String determineMimeType(InputStream inputStream, Metadata metadata) {
        DefaultDetector detector = new DefaultDetector();
        String mimeType = null;

        try {
            MediaType e = detector.detect(inputStream, metadata);
            if (e != null) {
                mimeType = e.getType() + "/" + e.getSubtype();
            }
        } catch (IOException e) {
            String error = "Unable to detect mimeType for inputStream: ";
            log.warn(error + e.getMessage());
            mimeType = "application/octet-stream";
        }

        return mimeType;
    }

    public static String detectMimeType(byte[] fileData, String filename) {
        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(fileData)) {
            Metadata e = new Metadata();
            e.set("resourceName", filename);
            String mimeType = determineMimeType(byteInputStream, e);
            return mimeType;
        } catch (IOException e) {
            throw new AppException("Unable to detect Mime type", e);
        }
    }

    public static String detectMimeType(InputStream inputStream) {
        return detectMimeType(inputStream, "");
    }
    
    public static String detectMimeType(InputStream inputStream, String filename) {
        Metadata e = new Metadata();
        e.set("resourceName", filename);
        return determineMimeType(inputStream, e);
    }
    
    public static ByteArrayOutputStream createBaos(InputStream is) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] objectBytes = IOUtils.toByteArray(is);
            outputStream.write(objectBytes, 0, objectBytes.length);
            return outputStream;
        } catch (IOException e) {
            throw new AppException(e, "Unable to create byte array");
        }
    }
    
    public static byte[] createByteArray(InputStream is) {
        ByteArrayOutputStream baos = createBaos(is);
        return baos.toByteArray();
    }
}
