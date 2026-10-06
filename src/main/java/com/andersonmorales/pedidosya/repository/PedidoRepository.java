// PedidoRepository.java
package com.andersonmorales.pedidosya.repository;

import com.andersonmorales.pedidosya.entity.Pedido;
import com.andersonmorales.pedidosya.enums.EstadoPedido;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"cliente", "repartidor", "detalles", "detalles.producto"})
    Optional<Pedido> findWithDetallesById(Long id);

    @EntityGraph(attributePaths = {"cliente", "repartidor", "detalles", "detalles.producto"})
    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "repartidor", "detalles", "detalles.producto"})
    List<Pedido> findByRepartidorIsNullAndEstadoInOrderByFechaPedidoAsc(Collection<EstadoPedido> estados);

    @EntityGraph(attributePaths = {"cliente", "repartidor", "detalles", "detalles.producto"})
    List<Pedido> findByRepartidorIdOrderByFechaPedidoDesc(Long repartidorId);

    @EntityGraph(attributePaths = {"cliente", "repartidor", "detalles", "detalles.producto"})
    List<Pedido> findAllByOrderByFechaPedidoDesc();
}