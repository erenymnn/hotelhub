package com.example.hotelhub.service.impl;

import com.example.hotelhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    // Cache'i tam olarak burada, metodun çalışması anında yakalıyoruz!
    @Cacheable(value = "userDetails", key = "#username", sync = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("DİKKAT: Spring Security arka planda bu servisi çağırdı!");
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
    }
}