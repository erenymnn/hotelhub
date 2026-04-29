package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.RegisterRequest;
import com.example.hotelhub.entity.Role;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public String register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Şifreyi BCrypt ile şifreliyoruz
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gelen String rolleri güvenli bir şekilde Enum'a çeviriyoruz
        Set<Role> roles = request.getRoles().stream()
                .map(roleStr -> Role.valueOf(roleStr.toUpperCase())) // Artık patlamaz!
                .collect(Collectors.toSet());
        user.setRoles(roles);

        userRepository.save(user);
        return "Kullanıcı başarıyla kaydedildi!";
    }
}
