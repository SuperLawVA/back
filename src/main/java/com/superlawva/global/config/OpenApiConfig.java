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
                        ## 🔑 JWT 토큰 발급 가이드
                        
                        API를 테스트하려면 JWT 토큰이 필요합니다. 토큰은 다음 두 가지 방법으로 발급받을 수 있습니다.
                        
                        ### 1. 일반 로그인 API 사용
                        - `POST /auth/login` API에 이메일과 비밀번호로 요청하여 토큰을 발급받습니다.
                        
                        ### 2. 소셜 로그인 (카카오)
                        - 웹 브라우저에서 `GET /oauth2/authorization/kakao` 주소로 직접 이동하여 카카오 로그인을 진행합니다.
                        - 로그인 성공 시, 백엔드 서버는 프론트엔드 리다이렉트 주소 (`http://localhost:3000/login/success`)로 이동시키며, URL에 토큰 정보를 포함하여 전달합니다.
                        - **리다이렉트 URL 예시**: `http://localhost:3000/login/success?token=eyJhbGciOiJIUzI1NiJ9...`
                        
                        ---
                        
                        ## ⚠️ 로그인 응답 데이터 안내
                        
                        `POST /auth/login` API의 응답(LoginResponseDTO)에 포함된 아래 필드들은 현재 실제 데이터가 아닌, **하드코딩된 샘플 데이터**를 반환합니다.
                        
                        - `notification` (알림 정보)
                        - `contract` (계약 정보)
                        - `recentChat` (최근 채팅 정보)
                        
                        프론트엔드에서는 이 필드들이 존재하고, 해당 DTO 형식에 맞게 데이터가 들어온다고 가정하고 UI를 우선 구현할 수 있습니다. 
                        실제 데이터 연동은 추후 백엔드에서 구현될 예정입니다.
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
