//package com.superlawva.global.config;
//
//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.License;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
//import io.swagger.v3.oas.models.servers.Server;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
//// OpenApiConfig.java와 중복되어 비활성화됨
//@Configuration
//public class SwaggerConfig {
//
//    @Bean
//    public OpenAPI openAPI() {
//        return new OpenAPI()
//                .info(apiInfo())
//                .servers(List.of(
//                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
//                        new Server().url("https://api.superlawva.com").description("운영 서버")
//                ))
//                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
//                .components(new Components()
//                        .addSecuritySchemes("Bearer Authentication",
//                                new SecurityScheme()
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")
//                                        .in(SecurityScheme.In.HEADER)
//                                        .name("Authorization")
//                        )
//                );
//    }
//
//    private Info apiInfo() {
//        return new Info()
//                .title("AI 법률 서비스 API")
//                .description("카카오/네이버 소셜 로그인 및 JWT 인증을 제공하는 AI 법률 서비스 백엔드 API")
//                .version("1.0.0")
//                .contact(new Contact()
//                        .name("개발팀")
//                        .email("dev@superlawva.com")
//                        .url("https://superlawva.com")
//                )
//                .license(new License()
//                        .name("MIT License")
//                        .url("https://opensource.org/licenses/MIT")
//                );
//    }
//}