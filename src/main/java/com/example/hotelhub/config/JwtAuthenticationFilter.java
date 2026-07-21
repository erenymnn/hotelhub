package com.example.hotelhub.config;

import com.example.hotelhub.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JWT_COOKIE_NAME = "jwt_token";
    private final JwtService jwtService;
    private final HandlerExceptionResolver exceptionResolver;

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override //kimin giriş yaptıgı onemli degil burada public
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/api/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = extractJwtFromCookies(request); //first cookie

        if (jwt == null) { //if not cookie,look header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Token'ın içini açıyoruz
            Claims claims = jwtService.extractAllClaims(jwt);
            String userEmail = claims.getSubject();

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // JWT içinden rolleri alıyoruz
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // VERİTABANINA İNMEDEN, sadece hafızada yaşayan sanal bir UserDetails oluşturuyoruz!
                User principal =
                        new org.springframework.security.core.userdetails.User(userEmail, "", authorities);

                // Token hala geçerli mi kontrol ediyoruz (Süresi dolmuş mu vs.)
                // (Senin isTokenValid metodun UserDetails istiyorsa bu sanal principal'ı verebiliriz)
                if (jwtService.isTokenValid(jwt, principal.getUsername())) {

                    //  Sisteme "Bu kişi yetkilidir" diyoruz
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT Doğrulama Hatası: " + e.getMessage());
            exceptionResolver.resolveException(request, response, null, e); //süresi dolduysa eğer hatayı yakalar ve düzgün bir mesaj döner
        }
    }
//bileti hem cookilerden hem de authorization:bearear token başlıgından arar eğer null ise tokenın yok der
    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null; //null dondurme sebebim sistem çökmemesi için metod nul doner ve alt satırda patlardı.
        for (Cookie cookie : request.getCookies()) {
            if (JWT_COOKIE_NAME.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}