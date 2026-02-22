package com.example.practice.common.config;

import com.example.practice.common.security.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {


    private final JwtProvider jwtProvider;

    // Swagger, 로그인 API는 필터 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Swagger, 로그인, 회원가입만 제외
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/auth/login")      // 로그인만
                || path.equals("/auth/signup");    // 회원가입만
    }


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        System.out.println("RAW Authorization = [" + auth + "]");


        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            token = token.replaceAll("\\s", "");

            try {
                var jwtUser = jwtProvider.parseAccessToken(token);

                if (jwtUser != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    var principal =
                            new UserPrincipal(jwtUser.id(), jwtUser.email());

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {
                // 토큰 오류 → 인증 실패로 처리
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    // Controller에서 바로 사용 가능
    public record UserPrincipal(Long id, String email) {}




}
