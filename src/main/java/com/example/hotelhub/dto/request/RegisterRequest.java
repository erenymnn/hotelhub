package com.example.hotelhub.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;


public record RegisterRequest(
        @NotBlank(message = "E-posta boş bırakılamaz!")
        @Email(message = "Geçerli bir e-posta giriniz!")
        String email,

        @NotBlank(message = "Şifre boş bırakılamaz!")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır!")
        String password,

        @NotBlank(message = "Ad boş bırakılamaz!")
        String firstName,

        @NotBlank(message = "Soyad boş bırakılamaz!")
        String lastName,

        Set<String> roles) {

}
