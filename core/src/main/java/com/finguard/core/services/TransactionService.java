package com.finguard.core.services;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.entities.Transaction;

public interface TransactionService {
    Transaction ejecutarTransaccion(CreateTransactionDTO dto);
}