package com.example.hotelhub.service;

public interface JwtService {
    String extractEmail(String token);
    String generateToken(String email);
    boolean isTokenValid(String token,String email);
}
