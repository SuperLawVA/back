package com.superlawva.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class MLApiConfig {

    @Value("${ml.api.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${ml.api.read-timeout:60000}")
    private int readTimeout;

    @Value("${api.servers.legal.base-url:${legal.api.base-url:http://3.34.41.104:8000}}")
    private String mlApiBaseUrl;

    /**
     * ML API 통신용 RestTemplate 설정
     */
    @Bean(name = "mlRestTemplate")
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        log.info("🔧 ML API RestTemplate 설정 시작");
        log.info("   - Base URL: {}", mlApiBaseUrl);
        log.info("   - Connection Timeout: {}ms", connectionTimeout);
        log.info("   - Read Timeout: {}ms", readTimeout);

        RestTemplate restTemplate = new RestTemplate();

        // 1. HTTP 요청 팩토리 설정 (타임아웃)
        restTemplate.setRequestFactory(clientHttpRequestFactory());

        // 2. 메시지 컨버터 설정 (JSON)
        restTemplate.setMessageConverters(getMessageConverters(objectMapper));

        // 3. 인터셉터 추가 (로깅, 에러 처리)
        restTemplate.setInterceptors(getInterceptors());

        log.info("✅ ML API RestTemplate 설정 완료");
        return restTemplate;
    }

    /**
     * HTTP 클라이언트 팩토리 설정 (타임아웃 설정)
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        return factory;
    }

    /**
     * HTTP 메시지 컨버터 설정
     */
    private List<HttpMessageConverter<?>> getMessageConverters(ObjectMapper objectMapper) {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // JSON 컨버터 설정
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        jsonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));

        converters.add(jsonConverter);

        return converters;
    }

    /**
     * HTTP 요청 인터셉터 설정 (로깅 및 에러 처리 포함)
     */
    private List<ClientHttpRequestInterceptor> getInterceptors() {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        // 로깅 인터셉터
        interceptors.add((request, body, execution) -> {
            log.info("📤 ML API 요청: {} {}", request.getMethod(), request.getURI());
            log.debug("   Headers: {}", request.getHeaders());

            long startTime = System.currentTimeMillis();

            try {
                var response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - startTime;

                log.info("📥 ML API 응답: {} ({}ms)", response.getStatusCode(), duration);

                // 에러 상태 코드 체크 및 로깅
                if (response.getStatusCode().isError()) {
                    log.error("❌ ML API 오류 응답: {} - {}", response.getStatusCode(), response.getStatusText());
                }

                return response;

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("❌ ML API 요청 실패 ({}ms): {}", duration, e.getMessage());

                // 에러 타입에 따른 세분화된 예외 처리
                if (e.getMessage().contains("timeout")) {
                    throw new RuntimeException("ML API 타임아웃: 서버 응답이 지연되고 있습니다.");
                } else if (e.getMessage().contains("Connection refused")) {
                    throw new RuntimeException("ML API 서버에 연결할 수 없습니다. 서버 상태를 확인하세요.");
                } else {
                    throw new RuntimeException("ML API 통신 오류: " + e.getMessage());
                }
            }
        });

        return interceptors;
    }

    /**
     * ML API 상태 확인용 Bean
     */
    @Bean
    public MLApiHealthIndicator mlApiHealthIndicator() {
        return new MLApiHealthIndicator(mlApiBaseUrl, connectionTimeout);
    }

    /**
     * ML API 상태 확인 클래스 (개선된 버전)
     */
    public static class MLApiHealthIndicator {
        private final String baseUrl;
        private final int timeout;

        public MLApiHealthIndicator(String baseUrl, int timeout) {
            this.baseUrl = baseUrl;
            this.timeout = timeout;
        }

        public boolean isHealthy() {
            try {
                // 간단한 RestTemplate 생성하여 헬스체크
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(timeout);
                factory.setReadTimeout(timeout);

                RestTemplate healthCheckTemplate = new RestTemplate();
                healthCheckTemplate.setRequestFactory(factory);

                String response = healthCheckTemplate.getForObject(baseUrl + "/docs", String.class);
                return response != null;

            } catch (Exception e) {
                log.warn("ML API 상태 확인 실패: {}", e.getMessage());
                return false;
            }
        }

        public String getHealthStatus() {
            if (isHealthy()) {
                return "ML API 서버가 정상적으로 동작 중입니다.";
            } else {
                return "ML API 서버에 연결할 수 없습니다.";
            }
        }
    }
}