// ComercioRepository.java
package com.andersonmorales.pedidosya.repository;

import com.andersonmorales.pedidosya.entity.Comercio;
import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Long> {
    List<Comercio> findByAbiertoTrue();
    List<Comercio> findByAbiertoTrueAndCategoria(CategoriaComercio categoria);
}