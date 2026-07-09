package com.finguard.core.services;

import com.finguard.core.entities.Account;

public interface AccountService {
    Account crearCuenta(String name, String userEmail);
}