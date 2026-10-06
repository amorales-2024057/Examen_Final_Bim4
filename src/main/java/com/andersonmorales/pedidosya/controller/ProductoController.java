package com.andersonmorales.pedidosya.controller;

import com.andersonmorales.pedidosya.dto.ProductoRequest;
import com.andersonmorales.pedidosya.dto.ProductoResponse;
import com.andersonmorales.pedidosya.service.ProductoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de productos dentro de los comercios.
 * Gestiona catálogos, disponibilidad, precios e inventario.
 */
@RestController
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista todos los productos asociados a un comercio específico.
     *
     * @param comercioId ID del comercio al cual pertenecen los productos.
     * @return Lista de productos del comercio con estado HTTP 200 (OK).
     */
    @GetMapping("/api/comercios/{comercioId}/productos")
    public ResponseEntity<List<ProductoResponse>> listarPorComercio(@PathVariable Long comercioId) {
        List<ProductoResponse> productos = productoService.listarPorComercio(comercioId);
        return ResponseEntity.ok(productos);
    }

    /**
     * Registra un nuevo producto para un comercio especificado.
     *
     * @param comercioId ID del comercio al cual se agregará el producto.
     * @param request Datos del nuevo producto.
     * @return Producto creado con cabecera Location y estado HTTP 201 (Created).
     */
    @PostMapping("/api/comercios/{comercioId}/productos")
    public ResponseEntity<ProductoResponse> crear(
            @PathVariable Long comercioId,
            @Valid @RequestBody ProductoRequest request
    ) {
        ProductoResponse creado = productoService.crear(comercioId, request);
        URI ubicacion = URI.create("/api/productos/" + creado.id());
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * Obtiene el detalle de un producto por su identificador único.
     *
     * @param id ID del producto a consultar.
     * @return Producto encontrado con estado HTTP 200 (OK).
     */
    @GetMapping("/api/productos/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerResponsePorId(id));
    }

    /**
     * Actualiza la información de un producto existente.
     *
     * @param id ID del producto a modificar.
     * @param request Nuevos datos del producto.
     * @return Producto actualizado con estado HTTP 200 (OK).
     */
    @PutMapping("/api/productos/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    /**
     * Modifica la disponibilidad de un producto (habilitar o deshabilitar para compra).
     *
     * @param id ID del producto.
     * @param disponible Estado de disponibilidad (true = disponible, false = deshabilitado).
     * @return Producto con disponibilidad actualizada con estado HTTP 200 (OK).
     */
    @PatchMapping("/api/productos/{id}/disponibilidad")
    public ResponseEntity<ProductoResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible
    ) {
        return ResponseEntity.ok(productoService.cambiarDisponibilidad(id, disponible));
    }

    /**
     * Elimina un producto por su identificador único.
     *
     * @param id ID del producto a eliminar.
     * @return Respuesta vacía con estado HTTP 204 (No Content).
     */
    @DeleteMapping("/api/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
