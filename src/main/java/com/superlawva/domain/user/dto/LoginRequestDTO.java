package com.superlawva.domain.user.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @Email
    private String email;
    private String password;
}
