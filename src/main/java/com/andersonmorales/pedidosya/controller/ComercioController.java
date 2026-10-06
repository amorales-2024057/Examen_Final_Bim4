package com.andersonmorales.pedidosya.controller;

import com.andersonmorales.pedidosya.dto.ComercioRequest;
import com.andersonmorales.pedidosya.dto.ComercioResponse;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import com.andersonmorales.pedidosya.service.ComercioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de comercios (restaurantes, supermercados, farmacias).
 * Proporciona endpoints para consulta pública y operaciones administrativas de gestión.
 */
@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final ComercioService comercioService;

    /**
     * Lista los comercios actualmente abiertos, con filtro opcional por categoría.
     *
     * @param categoria Categoría de comercio opcional (RESTAURANTE, SUPERMERCADO, FARMACIA).
     * @return Lista de comercios abiertos con estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<ComercioResponse>> listarActivos(
            @RequestParam(required = false) CategoriaComercio categoria
    ) {
        List<ComercioResponse> comercios = comercioService.listarActivos(categoria);
        return ResponseEntity.ok(comercios);
    }

    /**
     * Lista todos los comercios del sistema, tanto abiertos como cerrados (vista administrativa).
     *
     * @return Lista completa de comercios registrados con estado HTTP 200 (OK).
     */
    @GetMapping("/todos")
    public ResponseEntity<List<ComercioResponse>> listarTodos() {
        return ResponseEntity.ok(comercioService.listarTodos());
    }

    /**
     * Obtiene los datos detallados de un comercio por su identificador único.
     *
     * @param id ID del comercio a consultar.
     * @return Comercio encontrado con estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comercioService.obtenerResponsePorId(id));
    }

    /**
     * Registra un nuevo comercio en el sistema.
     *
     * @param request Datos de creación del comercio.
     * @return Comercio creado con cabecera Location y estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<ComercioResponse> crear(@Valid @RequestBody ComercioRequest request) {
        ComercioResponse creado = comercioService.crear(request);
        URI ubicacion = URI.create("/api/comercios/" + creado.id());
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * Actualiza la información de un comercio existente.
     *
     * @param id ID del comercio a modificar.
     * @param request Nuevos datos del comercio.
     * @return Comercio actualizado con estado HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComercioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComercioRequest request
    ) {
        return ResponseEntity.ok(comercioService.actualizar(id, request));
    }

    /**
     * Modifica el estado operativo (abierto/cerrado) de un comercio.
     *
     * @param id ID del comercio.
     * @param abierto Indicador si el comercio está abierto (true) o cerrado (false).
     * @return Comercio con el estado actualizado con estado HTTP 200 (OK).
     */
    @PatchMapping("/{id}/abierto")
    public ResponseEntity<ComercioResponse> cambiarEstadoAbierto(
            @PathVariable Long id,
            @RequestParam boolean abierto
    ) {
        return ResponseEntity.ok(comercioService.cambiarEstadoAbierto(id, abierto));
    }

    /**
     * Elimina un comercio por su ID.
     *
     * @param id ID del comercio a eliminar.
     * @return Respuesta vacía con estado HTTP 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comercioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
