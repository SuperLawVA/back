package com.superlawva.domain.chatbot.controller;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "🤖 Chatbot API", description = "ML 팀 연동 챗봇 API (🔒인증 필요)")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    
    @Operation(
        summary = "💬 챗봇과 대화하기", 
        description = """
        ML 팀 챗봇 API와 연동하여 법률 상담을 제공합니다.
        
        **주요 기능:**
        - 임대차, 계약서 등 부동산 법률 질문 답변
        - 세션 기반 연속 대화 가능
        - 질문 유형 자동 분류 (first_chat, legal_rag, case_rag 등)
        
        **세션 관리:**
        - session_id 없으면 새 세션 자동 생성
        - 동일 session_id로 연속 대화 가능
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
                        "answer": "## 🏠 상황 정리\\n임대차 보증금을 돌려받지 못한 상황으로 이해하겠습니다.\\n\\n## 💡 도움 방법\\n1. **법률 해결방안**: 관련 법률인 '임대차 보증금 반환에 관한 법률'을 확인하여...",
                        "session_id": "dc5bc993-861d-4b78-89e6-df356c8f03fb",
                        "timestamp": "2025-06-20T15:49:27.843808",
                        "response_time_seconds": 3.47,
                        "question_type": "first_chat"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 잘못된 요청",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "메시지는 필수입니다.",
                        "status": 400
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/chat")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ChatbotResponseDTO> chat(
            @Valid @RequestBody ChatbotRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        log.info("ML 챗봇 대화 요청 - 사용자: {}, 세션: {}", user.getId(), request.session_id());
        
        ChatbotResponseDTO response = chatbotService.sendMessage(request, user);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "📋 내 대화 이력 조회", 
        description = """
        사용자의 모든 챗봇 대화 메시지를 조회합니다.
        
        **응답 데이터:**
        - 사용자 메시지와 봇 응답이 모두 포함
        - 최신 메시지부터 내림차순 정렬
        - 페이징 지원
        """
    )
    @GetMapping("/chat/history")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Page<ChatMessageEntity>> getChatHistory(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageEntity> history = chatbotService.getChatHistory(user, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    @Operation(
        summary = "🔗 세션별 대화 내역", 
        description = """
        특정 세션의 전체 대화를 시간순으로 조회합니다.
        
        **보안:** 본인 세션만 조회 가능
        **정렬:** 시간순 오름차순 (대화 흐름대로)
        """
    )
    @GetMapping("/chat/session/{sessionId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<ChatMessageEntity>> getSessionHistory(
            @Parameter(description = "세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
            @PathVariable String sessionId,
            
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        List<ChatMessageEntity> sessionHistory = chatbotService.getSessionHistory(sessionId, user);
        return ResponseEntity.ok(sessionHistory);
    }
    
    @Operation(
        summary = "📝 내 세션 목록", 
        description = """
        사용자의 모든 대화 세션 목록을 조회합니다.
        
        **활용:** 이전 대화를 이어서 계속할 때 사용
        **정렬:** 최근 세션부터 내림차순
        """
    )
    @GetMapping("/chat/sessions")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Page<ChatSessionEntity>> getUserSessions(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatSessionEntity> sessions = chatbotService.getUserSessions(user, pageable);
        return ResponseEntity.ok(sessions);
    }
    
    @Operation(
        summary = "🔚 세션 종료", 
        description = "특정 세션을 종료 상태로 변경합니다."
    )
    @PutMapping("/chat/session/{sessionId}/close")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Void> closeSession(
            @PathVariable String sessionId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        chatbotService.closeSession(sessionId, user);
        return ResponseEntity.ok().build();
    }
} 