package com.capstone.adaptivelearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/health", "/api/auth/me", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Dashboard & Analytics: Advisor & Admin only
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADVISOR", "ADMIN")

                        // Prediction creation: Student, Advisor, Admin
                        .requestMatchers("/api/predictions/predict/**").hasAnyRole("STUDENT", "ADVISOR", "ADMIN")

                        // Subject & Grade modifications: Advisor & Admin
                        .requestMatchers(HttpMethod.POST, "/api/subjects/**", "/api/grades/**").hasAnyRole("ADVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/subjects/**", "/api/grades/**").hasAnyRole("ADVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/subjects/**", "/api/grades/**").hasAnyRole("ADVISOR", "ADMIN")

                        // Student management
                        .requestMatchers(HttpMethod.POST, "/api/students/**").hasAnyRole("ADVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyRole("ADVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasAnyRole("ADMIN")

                        // All other authenticated requests
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

