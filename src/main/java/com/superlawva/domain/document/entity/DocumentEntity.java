package com.superlawva.domain.document.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "encrypted_file_key")
    private String encryptedFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type")
    private StorageType storageType;

    @Column(name = "gridfs_file_id")
    private String gridfsFileId;

    @Lob
    @Column(name = "file_content", columnDefinition = "LONGBLOB")
    private byte[] fileContent;

    @Lob
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    public enum DocumentType {
        LEASE_JEONSE, LEASE_MONTHLY, CERTIFICATE, CONTENT_PROOF, OTHER
    }

    public enum DocumentStatus {
        UPLOADED, OCR_PROCESSING, OCR_COMPLETED, OCR_FAILED
    }

    public enum StorageType {
        GRIDFS, INLINE, EXTERNAL
    }
}
