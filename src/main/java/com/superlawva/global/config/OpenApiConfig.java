package com.superlawva.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {

        final String securitySchemeName = "bearerAuth";

        Info info = new Info()
                .title("SuperLawVA API 명세서")
                .version("v1.0.0")
                .description("""
                        ## 🎯 테스트용 계정 안내

                        **누구든 바로 테스트할 수 있는 계정들이 준비되어 있습니다!**

                        ### 📝 테스트 계정 목록
                        | 구분 | 이메일 | 비밀번호 | 설명 |
                        |------|--------|----------|------|
                        | **일반 사용자** | `test@example.com` | `password123` | 기본 테스트용 |
                        | **관리자** | `admin@example.com` | `admin123` | 관리자 권한 |
                        | **데모 사용자** | `demo@example.com` | `demo123` | 데모 및 프레젠테이션용 |

                        ### 🚀 빠른 테스트 방법
                        1. **`POST /auth/login`** API 클릭
                        2. 위 계정 중 하나로 로그인
                        3. 응답에서 **`token`** 복사
                        4. 우측 상단 **"Authorize"** 클릭 → `Bearer {토큰}` 입력
                        5. 🔒 마크가 있는 모든 API 테스트 가능!

                        ---

                        ## 🔑 JWT 토큰 발급 가이드

                        API를 테스트하려면 JWT 토큰이 필요합니다. 토큰은 다음 방법들로 발급받을 수 있습니다.

                        ### 1. 일반 로그인 API 사용
                        - `POST /auth/login` API에 이메일과 비밀번호로 요청하여 토큰을 발급받습니다.

                        ### 2. 소셜 로그인 (OAuth2 URL 방식) - **추천**
                        **카카오 로그인:**
                        1. `GET /auth/oauth2/kakao` API를 호출하여 카카오 인증 URL을 받습니다
                        2. 받은 URL로 사용자를 리다이렉트합니다
                        3. 카카오 로그인 완료 후 자동으로 콜백 URL로 리다이렉트되며 JWT 토큰을 받습니다

                        **네이버 로그인:**
                        1. `GET /auth/oauth2/naver` API를 호출하여 네이버 인증 URL을 받습니다
                        2. 받은 URL로 사용자를 리다이렉트합니다
                        3. 네이버 로그인 완료 후 자동으로 콜백 URL로 리다이렉트되며 JWT 토큰을 받습니다

                        ### 3. 소셜 로그인 (SDK 방식)
                        **카카오 SDK 사용:**
                        - 카카오 SDK로 인가 코드를 받은 후 `POST /auth/login/kakao` API로 전송

                        **네이버 SDK 사용:**
                        - 네이버 SDK로 인가 코드를 받은 후 `POST /auth/login/naver` API로 전송

                        ---

                        ## 🚀 소셜 로그인 구현 가이드

                        ### OAuth2 URL 방식 (추천)
                        ```javascript
                        // 카카오 로그인
                        const response = await fetch('/auth/oauth2/kakao');
                        const { authUrl } = await response.json();
                        window.location.href = authUrl;

                        // 네이버 로그인
                        const response = await fetch('/auth/oauth2/naver');
                        const { authUrl } = await response.json();
                        window.location.href = authUrl;
                        ```

                        ### SDK 방식
                        ```javascript
                        // 카카오 SDK
                        Kakao.Auth.login({
                            success: function(authObj) {
                                fetch('/auth/login/kakao', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({
                                        authorizationCode: authObj.code
                                    })
                                })
                                .then(response => response.json())
                                .then(data => {
                                    localStorage.setItem('token', data.result.token);
                                });
                            }
                        });

                        // 네이버 SDK
                        naverLogin.getLoginStatus(function(status) {
                            if (status) {
                                fetch('/auth/login/naver', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({
                                        authorizationCode: naverLogin.authorizationCode,
                                        state: naverLogin.state
                                    })
                                })
                                .then(response => response.json())
                                .then(data => {
                                    localStorage.setItem('token', data.result.token);
                                });
                            }
                        });
                        ```

                        ---

                        ## ⚠️ 로그인 응답 데이터 안내

                        `POST /auth/login` API의 응답(LoginResponseDTO)에 포함된 아래 필드들은 현재 실제 데이터가 아닌, **하드코딩된 샘플 데이터**를 반환합니다.

                        - `notification` (알림 정보)
                        - `contract` (계약 정보)
                        - `recentChat` (최근 채팅 정보)

                        프론트엔드에서는 이 필드들이 존재하고, 해당 DTO 형식에 맞게 데이터가 들어온다고 가정하고 UI를 우선 구현할 수 있습니다.
                        실제 데이터 연동은 추후 백엔드에서 구현될 예정입니다.

                        ---

                        ## 🔒 인증이 필요한 API 사용법

                        1. 위의 방법 중 하나로 JWT 토큰을 발급받습니다
                        2. Swagger 우측 상단의 **"Authorize"** 버튼을 누릅니다
                        3. `Bearer {복사한_토큰}` 형식으로 붙여넣어 인증을 완료합니다
                        4. 이제 자물쇠(🔒)가 걸린 모든 API를 자유롭게 테스트할 수 있습니다
                        """);

        return new OpenAPI()
                .addServersItem(new Server().url("http://43.203.127.128:8080").description("운영 서버"))
                .addServersItem(new Server().url("http://localhost:8080").description("로컬 개발 서버"))
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
