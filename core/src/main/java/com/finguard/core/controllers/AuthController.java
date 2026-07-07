package com.finguard.core.controllers;

import com.finguard.core.dto.RegisterRequestDTO;
import com.finguard.core.dto.UserResponseDTO;
import com.finguard.core.entities.User;
import com.finguard.core.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registrar(@RequestBody RegisterRequestDTO request) {
        User usuario = new User();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());

        User usuarioGuardado = userService.registrarUsuario(usuario);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(usuarioGuardado.getId());
        response.setEmail(usuarioGuardado.getEmail());
        response.setCreatedAt(usuarioGuardado.getCreatedAt());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}