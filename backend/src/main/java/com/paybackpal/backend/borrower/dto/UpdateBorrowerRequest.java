package com.paybackpal.backend.borrower.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateBorrowerRequest {

    @NotBlank(message = "Borrower name is required")
    @Size(max = 100, message = "Borrower name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must contain 10 to 15 digits and may start with +"
    )
    private String phoneNumber;

    public UpdateBorrowerRequest() {
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}