package com.finguard.core.services;

import java.util.List;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.dto.TransactionResponseDTO;
import com.finguard.core.entities.Transaction;

public interface TransactionService {
    Transaction ejecutarTransaccion(CreateTransactionDTO dto);

    List<TransactionResponseDTO> obtenerHistorialCuenta(Long accountId);
}