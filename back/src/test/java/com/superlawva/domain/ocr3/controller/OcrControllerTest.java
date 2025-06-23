package com.superlawva.domain.ocr3.controller;

import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.service.OcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OcrControllerTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private OcrController ocrController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ocrController).build();
    }

    @Test
    void uploadAndProcessContract_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-contract.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        ContractData contractData = new ContractData();
        contractData.setId("test-id");
        contractData.setContractType("전세");
        
        OcrResponse response = OcrResponse.builder()
                .contractData(contractData)
                .debugMode(false)
                .build();

        when(ocrService.processContract(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/upload/ocr3")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contract_data.id").value("test-id"))
                .andExpect(jsonPath("$.contract_data.contract_type").value("전세"));
    }

    @Test
    void uploadAndProcessContract_EmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/ocr3")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMPTY_FILE"));
    }

    @Test
    void uploadAndProcessContract_InvalidFileType() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/ocr3")
                        .file(textFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"));
    }
} 