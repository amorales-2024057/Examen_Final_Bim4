package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.ComercioRequest;
import com.andersonmorales.pedidosya.dto.ComercioResponse;
import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.ComercioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar la lógica de negocio para los comercios (restaurantes, supermercados, farmacias).
 */
@Service
@RequiredArgsConstructor
public class ComercioService {

    private final ComercioRepository comercioRepository;

    /**
     * Lista los comercios que se encuentran abiertos, filtrados opcionalmente por categoría.
     *
     * @param categoria Categoría por la cual filtrar (opcional).
     * @return Lista de DTOs {@link ComercioResponse} de comercios abiertos.
     */
    @Transactional(readOnly = true)
    public List<ComercioResponse> listarActivos(CategoriaComercio categoria) {
        List<Comercio> comercios = categoria == null
                ? comercioRepository.findByAbiertoTrue()
                : comercioRepository.findByAbiertoTrueAndCategoria(categoria);
        return comercios.stream().map(ComercioResponse::from).toList();
    }

    /**
     * Lista todos los comercios registrados en el sistema, sin importar si están abiertos o cerrados.
     *
     * @return Lista de todos los comercios registrados como {@link ComercioResponse}.
     */
    @Transactional(readOnly = true)
    public List<ComercioResponse> listarTodos() {
        return comercioRepository.findAll().stream()
                .map(ComercioResponse::from)
                .toList();
    }

    /**
     * Registra un nuevo comercio en la base de datos.
     *
     * @param request Datos de creación del comercio.
     * @return DTO {@link ComercioResponse} con los datos del comercio creado.
     */
    @Transactional
    public ComercioResponse crear(ComercioRequest request) {
        Comercio comercio = Comercio.builder()
                .nombre(request.nombre().trim())
                .categoria(request.categoria())
                .direccion(request.direccion().trim())
                .abierto(request.abierto() == null || request.abierto())
                .build();
        return ComercioResponse.from(comercioRepository.save(comercio));
    }

    /**
     * Obtiene una entidad {@link Comercio} a partir de su ID.
     *
     * @param id Identificador único del comercio.
     * @return La entidad {@link Comercio}.
     * @throws ResourceNotFoundException si el comercio no existe.
     */
    @Transactional(readOnly = true)
    public Comercio obtenerPorId(Long id) {
        return comercioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comercio", id));
    }

    /**
     * Obtiene la respuesta DTO de un comercio a partir de su ID.
     *
     * @param id Identificador único del comercio.
     * @return DTO {@link ComercioResponse}.
     */
    @Transactional(readOnly = true)
    public ComercioResponse obtenerResponsePorId(Long id) {
        return ComercioResponse.from(obtenerPorId(id));
    }

    /**
     * Actualiza la información de un comercio existente.
     *
     * @param id Identificador del comercio a actualizar.
     * @param request Nuevos datos del comercio.
     * @return DTO {@link ComercioResponse} con la información actualizada.
     */
    @Transactional
    public ComercioResponse actualizar(Long id, ComercioRequest request) {
        Comercio comercio = obtenerPorId(id);
        comercio.setNombre(request.nombre().trim());
        comercio.setCategoria(request.categoria());
        comercio.setDireccion(request.direccion().trim());
        if (request.abierto() != null) {
            comercio.setAbierto(request.abierto());
        }
        return ComercioResponse.from(comercioRepository.save(comercio));
    }

    /**
     * Cambia el estado operativo (abierto/cerrado) de un comercio.
     *
     * @param id Identificador del comercio.
     * @param abierto Nuevo estado de apertura.
     * @return DTO {@link ComercioResponse} actualizado.
     */
    @Transactional
    public ComercioResponse cambiarEstadoAbierto(Long id, boolean abierto) {
        Comercio comercio = obtenerPorId(id);
        comercio.setAbierto(abierto);
        return ComercioResponse.from(comercioRepository.save(comercio));
    }

    /**
     * Elimina un comercio del sistema según su ID.
     *
     * @param id Identificador del comercio a eliminar.
     */
    @Transactional
    public void eliminar(Long id) {
        Comercio comercio = obtenerPorId(id);
        comercioRepository.delete(comercio);
    }
}