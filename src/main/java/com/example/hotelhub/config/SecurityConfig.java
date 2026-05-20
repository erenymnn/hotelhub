package com.example.hotelhub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity //  Metot seviyesinde yetkilendirmeyi (PreAuthorize) aktif eder
@RequiredArgsConstructor // Bunu ekledik ki filtreyi içeri alabilsin
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Bizim yazdığımız filtre
    private final AuthenticationProvider authenticationProvider; // ApplicationConfig'deki Bean

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Login, Register, Hata mesajları VE Otel Arama kısımları herkese açık (serbest)
                        .requestMatchers("/api/auth/**", "/error", "/api/hotels/search").permitAll()
                        // Rezervasyon yapmak, iptal etmek vb. DİĞER HER ŞEY KİLİTLİ!
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Session yok, sadece Token!
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Filtreyi ekledik

        return http.build();
    }
}