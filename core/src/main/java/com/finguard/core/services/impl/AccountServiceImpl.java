package com.finguard.core.services.impl;

import com.finguard.core.entities.Account;
import com.finguard.core.entities.User;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.repositories.UserRepository;
import com.finguard.core.services.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Account crearCuenta(String name, String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userEmail));

        Account nuevaCuenta = new Account();
        nuevaCuenta.setName(name);
        nuevaCuenta.setBalance(BigDecimal.ZERO);
        nuevaCuenta.setUser(usuario);

        return accountRepository.save(nuevaCuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> listarCuentasPorUsuario(String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return accountRepository.findByUserId(usuario.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Account obtenerCuentaPorIdYUsuario(Long accountId, String userEmail) {
        Account cuenta = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + accountId));

        if (!cuenta.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Acceso denegado: Esta cuenta no te pertenece");
        }

        return cuenta;
    }
}