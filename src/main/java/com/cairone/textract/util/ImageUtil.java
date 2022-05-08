package com.cairone.textract.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.cairone.textract.ui.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtil {

    private static String FORMAT_NAME = "PNG";
    private static int FIRST_PAGE_INDEX = 0;
    
    public static InputStream pdfPageToPngImage(InputStream is) {
        
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); 
                PDDocument document = PDDocument.load(is)) {
            
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(FIRST_PAGE_INDEX);
            ImageIO.write(image, FORMAT_NAME, os);
            InputStream output = new ByteArrayInputStream(os.toByteArray());
            return output;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(e, e.getMessage());
        }
    }
}
