package com.andersonmorales.pedidosya.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en el sistema.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }
}

