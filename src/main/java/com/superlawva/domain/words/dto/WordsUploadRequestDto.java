package com.superlawva.domain.words.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "법률 용어 등록 요청 DTO")
public class WordsUploadRequestDto {

    @NotBlank(message = "용어명은 필수입니다.")
    @Size(max = 255, message = "용어명은 255자를 초과할 수 없습니다.")
    @Schema(description = "용어명", example = "신규용어", requiredMode = Schema.RequiredMode.REQUIRED)
    private String word;    // 용어명

    @NotBlank(message = "설명은 필수입니다.")
    @Schema(description = "용어 설명", example = "새로운 법률 용어의 정의", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content; // 용어 설명
}