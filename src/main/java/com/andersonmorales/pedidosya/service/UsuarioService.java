package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.UsuarioResponse;
import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar los usuarios (clientes, repartidores, administradores).
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene una entidad {@link Usuario} a partir de su ID.
     *
     * @param id ID del usuario.
     * @return Entidad {@link Usuario}.
     * @throws ResourceNotFoundException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Obtiene el DTO {@link UsuarioResponse} de un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return DTO {@link UsuarioResponse}.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerResponsePorId(Long id) {
        return UsuarioResponse.from(obtenerPorId(id));
    }

    /**
     * Obtiene la entidad {@link Usuario} buscando por su correo electrónico.
     *
     * @param email Correo electrónico del usuario.
     * @return Entidad {@link Usuario}.
     * @throws ResourceNotFoundException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Obtiene la información del perfil de un usuario por su correo electrónico.
     *
     * @param email Correo electrónico del usuario autenticado.
     * @return DTO {@link UsuarioResponse}.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String email) {
        return UsuarioResponse.from(obtenerPorEmail(email));
    }

    /**
     * Verifica si un correo electrónico ya se encuentra registrado.
     *
     * @param email Correo electrónico a verificar.
     * @return true si existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return Lista de {@link UsuarioResponse}.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    /**
     * Guarda o actualiza un usuario en la base de datos.
     *
     * @param usuario Entidad {@link Usuario}.
     * @return Entidad persistida.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}