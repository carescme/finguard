package com.finguard.core.controllers;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.dto.TransactionMinResponseDTO;
import com.finguard.core.dto.TransactionResponseDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.entities.Transaction;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.security.SecurityUtils;
import com.finguard.core.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    public TransactionController(TransactionService transactionService, AccountRepository accountRepository, SecurityUtils securityUtils) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<?> procesarMovimiento(@RequestBody CreateTransactionDTO request) {
        String emailUsuarioLogueado = securityUtils.getAuthenticatedUserEmail();

        if (request.getSourceAccountId() != null) {
            Account cuentaOrigen = accountRepository.findById(request.getSourceAccountId()).orElse(null);
            
            if (cuentaOrigen != null && !cuentaOrigen.getUser().getEmail().equals(emailUsuarioLogueado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No puedes mover dinero de una cuenta que no te pertenece");
            }
        }

        try {
            Transaction resultado = transactionService.ejecutarTransaccion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Transacción procesada con éxito. ID: " + resultado.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.valueOf(422)).body(e.getMessage());
        }
    }

    @GetMapping("/my-account/{accountId}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long accountId) {
        String emailUsuarioLogueado = securityUtils.getAuthenticatedUserEmail();

        Account cuenta = accountRepository.findById(accountId).orElse(null);
        if (cuenta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuenta no encontrada");
        }
        
        if (!cuenta.getUser().getEmail().equals(emailUsuarioLogueado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No tienes permiso para ver los movimientos de esta cuenta");
        }

        try {
            List<TransactionMinResponseDTO> historial = transactionService.obtenerHistorialCuenta(accountId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleTransaccion(@PathVariable Long id) {
        String emailUsuarioLogueado = securityUtils.getAuthenticatedUserEmail();

        try {
            TransactionResponseDTO dto = transactionService.obtenerTransaccionPorId(id);

            boolean esPropietario = false;

            if (dto.getSourceAccountId() != null) {
                Account origen = accountRepository.findById(dto.getSourceAccountId()).orElse(null);
                if (origen != null && origen.getUser().getEmail().equals(emailUsuarioLogueado)) {
                    esPropietario = true;
                }
            }

            if (dto.getDestinationAccountId() != null) {
                Account destino = accountRepository.findById(dto.getDestinationAccountId()).orElse(null);
                if (destino != null && destino.getUser().getEmail().equals(emailUsuarioLogueado)) {
                    esPropietario = true;
                }
            }

            if (!esPropietario) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No tienes permiso para ver los detalles de esta transacción");
            }

            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}