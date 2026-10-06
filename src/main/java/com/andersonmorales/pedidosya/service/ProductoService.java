package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.ProductoRequest;
import com.andersonmorales.pedidosya.dto.ProductoResponse;
import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.entity.Producto;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar los productos de cada comercio del sistema.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ComercioService comercioService;

    /**
     * Lista todos los productos pertenecientes a un comercio determinado.
     *
     * @param comercioId ID del comercio.
     * @return Lista de {@link ProductoResponse}.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorComercio(Long comercioId) {
        comercioService.obtenerPorId(comercioId);
        return productoRepository.findByComercioId(comercioId).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    /**
     * Obtiene una entidad {@link Producto} a partir de su ID.
     *
     * @param id ID del producto.
     * @return La entidad {@link Producto}.
     * @throws ResourceNotFoundException si el producto no existe.
     */
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    /**
     * Obtiene el DTO de respuesta para un producto según su ID.
     *
     * @param id ID del producto.
     * @return {@link ProductoResponse}.
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtenerResponsePorId(Long id) {
        return ProductoResponse.from(obtenerPorId(id));
    }

    /**
     * Registra un nuevo producto para un comercio especificado.
     *
     * @param comercioId ID del comercio al que pertenece el producto.
     * @param request Datos del producto.
     * @return DTO {@link ProductoResponse} creado.
     */
    @Transactional
    public ProductoResponse crear(Long comercioId, ProductoRequest request) {
        Comercio comercio = comercioService.obtenerPorId(comercioId);
        Producto producto = Producto.builder()
                .comercio(comercio)
                .nombre(request.nombre().trim())
                .precio(request.precio())
                .stock(request.stock())
                .disponible(request.disponible() == null || request.disponible())
                .build();
        return ProductoResponse.from(productoRepository.save(producto));
    }

    /**
     * Actualiza la información de un producto existente.
     *
     * @param id ID del producto.
     * @param request Nuevos datos del producto.
     * @return DTO {@link ProductoResponse} actualizado.
     */
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        if (request.disponible() != null) {
            producto.setDisponible(request.disponible());
        }
        return ProductoResponse.from(productoRepository.save(producto));
    }

    /**
     * Cambia la disponibilidad de un producto (habilitado/deshabilitado para venta).
     *
     * @param id ID del producto.
     * @param disponible Nuevo estado de disponibilidad.
     * @return DTO {@link ProductoResponse} actualizado.
     */
    @Transactional
    public ProductoResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Producto producto = obtenerPorId(id);
        producto.setDisponible(disponible);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar.
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        productoRepository.delete(producto);
    }
}