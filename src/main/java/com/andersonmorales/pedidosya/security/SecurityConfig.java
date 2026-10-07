package com.andersonmorales.pedidosya.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security con arquitectura Stateless basada en JWT.
 * Define la jerarquía de roles (ADMIN, REPARTIDOR, CLIENTE) y reglas de autorización HTTP.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comercios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comercios/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comercios/{comercioId}/productos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                        // 2. Gestión administrativa de comercios y productos
                        .requestMatchers(HttpMethod.POST, "/api/comercios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/comercios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/comercios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/comercios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                        // 3. Endpoints de pedidos para Repartidores
                        .requestMatchers("/api/pedidos/disponibles").hasAnyRole("REPARTIDOR", "ADMIN")
                        .requestMatchers("/api/pedidos/*/tomar").hasAnyRole("REPARTIDOR", "ADMIN")
                        .requestMatchers("/api/pedidos/mis-entregas").hasAnyRole("REPARTIDOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").hasAnyRole("REPARTIDOR", "ADMIN")

                        // 4. Endpoints de pedidos para Clientes
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/api/pedidos/mis-pedidos").hasAnyRole("CLIENTE", "ADMIN")

                        // 5. Panel administrativo de pedidos
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").hasRole("ADMIN")

                        // 6. Gestión de usuarios
                        .requestMatchers("/api/usuarios/perfil").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // 7. Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proveedor de codificación de contraseñas de usuarios mediante el algoritmo BCrypt.
     * Configurado con factor de costo 12 (4096 iteraciones de hashing) cumpliendo las
     * recomendaciones internacionales de ciberseguridad OWASP para máxima resistencia a ataques de fuerza bruta.
     *
     * @return Instancia de {@link BCryptPasswordEncoder} con fuerza 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

