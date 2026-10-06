package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.ComercioRequest;
import com.andersonmorales.pedidosya.dto.ComercioResponse;
import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import com.andersonmorales.pedidosya.exception.ResourceNotFoudException;
import com.andersonmorales.pedidosya.repository.ComercioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComercioService {

    private final ComercioRepository comercioRepository;

    @Transactional(readOnly = true)
    public List<ComercioResponse> listarActivos(CategoriaComercio categoria) {
        List<Comercio> comercios = categoria == null
                ? comercioRepository.findByAbiertoTrue()
                : comercioRepository.findByAbiertoTrueAndCategoria(categoria);
        return comercios.stream().map(ComercioResponse::from).toList();
    }

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

    @Transactional(readOnly = true)
    public Comercio obtenerPorId(Long id) {
        return comercioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoudException("Comercio", id));
    }
}