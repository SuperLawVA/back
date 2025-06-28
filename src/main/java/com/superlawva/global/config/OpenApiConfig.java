package com.superlawva.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

                        ### 📝 응답 형식
                        모든 API는 표준화된 응답 형식을 사용합니다:
                        ```json
                        {
                            "isSuccess": true,
                            "code": "200",
                            "message": "요청에 성공했습니다.",
                            "result": { ... }
                        }
                        ```

                        """)
                .contact(new Contact()
                        .name("SuperLawVA Development Team")
                        .email("backend@superlawva.com")
                        .url("https://superlawva.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));

        return new OpenAPI()
                .addServersItem(new Server().url("/").description("현재 접속한 서버"))
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."))
                        .addResponses("UnauthorizedError", new ApiResponse()
                                .description("인증이 필요합니다. JWT 토큰을 확인해주세요.")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema()
                                                        .type("object")
                                                        .addProperties("isSuccess", new io.swagger.v3.oas.models.media.Schema().type("boolean").example(false))
                                                        .addProperties("code", new io.swagger.v3.oas.models.media.Schema().type("string").example("COMMON401"))
                                                        .addProperties("message", new io.swagger.v3.oas.models.media.Schema().type("string").example("인증이 필요합니다."))
                                                        .addProperties("result", new io.swagger.v3.oas.models.media.Schema().type("object").nullable(true).example(null))))))
                        .addResponses("BadRequestError", new ApiResponse()
                                .description("잘못된 요청입니다. 요청 데이터를 확인해주세요.")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema()
                                                        .type("object")
                                                        .addProperties("isSuccess", new io.swagger.v3.oas.models.media.Schema().type("boolean").example(false))
                                                        .addProperties("code", new io.swagger.v3.oas.models.media.Schema().type("string").example("COMMON400"))
                                                        .addProperties("message", new io.swagger.v3.oas.models.media.Schema().type("string").example("잘못된 요청입니다."))
                                                        .addProperties("result", new io.swagger.v3.oas.models.media.Schema().type("object").nullable(true).example(null))))))
                        .addResponses("NotFoundError", new ApiResponse()
                                .description("요청한 리소스를 찾을 수 없습니다.")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema()
                                                        .type("object")
                                                        .addProperties("isSuccess", new io.swagger.v3.oas.models.media.Schema().type("boolean").example(false))
                                                        .addProperties("code", new io.swagger.v3.oas.models.media.Schema().type("string").example("COMMON404"))
                                                        .addProperties("message", new io.swagger.v3.oas.models.media.Schema().type("string").example("요청한 리소스를 찾을 수 없습니다."))
                                                        .addProperties("result", new io.swagger.v3.oas.models.media.Schema().type("object").nullable(true).example(null))))))
                        .addResponses("InternalServerError", new ApiResponse()
                                .description("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema()
                                                        .type("object")
                                                        .addProperties("isSuccess", new io.swagger.v3.oas.models.media.Schema().type("boolean").example(false))
                                                        .addProperties("code", new io.swagger.v3.oas.models.media.Schema().type("string").example("COMMON500"))
                                                        .addProperties("message", new io.swagger.v3.oas.models.media.Schema().type("string").example("서버 에러, 관리자에게 문의 바랍니다."))
                                                        .addProperties("result", new io.swagger.v3.oas.models.media.Schema().type("object").nullable(true).example(null)))))));
    }
}
