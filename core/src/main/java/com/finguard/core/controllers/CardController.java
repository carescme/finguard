package com.finguard.core.controllers;

import com.finguard.core.dto.CardResponseDTO;
import com.finguard.core.dto.CreateCardDTO;
import com.finguard.core.security.JwtUtils;
import com.finguard.core.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Controller", description = "Endpoints para la gestión de tarjetas físicas y virtuales de FinGuard")
public class CardController {

    private final CardService cardService;
    private final JwtUtils jwtUtils;

    public CardController(CardService cardService, JwtUtils jwtUtils) {
        this.cardService = cardService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    @Operation(summary = "Solicitar una nueva tarjeta", description = "Crea una tarjeta física o virtual asociada a una cuenta bancaria del usuario autenticado.")
    public ResponseEntity<?> crearTarjeta(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody CreateCardDTO dto) {
        
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ausente o inválido");
        }

        String token = tokenHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String emailUsuario = jwtUtils.getEmailFromToken(token);
        
        try {
            CardResponseDTO nuevaTarjeta = cardService.crearTarjeta(dto, emailUsuario);
            return new ResponseEntity<>(nuevaTarjeta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Listar mis tarjetas", description = "Recupera todas las tarjetas de crédito, débito o virtuales asociadas a cualquiera de las cuentas del usuario autenticado.")
    public ResponseEntity<?> obtenerMisTarjetas(
            @RequestHeader("Authorization") String tokenHeader) {
        
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ausente o inválido");
        }

        String token = tokenHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String emailUsuario = jwtUtils.getEmailFromToken(token);
        
        List<CardResponseDTO> tarjetas = cardService.obtenerTarjetasUsuario(emailUsuario);
        return ResponseEntity.ok(tarjetas);
    }

    @PatchMapping("/{cardId}/status")
    @Operation(summary = "Cambiar estado de una tarjeta", description = "Permite bloquear o activar de manera instantánea una tarjeta por motivos de seguridad.")
    public ResponseEntity<?> cambiarEstado(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long cardId,
            @RequestParam String nuevoEstado) {
        
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ausente o inválido");
        }

        String token = tokenHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String emailUsuario = jwtUtils.getEmailFromToken(token);
        
        try {
            CardResponseDTO tarjetaActualizada = cardService.cambiarEstadoTarjeta(cardId, nuevoEstado, emailUsuario);
            return ResponseEntity.ok(tarjetaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}