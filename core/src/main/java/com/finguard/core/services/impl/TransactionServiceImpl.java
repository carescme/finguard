package com.finguard.core.services.impl;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.dto.TransactionMinResponseDTO;
import com.finguard.core.dto.TransactionResponseDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.entities.Transaction;
import com.finguard.core.entities.TransactionType;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.repositories.TransactionRepository;
import com.finguard.core.services.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    }

    private void ingresarSaldo(Account cuenta, BigDecimal cantidad) {
        cuenta.setBalance(cuenta.getBalance().add(cantidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionMinResponseDTO> obtenerHistorialCuenta(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("La cuenta solicitada no existe");
        }
        
        List<Transaction> transacciones = transactionRepository
                .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountId, accountId);
        
        return transacciones.stream()
                .map(t -> convertirAMinDTO(t, accountId))
                .toList();
    }

    private TransactionMinResponseDTO convertirAMinDTO(Transaction t, Long accountId) {
        String sign = "+";
        
        if (t.getSourceAccount() != null && t.getSourceAccount().getId().equals(accountId)) {
            sign = "-";
        }
    
        return TransactionMinResponseDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .type(t.getType())
                .createdAt(t.getCreatedAt())
                .sign(sign)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDTO obtenerTransaccionPorId(Long transactionId) {
        Transaction transaccion = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada con el ID: " + transactionId));
                
        return convertirADTO(transaccion);
    }

    private TransactionResponseDTO convertirADTO(Transaction t) {
        return TransactionResponseDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .type(t.getType())
                .createdAt(t.getCreatedAt())
                .sourceAccountId(t.getSourceAccount() != null ? t.getSourceAccount().getId() : null)
                .sourceAccountName(t.getSourceAccount() != null ? t.getSourceAccount().getName() : null)
                .destinationAccountId(t.getDestinationAccount() != null ? t.getDestinationAccount().getId() : null)
                .destinationAccountName(t.getDestinationAccount() != null ? t.getDestinationAccount().getName() : null)
                .build();
    }
}