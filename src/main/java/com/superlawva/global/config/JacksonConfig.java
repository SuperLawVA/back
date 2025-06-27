package com.superlawva.global.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.superlawva.global.util.NumberLongDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module numberLongModule() {
        SimpleModule module = new SimpleModule();
        NumberLongDeserializer deserializer = new NumberLongDeserializer();
        module.addDeserializer(Long.class, deserializer);
        module.addDeserializer(Long.TYPE, deserializer);
        return module;
    }
} 