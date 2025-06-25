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
                        ... (중략) ...
                        ### 3. 소셜 로그인 (SDK 방식)
                        **카카오 SDK 사용:**
                        - 카카오 SDK로 인가 코드를 받은 후 `POST /auth/oauth2/login/kakao` API로 전송

                        **네이버 SDK 사용:**
                        - 네이버 SDK로 인가 코드를 받은 후 `POST /auth/oauth2/login/naver` API로 전송
                        ... (이하 동일)
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