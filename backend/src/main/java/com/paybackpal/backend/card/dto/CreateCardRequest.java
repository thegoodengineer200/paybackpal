package com.paybackpal.backend.card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCardRequest {

    @NotBlank(message = "Card name is required")
    @Size(max = 100, message = "Card name must be at most 100 characters")
    private String cardName;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must be at most 100 characters")
    private String bankName;

    @NotBlank(message = "Last four digits are required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Last four digits must contain exactly 4 digits")
    private String lastFourDigits;

    @NotNull(message = "Billing cycle day is required")
    @Min(value = 1, message = "Billing cycle day must be between 1 and 31")
    @Max(value = 31, message = "Billing cycle day must be between 1 and 31")
    private Integer billingCycleDay;

    @NotNull(message = "Due day is required")
    @Min(value = 1, message = "Due day must be between 1 and 31")
    @Max(value = 31, message = "Due day must be between 1 and 31")
    private Integer dueDay;

    public CreateCardRequest() {
    }

    public String getCardName() {
        return cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public Integer getBillingCycleDay() {
        return billingCycleDay;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public void setBillingCycleDay(Integer billingCycleDay) {
        this.billingCycleDay = billingCycleDay;
    }

    public void setDueDay(Integer dueDay) {
        this.dueDay = dueDay;
    }
}