package com.andersonmorales.pedidosya.config;

import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.entity.Producto;
import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import com.andersonmorales.pedidosya.enums.Rol;
import com.andersonmorales.pedidosya.repository.ComercioRepository;
import com.andersonmorales.pedidosya.repository.ProductoRepository;
import com.andersonmorales.pedidosya.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeder de datos iniciales para el entorno de pruebas y evaluación académica.
 * Si la base de datos no contiene usuarios, crea automáticamente usuarios de prueba con cada rol,
 * comercios y productos de catálogo inicial.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Base de datos ya inicializada. Omitiendo DataSeeder.");
            return;
        }

        log.info("Inicializando datos semilla para PedidosYa...");

        // 1. Usuarios de prueba
        Usuario admin = Usuario.builder()
                .nombre("Administrador del Sistema")
                .direccion("Oficinas Centrales PedidosYa")
                .telefono("12345678")
                .email("admin@pedidosya.com")
                .password(passwordEncoder.encode("admin1234"))
                .rol(Rol.ADMIN)
                .build();

        Usuario repartidor = Usuario.builder()
                .nombre("Carlos Repartidor")
                .direccion("Zona 10, Ciudad de Guatemala")
                .telefono("87654321")
                .email("repartidor@pedidosya.com")
                .password(passwordEncoder.encode("repartidor1234"))
                .rol(Rol.REPARTIDOR)
                .build();

        Usuario cliente = Usuario.builder()
                .nombre("Anderson Morales")
                .direccion("Colonia Las Flores, Lote 12")
                .telefono("55443322")
                .email("cliente@pedidosya.com")
                .password(passwordEncoder.encode("cliente1234"))
                .rol(Rol.CLIENTE)
                .build();

        usuarioRepository.saveAll(List.of(admin, repartidor, cliente));

        // 2. Comercios de prueba
        Comercio restaurante = Comercio.builder()
                .nombre("Burger House Central")
                .categoria(CategoriaComercio.RESTAURANTE)
                .direccion("Av. Las Américas 12-40, Zona 13")
                .abierto(true)
                .build();

        Comercio supermercado = Comercio.builder()
                .nombre("SuperExpress Gourmet")
                .categoria(CategoriaComercio.SUPERMERCADO)
                .direccion("Diagonal 6 10-01, Zona 10")
                .abierto(true)
                .build();

        Comercio farmacia = Comercio.builder()
                .nombre("FarmaVida Express")
                .categoria(CategoriaComercio.FARMACIA)
                .direccion("Calzada Roosevelt 22-00, Zona 11")
                .abierto(true)
                .build();

        comercioRepository.saveAll(List.of(restaurante, supermercado, farmacia));

        // 3. Productos para Restaurante
        Producto p1 = Producto.builder()
                .comercio(restaurante)
                .nombre("Hamburguesa Doble Carne con Queso")
                .precio(new BigDecimal("55.00"))
                .stock(50)
                .disponible(true)
                .build();

        Producto p2 = Producto.builder()
                .comercio(restaurante)
                .nombre("Papas Fritas Medianas")
                .precio(new BigDecimal("18.00"))
                .stock(100)
                .disponible(true)
                .build();

        Producto p3 = Producto.builder()
                .comercio(restaurante)
                .nombre("Bebida Gaseosa 500ml")
                .precio(new BigDecimal("12.00"))
                .stock(80)
                .disponible(true)
                .build();

        // 4. Productos para Supermercado
        Producto p4 = Producto.builder()
                .comercio(supermercado)
                .nombre("Leche Deslactosada 1L")
                .precio(new BigDecimal("16.50"))
                .stock(40)
                .disponible(true)
                .build();

        Producto p5 = Producto.builder()
                .comercio(supermercado)
                .nombre("Pan Integral Artesanal")
                .precio(new BigDecimal("22.00"))
                .stock(30)
                .disponible(true)
                .build();

        // 5. Productos para Farmacia
        Producto p6 = Producto.builder()
                .comercio(farmacia)
                .nombre("Paracetamol 500mg (Caja 20 tabletas)")
                .precio(new BigDecimal("25.00"))
                .stock(60)
                .disponible(true)
                .build();

        productoRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        log.info("Datos semilla creados exitosamente:");
        log.info("ADMIN: admin@pedidosya.com / admin1234");
        log.info("REPARTIDOR: repartidor@pedidosya.com / repartidor1234");
        log.info("CLIENTE: cliente@pedidosya.com / cliente1234");
    }
}

