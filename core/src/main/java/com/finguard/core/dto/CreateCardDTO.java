package com.finguard.core.dto;

import com.finguard.core.entities.CardType;
import lombok.Data;

@Data
public class CreateCardDTO {
    private Long accountId;
    private CardType type;
}