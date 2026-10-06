package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.ProductoRequest;
import com.andersonmorales.pedidosya.dto.ProductoResponse;
import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.entity.Producto;
import com.andersonmorales.pedidosya.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ComercioService comercioService;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorComercio(Long comercioId) {
        comercioService.obtenerPorId(comercioId);
        return productoRepository.findByComercioId(comercioId).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional
    public ProductoResponse crear(Long comercioId, ProductoRequest request) {
        Comercio comercio = comercioService.obtenerPorId(comercioId);
        Producto producto = Producto.builder()
                .comercio(comercio)
                .nombre(request.nombre().trim())
                .precio(request.precio())
                .stock(request.stock())
                .disponible(request.disponible() == null || request.disponible())
                .build();
        return ProductoResponse.from(productoRepository.save(producto));
    }
}