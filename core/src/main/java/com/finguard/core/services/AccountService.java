package com.finguard.core.services;

import java.util.List;

import com.finguard.core.entities.Account;

public interface AccountService {
    Account crearCuenta(String name, String userEmail);
    List<Account> listarCuentasPorUsuario(String userEmail);
    Account obtenerCuentaPorIdYUsuario(Long accountId, String userEmail);
}