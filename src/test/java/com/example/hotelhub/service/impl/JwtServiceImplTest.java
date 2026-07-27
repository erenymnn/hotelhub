package com.example.hotelhub.service.impl;

import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        // 256-bit+ secure base64 key
        String testSecretKey = "dGhpc0lzQVZlcnlTZWNyZXRLZXlUaGF0SXNBdExlYXN0MjU2Qml0c0xvbmdGb3JKd3RUZXN0ITEyMw==";
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);

        testUser = new User();
        testUser.setEmail("test@hotelhub.com");
        testUser.setRoles(Set.of(Role.CUSTOMER));
    }

    @Test
    void generateToken_ShouldReturnValidJwtString() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT always consists of 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        
        String email = jwtService.extractEmail(token);
        
        assertEquals("test@hotelhub.com", email);
    }

    @Test
    void extractAllClaims_ShouldIncludeRoles() {
        String token = jwtService.generateToken(testUser);

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals("test@hotelhub.com", claims.getSubject());
        
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertTrue(roles.contains("CUSTOMER"));
    }

    @Test
    void isTokenValid_WithCorrectEmail_ShouldReturnTrue() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, "test@hotelhub.com");

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithIncorrectEmail_ShouldReturnFalse() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, "wrong@hotelhub.com");

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithMalformedToken_ShouldReturnFalse() {
        boolean isValid = jwtService.isTokenValid("malformed.token.string", "test@hotelhub.com");
        
        assertFalse(isValid);
    }
}
