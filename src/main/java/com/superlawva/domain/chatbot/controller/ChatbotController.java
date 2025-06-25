package com.superlawva.domain.chatbot.controller;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
import com.superlawva.domain.chatbot.dto.SessionCreateRequestDTO;
import com.superlawva.domain.chatbot.dto.SessionDeleteResponseDTO;
import com.superlawva.domain.chatbot.dto.SessionListResponseDTO;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import com.superlawva.domain.chatbot.service.ChatbotService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;

import java.util.List;
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

@Slf4j
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Tag(name = "🤖 Chatbot API", description = "AI 챗봇 대화 API")
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
        
        fetch('/chatbot/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + JWT토큰
            },
            body: JSON.stringify(firstMessage)
        })
        .then(response => response.text())
        .then(answer => {
            console.log('챗봇 답변:', answer);
            // 답변만 반환되므로 세션 ID는 별도 관리 필요
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
        response.text().then(answer => {
            // 답변 문자열을 직접 받아서 처리
            console.log('챗봇 답변:', answer);
            
            // 마크다운 형태의 답변을 HTML로 변환
            const htmlAnswer = markdownToHtml(answer);
            document.getElementById('chat-response').innerHTML = htmlAnswer;
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
                    ## 🏠 임대차 보증금 반환 절차

                    임대차 보증금을 돌려받지 못한 상황에 대해 도움을 드리겠습니다.

                    ### 📋 1단계: 내용증명 발송
                    임대인에게 보증금 반환을 요구하는 내용증명을 발송하세요.

                    ### ⚖️ 2단계: 법적 조치
                    내용증명에도 불응할 경우 다음 조치를 취할 수 있습니다:
                    - 소액심판 신청 (3천만원 이하)
                    - 민사소송 제기

                    ### 🔍 관련 법령
                    - 주택임대차보호법 제3조 (보증금 반환의무)
                    - 민법 제618조 (임대차 종료 시 원상회복)
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
                        "code": "COMMON400",
                        "message": "잘못된 요청입니다.",
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
                    value = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                )
            )
        )
    })
    @PostMapping("/chat")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<String> chat(
            @Valid @RequestBody ChatbotRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        log.info("ML 챗봇 대화 요청 - 사용자: {}, 세션: {}", user.getId(), request.session_id());
        
        ChatbotResponseDTO response = chatbotService.sendMessage(request, user);
        return ResponseEntity.ok(response.answer());
    }
    
    @Operation(
        summary = "📋 내 대화 이력 조회", 
        description = """
        사용자의 모든 챗봇 대화 메시지를 조회합니다.
        
        **프론트엔드 활용:**
        - 전체 대화 히스토리 페이지 구현
        - 검색 기능과 함께 사용
        - 페이징으로 성능 최적화
        
        **응답 데이터:**
        - 사용자 메시지와 봇 응답이 모두 포함
        - 최신 메시지부터 내림차순 정렬
        - 페이징 지원 (기본 20개씩)
        
        **요청 예시:**
        ```
        GET /chatbot/chat/history?page=0&size=20
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 대화 이력 조회 성공",
            content = @Content(
                examples = @ExampleObject(
                    name = "대화 이력 조회 성공",
                    value = """
                    {
                        "content": [
                            {
                                "id": 102,
                                "role": "assistant",
                                "content": "임대차 보증금에 대해 도움을 드리겠습니다...",
                                "questionType": "legal_rag",
                                "responseTimeSeconds": 2.5,
                                "createdAt": "2025-01-20T15:30:47.623456"
                            },
                            {
                                "id": 101,
                                "role": "user", 
                                "content": "임대차 보증금 관련 상담",
                                "questionType": null,
                                "responseTimeSeconds": null,
                                "createdAt": "2025-01-20T15:30:45.123456"
                            }
                        ],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20
                        },
                        "totalElements": 50,
                        "totalPages": 3,
                        "first": true,
                        "last": false
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패",
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
        )
    })
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
        
        **프론트엔드 활용:**
        - 채팅창에서 이전 대화 내역 로딩
        - 세션 재진입 시 대화 히스토리 복원
        - 대화 흐름 파악
        
        **보안:** 본인 세션만 조회 가능 (타인 세션 접근 시 빈 배열 반환)
        **정렬:** 시간순 오름차순 (대화 흐름대로)
        
        **요청 예시:**
        ```
        GET /chatbot/session/dc5bc993-861d-4b78-89e6-df356c8f03fb/history
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 세션 대화 내역 조회 성공",
            content = @Content(
                examples = @ExampleObject(
                    name = "세션 대화 내역 성공",
                    value = """
                    [
                        {
                            "id": 101,
                            "role": "user",
                            "content": "임대차 보증금 관련 상담",
                            "questionType": null,
                            "responseTimeSeconds": null,
                            "createdAt": "2025-01-20T15:30:45.123456"
                        },
                        {
                            "id": 102,
                            "role": "assistant",
                            "content": "임대차 보증금에 대해 도움을 드리겠습니다...",
                            "questionType": "legal_rag",
                            "responseTimeSeconds": 2.5,
                            "createdAt": "2025-01-20T15:30:47.623456"
                        },
                        {
                            "id": 103,
                            "role": "user",
                            "content": "구체적인 절차를 알려주세요",
                            "questionType": null,
                            "responseTimeSeconds": null,
                            "createdAt": "2025-01-20T15:31:20.789012"
                        }
                    ]
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패",
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
            responseCode = "403", 
            description = "❌ 권한 없음 (타인 세션 접근 시 빈 배열 반환)",
            content = @Content(
                examples = @ExampleObject(
                    name = "권한 없는 세션 접근",
                    value = "[]"
                )
            )
        )
    })
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
        사용자의 모든 대화 세션 목록을 간소화된 형태로 조회합니다.
        
        **프론트엔드 활용:**
        - 대화 목록 화면 구현
        - 이전 대화를 이어서 계속할 때 사용
        - 세션별 미리보기 제공
        
        **응답 특징:**
        - 세션 제목은 첫 번째 사용자 메시지 기반 (30자 초과 시 "..." 추가)
        - 메시지가 없는 세션은 질문 유형에 따라 기본 제목 설정
        - 최근 활동 순서부터 내림차순 정렬
        
        **요청 예시:**
        ```
        GET /chatbot/sessions?userId=123
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 세션 목록 조회 성공",
            content = @Content(
                examples = @ExampleObject(
                    name = "세션 목록 조회 성공",
                    value = """
                    [
                        {
                            "sessionId": "dc5bc993-861d-4b78-89e6-df356c8f03fb",
                            "title": "임대차 보증금 관련 상담",
                            "last_datetime": "2025-01-20T15:30:45.123456"
                        },
                        {
                            "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                            "title": "계약서 검토 요청",
                            "last_datetime": "2025-01-19T14:20:30.987654"
                        },
                        {
                            "sessionId": "xyz789-1234-5678-9abc-def123456789",
                            "title": "법률 상담",
                            "last_datetime": "2025-01-18T10:15:20.456789"
                        }
                    ]
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 잘못된 요청 파라미터",
            content = @Content(
                examples = @ExampleObject(
                    name = "userId 누락 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON400",
                        "message": "잘못된 요청입니다.",
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
            responseCode = "403", 
            description = "❌ 권한 없음 (타 사용자 세션 접근)",
            content = @Content(
                examples = @ExampleObject(
                    name = "권한 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON403",
                        "message": "금지된 요청입니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/sessions")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<SessionListResponseDTO>> getUserSessions(
            @Parameter(description = "사용자 ID", example = "123", required = true)
            @RequestParam Long userId,
            
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        // 요청된 userId와 JWT 토큰의 사용자 ID가 일치하는지 확인
        if (!user.getId().equals(userId)) {
            log.warn("JWT 토큰의 사용자 ID({})와 요청 사용자 ID({})가 일치하지 않음", user.getId(), userId);
            throw new BaseException(ErrorStatus._FORBIDDEN);
        }
        
        List<SessionListResponseDTO> sessions = chatbotService.getUserSessionList(userId);
        return ResponseEntity.ok(sessions);
    }
    
    @Operation(
        summary = "🆕 새 세션 생성", 
        description = """
        새로운 대화 세션을 생성합니다.
        
        **프론트엔드 활용:** 
        - "새 대화 시작" 버튼 구현
        - 명시적으로 세션을 미리 생성하고 싶을 때
        - 대화방 생성 후 세션 ID 저장
        
        **참고:** 
        - `/chat` API 호출 시에도 `session_id` 없으면 자동 생성됩니다
        - 이 API는 UI에서 명시적인 "새 대화" 기능 구현 시 사용
        
        **요청 예시:**
        ```json
        {
            "userId": 123
        }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 새 세션 생성 성공",
            content = @Content(
                examples = @ExampleObject(
                    name = "세션 생성 성공",
                    value = """
                    {
                        "sessionId": "dc5bc993-861d-4b78-89e6-df356c8f03fb",
                        "user": {
                            "id": 123,
                            "nickname": "홍길동",
                            "email": "user@example.com"
                        },
                        "createdAt": "2025-01-20T15:30:45.123456",
                        "lastActiveAt": "2025-01-20T15:30:45.123456",
                        "lastQuestionType": null,
                        "totalMessages": 0,
                        "status": "active"
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
                    name = "userId 누락 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON400",
                        "message": "잘못된 요청입니다.",
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
            responseCode = "403", 
            description = "❌ 권한 없음 (타 사용자 대신 세션 생성 시도)",
            content = @Content(
                examples = @ExampleObject(
                    name = "권한 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON403",
                        "message": "금지된 요청입니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/session")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ChatSessionEntity> createSession(
            @Valid @RequestBody SessionCreateRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        // 요청된 userId와 JWT 토큰의 사용자 ID가 일치하는지 확인
        if (!user.getId().equals(request.userId())) {
            log.warn("JWT 토큰의 사용자 ID({})와 요청 사용자 ID({})가 일치하지 않음", user.getId(), request.userId());
            throw new BaseException(ErrorStatus._FORBIDDEN);
        }
        
        ChatSessionEntity newSession = chatbotService.createNewSession(user);
        log.info("새 세션 생성 API 호출 - 사용자: {}, 세션: {}", user.getId(), newSession.getSessionId());
        
        return ResponseEntity.ok(newSession);
    }

    @Operation(
        summary = "🗑️ 세션 완전 삭제", 
        description = """
        특정 세션과 관련된 모든 메시지를 완전히 삭제합니다.
        
        **프론트엔드 활용:**
        - 대화 목록에서 "삭제" 버튼 구현
        - 개인정보 보호를 위한 대화 내역 삭제
        - 저장 공간 정리
        
        **⚠️ 주의사항:**
        - 삭제된 세션과 메시지는 복구할 수 없습니다
        - 본인 세션만 삭제 가능합니다
        - 존재하지 않는 세션 삭제 시도도 "성공"으로 응답
        
        **요청 예시:**
        ```
        DELETE /chatbot/session/dc5bc993-861d-4b78-89e6-df356c8f03fb
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 세션 삭제 성공 (존재하지 않는 세션 포함)",
            content = @Content(
                examples = @ExampleObject(
                    name = "삭제 성공",
                    value = """
                    {
                        "message": "세션 'dc5bc993-861d-4b78-89e6-df356c8f03fb'이 성공적으로 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패",
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
            responseCode = "403", 
            description = "❌ 권한 없음 (타인 세션 삭제 시도)",
            content = @Content(
                examples = @ExampleObject(
                    name = "권한 오류",
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

} 