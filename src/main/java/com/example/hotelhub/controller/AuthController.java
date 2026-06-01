package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;
import com.example.hotelhub.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    // DİKKAT: Metot parametrelerine HttpServletResponse ekledik
    @PostMapping("/Login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // 1. Kilitli Çerezi (Cookie) Oluşturuyoruz
        // Not:Eğer LoginResponse bir record ise loginResponse.token(), normal class ise loginResponse.getToken() yazmalısın.
        Cookie cookie = new Cookie("jwt_token", loginResponse.token());

        cookie.setHttpOnly(true); // İŞTE SİHİR BURADA! Frontend JavaScript'i bu token'ı asla göremez.
        cookie.setPath("/"); // Uygulamanın tüm sayfalarında geçerli olsun
        cookie.setMaxAge(24 * 60 * 60); // Çerez süresi: 1 Gün (Saniye cinsinden)

        // cookie.setSecure(true); ayarı sadece HTTPS'te çalışır.
        // Şu an localhost (HTTP) kullandığımız için onu eklemiyoruz, canlıya alırken eklenecek.

        //  Çerezi response ekliyoruz
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "message", "Giriş başarılı!",
                "email", request.email() // Hangi kullanıcının girdiğini frontend'e söyleyebiliriz
        ));
    }
}
