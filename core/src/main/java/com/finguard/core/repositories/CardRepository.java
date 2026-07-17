package com.finguard.core.repositories;

import com.finguard.core.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByAccountUserId(Long userId);
    
    List<Card> findByAccountId(Long accountId);
    
    List<Card> findByAccountUserEmail(String email);

    Optional<Card> findByCardNumber(String cardNumber);
}