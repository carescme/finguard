package com.finguard.core.repositories;

import com.finguard.core.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccountIdOrderByCreatedAtDesc(Long sourceAccountId);

    List<Transaction> findByDestinationAccountIdOrderByCreatedAtDesc(Long destinationAccountId);
    
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(Long sourceId, Long destId);
}