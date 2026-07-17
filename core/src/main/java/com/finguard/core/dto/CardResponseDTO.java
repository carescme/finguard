package com.finguard.core.dto;

import com.finguard.core.entities.CardStatus;
import com.finguard.core.entities.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDTO {
    private Long id;
    private String maskedCardNumber;
    private String cardholderName;
    private LocalDate expirationDate;
    private CardType type;
    private CardStatus status;
    private Long accountId;
    private String accountName;
}