package com.andersonmorales.pedidosya.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura estándar para respuestas de error devueltas por la API REST.
 *
 * @param timestamp Fecha y hora en que ocurrió el error.
 * @param status Código de estado HTTP (ej. 400, 404, 500).
 * @param error Nombre descriptivo del error HTTP.
 * @param message Mensaje legible para el usuario o cliente de la API.
 * @param path Ruta del endpoint donde se originó el error.
 * @param validationErrors Mapa de errores de validación de campos (opcional).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}

