package com.example.hotelhub.dto.request;


import java.util.Set;


public record RegisterRequest(String email,
                              String password,
                              String firstName,
                              String lastName,
                              Set<String> roles) {

}
