package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;
import com.example.hotelhub.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Kullanıcı kayıt ve giriş (JWT) işlemleri")
public class AuthController {
    private final AuthService authService;
    private final MessageSource messageSource; // I18N içeri aktarıyoruz

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Metot parametrelerine HttpServletResponse ekledik
    @PostMapping("/Login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);


        Cookie cookie = new Cookie("jwt_token", loginResponse.token());

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // Çerez süresi: 1 Gün (Saniye cinsinden)


        //  Çerezi response ekliyoruz
        response.addCookie(cookie);


        String successMessage = messageSource.getMessage("auth.login.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(Map.of(
                "message", successMessage,
                "email", request.email() // Hangi kullanıcının girdiğini frontend'e söyleyebiliriz
        ));
    }
}
