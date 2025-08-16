package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberUtil;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Tests")
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
    private static final Long USER_ID = 1L;
    private static final Long CARD_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCard = new Card();
        testCard.setId(CARD_ID);
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

    @Nested
    @DisplayName("Create Card Tests")
    class CreateCardTests {

        @Test
        @DisplayName("Should create card successfully")
        void createCard_Success() {
            // Given
            when(userService.getUserEntityById(USER_ID)).thenReturn(testUser);
            when(cardNumberUtil.generateCardNumber()).thenReturn("1234567890123456");
            when(cardNumberUtil.encryptCardNumber("1234567890123456")).thenReturn("encrypted1234567890123456");
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            CardDto result = cardService.createCard(createCardDto, USER_ID);

            // Then
            assertNotNull(result);
            assertEquals("**** **** **** 3456", result.getCardNumber());
            assertEquals("Test Owner", result.getOwner());
            assertEquals(CardStatus.ACTIVE, result.getStatus());
            assertEquals(BigDecimal.valueOf(1000), result.getBalance());
            
            verify(userService).getUserEntityById(USER_ID);
            verify(cardNumberUtil).generateCardNumber();
            verify(cardRepository).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Get Card Tests")
    class GetCardTests {

        @Test
        @DisplayName("Should get card by ID successfully")
        void getCardById_Success() {
            // Given
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            CardDto result = cardService.getCardById(CARD_ID, USER_ID);

            // Then
            assertNotNull(result);
            assertEquals(CARD_ID, result.getId());
            assertEquals("**** **** **** 3456", result.getCardNumber());
            verify(cardRepository).findById(CARD_ID);
        }

        @Test
        @DisplayName("Should throw CardNotFoundException when card not found")
        void getCardById_NotFound() {
            // Given
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CardNotFoundException.class, () -> cardService.getCardById(CARD_ID, USER_ID));
            verify(cardRepository).findById(CARD_ID);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not authorized")
        void getCardById_Unauthorized() {
            // Given
            Long differentUserId = 2L;
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));

            // When & Then
            assertThrows(UnauthorizedException.class, () -> cardService.getCardById(CARD_ID, differentUserId));
            verify(cardRepository).findById(CARD_ID);
        }
    }

    @Nested
    @DisplayName("Get User Cards Tests")
    class GetUserCardsTests {

        @Test
        @DisplayName("Should get user cards successfully")
        void getUserCards_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Card> cardPage = new PageImpl<>(List.of(testCard));
            when(cardRepository.findByUserIdAndStatusNot(USER_ID, CardStatus.EXPIRED, pageable)).thenReturn(cardPage);
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            Page<CardDto> result = cardService.getUserCards(USER_ID, pageable, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("**** **** **** 3456", result.getContent().get(0).getCardNumber());
            verify(cardRepository).findByUserIdAndStatusNot(USER_ID, CardStatus.EXPIRED, pageable);
        }

        @Test
        @DisplayName("Should get user cards with filters successfully")
        void getUserCards_WithFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Card> cardPage = new PageImpl<>(List.of(testCard));
            when(cardRepository.findByUserIdAndFilters(USER_ID, CardStatus.ACTIVE, "Test", pageable)).thenReturn(cardPage);
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            Page<CardDto> result = cardService.getUserCards(USER_ID, pageable, CardStatus.ACTIVE, "Test");

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(cardRepository).findByUserIdAndFilters(USER_ID, CardStatus.ACTIVE, "Test", pageable);
        }
    }

    @Nested
    @DisplayName("Update Card Tests")
    class UpdateCardTests {

        @Test
        @DisplayName("Should update card status successfully")
        void updateCardStatus_Success() {
            // Given
            CardStatus newStatus = CardStatus.BLOCKED;
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            CardDto result = cardService.updateCardStatus(CARD_ID, newStatus, USER_ID);

            // Then
            assertNotNull(result);
            verify(cardRepository).findById(CARD_ID);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("Should update card balance successfully")
        void updateCardBalance_Success() {
            // Given
            BigDecimal newBalance = BigDecimal.valueOf(2000);
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(cardNumberUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
            when(cardNumberUtil.decryptCardNumber("encrypted1234567890123456")).thenReturn("1234567890123456");

            // When
            CardDto result = cardService.updateCardBalance(CARD_ID, USER_ID, newBalance);

            // Then
            assertNotNull(result);
            verify(cardRepository).findById(CARD_ID);
            verify(cardRepository).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Delete Card Tests")
    class DeleteCardTests {

        @Test
        @DisplayName("Should delete card successfully")
        void deleteCard_Success() {
            // Given
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));

            // When
            cardService.deleteCard(CARD_ID, USER_ID);

            // Then
            verify(cardRepository).findById(CARD_ID);
            verify(cardRepository).delete(testCard);
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should get card entity by ID successfully")
        void getCardEntityById_Success() {
            // Given
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));

            // When
            Card result = cardService.getCardEntityById(CARD_ID);

            // Then
            assertNotNull(result);
            assertEquals(CARD_ID, result.getId());
            verify(cardRepository).findById(CARD_ID);
        }

        @Test
        @DisplayName("Should save card successfully")
        void saveCard_Success() {
            // Given
            when(cardRepository.save(testCard)).thenReturn(testCard);

            // When
            Card result = cardService.saveCard(testCard);

            // Then
            assertNotNull(result);
            assertEquals(testCard, result);
            verify(cardRepository).save(testCard);
        }

        @Test
        @DisplayName("Should check and update expired cards successfully")
        void checkAndUpdateExpiredCards_Success() {
            // Given
            LocalDate today = LocalDate.now();
            List<Card> expiredCards = List.of(testCard);
            when(cardRepository.findExpiredCards(today)).thenReturn(expiredCards);
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);

            // When
            cardService.checkAndUpdateExpiredCards();

            // Then
            verify(cardRepository).findExpiredCards(today);
            verify(cardRepository).save(any(Card.class));
        }
    }
}
