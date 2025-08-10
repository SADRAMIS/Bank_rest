package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        Long userId = getCurrentUserId();
        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto, userId);
        return ResponseEntity.ok(createdTransaction);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable Long transactionId) {
        Long userId = getCurrentUserId();
        TransactionDto transaction = transactionService.getTransactionById(transactionId, userId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @RequestParam(required = false) TransactionStatus status,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<TransactionDto> transactions = transactionService.getUserTransactions(userId, pageable, status);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<TransactionDto> cancelTransaction(@PathVariable Long transactionId) {
        Long userId = getCurrentUserId();
        TransactionDto cancelledTransaction = transactionService.cancelTransaction(transactionId, userId);
        return ResponseEntity.ok(cancelledTransaction);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        List<TransactionDto> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
