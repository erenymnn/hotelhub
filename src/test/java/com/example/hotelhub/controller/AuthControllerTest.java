package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;
import com.example.hotelhub.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class},
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.example.hotelhub.config.SecurityConfig.class,
            com.example.hotelhub.config.JwtAuthenticationFilter.class,
            com.example.hotelhub.config.RateLimitFilter.class
        }
    )
)
@AutoConfigureMockMvc(addFilters = false) // Güvenlik katmanını testte devre dışı bırakıyoruz ki sadece controller'ı test edelim
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_WhenValidRequest_ShouldReturn200() throws Exception {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "John", "Doe", null);
        RegisterResponse response = new RegisterResponse("Kayıt başarılı", "test@test.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.message").value("Kayıt başarılı"));
    }

    @Test
    void register_WhenEmailIsInvalid_ShouldReturn400() throws Exception {
        // email is not a valid email format
        RegisterRequest request = new RegisterRequest("invalid-email", "password123", "John", "Doe", null);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WhenValidCredentials_ShouldReturn200AndSetCookie() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        LoginResponse response = new LoginResponse("mock-jwt-token", "test@test.com");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Başarıyla giriş yapıldı");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.message").value("Başarıyla giriş yapıldı"))
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().httpOnly("jwt_token", true));
    }

    @Test
    void logout_ShouldReturn204AndClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().maxAge("jwt_token", 0));
    }
}

