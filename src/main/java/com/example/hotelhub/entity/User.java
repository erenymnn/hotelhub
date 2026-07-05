package com.example.hotelhub.entity;

import com.example.hotelhub.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) //ee ama service kısmında kontrol vardır ama 2 kullanıcı aynı anda kayıta basarsa senkronizasyon hatası olusabilir unique lazım.
    private String email; // örnegin sistemde bir gün valid anatasyonu kullanmayı unuttun ise bu 2. savunma olarak ortaya çıkar sistemde tek güvenlik yeterli olmaz.
    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;


    @ElementCollection(fetch = FetchType.EAGER) //eager ile veritabanından çektigin an onunla ilişkili herşeyi hemen çek demek. tek sroguyla herşeyi çekersin role bilgileri bize hemen lazım old için.
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    private boolean isActive = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return java.util.List.of();
        }
        // Sadece role.name() dön, ROLE_ öneki ekleme.
        // Böylece SecurityConfig'deki hasAuthority('MANAGER') ile tam eşleşir.
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
    @Override
    public String getUsername() {
        return email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}

