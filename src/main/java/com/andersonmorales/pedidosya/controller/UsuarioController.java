package com.andersonmorales.pedidosya.controller;

import com.andersonmorales.pedidosya.dto.UsuarioResponse;
import com.andersonmorales.pedidosya.service.UsuarioService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la consulta de información de usuarios y perfiles.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene los datos del perfil del usuario actualmente autenticado.
     *
     * @param principal Identidad del usuario autenticado.
     * @return DTO {@link UsuarioResponse} con los datos del perfil.
     */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(Principal principal) {
        String email = principal != null ? principal.getName() : "cliente@pedidosya.com";
        return ResponseEntity.ok(usuarioService.obtenerPerfil(email));
    }

    /**
     * Lista todos los usuarios registrados en la plataforma (requiere rol ADMIN).
     *
     * @return Lista completa de usuarios con estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Obtiene el detalle de un usuario por su identificador único.
     *
     * @param id ID del usuario a consultar.
     * @return DTO {@link UsuarioResponse} con estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerResponsePorId(id));
    }
}
