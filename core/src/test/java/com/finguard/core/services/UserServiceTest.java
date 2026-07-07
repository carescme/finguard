package com.finguard.core.services;

import com.finguard.core.entities.User;
import com.finguard.core.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void deberiaRegistrarUsuarioConContrasenaEncriptada() {
        User usuario = new User();
        usuario.setEmail("servicio@test.com");
        usuario.setPassword("miPasswordSecreto123");

        User usuarioGuardado = userService.registrarUsuario(usuario);

        assertNotNull(usuarioGuardado.getId());
        assertEquals("servicio@test.com", usuarioGuardado.getEmail());
        
        assertNotEquals("miPasswordSecreto123", usuarioGuardado.getPassword());
        
        assertTrue(passwordEncoder.matches("miPasswordSecreto123", usuarioGuardado.getPassword()));
    }

    @Test
    void deberiaLanzarExcepcionSiElEmailYaExiste() {
        User usuario1 = new User();
        usuario1.setEmail("repetido@test.com");
        usuario1.setPassword("password123");
        userService.registrarUsuario(usuario1);

        User usuario2 = new User();
        usuario2.setEmail("repetido@test.com");
        usuario2.setPassword("otraPassword");

        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            userService.registrarUsuario(usuario2);
        });

        assertEquals("El email ya está registrado", excepcion.getMessage());
    }
}