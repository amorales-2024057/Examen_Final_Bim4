package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComercioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull CategoriaComercio categoria,
        @NotBlank @Size(max = 255) String direccion,
        Boolean abierto
) {
}