package com.cairone.textract.ui.request;

import java.util.UUID;

import com.cairone.textract.data.model.AttachedTo;

import lombok.Data;

@Data
public class FileDescriptorDto {

    private UUID id;
    private String description;
    private long fileSizeBytes;
    private String filename;
    private String filetype;
    private boolean isPHI;

    private AttachedTo attachedTo;
    private String attachedToId;

    private String createdBy;
    private long createdTimestamp;

    private String deletedBy;
    private long deletedTimestamp;

    private String updatedBy;
    private long updatedTimestamp;

    private boolean isThumbnail;

    private int version;

}
