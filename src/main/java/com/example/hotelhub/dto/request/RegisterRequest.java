package com.example.hotelhub.dto.request;

import com.example.hotelhub.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Set<String> roles; //
}
