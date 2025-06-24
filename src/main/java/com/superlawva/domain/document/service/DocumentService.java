package com.superlawva.domain.document.service;

import com.superlawva.domain.document.dto.DocumentCreateDTO;
import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.entity.Document;
import com.superlawva.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final GridFSService gridFSService;

    /**
     * 문서 생성 (GridFS 연동)
     */
    public DocumentResponseDTO createDocument(DocumentCreateDTO request) {
        log.info("MongoDB에 문서 생성: {}", request.getOriginalFilename());
        
        // Document 엔티티 생성
        Document document = Document.builder()
            .userId(request.getUserId())
            .originalFilename(request.getOriginalFilename())
            .encryptedFileKey(UUID.randomUUID().toString())
            .documentType(request.getDocumentType() != null ? 
                request.getDocumentType() : Document.DocumentType.OTHER)
            .mimeType(request.getMimeType() != null ? 
                request.getMimeType() : "application/pdf")
            .fileSizeBytes(request.getFileSizeBytes() != null ? 
                request.getFileSizeBytes() : 0L)
            .status(Document.DocumentStatus.UPLOADED)
            .storageType(Document.StorageType.GRIDFS) // 기본적으로 GridFS 사용
            .metadata(createDefaultMetadata(request))
            .build();
        
        document = documentRepository.save(document);
        
        log.info("MongoDB 문서 생성 완료 - Document ID: {}", document.getId());
        return DocumentResponseDTO.fromEntity(document);
    }

    /**
     * 파일 내용과 함께 문서 생성
     */
    public DocumentResponseDTO createDocumentWithFile(DocumentCreateDTO request, byte[] fileContent) {
        log.info("파일과 함께 MongoDB 문서 생성: {}, 크기: {} bytes", 
                request.getOriginalFilename(), fileContent.length);
        
        // 파일 크기에 따라 저장 방식 결정
        Document.StorageType storageType = gridFSService.shouldUseGridFS(fileContent.length) 
                ? Document.StorageType.GRIDFS 
                : Document.StorageType.INLINE;
        
        String gridfsFileId = null;
        byte[] inlineContent = null;
        
        if (storageType == Document.StorageType.GRIDFS) {
            // GridFS에 파일 저장
            gridfsFileId = gridFSService.storeFile(
                fileContent, 
                request.getOriginalFilename(), 
                request.getMimeType()
            );
        } else {
            // 인라인으로 저장 (소용량 파일)
            inlineContent = fileContent;
        }
        
        // Document 엔티티 생성
        Document document = Document.builder()
            .userId(request.getUserId())
            .originalFilename(request.getOriginalFilename())
            .encryptedFileKey(UUID.randomUUID().toString())
            .documentType(request.getDocumentType() != null ? 
                request.getDocumentType() : Document.DocumentType.OTHER)
            .mimeType(request.getMimeType() != null ? 
                request.getMimeType() : "application/pdf")
            .fileSizeBytes((long) fileContent.length)
            .status(Document.DocumentStatus.UPLOADED)
            .storageType(storageType)
            .gridfsFileId(gridfsFileId)
            .fileContent(inlineContent)
            .metadata(createFileMetadata(request, fileContent))
            .build();
        
        document = documentRepository.save(document);
        
        log.info("파일과 함께 MongoDB 문서 생성 완료 - Document ID: {}, 저장 방식: {}", 
                document.getId(), storageType);
        return DocumentResponseDTO.fromEntity(document);
    }

    /**
     * 문서 파일 내용 조회
     */
    public byte[] getDocumentFile(String documentId) {
        log.info("문서 파일 내용 조회 - Document ID: {}", documentId);
        
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        if (document.getStorageType() == Document.StorageType.GRIDFS) {
            if (document.getGridfsFileId() == null) {
                throw new RuntimeException("GridFS 파일 ID가 없습니다: " + documentId);
            }
            return gridFSService.getFile(document.getGridfsFileId());
        } else if (document.getStorageType() == Document.StorageType.INLINE) {
            if (document.getFileContent() == null) {
                throw new RuntimeException("인라인 파일 내용이 없습니다: " + documentId);
            }
            return document.getFileContent();
        } else {
            throw new RuntimeException("지원하지 않는 저장 방식: " + document.getStorageType());
        }
    }

    public List<DocumentResponseDTO> getDocuments(Long userId) {
        List<Document> documents = documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return documents.stream()
            .map(DocumentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public DocumentResponseDTO getDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        return DocumentResponseDTO.fromEntity(document);
    }

    /**
     * 문서 삭제 (GridFS 파일도 함께 삭제)
     */
    public void deleteDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // GridFS 파일 삭제
        if (document.getStorageType() == Document.StorageType.GRIDFS && 
            document.getGridfsFileId() != null) {
            try {
                gridFSService.deleteFile(document.getGridfsFileId());
            } catch (Exception e) {
                log.warn("GridFS 파일 삭제 실패 (계속 진행): {}", e.getMessage());
            }
        }
        
        // 문서 삭제
        documentRepository.delete(document);
        log.info("문서 삭제 완료: {}", documentId);
    }

    /**
     * 문서 상태 업데이트
     */
    public void updateDocumentStatus(String documentId, Document.DocumentStatus status) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        document.setStatus(status);
        documentRepository.save(document);
        
        log.info("문서 상태 업데이트 완료 - Document ID: {}, Status: {}", documentId, status);
    }

    /**
     * 메타데이터 업데이트
     */
    public void updateDocumentMetadata(String documentId, Map<String, Object> metadata) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        if (document.getMetadata() == null) {
            document.setMetadata(new HashMap<>());
        }
        
        document.getMetadata().putAll(metadata);
        documentRepository.save(document);
        
        log.info("문서 메타데이터 업데이트 완료 - Document ID: {}", documentId);
    }

    // ==================== Private Methods ====================

    private Map<String, Object> createDefaultMetadata(DocumentCreateDTO request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("uploadTime", System.currentTimeMillis());
        metadata.put("version", "1.0");
        if (request.getDocumentType() != null) {
            metadata.put("documentType", request.getDocumentType().name());
        }
        return metadata;
    }

    private Map<String, Object> createFileMetadata(DocumentCreateDTO request, byte[] fileContent) {
        Map<String, Object> metadata = createDefaultMetadata(request);
        metadata.put("fileSize", fileContent.length);
        metadata.put("fileSizeFormatted", formatFileSize(fileContent.length));
        metadata.put("hasContent", true);
        return metadata;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
} 