package com.finguard.core.controllers;

import com.finguard.core.dto.LoginRequestDTO;
import com.finguard.core.dto.RegisterRequestDTO;
import com.finguard.core.dto.UserResponseDTO;
import com.finguard.core.security.JwtUtils;
import com.finguard.core.entities.User;
import com.finguard.core.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
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

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequestDTO request) {
        User usuarioAutenticado = userService.loginUsuario(request.getEmail(), request.getPassword());

        String tokenGenerado = jwtUtils.generateToken(usuarioAutenticado.getEmail());

        UserResponseDTO response = new UserResponseDTO();
        response.setId(usuarioAutenticado.getId());
        response.setEmail(usuarioAutenticado.getEmail());
        response.setCreatedAt(usuarioAutenticado.getCreatedAt());
        response.setToken(tokenGenerado);

        return ResponseEntity.ok(response);
    }
}