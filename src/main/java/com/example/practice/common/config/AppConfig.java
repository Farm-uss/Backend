package com.example.practice.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // HTTP 통신을 담당하는 객체를 빈으로 등록
        return new RestTemplate();
    }
}