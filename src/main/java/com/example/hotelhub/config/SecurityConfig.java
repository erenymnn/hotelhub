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
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity //controller kısmındaki @PreAuthorize("hasAuthority('CUSTOMER')") gibi anotasyonları çalıştırır.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Bizim yazdığımız filtre
    private final AuthenticationProvider authenticationProvider; // ApplicationConfig'deki Bean

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) //"Cross-Site Request Forgery" korumasını devre dışı bırakıyoruz. Neden? Çünkü biz Stateless (JWT tabanlı) bir sistem kullanıyoruz ve token bazlı çalıştığımız için CSRF korumasına bu mimaride gerek kalmıyor.
                .authorizeHttpRequests(auth -> auth
                        //  PUBLIC
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Otelleri, odaları ve aramayı herkes görebilir/kullanabilir
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**", "/api/rooms/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/search/**").permitAll()

                        // PRIVATE (Sadece Yetkililer)
                        // Booking işlemleri için en azından giriş yapmış olmak şart
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Geri kalan her yer için en azından login şart
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        //stateless ile hafızada kimseyi tutma her seferinde kimlik bak sonra unut
                )
                .authenticationProvider(authenticationProvider) //Kimlik doğrulama işleminin (kullanıcıyı bulma, şifreyi kontrol etme) nasıl yapılacağını buraya bağlıyoruz.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        //Kendi yazdığımız jwtAuthFilterı, Spring'in standart kullanıcı adı/şifre doğrulama filtresinin önüne yerleştiriyoruz. Böylece kullanıcı istek attığında önce bizim JWT kontrolcümüz çalışıyor.

        return http.build();
    }
}
