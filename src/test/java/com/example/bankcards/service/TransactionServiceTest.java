package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private Transaction testTransaction;
    private TransactionDto transactionDto;
    private static final Long USER_ID = 1L;
    private static final Long FROM_CARD_ID = 1L;
    private static final Long TO_CARD_ID = 2L;
    private static final Long TRANSACTION_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        fromCard = new Card();
        fromCard.setId(FROM_CARD_ID);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(testUser);

        toCard = new Card();
        toCard.setId(TO_CARD_ID);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(testUser);

        testTransaction = new Transaction(fromCard, toCard, BigDecimal.valueOf(100));
        testTransaction.setId(TRANSACTION_ID);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setCreatedAt(LocalDateTime.now());

        transactionDto = new TransactionDto();
        transactionDto.setFromCardId(FROM_CARD_ID);
        transactionDto.setToCardId(TO_CARD_ID);
        transactionDto.setAmount(BigDecimal.valueOf(100));
    }

    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction successfully")
        void createTransaction_Success() {
            // Given
            when(cardService.getCardEntityById(FROM_CARD_ID)).thenReturn(fromCard);
            when(cardService.getCardEntityById(TO_CARD_ID)).thenReturn(toCard);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(cardService.saveCard(any(Card.class))).thenReturn(fromCard, toCard);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

            // When
            TransactionDto result = transactionService.createTransaction(transactionDto, USER_ID);

            // Then
            assertNotNull(result);
            verify(cardService, times(2)).getCardEntityById(any());
            verify(transactionRepository, times(2)).save(any(Transaction.class));
            verify(cardService, times(2)).saveCard(any(Card.class));
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not authorized for from card")
        void createTransaction_UnauthorizedFromCard() {
            // Given
            User differentUser = new User();
            differentUser.setId(2L);
            fromCard.setUser(differentUser);
            when(cardService.getCardEntityById(FROM_CARD_ID)).thenReturn(fromCard);

            // When & Then
            assertThrows(UnauthorizedException.class, () -> 
                transactionService.createTransaction(transactionDto, USER_ID));
            verify(cardService).getCardEntityById(FROM_CARD_ID);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not authorized for to card")
        void createTransaction_UnauthorizedToCard() {
            // Given
            User differentUser = new User();
            differentUser.setId(2L);
            toCard.setUser(differentUser);
            when(cardService.getCardEntityById(FROM_CARD_ID)).thenReturn(fromCard);
            when(cardService.getCardEntityById(TO_CARD_ID)).thenReturn(toCard);

            // When & Then
            assertThrows(UnauthorizedException.class, () -> 
                transactionService.createTransaction(transactionDto, USER_ID));
            verify(cardService, times(2)).getCardEntityById(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when from card is not active")
        void createTransaction_FromCardNotActive() {
            // Given
            fromCard.setStatus(CardStatus.BLOCKED);
            when(cardService.getCardEntityById(FROM_CARD_ID)).thenReturn(fromCard);
            when(cardService.getCardEntityById(TO_CARD_ID)).thenReturn(toCard);

            // When & Then
            assertThrows(RuntimeException.class, () -> 
                transactionService.createTransaction(transactionDto, USER_ID));
            verify(cardService, times(2)).getCardEntityById(any());
        }

        @Test
        @DisplayName("Should throw InsufficientBalanceException when insufficient balance")
        void createTransaction_InsufficientBalance() {
            // Given
            transactionDto.setAmount(BigDecimal.valueOf(2000)); // More than balance
            when(cardService.getCardEntityById(FROM_CARD_ID)).thenReturn(fromCard);
            when(cardService.getCardEntityById(TO_CARD_ID)).thenReturn(toCard);

            // When & Then
            assertThrows(InsufficientBalanceException.class, () -> 
                transactionService.createTransaction(transactionDto, USER_ID));
            verify(cardService, times(2)).getCardEntityById(any());
        }
    }

    @Nested
    @DisplayName("Get Transaction Tests")
    class GetTransactionTests {

        @Test
        @DisplayName("Should get transaction by ID successfully")
        void getTransactionById_Success() {
            // Given
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));

            // When
            TransactionDto result = transactionService.getTransactionById(TRANSACTION_ID, USER_ID);

            // Then
            assertNotNull(result);
            assertEquals(TRANSACTION_ID, result.getId());
            verify(transactionRepository).findById(TRANSACTION_ID);
        }

        @Test
        @DisplayName("Should throw RuntimeException when transaction not found")
        void getTransactionById_NotFound() {
            // Given
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> 
                transactionService.getTransactionById(TRANSACTION_ID, USER_ID));
            verify(transactionRepository).findById(TRANSACTION_ID);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not authorized")
        void getTransactionById_Unauthorized() {
            // Given
            User differentUser = new User();
            differentUser.setId(2L);
            fromCard.setUser(differentUser);
            toCard.setUser(differentUser);
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));

            // When & Then
            assertThrows(UnauthorizedException.class, () -> 
                transactionService.getTransactionById(TRANSACTION_ID, USER_ID));
            verify(transactionRepository).findById(TRANSACTION_ID);
        }
    }

    @Nested
    @DisplayName("Get User Transactions Tests")
    class GetUserTransactionsTests {

        @Test
        @DisplayName("Should get user transactions successfully")
        void getUserTransactions_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> transactionPage = new PageImpl<>(List.of(testTransaction));
            when(transactionRepository.findByUserIdAndStatus(USER_ID, TransactionStatus.PENDING, pageable))
                .thenReturn(transactionPage);

            // When
            Page<TransactionDto> result = transactionService.getUserTransactions(USER_ID, pageable, TransactionStatus.PENDING);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(transactionRepository).findByUserIdAndStatus(USER_ID, TransactionStatus.PENDING, pageable);
        }
    }

    @Nested
    @DisplayName("Cancel Transaction Tests")
    class CancelTransactionTests {

        @Test
        @DisplayName("Should cancel pending transaction successfully")
        void cancelTransaction_Pending_Success() {
            // Given
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

            // When
            TransactionDto result = transactionService.cancelTransaction(TRANSACTION_ID, USER_ID);

            // Then
            assertNotNull(result);
            verify(transactionRepository).findById(TRANSACTION_ID);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when trying to cancel completed transaction")
        void cancelTransaction_Completed_ThrowsException() {
            // Given
            testTransaction.setStatus(TransactionStatus.COMPLETED);
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));

            // When & Then
            assertThrows(RuntimeException.class, () -> 
                transactionService.cancelTransaction(TRANSACTION_ID, USER_ID));
            verify(transactionRepository).findById(TRANSACTION_ID);
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should get all transactions successfully")
        void getAllTransactions_Success() {
            // Given
            when(transactionRepository.findAll()).thenReturn(List.of(testTransaction));

            // When
            List<TransactionDto> result = transactionService.getAllTransactions();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findAll();
        }
    }
}
