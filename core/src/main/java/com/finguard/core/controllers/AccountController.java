package com.finguard.core.controllers;

import com.finguard.core.dto.AccountListDTO;
import com.finguard.core.dto.AccountResponseDTO;
import com.finguard.core.dto.CreateAccountDTO;
import com.finguard.core.entities.Account;
import com.finguard.core.security.SecurityUtils;
import com.finguard.core.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    public AccountController(AccountService accountService, SecurityUtils securityUtils) {
        this.accountService = accountService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<?> crearCuenta(@RequestBody CreateAccountDTO request) {
        String email = securityUtils.getAuthenticatedUserEmail();
        
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

    @GetMapping
    public ResponseEntity<List<AccountListDTO>> listarMisCuentas() {
        String email = securityUtils.getAuthenticatedUserEmail();
        List<Account> cuentas = accountService.listarCuentasPorUsuario(email);

        List<AccountListDTO> response = cuentas.stream()
                .map(cuenta -> AccountListDTO.builder()
                        .id(cuenta.getId())
                        .name(cuenta.getName())
                        .balance(cuenta.getBalance())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleCuenta(@PathVariable Long id) {
        String email = securityUtils.getAuthenticatedUserEmail();

        try {
            Account cuenta = accountService.obtenerCuentaPorIdYUsuario(id, email);
            
            AccountResponseDTO response = AccountResponseDTO.builder()
                    .id(cuenta.getId())
                    .name(cuenta.getName())
                    .balance(cuenta.getBalance())
                    .userId(cuenta.getUser().getId())
                    .createdAt(cuenta.getCreatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}