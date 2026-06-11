package com.smartfarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // 센서·장치 조회는 대시보드에서 인증 없이 사용
                .requestMatchers(HttpMethod.GET, "/api/v1/sensors/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/devices/**").permitAll()
                // 장치 등록·수정·삭제 및 나머지 쓰기 작업은 인증 필요
                // Phase 2: JWT 필터 추가 후 완전히 동작함 (현재 401 반환)
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
