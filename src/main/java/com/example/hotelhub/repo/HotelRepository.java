package com.example.hotelhub.repo;

import com.example.hotelhub.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel,Long> {
// Spring Data JPA arka planda save, findById, findAll gibi metotları bizim için hazır edecek.
}
