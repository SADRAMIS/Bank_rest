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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@Transactional
public class CardService {
    
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardNumberUtil cardNumberUtil;
    
    public CardService(CardRepository cardRepository, UserService userService, CardNumberUtil cardNumberUtil) {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.cardNumberUtil = cardNumberUtil;
    }
    
    @Transactional
    public CardDto createCard(CreateCardDto createCardDto, Long userId) {
        User user = userService.getUserEntityById(userId);
        
        String cardNumber = cardNumberUtil.generateCardNumber();
        String encryptedCardNumber = cardNumberUtil.encryptCardNumber(cardNumber);
        
        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setOwner(createCardDto.getOwner());
        card.setExpiryDate(createCardDto.getExpiryDate());
        card.setBalance(createCardDto.getInitialBalance());
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);
        
        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }
    
    @Transactional(readOnly = true)
    public CardDto getCardById(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной карте");
        }
        
        return convertToDto(card);
    }
    
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(Long userId, Pageable pageable, CardStatus status, String owner) {
        Page<Card> cards;
        
        if (status != null || (owner != null && !owner.trim().isEmpty())) {
            cards = cardRepository.findByUserIdAndFilters(userId, status, owner, pageable);
        } else {
            cards = cardRepository.findByUserIdAndStatusNot(userId, CardStatus.EXPIRED, pageable);
        }
        
        return cards.map(this::convertToDto);
    }
    
    @Transactional
    public CardDto updateCardStatus(Long cardId, CardStatus status, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной карте");
        }
        
        card.setStatus(status);
        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }
    
    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной карте");
        }
        
        cardRepository.delete(card);
    }
    
    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CardDto updateCardBalance(Long cardId, Long userId, BigDecimal newBalance) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Нет доступа к данной карте");
        }
        
        card.setBalance(newBalance);
        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }
    
    @Transactional
    public void checkAndUpdateExpiredCards() {
        LocalDate today = LocalDate.now();
        List<Card> expiredCards = cardRepository.findExpiredCards(today);
        
        for (Card card : expiredCards) {
            if (card.getStatus() == CardStatus.ACTIVE) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public Card getCardEntityById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }
    
    @Transactional
    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }
    
    private CardDto convertToDto(Card card) {
        String maskedCardNumber = cardNumberUtil.maskCardNumber(cardNumberUtil.decryptCardNumber(card.getCardNumber()));
        
        return new CardDto(
                card.getId(),
                maskedCardNumber,
                card.getOwner(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getUser().getId()
        );
    }
}
