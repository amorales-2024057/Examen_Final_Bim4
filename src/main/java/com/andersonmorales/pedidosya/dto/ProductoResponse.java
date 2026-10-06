package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.entity.Producto;
import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        Long comercioId,
        String nombre,
        BigDecimal precio,
        Integer stock,
        Boolean disponible
) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getComercio().getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getDisponible());
    }
}