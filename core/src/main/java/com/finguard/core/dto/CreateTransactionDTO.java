package com.finguard.core.dto;

import com.finguard.core.entities.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionDTO {
    
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private Long sourceAccountId;
    private Long destinationAccountId;
}