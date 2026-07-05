package com.example.hotelhub.repository;

import com.example.hotelhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);//o kullanıcı olmayabilir sistem patlamamsı için null kontrolüne gerek kalmaz.

}
