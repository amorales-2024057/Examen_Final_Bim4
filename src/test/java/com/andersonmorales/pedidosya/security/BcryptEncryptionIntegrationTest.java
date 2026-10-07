package com.andersonmorales.pedidosya.security;

import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.enums.Rol;
import com.andersonmorales.pedidosya.repository.UsuarioRepository;
import com.andersonmorales.pedidosya.service.UsuarioService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BcryptEncryptionIntegrationTest {

    @Test
    @DisplayName("Debe encriptar contraseñas de usuarios con BCrypt (Rounds: 12) y validar coincidencia")
    void testPasswordEncryptionWithBcrypt() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        String rawPassword = "passwordSegura2026*";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$12$"), "El hash debe iniciar con prefijo $2a$12$ indicando 12 rondas");
        assertEquals(60, encodedPassword.length(), "El hash BCrypt estándar debe tener longitud de 60 caracteres");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("passwordIncorrecta", encodedPassword));
    }

    @Test
    @DisplayName("UsuarioService debe encriptar automáticamente contraseñas en texto plano con BCrypt")
    void testUsuarioServiceAutoEncryptsPassword() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);

        Usuario nuevoUsuario = Usuario.builder()
                .nombre("Juan Perez")
                .email("juan@test.com")
                .direccion("Ciudad")
                .telefono("12345678")
                .password("clavePlana123")
                .rol(Rol.CLIENTE)
                .build();

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(nuevoUsuario);

        assertTrue(usuarioService.isBCryptHash(guardado.getPassword()));
        assertTrue(guardado.getPassword().startsWith("$2a$12$"));
        assertTrue(passwordEncoder.matches("clavePlana123", guardado.getPassword()));

        // Validar que no se vuelve a encriptar si ya es un hash BCrypt
        String hashOriginal = guardado.getPassword();
        Usuario guardadoSegundaVez = usuarioService.guardar(guardado);
        assertEquals(hashOriginal, guardadoSegundaVez.getPassword(), "No debe sobre-encriptar un hash existente");
    }

    @Test
    @DisplayName("JwtService debe resolver la clave secreta protegida con BCrypt y emitir/validar tokens")
    void testJwtServiceWithBcryptSecret() {
        JwtService jwtService = new JwtService();
        String bcryptSecret = "$2a$12$e8kqX9J1Z1qK8Q7b1m4o5u0WcvOba81EeXqE9EUB7bdTi8S2gzcj6";
        ReflectionTestUtils.setField(jwtService, "secretKey", bcryptSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
        jwtService.init();

        assertTrue(jwtService.isBCryptHash(bcryptSecret));

        UserDetails userDetails = new User("admin@pedidosya.com", "admin1234", Collections.emptyList());
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", "ADMIN");

        String token = jwtService.generateToken(claims, userDetails);

        assertNotNull(token);
        assertEquals("admin@pedidosya.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("JwtService debe encriptar determinísticamente con BCrypt un secreto plano")
    void testJwtServiceWithPlainSecretConversion() {
        JwtService jwtService = new JwtService();
        String plainSecret = "SuperSecretKeyPlainSeed2026";
        ReflectionTestUtils.setField(jwtService, "secretKey", plainSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
        jwtService.init();

        String resolvedSecret = jwtService.resolveBcryptSecret(plainSecret);
        assertNotNull(resolvedSecret);
        assertTrue(jwtService.isBCryptHash(resolvedSecret));
        assertTrue(resolvedSecret.startsWith("$2a$12$"));

        UserDetails userDetails = new User("cliente@pedidosya.com", "cliente1234", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("cliente@pedidosya.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}

