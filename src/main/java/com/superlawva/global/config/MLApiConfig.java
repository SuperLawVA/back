//package com.superlawva.global.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//@Slf4j
//@Configuration
//public class MLApiConfig {
//
//    @Value("${ml.api.connection-timeout:10000}")
//    private int connectionTimeout;
//
//    @Value("${ml.api.read-timeout:60000}")
//    private int readTimeout;
//
//    /**
//     * ML API 통신용 RestTemplate 설정
//     */
//    @Bean
//    public RestTemplate restTemplate() {
//        log.info("🔧 RestTemplate 설정 - Connection Timeout: {}ms, Read Timeout: {}ms",
//                 connectionTimeout, readTimeout);
//
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setRequestFactory(clientHttpRequestFactory());
//
//        return restTemplate;
//    }
//
//    /**
//     * HTTP 클라이언트 팩토리 설정 (타임아웃 설정)
//     */
//    private ClientHttpRequestFactory clientHttpRequestFactory() {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setConnectTimeout(connectionTimeout);
//        factory.setReadTimeout(readTimeout);
//
//        return factory;
//    }
//}