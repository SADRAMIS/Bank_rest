package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardNumberUtil cardNumberUtil;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CreateCardDto createCardDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted1234567890123456");
        testCard.setOwner("Test Owner");
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000));
        testCard.setUser(testUser);

        createCardDto = new CreateCardDto();
        createCardDto.setOwner("Test Owner");
        createCardDto.setExpiryDate(LocalDate.now().plusYears(2));
        createCardDto.setInitialBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void createCard_Success() {
        // Given
        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardNumberUtil.generateCardNumber()).thenReturn("1234567890123456");
        when(cardNumberUtil.encryptCardNumber("1234567890123456")).thenReturn("encrypted1234567890123456");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

        // When
        CardDto result = cardService.createCard(createCardDto, 1L);

        // Then
        assertNotNull(result);
        assertEquals("**** **** **** 3456", result.getCardNumber());
        assertEquals("Test Owner", result.getOwner());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
    }

    @Test
    void getCardById_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

        // When
        CardDto result = cardService.getCardById(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getCardNumber());
    }

    @Test
    void getCardById_NotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(1L, 1L));
    }

    @Test
    void getUserCards_Success() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findByUserIdAndStatusNot(1L, CardStatus.EXPIRED, pageable)).thenReturn(cardPage);
        when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

        // When
        Page<CardDto> result = cardService.getUserCards(1L, pageable, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("**** **** **** 3456", result.getContent().get(0).getCardNumber());
    }
}
