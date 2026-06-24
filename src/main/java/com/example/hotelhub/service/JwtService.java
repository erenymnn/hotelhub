package com.example.hotelhub.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractEmail(String token);

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token, String email);

    Claims extractAllClaims(String token);
}
