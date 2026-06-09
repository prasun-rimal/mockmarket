package com.prasun.mockmarket.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 8, message = "must be at least 8 characters")
        @Pattern(regexp = ".*[A-Z].*", message = "must contain one uppercase letter")
        @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "must contain one symbol")
        String password
) {
}
