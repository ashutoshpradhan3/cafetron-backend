package com.cafetron.auth.dto;

import com.cafetron.user.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String employeeId;
    private String department;
    private Role role;          // optional — default to EMPLOYEE if null
}