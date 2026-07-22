package com.finguard.core.controllers;

import com.finguard.core.dto.CardResponseDTO;
import com.finguard.core.dto.CreateCardDTO;
import com.finguard.core.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    public CardController(CardService cardService, SecurityUtils securityUtils) {
        this.cardService = cardService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @Operation(summary = "Solicitar una nueva tarjeta", description = "Crea una tarjeta física o virtual asociada a una cuenta bancaria del usuario autenticado.")
    public ResponseEntity<?> crearTarjeta(@RequestBody CreateCardDTO dto) {
        String emailUsuario = securityUtils.getAuthenticatedUserEmail();
        
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
    @Operation(summary = "Listar mis tarjetas", description = "Recupera todas las tarjetas asociadas a cualquiera de las cuentas del usuario autenticado.")
    public ResponseEntity<List<CardResponseDTO>> obtenerMisTarjetas() {
        String emailUsuario = securityUtils.getAuthenticatedUserEmail();
        List<CardResponseDTO> tarjetas = cardService.obtenerTarjetasUsuario(emailUsuario);
        return ResponseEntity.ok(tarjetas);
    }

    @PatchMapping("/{cardId}/status")
    @Operation(summary = "Cambiar estado de una tarjeta", description = "Permite bloquear o activar de manera instantánea una tarjeta por motivos de seguridad.")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long cardId, @RequestParam String nuevoEstado) {
        String emailUsuario = securityUtils.getAuthenticatedUserEmail();
        
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