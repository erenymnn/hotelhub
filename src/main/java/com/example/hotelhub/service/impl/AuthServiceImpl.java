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
import com.example.hotelhub.service.AuthService;
import com.example.hotelhub.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Bu E-Posta adresi zaten kullanımda!");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(resolveRoles(request.roles()));

        userRepository.save(user);

        return new RegisterResponse("Kullanıcı başarıyla kaydedildi!", user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Hatalı şifre!");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getEmail());
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        // 1. Rol gelmediyse varsayılan olarak CUSTOMER ver
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            return EnumSet.of(Role.CUSTOMER);
        }

        // 2. Rolleri işlerken ADMIN veya bilmediğimiz bir rol gelirse engelle
        return requestedRoles.stream()
                .map(String::toUpperCase)
                .filter(roleStr -> {
                    if (roleStr.equals("ADMIN")) {
                        throw new IllegalArgumentException("Hata: ADMIN rolü seçilemez!");
                    }
                    return true;
                })
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

}
