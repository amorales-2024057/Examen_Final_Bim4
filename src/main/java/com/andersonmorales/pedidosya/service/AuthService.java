package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.AuthResponse;
import com.andersonmorales.pedidosya.dto.LoginRequest;
import com.andersonmorales.pedidosya.dto.RegisterRequest;
import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.enums.Rol;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.UsuarioRepository;
import com.andersonmorales.pedidosya.security.JwtService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de la autenticación, registro de nuevos usuarios y emisión de tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario cliente en el sistema y retorna su token de acceso JWT.
     *
     * @param request Datos del registro del usuario.
     * @return DTO {@link AuthResponse} con el token de autenticación.
     */
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo electrónico " + request.email() + " ya está registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .direccion(request.direccion().trim())
                .telefono(request.telefono().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", guardado.getRol().name());
        claims.put("nombre", guardado.getNombre());
        claims.put("userId", guardado.getId());

        String token = jwtService.generateToken(claims, guardado.getEmail());
        return AuthResponse.bearer(token, guardado.getEmail(), guardado.getRol());
    }

    /**
     * Autentica las credenciales de un usuario existente y emite un token de acceso JWT.
     *
     * @param request Credenciales de acceso (email y contraseña).
     * @return DTO {@link AuthResponse} con el token de autenticación.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.email()));

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("nombre", usuario.getNombre());
        claims.put("userId", usuario.getId());

        String token = jwtService.generateToken(claims, usuario.getEmail());
        return AuthResponse.bearer(token, usuario.getEmail(), usuario.getRol());
    }
}

