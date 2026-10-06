package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;

public record ComercioResponse(
        Long id,
        String nombre,
        CategoriaComercio categoria,
        String direccion,
        Boolean abierto
) {
    public static ComercioResponse from(Comercio comercio) {
        return new ComercioResponse(
                comercio.getId(),
                comercio.getNombre(),
                comercio.getCategoria(),
                comercio.getDireccion(),
                comercio.getAbierto());
    }
}