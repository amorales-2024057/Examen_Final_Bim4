package com.andersonmorales.pedidosya.controller;

import com.andersonmorales.pedidosya.dto.AuthResponse;
import com.andersonmorales.pedidosya.dto.LoginRequest;
import com.andersonmorales.pedidosya.dto.RegisterRequest;
import com.andersonmorales.pedidosya.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de autenticación pública (registro e inicio de sesión).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registra una nueva cuenta de cliente en el sistema.
     *
     * @param request Datos de registro del usuario (nombre, dirección, teléfono, email, contraseña).
     * @return DTO {@link AuthResponse} con token JWT generado y estado HTTP 201 (Created).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Inicia sesión con credenciales existentes y genera un token JWT.
     *
     * @param request Credenciales del usuario (email y contraseña).
     * @return DTO {@link AuthResponse} con token JWT y estado HTTP 200 (OK).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

