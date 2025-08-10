package com.example.bankcards.exception;

public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException() {
        super("Недостаточно средств на карте");
    }
}
