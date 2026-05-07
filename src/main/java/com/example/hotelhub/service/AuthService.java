package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;

public interface AuthService {
   RegisterResponse register(RegisterRequest request);
   LoginResponse login(LoginRequest request);

}
