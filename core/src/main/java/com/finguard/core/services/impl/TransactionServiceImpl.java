package com.finguard.core.services.impl;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.entities.Transaction;
import com.finguard.core.entities.TransactionType;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.repositories.TransactionRepository;
import com.finguard.core.services.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Transaction ejecutarTransaccion(CreateTransactionDTO dto) {
        
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe de la transacción debe ser mayor que cero");
        }

        Account origen = null;
        Account destino = null;

        if (dto.getSourceAccountId() != null) {
            origen = accountRepository.findById(dto.getSourceAccountId())
                    .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
        }
        if (dto.getDestinationAccountId() != null) {
            destino = accountRepository.findById(dto.getDestinationAccountId())
                    .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));
        }

        switch (dto.getType()) {
            case TRANSFERENCIA:
            case BIZUM:
                validarTransferenciaOBizum(origen, destino);
                descontarSaldo(origen, dto.getAmount());
                ingresarSaldo(destino, dto.getAmount());
                break;

            case PAGO_TARJETA:
            case RETIRADA_CAJERO:
                validarGastoExterno(origen, destino, dto.getType());
                descontarSaldo(origen, dto.getAmount());
                break;

            case INGRESO:
                validarIngresoExterno(origen, destino);
                ingresarSaldo(destino, dto.getAmount());
                break;

            default:
                throw new IllegalArgumentException("Tipo de transacción no soportado");
        }

        Transaction nuevaTransaccion = Transaction.builder()
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .type(dto.getType())
                .sourceAccount(origen)
                .destinationAccount(destino)
                .build();

        return transactionRepository.save(nuevaTransaccion);
    }

    private void validarTransferenciaOBizum(Account origen, Account destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Una transferencia o Bizum requiere obligatoriamente una cuenta de origen y una de destino");
        }
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("No puedes realizar una transferencia a la misma cuenta de origen");
        }
    }

    private void validarGastoExterno(Account origen, Account destino, TransactionType tipo) {
        if (origen == null) {
            throw new IllegalArgumentException("Un " + tipo + " requiere una cuenta de origen de donde descontar los fondos");
        }
        if (destino != null) {
            throw new IllegalArgumentException("Un " + tipo + " es un movimiento externo, no puede tener una cuenta de destino interna");
        }
    }

    private void validarIngresoExterno(Account origen, Account destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Un ingreso requiere una cuenta de destino donde depositar los fondos");
        }
        if (origen != null) {
            throw new IllegalArgumentException("Un ingreso es externo (nómina, efectivo), no puede tener una cuenta de origen interna");
        }
    }

    private void descontarSaldo(Account cuenta, BigDecimal cantidad) {
        if (cuenta.getBalance().compareTo(cantidad) < 0) {
            throw new RuntimeException("Saldo insuficiente en la cuenta '" + cuenta.getName() + "'");
        }
        cuenta.setBalance(cuenta.getBalance().subtract(cantidad));
        accountRepository.save(cuenta);
    }

    private void ingresarSaldo(Account cuenta, BigDecimal cantidad) {
        cuenta.setBalance(cuenta.getBalance().add(cantidad));
        accountRepository.save(cuenta);
    }
}