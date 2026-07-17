package com.finguard.core.services;

import com.finguard.core.dto.CardResponseDTO;
import com.finguard.core.dto.CreateCardDTO;
import java.util.List;

public interface CardService {
    CardResponseDTO crearTarjeta(CreateCardDTO dto, String emailUsuario);
    List<CardResponseDTO> obtenerTarjetasUsuario(String emailUsuario);
    CardResponseDTO cambiarEstadoTarjeta(Long cardId, String nuevoEstado, String emailUsuario);
}