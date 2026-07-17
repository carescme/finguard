package com.finguard.core.dto;

import com.finguard.core.entities.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMinResponseDTO {
    private Long id; 
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private LocalDateTime createdAt;
    private String sign;
}