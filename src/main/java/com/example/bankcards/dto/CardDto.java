package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CardDto {
    private Long id;
    
    @NotBlank(message = "Номер карты обязателен")
    private String cardNumber; // Маскированный номер
    
    @NotBlank(message = "Владелец обязателен")
    private String owner;
    
    @NotNull(message = "Дата истечения обязательна")
    private LocalDate expiryDate;
    
    private CardStatus status;
    
    @Positive(message = "Баланс должен быть положительным")
    private BigDecimal balance;
    
    private Long userId;

    public CardDto() {}

    public CardDto(Long id, String cardNumber, String owner, LocalDate expiryDate, 
                   CardStatus status, BigDecimal balance, Long userId) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

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

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
