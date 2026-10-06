package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.entity.DetallePedido;
import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long id,
        Long productoId,
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    public static DetallePedidoResponse from(DetallePedido detalle) {
        return new DetallePedidoResponse(
                detalle.getId(),
                detalle.getProducto().getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}