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
    @Schema(description = "등록할 법률 용어의 이름", example = "임차권등기명령", requiredMode = Schema.RequiredMode.REQUIRED)
    private String word;    // 용어명

    @NotBlank(message = "설명은 필수입니다.")
    @Schema(description = "해당 법률 용어에 대한 상세한 정의 및 설명", example = "임대차 계약이 종료된 후 보증금을 돌려받지 못한 임차인이 법원에 신청하여 등기부등본에 관련 사실을 기재하는 제도", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content; // 용어 설명
}