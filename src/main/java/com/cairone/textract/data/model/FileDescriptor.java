package com.cairone.textract.data.model;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class FileDescriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    @EqualsAndHashCode.Include
    private UUID id;

    private String filetype;

    private boolean isPHI;

    @Enumerated(EnumType.STRING)
    private AttachedTo attachedTo;

    private String attachedToId;

    private String description;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Timestamp createdTimestamp;
    @Column(nullable = false)
    private String createdBy;

    private Timestamp deletedTimestamp;
    private String deletedBy;

    private Timestamp updatedTimestamp;
    private String updatedBy;

    private boolean isThumbnail;

    private UUID parentDescriptor;

    private int version;

    @Builder
    public FileDescriptor(UUID id, String filetype, boolean isPHI, AttachedTo attachedTo, 
            String attachedToId, String description, String filename, Timestamp createdTimestamp, 
            String createdBy, Timestamp deletedTimestamp, String deletedBy, 
            Timestamp updatedTimestamp, String updatedBy, boolean isThumbnail, 
            UUID parentDescriptor, int version) {
        super();
        this.id = id;
        this.filetype = filetype;
        this.isPHI = isPHI;
        this.attachedTo = attachedTo;
        this.attachedToId = attachedToId;
        this.description = description;
        this.filename = filename;
        this.createdTimestamp = createdTimestamp;
        this.createdBy = createdBy;
        this.deletedTimestamp = deletedTimestamp;
        this.deletedBy = deletedBy;
        this.updatedTimestamp = updatedTimestamp;
        this.updatedBy = updatedBy;
        this.isThumbnail = isThumbnail;
        this.parentDescriptor = parentDescriptor;
        this.version = version;
    }

}
