package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateCardDto {
    
    @NotBlank(message = "Владелец обязателен")
    private String owner;
    
    @NotNull(message = "Дата истечения обязательна")
    private LocalDate expiryDate;
    
    @Positive(message = "Начальный баланс должен быть положительным")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    public CreateCardDto() {}

    public CreateCardDto(String owner, LocalDate expiryDate, BigDecimal initialBalance) {
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.initialBalance = initialBalance;
    }

    // Getters and Setters
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
