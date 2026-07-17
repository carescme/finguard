package com.finguard.core.services.impl;

import com.finguard.core.entities.User;
import com.finguard.core.repositories.UserRepository;
import com.finguard.core.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registrarUsuario(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        String contrasenaEncriptada = passwordEncoder.encode(user.getPassword());
        user.setPassword(contrasenaEncriptada);

        if (user.getNombre() == null || user.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio para registrar un usuario");
        }
        if (user.getApellidos() == null || user.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios para registrar un usuario");
        }
        
        return userRepository.save(user);
    }

    @Override
    public Optional<User> obtenerPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User loginUsuario(String email, String password) {
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return usuario;
    }
}