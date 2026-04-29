package com.example.hotelhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password; // Bu şifreyi daha sonra BCrypt ile şifreleyeceğiz

    private String firstName;
    private String lastName;

    // Kullanıcı silindiğinde rolleri de silinsin diye ElementCollection
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING) // Veritabanında "ADMIN" diye metin olarak saklanır
    private Set<Role> roles;//Bir kullanıcının aynı role örneğin iki tane ADMIN rolüne sahip olmasını engelleriz

    private boolean isActive = true;
}
