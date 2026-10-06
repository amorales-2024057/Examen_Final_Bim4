package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.enums.Rol;

public record UsuarioResponse(
        Long id,
        String nombre,
        String direccion,
        String telefono,
        String email,
        Rol rol
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getEmail(),
                usuario.getRol());
    }
}