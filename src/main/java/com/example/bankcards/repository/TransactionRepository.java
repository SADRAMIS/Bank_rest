package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findByFromCardUserIdOrToCardUserId(Long fromUserId, Long toUserId, Pageable pageable);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromCard.user.id = :userId OR t.toCard.user.id = :userId) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<Transaction> findByUserIdAndStatus(@Param("userId") Long userId, 
                                           @Param("status") TransactionStatus status, 
                                           Pageable pageable);
}
