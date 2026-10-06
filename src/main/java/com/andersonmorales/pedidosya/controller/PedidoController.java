package com.andersonmorales.pedidosya.controller;

import com.andersonmorales.pedidosya.dto.EstadoPedidoRequest;
import com.andersonmorales.pedidosya.dto.PedidoRequest;
import com.andersonmorales.pedidosya.dto.PedidoResponse;
import com.andersonmorales.pedidosya.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el ciclo de vida de los pedidos.
 * Proporciona operaciones para creación de órdenes, asignación a repartidores,
 * seguimiento y actualización de estados del delivery.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Crea un nuevo pedido para el cliente autenticado.
     *
     * @param request Datos de los productos y cantidades a pedir.
     * @param principal Identidad del usuario autenticado vía Spring Security.
     * @return Pedido generado con cabecera Location y estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            Principal principal
    ) {
        String email = resolverEmailUsuario(principal);
        PedidoResponse creado = pedidoService.crearPedido(email, request);
        URI ubicacion = URI.create("/api/pedidos/" + creado.id());
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * Obtiene el detalle completo de un pedido por su identificador.
     *
     * @param id ID del pedido a consultar.
     * @return Datos del pedido con estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    /**
     * Lista los pedidos realizados por el cliente autenticado en orden cronológico descendente.
     *
     * @param principal Identidad del cliente autenticado.
     * @return Lista de pedidos del cliente con estado HTTP 200 (OK).
     */
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarMisPedidos(Principal principal) {
        String email = resolverEmailUsuario(principal);
        return ResponseEntity.ok(pedidoService.listarMisPedidos(email));
    }

    /**
     * Lista los pedidos disponibles para ser tomados por repartidores (estados PENDIENTE o EN_PREPARACION sin repartidor).
     *
     * @return Lista de pedidos disponibles con estado HTTP 200 (OK).
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<PedidoResponse>> listarDisponibles() {
        return ResponseEntity.ok(pedidoService.listarDisponiblesParaRepartidor());
    }

    /**
     * Permite a un repartidor autenticado tomar un pedido disponible para realizar la entrega.
     *
     * @param id ID del pedido a tomar.
     * @param principal Identidad del repartidor autenticado.
     * @return Pedido asignado con estado HTTP 200 (OK).
     */
    @PostMapping("/{id}/tomar")
    public ResponseEntity<PedidoResponse> tomarPedido(
            @PathVariable Long id,
            Principal principal
    ) {
        String email = resolverEmailUsuario(principal);
        return ResponseEntity.ok(pedidoService.tomarPedido(id, email));
    }

    /**
     * Actualiza el estado de un pedido (por ejemplo a EN_CAMINO, ENTREGADO o CANCELADO).
     *
     * @param id ID del pedido.
     * @param request DTO con el nuevo estado a aplicar.
     * @return Pedido con el estado actualizado con estado HTTP 200 (OK).
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoPedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, request.estado()));
    }

    /**
     * Lista todos los pedidos registrados en el sistema (panel administrativo).
     *
     * @return Lista de todos los pedidos con estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    /**
     * Lista los pedidos asignados al repartidor autenticado.
     *
     * @param principal Identidad del repartidor autenticado.
     * @return Lista de pedidos en entrega o entregados por el repartidor con estado HTTP 200 (OK).
     */
    @GetMapping("/mis-entregas")
    public ResponseEntity<List<PedidoResponse>> listarMisEntregas(Principal principal) {
        String email = resolverEmailUsuario(principal);
        return ResponseEntity.ok(pedidoService.listarPorRepartidor(email));
    }

    private String resolverEmailUsuario(Principal principal) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        return "cliente@pedidosya.com";
    }
}
