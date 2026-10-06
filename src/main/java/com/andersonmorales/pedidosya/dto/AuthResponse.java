package com.andersonmorales.pedidosya.dto;

import com.andersonmorales.pedidosya.enums.Rol;

public record AuthResponse(
        String token,
        String tipo,
        String email,
        Rol rol
) {
    public static AuthResponse bearer(String token, String email, Rol rol) {
        return new AuthResponse(token, "Bearer", email, rol);
    }
}