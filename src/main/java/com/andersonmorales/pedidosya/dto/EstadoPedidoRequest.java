package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoPedidoRequest(
        @NotNull EstadoPedido estado
) {
}