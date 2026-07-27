package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.exception.UserAlreadyExistsException;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@test.com", "password", "Test", "User", null);
        loginRequest = new LoginRequest("test@test.com", "password");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRoles(Set.of(Role.CUSTOMER));
    }

    @Test
    void register_WhenEmailNotExists_ShouldRegisterSuccessfully() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@test.com", response.email());
        assertEquals("Kullanıcı başarıyla kaydedildi!", response.message());
        
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_WhenEmailExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        assertEquals("Bu E-Posta adresi zaten kullanımda!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_WhenRoleIsAdmin_ShouldThrowIllegalArgumentException() {
        RegisterRequest adminRequest = new RegisterRequest("admin@test.com", "pass", "Admin", "User", Set.of("ADMIN"));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(adminRequest));

        assertEquals("Hata: ADMIN rolü seçilemez!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnToken() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.password(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(mockUser)).thenReturn("mockJwtToken");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@test.com", response.email());
        assertEquals("mockJwtToken", response.token());
        verify(jwtService, times(1)).generateToken(mockUser);
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authService.login(loginRequest));

        assertEquals("Kullanıcı bulunamadı!", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WhenPasswordIsIncorrect_ShouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.password(), mockUser.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertEquals("Hatalı şifre!", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }
}
