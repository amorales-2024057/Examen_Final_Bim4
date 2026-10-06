package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.entity.Pedido;
import com.andersonmorales.pedidosya.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        UsuarioResponse cliente,
        UsuarioResponse repartidor,
        LocalDateTime fechaPedido,
        BigDecimal costoEnvio,
        BigDecimal montoTotal,
        EstadoPedido estado,
        List<DetallePedidoResponse> detalles
) {
    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                UsuarioResponse.from(pedido.getCliente()),
                pedido.getRepartidor() == null ? null : UsuarioResponse.from(pedido.getRepartidor()),
                pedido.getFechaPedido(),
                pedido.getCostoEnvio(),
                pedido.getMontoTotal(),
                pedido.getEstado(),
                pedido.getDetalles().stream().map(DetallePedidoResponse::from).toList());
    }
}