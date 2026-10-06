package com.andersonmorales.pedidosya.util;

import java.math.BigDecimal;

/**
 * Constantes transversales de la aplicación PedidosYa.
 */
public final class AppConstants {

    private AppConstants() {
        // Clase de constantes, no instanciable
    }

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_REPARTIDOR = "REPARTIDOR";
    public static final String ROL_CLIENTE = "CLIENTE";

    public static final BigDecimal COSTO_ENVIO_PREDETERMINADO = new BigDecimal("15.00");
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
}

