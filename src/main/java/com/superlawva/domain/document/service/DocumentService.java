package com.superlawva.domain.document.service;

import com.superlawva.domain.document.dto.DocumentCreateDTO;
import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.entity.DocumentEntity;
import com.superlawva.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    /**
     * 문서 생성
     */
    public DocumentResponseDTO createDocument(DocumentCreateDTO request) {
        log.info("데이터베이스에 문서 생성: {}", request.getOriginalFilename());
        
        // Document 엔티티 생성
        DocumentEntity document = DocumentEntity.builder()
            .userId(request.getUserId())
            .originalFilename(request.getOriginalFilename())
            .encryptedFileKey(UUID.randomUUID().toString())
            .documentType(request.getDocumentType() != null ? 
                request.getDocumentType() : DocumentEntity.DocumentType.OTHER)
            .mimeType(request.getMimeType() != null ? 
                request.getMimeType() : "application/pdf")
            .fileSizeBytes(request.getFileSizeBytes() != null ? 
                request.getFileSizeBytes() : 0L)
            .status(DocumentEntity.DocumentStatus.UPLOADED)
            .storageType(DocumentEntity.StorageType.INLINE) // 기본적으로 INLINE 사용
            .createdAt(LocalDateTime.now())
            .metadataJson(createDefaultMetadataJson(request))
            .build();
        
        document = documentRepository.save(document);
        
        log.info("데이터베이스 문서 생성 완료 - Document ID: {}", document.getId());
        return DocumentResponseDTO.fromEntity(document);
    }

    /**
     * 파일 내용과 함께 문서 생성
     */
    public DocumentResponseDTO createDocumentWithContent(DocumentCreateDTO request, byte[] fileContent) {
        log.info("파일 내용과 함께 문서 생성: {}, 크기: {} bytes", 
                request.getOriginalFilename(), fileContent.length);

        DocumentEntity.StorageType storageType = shouldUseExternalStorage(fileContent.length)
                ? DocumentEntity.StorageType.EXTERNAL
                : DocumentEntity.StorageType.INLINE;

        DocumentEntity document = DocumentEntity.builder()
                .userId(request.getUserId())
                .originalFilename(request.getOriginalFilename())
                .encryptedFileKey(UUID.randomUUID().toString())
                .documentType(request.getDocumentType() != null ?
                        request.getDocumentType() : DocumentEntity.DocumentType.OTHER)
                .mimeType(request.getMimeType() != null ?
                        request.getMimeType() : "application/pdf")
                .fileSizeBytes((long) fileContent.length)
                .status(DocumentEntity.DocumentStatus.UPLOADED)
                .storageType(storageType)
                .createdAt(LocalDateTime.now())
                .fileContent(storageType == DocumentEntity.StorageType.INLINE ? fileContent : null)
                .metadataJson(createDefaultMetadataJson(request))
                .build();

        document = documentRepository.save(document);

        log.info("파일 내용과 함께 문서 생성 완료 - Document ID: {}", document.getId());
        return DocumentResponseDTO.fromEntity(document);
    }

    /**
     * 문서 파일 내용 조회
     */
    @Transactional(readOnly = true)
    public byte[] getDocumentContent(Long documentId) {
        log.info("문서 파일 내용 조회 - Document ID: {}", documentId);
        
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다: " + documentId));

        if (document.getStorageType() == DocumentEntity.StorageType.INLINE) {
            if (document.getFileContent() == null) {
                throw new RuntimeException("파일 내용이 없습니다: " + documentId);
            }
            return document.getFileContent();
        } else {
            throw new RuntimeException("외부 저장소는 아직 구현되지 않았습니다: " + documentId);
        }
    }

    /**
     * 사용자의 문서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getUserDocuments(Long userId) {
        log.info("사용자 문서 목록 조회 - User ID: {}", userId);
        
        List<DocumentEntity> documents = documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return documents.stream()
                .map(DocumentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 문서 삭제
     */
    public void deleteDocument(Long documentId) {
        log.info("문서 삭제 - Document ID: {}", documentId);
        
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다: " + documentId));

        // 문서 삭제
        documentRepository.delete(document);
        
        log.info("문서 삭제 완료 - Document ID: {}", documentId);
    }

    /**
     * 기본 메타데이터 생성
     */
    private String createDefaultMetadataJson(DocumentCreateDTO request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("upload_timestamp", LocalDateTime.now().toString());
        metadata.put("original_filename", request.getOriginalFilename());
        metadata.put("user_id", request.getUserId());
        metadata.put("document_type", request.getDocumentType() != null ? 
                request.getDocumentType().toString() : "OTHER");
        
        try {
            // JSON 문자열로 변환 (간단한 구현)
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            log.warn("메타데이터 JSON 생성 실패: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 외부 저장소 사용 여부 결정
     */
    private boolean shouldUseExternalStorage(long fileSize) {
        // 10MB 이상이면 외부 저장소 사용 (현재는 미구현)
        return fileSize > 10 * 1024 * 1024;
    }
} 