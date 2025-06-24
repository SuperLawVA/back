package com.superlawva.domain.chatbot.controller;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
import com.superlawva.domain.chatbot.dto.SessionDeleteResponseDTO;
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
        ## 📖 API 설명
        ML 팀의 AI 챗봇과 대화하여 부동산 법률 상담을 받을 수 있습니다.
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 첫 대화 시작
        ```javascript
        // 첫 대화: session_id 없이 요청
        const firstMessage = {
            message: "임대차 보증금을 돌려받으려면 어떻게 해야 하나요?"
            // session_id는 생략 (자동으로 새 세션 생성)
        };
        
        fetch('/api/v1/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + JWT토큰
            },
            body: JSON.stringify(firstMessage)
        })
        .then(response => response.json())
        .then(data => {
            console.log('챗봇 답변:', data.answer);
            console.log('새 세션 ID:', data.session_id);  // 이 값을 저장!
        });
        ```
        
        ### 2. 이어서 대화하기
        ```javascript
        // 연속 대화: 받은 session_id 계속 사용
        const followUpMessage = {
            message: "구체적인 절차를 알려주세요",
            session_id: "dc5bc993-861d-4b78-89e6-df356c8f03fb"  // 저장한 세션 ID
        };
        ```
        
        ### 3. 응답 데이터 처리
        ```javascript
        response.json().then(data => {
            // 마크다운 형태의 답변을 HTML로 변환
            const htmlAnswer = markdownToHtml(data.answer);
            document.getElementById('chat-response').innerHTML = htmlAnswer;
            
            // 질문 유형별 UI 처리
            switch(data.question_type) {
                case 'first_chat': 
                    showWelcomeUI();
                    break;
                case 'legal_rag': 
                    showLegalSourcesUI();
                    break;
                case 'case_rag': 
                    showCaseStudyUI();
                    break;
            }
        });
        ```
        
        ### 4. 세션 관리 팁
        - **세션 유지**: 연속 대화를 위해 `session_id`를 브라우저 저장소에 보관
        - **새 대화**: 새로운 주제로 대화하려면 `session_id` 없이 요청
        - **세션 삭제**: 대화 종료 시 `DELETE /session/{sessionId}` 호출
        
        ### 5. 에러 처리
        - **400**: 메시지가 비어있음
        - **401**: JWT 토큰 문제 → 로그인 페이지로 이동
        - **500**: AI 서버 오류 → "잠시 후 다시 시도" 안내
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 챗봇 응답 성공",
            content = @Content(
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "answer": "## 🏠 임대차 보증금 반환 절차\\n\\n임대차 보증금을 돌려받지 못한 상황에 대해 도움을 드리겠습니다.\\n\\n### 📋 1단계: 내용증명 발송\\n임대인에게 보증금 반환을 요구하는 내용증명을 발송하세요.\\n\\n### ⚖️ 2단계: 법적 조치\\n내용증명에도 불응할 경우 다음 조치를 취할 수 있습니다:\\n- 소액심판 신청 (3천만원 이하)\\n- 민사소송 제기\\n\\n### 🔍 관련 법령\\n- 주택임대차보호법 제3조 (보증금 반환의무)\\n- 민법 제618조 (임대차 종료 시 원상회복)",
                        "session_id": "dc5bc993-861d-4b78-89e6-df356c8f03fb",
                        "timestamp": "2025-06-20T15:49:27.843808",
                        "response_time_seconds": 3.47,
                        "question_type": "legal_rag"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 잘못된 요청 데이터",
            content = @Content(
                examples = @ExampleObject(
                    name = "메시지 누락 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "400",
                        "message": "메시지는 필수입니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패 (JWT 토큰 문제)",
            content = @Content(
                examples = @ExampleObject(
                    name = "인증 오류",
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "❌ 서버 오류 (ML API 연결 실패 등)",
            content = @Content(
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "answer": "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                        "session_id": null,
                        "timestamp": "2025-06-20T15:49:27.843808",
                        "response_time_seconds": 0.0,
                        "question_type": "error"
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
    @GetMapping("/session/{sessionId}/history")
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
    @GetMapping("/sessions")
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
        summary = "🆕 새 세션 생성", 
        description = """
        새로운 대화 세션을 생성합니다.
        
        **활용:** 
        - "새 대화 시작" 기능 구현 시 사용
        - 명시적으로 세션을 미리 생성하고 싶을 때
        
        **참고:** /chat API 호출 시에도 session_id 없으면 자동 생성됩니다.
        """
    )
    @PostMapping("/session")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ChatSessionEntity> createSession(
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        ChatSessionEntity newSession = chatbotService.createNewSession(user);
        log.info("새 세션 생성 API 호출 - 사용자: {}, 세션: {}", user.getId(), newSession.getSessionId());
        
        return ResponseEntity.ok(newSession);
    }

    @Operation(
        summary = "🗑️ 세션 완전 삭제 (ML 스펙)", 
        description = """
        특정 세션과 관련된 모든 메시지를 완전히 삭제합니다.
        
        **⚠️ 주의사항:**
        - 삭제된 세션과 메시지는 복구할 수 없습니다
        - 본인 세션만 삭제 가능합니다
        
        **ML 팀 API 스펙에 맞춤**
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 세션 삭제 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "세션 'dc5bc993-861d-4b78-89e6-df356c8f03fb'이 성공적으로 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "❌ 세션을 찾을 수 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "해당 세션을 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/session/{sessionId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<SessionDeleteResponseDTO> deleteSession(
            @Parameter(description = "삭제할 세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
            @PathVariable String sessionId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        log.info("세션 삭제 요청 - 사용자: {}, 세션: {}", user.getId(), sessionId);
        
        boolean deleted = chatbotService.deleteSession(sessionId, user);
        if (deleted) {
            return ResponseEntity.ok(SessionDeleteResponseDTO.success(sessionId));
        } else {
            return ResponseEntity.ok(SessionDeleteResponseDTO.notFound());
        }
    }

    @Operation(
        summary = "🔚 세션 종료 (deprecated)", 
        description = """
        특정 세션을 종료 상태로 변경합니다.
        
        **⚠️ Deprecated:** `/session/{sessionId}` DELETE 사용을 권장합니다.
        """
    )
    @PutMapping("/chat/session/{sessionId}/close")
    @SecurityRequirement(name = "JWT")
    @Deprecated
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