package com.aiskin.demo.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(50_000_000) // Allow up to 50MB base64 strings
                .build();
        mapper.getFactory().setStreamReadConstraints(constraints);
        return mapper;
    }
}
