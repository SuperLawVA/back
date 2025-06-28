package com.superlawva.domain.chatbot.controller;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
import com.superlawva.domain.chatbot.dto.spec.*;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import com.superlawva.domain.chatbot.service.ChatbotService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chtbot")
@RequiredArgsConstructor
@Tag(name = "🤖 Chatbot API", description = "AI 챗봇 대화 및 세션 관리 API")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(
        summary = "🗨️ 챗봇과 대화", 
        description = """
        ## 📖 API 설명
        AI 챗봇과 법률 관련 대화를 나눕니다. 사용자의 질문에 대해 법률적 조언과 답변을 제공합니다.


        
        ### 2. 대화 흐름
        1. **첫 대화**: `session_id`를 `null`로 전송
        2. **응답 수신**: 챗봇이 `session_id`와 답변을 반환
        3. **연속 대화**: 받은 `session_id`로 계속 대화
     
        
        ### 4. 질문 유형 (question_type)
        - `contract_issue`: 계약 관련 문제
        - `legal_advice`: 법률 상담
        - `document_help`: 문서 작성 도움
        - `general`: 일반적인 질문
        
    
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 챗봇 응답 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "answer": "보증금 반환과 관련하여 다음과 같은 법적 조치를 취할 수 있습니다. 먼저 임대차계약서를 확인하여 보증금 반환 조건을 살펴보시고, 임대인에게 서면으로 반환을 요구하세요. 만약 임대인이 응하지 않는다면 주택임대차보호법에 따라 법원에 소송을 제기할 수 있습니다.",
                        "session_id": "session_123",
                        "question_type": "contract_issue",
                        "response_time_seconds": 2.5,
                        "timestamp": "2025-01-15T10:30:00Z"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패 (JWT 토큰 필요)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON401",
                        "message": "인증이 필요합니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/chat")
    public ResponseEntity<ApiChatResponseDTO> chat(
            @Valid @RequestBody ChatbotRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED);
        }
        log.info("챗봇 대화 요청 - 사용자: {}, 세션: {}", user.getId(), request.session_id());

        ChatbotResponseDTO serviceResponse = chatbotService.sendMessage(request, user);
        ChatMessageEntity botMessage = serviceResponse.getMessageEntity();

        ApiChatResponseDTO response = new ApiChatResponseDTO(
                botMessage.getContent(),
                botMessage.getQuestionType(),
                botMessage.getResponseTimeSeconds() != null ? botMessage.getResponseTimeSeconds().doubleValue() : 0.0,
                botMessage.getSession().getSessionId(),
                botMessage.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "🆕 새 대화 세션 생성", 
        description = """
        새로운 챗봇 대화 세션을 생성합니다.
        
        **특징:**
        - 사용자별로 독립적인 세션 생성
        - 세션 ID는 대화 시 사용
        - 24시간 후 자동 만료
        """
    )
    @PostMapping("/sessionsGenerate")
    public ResponseEntity<ApiNewSessionResponseDTO> createSession(
            @Parameter(hidden = true) @LoginUser User user
    ) {
        // 인증된 사용자가 없으면 익명 사용자로 처리
        if (user == null) {
            // 익명 사용자용 기본 응답 (실제로는 세션 생성 없이 처리)
            log.info("익명 사용자의 세션 생성 요청");
            return ResponseEntity.ok(new ApiNewSessionResponseDTO(
                    "anonymous_session_" + System.currentTimeMillis(),
                    java.time.Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                    "익명 사용자 세션이 생성되었습니다."
            ));
        }
        ChatSessionEntity session = chatbotService.createSession(user);
        return ResponseEntity.ok(new ApiNewSessionResponseDTO(
                session.getSessionId(),
                session.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                "새로운 채팅 세션이 생성되었습니다."
        ));
    }

    @Operation(
        summary = "📜 대화 히스토리 조회", 
        description = """
        특정 세션의 대화 히스토리를 조회합니다.
        """
    )
    @GetMapping("/sessionHistory/{sessionId}/history")
    public ResponseEntity<ApiSessionHistoryResponseDTO> getSessionHistory(
            @Parameter(description = "세션 ID", example = "session_123") @PathVariable String sessionId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED);
        }
        List<ChatMessageEntity> messages = chatbotService.getSessionHistory(sessionId, user);
        
        List<ApiChatMessageHistoryDTO> chatHistory = new ArrayList<>();
        for (int i = 0; i < messages.size(); i += 2) {
            ChatMessageEntity userMessage = messages.get(i);
            if (i + 1 < messages.size()) {
                ChatMessageEntity botMessage = messages.get(i + 1);
                chatHistory.add(new ApiChatMessageHistoryDTO(
                        userMessage.getContent(),
                        botMessage.getContent(),
                        userMessage.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                        botMessage.getQuestionType()
                ));
            }
        }
        
        return ResponseEntity.ok(new ApiSessionHistoryResponseDTO(
                sessionId,
                messages.size(),
                chatHistory
        ));
    }

    @Operation(
        summary = "📋 내 대화 세션 목록", 
        description = """
        현재 사용자의 모든 대화 세션 목록을 조회합니다.
        
        """
    )
    @GetMapping("/sessionsList")
    public ResponseEntity<ApiSessionListResponseDTO> getUserSessions(
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED);
        }
        List<ChatSessionEntity> sessions = chatbotService.getUserSessions(user);
        
        List<ApiSessionListItemDTO> sessionListItems = sessions.stream()
                .map(session -> new ApiSessionListItemDTO(
                        session.getSessionId(),
                        session.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                        session.getTotalMessages(),
                        session.getLastQuestionType()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiSessionListResponseDTO(
                sessionListItems.size(),
                sessionListItems
        ));
    }

    @Operation(
        summary = "🗑️ 대화 세션 삭제", 
        description = """
        특정 대화 세션을 삭제합니다.
        **주의사항:**
        - 삭제된 세션은 복구할 수 없습니다
        - 대화 히스토리도 함께 삭제됩니다
        - 본인의 세션만 삭제 가능합니다
        """
    )
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiDeleteSessionResponseDTO> deleteSession(
            @Parameter(description = "세션 ID", example = "session_123") @PathVariable String sessionId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED);
        }
        chatbotService.deleteSession(sessionId, user);
        return ResponseEntity.ok(new ApiDeleteSessionResponseDTO(
                "세션 " + sessionId + "이(가) 삭제되었습니다."
        ));
    }
} 