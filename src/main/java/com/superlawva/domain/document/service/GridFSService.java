package com.superlawva.domain.document.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridFSService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    /**
     * 파일을 GridFS에 저장
     */
    public String storeFile(byte[] fileContent, String filename, String contentType) {
        try {
            log.info("GridFS에 파일 저장 시작 - 파일명: {}, 크기: {} bytes", filename, fileContent.length);
            
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            
            // 메타데이터 설정
            org.bson.Document metadata = new org.bson.Document();
            metadata.put("originalName", filename);
            metadata.put("contentType", contentType);
            metadata.put("uploadTime", System.currentTimeMillis());
            
            // GridFS에 저장
            org.bson.types.ObjectId fileId = gridFsTemplate.store(
                inputStream, 
                filename, 
                contentType, 
                metadata
            );
            
            log.info("GridFS 파일 저장 완료 - File ID: {}", fileId.toString());
            return fileId.toString();
            
        } catch (Exception e) {
            log.error("GridFS 파일 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GridFS에서 파일 조회
     */
    public byte[] getFile(String fileId) {
        try {
            log.info("GridFS에서 파일 조회 - File ID: {}", fileId);
            
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(fileId))
            );
            
            if (gridFSFile == null) {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + fileId);
            }
            
            InputStream inputStream = gridFsOperations.getResource(gridFSFile).getInputStream();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] fileContent = outputStream.toByteArray();
            log.info("GridFS 파일 조회 완료 - 크기: {} bytes", fileContent.length);
            
            return fileContent;
            
        } catch (IOException e) {
            log.error("GridFS 파일 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GridFS에서 파일 삭제
     */
    public void deleteFile(String fileId) {
        try {
            log.info("GridFS에서 파일 삭제 - File ID: {}", fileId);
            
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
            
            log.info("GridFS 파일 삭제 완료 - File ID: {}", fileId);
            
        } catch (Exception e) {
            log.error("GridFS 파일 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(fileId))
            );
            return gridFSFile != null;
        } catch (Exception e) {
            log.error("파일 존재 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 파일 메타데이터 조회
     */
    public org.bson.Document getFileMetadata(String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(fileId))
            );
            
            if (gridFSFile == null) {
                return null;
            }
            
            return gridFSFile.getMetadata();
            
        } catch (Exception e) {
            log.error("파일 메타데이터 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 파일 크기 기준으로 저장 방식 결정
     */
    public boolean shouldUseGridFS(long fileSize) {
        // 1MB 이상이면 GridFS 사용
        return fileSize > 1024 * 1024;
    }
} 