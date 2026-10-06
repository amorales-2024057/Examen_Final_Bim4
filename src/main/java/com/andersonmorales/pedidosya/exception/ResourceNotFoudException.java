package com.andersonmorales.pedidosya.exception;

import java.util.Objects;

public class ResourceNotFoudException extends RuntimeException {
    public ResourceNotFoudException(String message){
        super(message);
    }

    public ResourceNotFoudException(String recurso, Object id){
        super (recurso + " no encontrado con id: "+ id);
    }
}
