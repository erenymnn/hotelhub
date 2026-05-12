package com.example.hotelhub.entity;

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
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {
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
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Rollerimizi Spring'in anlayacağı dile çeviriyoruz
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email; // Giriş yaparken email kullanıyoruz
    }

    // Aşağıdakilerin hepsini TRUE yapıyoruz ki giriş engellenmesin
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

