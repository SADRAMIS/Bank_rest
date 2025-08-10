package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CardService cardService;
    
    public TransactionDto createTransaction(TransactionDto transactionDto, Long userId) {
        Card fromCard = cardService.getCardEntityById(transactionDto.getFromCardId());
        Card toCard = cardService.getCardEntityById(transactionDto.getToCardId());
        
        // Проверяем, что обе карты принадлежат пользователю
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к одной из карт");
        }
        
        // Проверяем, что карты активны
        if (fromCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE || 
            toCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new RuntimeException("Одна из карт не активна");
        }
        
        // Проверяем баланс
        if (fromCard.getBalance().compareTo(transactionDto.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }
        
        // Создаем транзакцию
        Transaction transaction = new Transaction(fromCard, toCard, transactionDto.getAmount());
        transaction.setStatus(TransactionStatus.PENDING);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Выполняем перевод
        return executeTransaction(savedTransaction);
    }
    
    private TransactionDto executeTransaction(Transaction transaction) {
        try {
            Card fromCard = transaction.getFromCard();
            Card toCard = transaction.getToCard();
            BigDecimal amount = transaction.getAmount();
            
            // Списываем с карты отправителя
            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            cardService.saveCard(fromCard);
            
            // Зачисляем на карту получателя
            toCard.setBalance(toCard.getBalance().add(amount));
            cardService.saveCard(toCard);
            
            // Обновляем статус транзакции
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            
            Transaction completedTransaction = transactionRepository.save(transaction);
            return convertToDto(completedTransaction);
            
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Ошибка выполнения транзакции", e);
        }
    }
    
    public TransactionDto getTransactionById(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
        
        // Проверяем, что пользователь имеет доступ к транзакции
        if (!transaction.getFromCard().getUser().getId().equals(userId) && 
            !transaction.getToCard().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной транзакции");
        }
        
        return convertToDto(transaction);
    }
    
    public Page<TransactionDto> getUserTransactions(Long userId, Pageable pageable, TransactionStatus status) {
        Page<Transaction> transactions = transactionRepository.findByUserIdAndStatus(userId, status, pageable);
        return transactions.map(this::convertToDto);
    }
    
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TransactionDto cancelTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
        
        // Проверяем, что пользователь имеет доступ к транзакции
        if (!transaction.getFromCard().getUser().getId().equals(userId) && 
            !transaction.getToCard().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной транзакции");
        }
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            throw new RuntimeException("Нельзя отменить завершенную транзакцию");
        }
        
        if (transaction.getStatus() == TransactionStatus.PENDING) {
            // Возвращаем деньги, если транзакция была выполнена
            if (transaction.getProcessedAt() != null) {
                Card fromCard = transaction.getFromCard();
                Card toCard = transaction.getToCard();
                BigDecimal amount = transaction.getAmount();
                
                fromCard.setBalance(fromCard.getBalance().add(amount));
                cardService.saveCard(fromCard);
                
                toCard.setBalance(toCard.getBalance().subtract(amount));
                cardService.saveCard(toCard);
            }
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED);
        Transaction cancelledTransaction = transactionRepository.save(transaction);
        return convertToDto(cancelledTransaction);
    }
    
    private TransactionDto convertToDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getFromCard().getId(),
                transaction.getToCard().getId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getProcessedAt()
        );
    }
}
