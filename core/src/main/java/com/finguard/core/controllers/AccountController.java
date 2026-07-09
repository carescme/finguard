package com.finguard.core.controllers;

import com.finguard.core.dto.CreateAccountDTO;
import com.finguard.core.dto.AccountResponseDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.security.JwtUtils;
import com.finguard.core.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    public AccountController(AccountService accountService, JwtUtils jwtUtils) {
        this.accountService = accountService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    public ResponseEntity<?> crearCuenta(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody CreateAccountDTO request) {

        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ausente o formato inválido");
        }

        String token = tokenHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String email = jwtUtils.getEmailFromToken(token);
        
        Account cuentaCreada = accountService.crearCuenta(request.getName(), email);

        AccountResponseDTO response = AccountResponseDTO.builder()
                .id(cuentaCreada.getId())
                .name(cuentaCreada.getName())
                .balance(cuentaCreada.getBalance())
                .userId(cuentaCreada.getUser().getId())
                .createdAt(cuentaCreada.getCreatedAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}