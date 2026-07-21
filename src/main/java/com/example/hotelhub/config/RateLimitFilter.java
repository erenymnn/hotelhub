package com.example.hotelhub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis tabanlı Rate Limiting filtresi.
 *
 * Her gelen isteğin IP adresini Redis'te bir sayaçla takip eder.
 * Belirlenen pencere süresi içinde maksimum istek sayısı aşılırsa,
 * istek arka uçtaki Java servisine hiç inmeden 429 Too Many Requests döner.
 *
 * Algoritma: Fixed Window Counter
 * Redis Key Formatı: "rate_limit:{ip}"
 * Varsayılan Limit: Dakikada 50 istek
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rate.limit.requests:50}")
    private int maxRequests;

    @Value("${rate.limit.window-seconds:60}")
    private int windowSeconds;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Rate limit kontrolünden muaf tutulacak endpoint'ler.
     * Actuator (sağlık kontrolü, prometheus) ve Swagger (API dokümantasyonu)
     * gibi izleme endpoint'leri sınırlandırılmaz.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String redisKey = RATE_LIMIT_PREFIX + clientIp;

        try {
            // Redis'te sayacı atomik olarak 1 artır
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount != null && currentCount == 1) {
                // Bu IP'den gelen ilk istek → Pencere süresini başlat
                redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
            }

            // Kalan istek hakkı bilgisini response header'larına ekle
            long remaining = Math.max(0, maxRequests - (currentCount != null ? currentCount : 0));
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("X-RateLimit-Reset", String.valueOf(ttl != null ? ttl : windowSeconds));

            if (currentCount != null && currentCount > maxRequests) {
                // ⛔ Limit aşıldı! İstek backend'e inmeden burada reddediliyor.
                log.warn("Rate limit aşıldı! IP: {}, İstek sayısı: {}/{}, Endpoint: {} {}",
                        clientIp, currentCount, maxRequests,
                        request.getMethod(), request.getRequestURI());

                writeRateLimitResponse(response, ttl);
                return; // filterChain.doFilter çağrılmıyor → istek arka uca inmez!
            }

            // ✅ Limit içinde, isteği bir sonraki filtreye (JwtAuthFilter → SecurityFilter) geçir
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // Redis bağlantı hatası olursa sistemi kilitlemiyoruz,
            // isteği geçirip loglamakla yetiniyoruz (fail-open stratejisi)
            log.error("Rate limiting sırasında Redis hatası oluştu, istek geçiriliyor: {}", ex.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Gerçek client IP adresini çözer.
     * Proxy/Load Balancer arkasındaysa X-Forwarded-For header'ından alır,
     * yoksa doğrudan request'ten alır.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For birden fazla IP içerebilir: "client, proxy1, proxy2"
            // İlk IP gerçek client'ın IP'sidir
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 429 Too Many Requests JSON yanıtı oluşturur.
     * ErrorResponse formatıyla uyumlu: {"timestamp": "...", "message": "...", "status": 429}
     */
    private void writeRateLimitResponse(HttpServletResponse response, Long ttl) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Client'a ne kadar beklemesi gerektiğini söyle
        long retryAfter = (ttl != null && ttl > 0) ? ttl : windowSeconds;
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        // ErrorResponse formatında JSON yanıtı oluştur
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().format(FORMATTER));
        errorBody.put("message", "Çok fazla istek gönderdiniz. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.");
        errorBody.put("status", HttpStatus.TOO_MANY_REQUESTS.value());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
