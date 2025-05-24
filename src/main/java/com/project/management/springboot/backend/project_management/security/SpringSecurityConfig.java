package com.project.management.springboot.backend.project_management.security;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.project.management.springboot.backend.project_management.repositories.UserRepository;
import com.project.management.springboot.backend.project_management.security.filter.JwtAuthenticationFilter;
import com.project.management.springboot.backend.project_management.security.filter.JwtValidationFilter;

@Configuration
public class SpringSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) throws Exception {
        return http.authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.GET, "/auth").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/check-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/resend-verification").permitAll()
                .requestMatchers(HttpMethod.GET, "/check-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login/google").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/user-board").permitAll()
                .requestMatchers(HttpMethod.PUT, "/auth/profile/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/profile/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/emails").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/user-roles").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/confirm-email-change").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/stripe/create-checkout-session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/stripe/cancel-subscription").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/stripe/webhook").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stripe/subscription-status").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/stripe/reactivate-subscription").permitAll()
                .requestMatchers(HttpMethod.GET, "/payment/success").permitAll()
                .requestMatchers(HttpMethod.GET, "/payment/cancel").permitAll()
                .requestMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET, "/.well-known/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
                .addFilter(new JwtAuthenticationFilter(authenticationManager, jwtService, userRepository))
                .addFilterAfter(new JwtValidationFilter(authenticationManager, jwtService),
                        JwtAuthenticationFilter.class)
                .csrf(config -> config.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-token"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return corsBean;
    }
}
