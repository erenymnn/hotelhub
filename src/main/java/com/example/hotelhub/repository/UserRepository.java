package com.example.hotelhub.repository;

import com.example.hotelhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    // Optional kullanıyoruz ki kullanıcı yoksa hata vermek yerine "boş" dönebilsin
    Optional<User> findByEmail(String email);

}
