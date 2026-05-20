package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.LoginRequest;
import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.dto.response.LoginResponse;
import com.example.hotelhub.dto.response.RegisterResponse;
import com.example.hotelhub.entity.Role;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.UserAlreadyExistsException;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.AuthService;
import com.example.hotelhub.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        Set<Role> roles = request.roles().stream()
                .map(roleStr -> Role.valueOf(roleStr.toUpperCase()))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        userRepository.save(user);


        return new RegisterResponse("Kullanıcı başarıyla kaydedildi!", user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email()) // request.email() oldu
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));


        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Hatalı şifre!");
        }


        String token = jwtService.generateToken(user.getEmail());


        return new LoginResponse(token, user.getEmail());
    }

}
