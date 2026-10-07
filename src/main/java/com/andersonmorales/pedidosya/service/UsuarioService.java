package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.UsuarioResponse;
import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar los usuarios (clientes, repartidores, administradores).
 * Garantiza que las contraseñas se almacenen y actualicen protegidas mediante el algoritmo BCrypt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
     * Guarda o actualiza un usuario en la base de datos, garantizando que su contraseña
     * quede debidamente encriptada mediante BCrypt si no se encuentra ya cifrada.
     *
     * @param usuario Entidad {@link Usuario}.
     * @return Entidad persistida.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !isBCryptHash(usuario.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            log.debug("Contraseña encriptada exitosamente con BCrypt para el usuario: {}", usuario.getEmail());
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza la contraseña de un usuario encriptándola con BCrypt.
     *
     * @param usuarioId ID del usuario.
     * @param nuevaPassword Contraseña en texto plano a encriptar.
     */
    @Transactional
    public void cambiarPassword(Long usuarioId, String nuevaPassword) {
        Usuario usuario = obtenerPorId(usuarioId);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada y cifrada con BCrypt para usuario ID: {}", usuarioId);
    }

    /**
     * Valida si una contraseña en texto plano coincide con el hash BCrypt de un usuario.
     *
     * @param rawPassword Contraseña en texto plano a verificar.
     * @param encodedPassword Hash BCrypt almacenado.
     * @return true si coincide, false en caso contrario.
     */
    public boolean verificarPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Verifica si una cadena de texto ya posee el formato de un hash generado por BCrypt ($2a$, $2b$ o $2y$).
     *
     * @param password Contraseña o hash a verificar.
     * @return true si es un hash BCrypt válido, false en caso contrario.
     */
    public boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}