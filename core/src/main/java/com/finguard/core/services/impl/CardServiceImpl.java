package com.finguard.core.services.impl;

import com.finguard.core.dto.CardResponseDTO;
import com.finguard.core.dto.CreateCardDTO;
import com.finguard.core.entities.*;
import com.finguard.core.repositories.AccountRepository;
import com.finguard.core.repositories.CardRepository;
import com.finguard.core.services.CardService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public CardServiceImpl(CardRepository cardRepository, 
                           AccountRepository accountRepository, 
                           PasswordEncoder passwordEncoder) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CardResponseDTO crearTarjeta(CreateCardDTO dto, String emailUsuario) {
        Account cuenta = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta asociada no existe"));

        if (!cuenta.getUser().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para crear una tarjeta en esta cuenta");
        }

        String numeroTarjeta = generarNumeroTarjetaUnico();

        String cvvClaro = generarCvvAleatorio();
        String cvvEncriptado = passwordEncoder.encode(cvvClaro);

        LocalDate fechaCaducidad = LocalDate.now().plusYears(5).withDayOfMonth(1);

        Card nuevaTarjeta = Card.builder()
                .cardNumber(numeroTarjeta)
                .cardholderName(cuenta.getUser().getNombre() + " " + cuenta.getUser().getApellidos())
                .expirationDate(fechaCaducidad)
                .cvv(cvvEncriptado)
                .type(dto.getType())
                .status(CardStatus.ACTIVE)
                .account(cuenta)
                .build();

        Card tarjetaGuardada = cardRepository.save(nuevaTarjeta);

        System.out.println("💳 [FinGuard] NUEVA TARJETA CREADA - Número: " + numeroTarjeta + " | CVV Real (No se volverá a mostrar): " + cvvClaro);

        return convertirADTO(tarjetaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDTO> obtenerTarjetasUsuario(String emailUsuario) {
        return cardRepository.findByAccountUserEmail(emailUsuario)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponseDTO cambiarEstadoTarjeta(Long cardId, String nuevoEstado, String emailUsuario) {
        Card tarjeta = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta especificada no existe"));

        if (!tarjeta.getAccount().getUser().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para modificar esta tarjeta");
        }

        try {
            CardStatus estado = CardStatus.valueOf(nuevoEstado.toUpperCase());
            tarjeta.setStatus(estado);
            return convertirADTO(cardRepository.save(tarjeta));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de tarjeta no válido. Debe ser: ACTIVE o BLOCKED");
        }
    }

    private String generarNumeroTarjetaUnico() {
        String numero;
        boolean existe;
        do {
            StringBuilder sb = new StringBuilder("4");
            for (int i = 0; i < 15; i++) {
                sb.append(random.nextInt(10));
            }
            numero = sb.toString();
            existe = cardRepository.findByCardNumber(numero).isPresent();
        } while (existe);
        
        return numero;
    }

    private String generarCvvAleatorio() {
        int cvv = 100 + random.nextInt(900); 
        return String.valueOf(cvv);
    }

    private String enmascararNumeroTarjeta(String numero) {
        if (numero == null || numero.length() < 16) return "****************";
        return numero.substring(0, 4) + "********" + numero.substring(12);
    }

    private CardResponseDTO convertirADTO(Card card) {
        return CardResponseDTO.builder()
                .id(card.getId())
                .maskedCardNumber(enmascararNumeroTarjeta(card.getCardNumber()))
                .cardholderName(card.getCardholderName())
                .expirationDate(card.getExpirationDate())
                .type(card.getType())
                .status(card.getStatus())
                .accountId(card.getAccount().getId())
                .accountName(card.getAccount().getName())
                .build();
    }
}