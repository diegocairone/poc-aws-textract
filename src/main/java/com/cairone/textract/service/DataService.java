package com.cairone.textract.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.textract.AppException;
import com.cairone.textract.aws.S3FileStorage;
import com.cairone.textract.data.model.FileDescriptor;
import com.cairone.textract.data.repository.FileDescriptorRepository;
import com.cairone.textract.ui.request.FileDescriptorDto;
import com.cairone.textract.util.ImageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataService {

    private final FileDescriptorRepository fileDescriptorRepository;
    private final S3FileStorage fileStorage;

    public FileDescriptor createFile(String principal, FileDescriptorDto fileDescriptorDto) {

        int version = generateVersion(fileDescriptorDto);

        fileDescriptorDto.setCreatedBy(principal);
        fileDescriptorDto.setCreatedTimestamp(Instant.now().toEpochMilli());
        fileDescriptorDto.setFileSizeBytes(0L);
        fileDescriptorDto.setVersion(version);

        FileDescriptor fileDescriptor = createFileDescriptor(fileDescriptorDto);

        return fileDescriptorRepository.save(fileDescriptor);
    }

    public Optional<FileDescriptor> findById(String id) {
        return fileDescriptorRepository.findById(UUID.fromString(id));
    }
    
    public Optional<InputStream> findContentById(String id) {
        return fileDescriptorRepository.findById(UUID.fromString(id))
                .map(descriptor -> fileStorage.getContent(descriptor.getId()));
    }

    @Transactional
    public void uploadPdfContent(String fileDescriptorId, long fileSizeInBytes, InputStream fileInputStream) {

        FileDescriptor parentDescriptor = findById(fileDescriptorId)
                .orElseThrow(() -> new AppException("File descriptor not found!"));

        int lastIdxOfDot = parentDescriptor.getFilename().lastIndexOf(".");
        String parentFileName = parentDescriptor.getFilename();
        String name = parentFileName.substring(0, lastIdxOfDot);

        try (PDDocument pdfDocument = PDDocument.load(fileInputStream)) {
            
            PDFRenderer renderer = new PDFRenderer(pdfDocument);
            int pages = pdfDocument.getNumberOfPages();

            for (int page = 0; page < pages; page++) {
                
                InputStream image = ImageUtil.getImageFromPage(renderer, page);
                
                FileDescriptorDto nestedDescriptorDto = new FileDescriptorDto();
                nestedDescriptorDto.setDescription(parentDescriptor.getDescription());
                nestedDescriptorDto.setFilename(name + "-" + page + ".png");
                nestedDescriptorDto.setFiletype("png");
                nestedDescriptorDto.setPHI(parentDescriptor.isPHI());
                nestedDescriptorDto.setAttachedTo(parentDescriptor.getAttachedTo());
                nestedDescriptorDto.setAttachedToId(parentDescriptor.getAttachedToId());
                nestedDescriptorDto.setThumbnail(parentDescriptor.isThumbnail());

                FileDescriptor nestedDescriptor = createFile("principal", nestedDescriptorDto);
                String nestedDescriptorId = nestedDescriptor.getId().toString();
                uploadSingleContent(nestedDescriptorId, image.available(), image);
            }
            
            fileDescriptorRepository.delete(parentDescriptor);
            
        } catch (IOException e) {
            throw new AppException(e, "Error updating PDF file");
        }
    }

    @Transactional
    public void uploadSingleContent(String fileDescriptorId, long fileSizeInBytes, InputStream fileInputStream) {

        FileDescriptor descriptor = findById(fileDescriptorId)
                .orElseThrow(() -> new AppException("File descriptor not found!"));

        try (InputStream buffered = new BufferedInputStream(fileInputStream)) {

            String contentType = ImageUtil.detectMimeType(buffered, descriptor.getFilename());
            descriptor.setFiletype(contentType);

            fileDescriptorRepository.save(descriptor);
            fileStorage.saveFile(descriptor, buffered, fileSizeInBytes);

        } catch (IOException e) {
            throw new AppException(e, "Error updating file descriptor");
        }
    }

    private FileDescriptor createFileDescriptor(FileDescriptorDto fileDescriptorDto) {

        FileDescriptor.FileDescriptorBuilder builder = FileDescriptor.builder().id(fileDescriptorDto.getId())
                .description(fileDescriptorDto.getDescription()).attachedTo(fileDescriptorDto.getAttachedTo())
                .attachedToId(fileDescriptorDto.getAttachedToId()).filetype(fileDescriptorDto.getFiletype())
                .isPHI(fileDescriptorDto.isPHI()).filename(fileDescriptorDto.getFilename())
                .createdBy(fileDescriptorDto.getCreatedBy())
                .createdTimestamp(new Timestamp(fileDescriptorDto.getCreatedTimestamp()))
                .updatedBy(fileDescriptorDto.getUpdatedBy()).deletedBy(fileDescriptorDto.getDeletedBy())
                .isThumbnail(fileDescriptorDto.isThumbnail()).version(fileDescriptorDto.getVersion());

        if (fileDescriptorDto.getUpdatedTimestamp() > 0) {
            builder.updatedTimestamp(new Timestamp(fileDescriptorDto.getUpdatedTimestamp()));
        }

        if (fileDescriptorDto.getDeletedTimestamp() > 0) {
            builder.deletedTimestamp(new Timestamp(fileDescriptorDto.getDeletedTimestamp()));
        }

        return builder.build();
    }

    private int generateVersion(final FileDescriptorDto fileDescriptorDto) {
        return 1;
    }
}
