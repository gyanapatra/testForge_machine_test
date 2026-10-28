package com.testforge.auth.request;

import com.testforge.auth.dto.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "Username is required")
        @Size(
                min = 8,
                max = 50,
                message = "Username must contain between 8 and 50 characters"
        )
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Pattern(
                regexp = "^(?=.*@)(?=.*_).+$",
                message = "Email must contain @ and _"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 12,
                message = "Password must contain between 8 and 12 characters"
        )
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,12}$",
                message = "Password must contain uppercase, lowercase, number and special character"
        )
        String password,

        @NotNull(message = "User type is required")
        UserRole userType,

        @NotBlank(message = "Country code is required")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "Country code must contain exactly two letters"
        )
        String countryCode
) {
}
