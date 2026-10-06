package com.andersonmorales.pedidosya.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en la base de datos.
 * Extiende ResourceNotFoudException para asegurar retrocompatibilidad con el código base.
 */
public class ResourceNotFoundException extends ResourceNotFoudException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso, id);
    }
}
