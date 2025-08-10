package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    Page<Card> findByUserIdAndStatusNot(Long userId, CardStatus status, Pageable pageable);
    
    Page<Card> findByUserId(Long userId, Pageable pageable);
    
    List<Card> findByUserIdAndStatus(Long userId, CardStatus status);
    
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:owner IS NULL OR c.owner ILIKE %:owner%)")
    Page<Card> findByUserIdAndFilters(@Param("userId") Long userId, 
                                     @Param("status") CardStatus status, 
                                     @Param("owner") String owner, 
                                     Pageable pageable);
    
    Optional<Card> findByCardNumber(String cardNumber);
    
    @Query("SELECT c FROM Card c WHERE c.expiryDate < :date")
    List<Card> findExpiredCards(@Param("date") LocalDate date);
    
    boolean existsByCardNumber(String cardNumber);
}
