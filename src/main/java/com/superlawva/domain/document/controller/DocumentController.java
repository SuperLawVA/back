package com.superlawva.domain.document.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.dto.DocumentCreateDTO;
import com.superlawva.domain.document.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Document API", description = "문서 관리 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "문서 생성", 
              description = "새로운 문서를 생성합니다. (테스트용)")
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> createDocument(
            @Valid @RequestBody DocumentCreateDTO request) {
        
        log.info("Creating document: {}", request.getOriginalFilename());
        
        DocumentResponseDTO response = documentService.createDocument(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "문서 목록 조회", 
              description = "사용자의 모든 문서를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getDocuments(
            @RequestParam(required = false) Long userId) {
        
        List<DocumentResponseDTO> documents = documentService.getDocuments(userId != null ? userId : 1L);
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "문서 상세 조회", 
              description = "특정 문서의 상세 정보를 조회합니다.")
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponseDTO> getDocument(@PathVariable String documentId) {
        DocumentResponseDTO document = documentService.getDocument(documentId);
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "문서 삭제", 
              description = "문서를 삭제합니다.")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok().build();
    }
} 