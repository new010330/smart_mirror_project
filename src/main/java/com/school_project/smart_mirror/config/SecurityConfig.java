package com.school_project.smart_mirror.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school_project.smart_mirror.auth.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())        // CORS 비활성화
                .csrf(csrf -> csrf.disable())        // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**", "/").permitAll()
                        .requestMatchers("/homes/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT에 적합한 세션 정책
                )
//                .formLogin(form -> form
//                        .loginPage("/users/login")
//                        .loginProcessingUrl("users/login")
//                        .defaultSuccessUrl("/")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/users/logout")
//                        .logoutSuccessUrl("/")
//                        .invalidateHttpSession(true)// 여기까지 수정(추가 필요)
//                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable()) // 폼 로그인 비활성화
                .logout(logout -> logout.disable()); // 로그아웃도 비활성화

        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    public static class JwtFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;

        public JwtFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            try {
                String token = getTokenFromRequest(request);

                // 토큰이 없는 경우는 그냥 다음 필터로 진행 (인증 불필요한 경로일 수 있음)
                if (token == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 토큰 검증
                if (jwtUtil.validateToken(token)) {
                    log.info("유효한 토큰 확인");
                    // 마스킹된 토큰 로깅 (보안 강화)
                    log.info("Token: " + maskToken(token));

                    String memberId = jwtUtil.getMemberIdFromToken(token);

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(memberId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 인증 성공 시 필터 체인 계속 진행
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("유효하지 않은 토큰 - 인증 실패");
                    handleInvalidToken(response, "Invalid token");
                }
            } catch (ExpiredJwtException e) {
                log.warn("만료된 토큰: {}", e.getMessage());
                handleInvalidToken(response, "Token expired");
            } catch (MalformedJwtException e) {
                log.warn("잘못된 형식의 토큰: {}", e.getMessage());
                handleInvalidToken(response, "Malformed token");
            } catch (SignatureException e) {
                log.warn("유효하지 않은 토큰 서명: {}", e.getMessage());
                handleInvalidToken(response, "Invalid token signature");
            } catch (Exception e) {
                log.error("토큰 처리 중 예외 발생: {}", e.getMessage());
                handleInvalidToken(response, "Authentication error");
            }
        }

        private String maskToken(String token) {
            if (token.length() <= 10) {
                return "***";
            }
            // 앞의 5자와 뒤의 5자만 표시하고 나머지는 마스킹
            return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
        }

        private String getTokenFromRequest(HttpServletRequest request) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                log.debug("Bearer 토큰 감지");
                return bearerToken.substring(7);
            }
            return null;
        }

        private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
            SecurityContextHolder.clearContext(); // 보안 컨텍스트 초기화

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", message);
            errorResponse.put("status", String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));

            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
        }
    }
}
