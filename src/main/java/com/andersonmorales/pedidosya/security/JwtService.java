package com.andersonmorales.pedidosya.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * Servicio central para la generación, validación y extracción de información de tokens JWT.
 * Implementa seguridad avanzada integrando el algoritmo BCrypt para la protección y derivación
 * de la clave secreta de firma HMAC-SHA256, garantizando un esquema criptográfico robusto y profesional.
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Clave secreta para la firma de tokens JWT cifrada mediante BCrypt (Rounds: 12).
     */
    @Value("${jwt.secret:$2a$12$e8kqX9J1Z1qK8Q7b1m4o5u0WcvOba81EeXqE9EUB7bdTi8S2gzcj6}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long jwtExpiration;

    /**
     * Clave criptográfica binaria generada a partir del secreto protegido con BCrypt.
     */
    private SecretKey cachedSignInKey;

    /**
     * Sal determinística utilizada cuando se suministra una clave semilla en texto plano,
     * garantizando derivación reproducible y consistente con BCrypt entre reinicios de la aplicación.
     */
    private static final String DETERMINISTIC_BCRYPT_SALT = "$2a$12$e8kqX9J1Z1qK8Q7b1m4o5u";

    @PostConstruct
    public void init() {
        this.cachedSignInKey = buildSignInKey();
        log.info("JwtService inicializado exitosamente: Clave secreta validada y protegida mediante BCrypt (Rounds: 12).");
    }

    /**
     * Extrae el email / username del sujeto del token.
     *
     * @param token Token JWT.
     * @return Correo electrónico del usuario.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token utilizando una función resolutora.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un UserDetails sin claims adicionales.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT incluyendo claims adicionales (roles, nombre, etc.).
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Genera un token JWT a partir del email y claims extras directamente.
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Valida si el token es legítimo y pertenece al usuario.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retorna la clave criptográfica para firma y verificación HMAC-SHA256.
     * Garantiza que la clave provenga de un secreto cifrado/hasheado con BCrypt.
     */
    public SecretKey getSignInKey() {
        if (this.cachedSignInKey == null) {
            this.cachedSignInKey = buildSignInKey();
        }
        return this.cachedSignInKey;
    }

    /**
     * Construye la clave binaria SecretKey a partir del hash BCrypt.
     */
    private SecretKey buildSignInKey() {
        String effectiveSecret = resolveBcryptSecret(this.secretKey);
        byte[] keyBytes = effectiveSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Resuelve y valida que la clave secreta esté cifrada/hasheada mediante BCrypt.
     * Si la clave ya tiene formato de hash BCrypt ($2a$, $2b$ o $2y$), se utiliza directamente.
     * Si se pasa texto plano, se genera determinísticamente su hash BCrypt con factor 12.
     *
     * @param secret Clave secreta a resolver.
     * @return Hash BCrypt válido de 60 caracteres.
     */
    public String resolveBcryptSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La clave secreta para JWT no puede estar vacía.");
        }
        if (isBCryptHash(secret)) {
            return secret;
        }
        return BCrypt.hashpw(secret, DETERMINISTIC_BCRYPT_SALT);
    }

    /**
     * Verifica si una cadena de texto corresponde a un hash generado por BCrypt ($2a$, $2b$ o $2y$).
     *
     * @param text Cadena a verificar.
     * @return true si cumple con el patrón estándar de BCrypt, false de lo contrario.
     */
    public boolean isBCryptHash(String text) {
        return text != null && text.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    /**
     * Genera un nuevo hash BCrypt con sal aleatoria (12 rounds) a partir de una semilla en texto plano.
     *
     * @param rawSecret Clave semilla en texto plano.
     * @return Hash BCrypt con factor de costo 12.
     */
    public String generateBcryptSecret(String rawSecret) {
        return BCrypt.hashpw(rawSecret, BCrypt.gensalt(12));
    }
}
