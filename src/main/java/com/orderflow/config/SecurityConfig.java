package com.orderflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API tasarladığımız için tarayıcı bağımlı CSRF korumasına bu aşamada ihtiyacımız yok.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Bu endpointler herkese açık olmalı.
                        .requestMatchers("/api/auth/**", "/graphql", "/graphiql").permitAll()
                        // Geri kalan her istek kimlik doğrulaması gerektirir.
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        // Sadece ihtiyaç duyulduğunda (örn: Login sonrası) session yarat.
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // Bir kullanıcı aynı anda sadece 1 cihazdan giriş yapabilsin (Kurumsal Tercih).
                        .maximumSessions(1)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Şifreleri hashlemek (özetini çıkarmak) için güvenli bir standart.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Controller'da kullandığımız AuthManager'ı Spring'in DI (Dependency Injection) container'ına dahil eder.
        return config.getAuthenticationManager();
    }
}