package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.RegisterRequest;

public interface AuthService {
    public String register(RegisterRequest request);
}
