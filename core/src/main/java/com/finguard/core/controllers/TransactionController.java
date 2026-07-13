package com.finguard.core.controllers;

import com.finguard.core.dto.CreateTransactionDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.entities.Transaction;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.security.JwtUtils;
import com.finguard.core.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;

    public TransactionController(TransactionService transactionService, AccountRepository accountRepository, JwtUtils jwtUtils) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    public ResponseEntity<?> procesarMovimiento(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody CreateTransactionDTO request) {

        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ausente o inválido");
        }

        String token = tokenHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String emailUsuarioLogueado = jwtUtils.getEmailFromToken(token);

        if (request.getSourceAccountId() != null) {
            Account cuentaOrigen = accountRepository.findById(request.getSourceAccountId())
                    .orElse(null);
            
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
}