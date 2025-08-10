package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {
    
    public CardNotFoundException(String message) {
        super(message);
    }
    
    public CardNotFoundException(Long cardId) {
        super("Карта с ID " + cardId + " не найдена");
    }
}
