package com.finguard.core.services;

import com.finguard.core.entities.User;
import java.util.Optional;

public interface UserService {
    User registrarUsuario(User user);
    Optional<User> obtenerPorEmail(String email);
}