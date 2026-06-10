package com.stride.ecom.dto;

import com.stride.ecom.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long   id;
    private String name;
    private String email;
    private User.Role role;

    public LoginResponse(String token, Long id, String name, String email, User.Role role) {
        this.token = token;
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.role  = role;
    }
}
