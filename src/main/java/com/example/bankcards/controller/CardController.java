package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
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
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardDto createCardDto, @RequestParam Long userId) {
        CardDto createdCard = cardService.createCard(createCardDto, userId);
        return ResponseEntity.ok(createdCard);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        CardDto card = cardService.getCardById(cardId, userId);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getUserCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String owner,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<CardDto> cards = cardService.getUserCards(userId, pageable, status, owner);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{cardId}/status")
    public ResponseEntity<CardDto> updateCardStatus(@PathVariable Long cardId, @RequestParam CardStatus status) {
        Long userId = getCurrentUserId();
        CardDto updatedCard = cardService.updateCardStatus(cardId, status, userId);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getAllCards() {
        List<CardDto> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
