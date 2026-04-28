package com.example.practice.common.config;

import com.example.practice.common.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(
            org.springframework.security.config.annotation.web.builders.HttpSecurity http
    ) throws Exception {

        http
                // ✅ CORS 활성화 (corsConfigurationSource Bean을 사용)
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .authorizeHttpRequests(auth -> auth

                        // ✅ Preflight(OPTIONS) 요청은 무조건 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/sw.js"
                        ).permitAll()

                        // 인증 없이 접근 가능한 API
                        .requestMatchers(
                                "/error",
                                "/",
                                "/health",
                                "/auth/signup",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/**",
                                "/api/chatbot"
                        ).permitAll()


                                .requestMatchers("/api/led/**").permitAll()
                        .requestMatchers("/api/devices/**").permitAll()
                        .requestMatchers("/api/env-data/**").permitAll()
                        .requestMatchers("/api/sensor-readings/**").permitAll()
                        .requestMatchers("/api/sensors/**").permitAll()
                                .requestMatchers("/api/v1/crops-recommend/**").permitAll()


                // 나머지는 JWT 인증
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new TokenAuthFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * ✅ CORS 설정 (프론트: http://localhost:5173)
     * 배포 프론트 도메인이 생기면 allowedOrigins에 추가하면 됨.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 주소 허용
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://farmus.netlify.app",
                "https://*.netlify.app",
                "https://farmus.io.kr"
        ));

        // 허용 메서드 (OPTIONS 꼭 포함)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용 헤더 (Authorization 포함)
        config.setAllowedHeaders(List.of("*"));

        // 프론트에서 읽을 헤더
        config.setExposedHeaders(List.of("Authorization"));

        // 쿠키/credentials 사용하면 true (JWT를 헤더로만 쓰면 true여도 문제는 거의 없음)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }






}
