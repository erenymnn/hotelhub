package com.example.hotelhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API projelerinde (Postman ile test için) devre dışı bırakılır
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ŞİMDİLİK her şeyi kabul et (accept) diyoruz
                );

        return http.build();
    }

}
