package com.example.hotelhub.exception;

import com.example.hotelhub.dto.response.ErrorResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {


    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class) //aranan bir bilgi bulunamadıgında bu hata fırlatılır ornegin id 99 gibi. 404
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(UserAlreadyExistsException.class) //eposta zaten kayıtlıysa gonderilecek hata çeşidi. 409
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(RoomAlreadyBookedException.class) //oda doluysa yonlendirilecek hata çeşidi. 409
    public ResponseEntity<ErrorResponse> handleRoomAlreadyBookedException(RoomAlreadyBookedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

//bu iş hatalarını yakalar mantıksal hata service de ben 5 dedim sen bana -1 gönderdin ise veya odayı rezerve etmeye çalışıyon oda dolu ise veya bad request 400
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleLogicExceptions(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(), //hatanın mesajı
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
//müşteri rolündeki biri admin sayfasına girmeye kalkarsa 403 forbidden yetki doner.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Rate limit aşıldığında 429 Too Many Requests döner.
    // Filter seviyesinde direkt JSON yazılsa da, eğer istek bir şekilde controller'a ulaşırsa bu handler devreye girer.
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

//ornegin email hatalı ise bir map olusturur ve json formatında döner.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Hatalı alanları bulup "alanAdı": "Hata mesajı" şeklinde map'e dolduruyoruz
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
//eğer token süresi dolduysa veya sahteyse 401 Unauthorized yetkisiz döner.
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException ex) {

        String message = messageSource.getMessage("error.session.expired", null, LocaleContextHolder.getLocale()); //çok dil yapısı kullanıyoruz.

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                message,
                HttpStatus.UNAUTHORIZED.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
//401 Unauthorized
//Hepsi "kullanıcının kimliği belirsiz veya geçersiz" anlamına geldiği için bunları tek tek yakalamak yerine bir küme içinde ({...}) yakalamak kodunu çok temiz tutar.
    @ExceptionHandler({
            io.jsonwebtoken.security.SignatureException.class,
            io.jsonwebtoken.MalformedJwtException.class,
            io.jsonwebtoken.UnsupportedJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtSignatureException(Exception ex) {

        String message = messageSource.getMessage("error.jwt.invalid", null, LocaleContextHolder.getLocale());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
               message,
                HttpStatus.UNAUTHORIZED.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
//eğer kodlar dışında hiç beklemedigin veritabanı bağlantısı gibi bir hata olusursa 500 Internal Server Error döner. genel bir hata yani.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                "Beklenmeyen bir hata oluştu: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
