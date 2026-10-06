package com.andersonmorales.pedidosya.service;

import com.andersonmorales.pedidosya.dto.ItemPedidoRequest;
import com.andersonmorales.pedidosya.dto.PedidoRequest;
import com.andersonmorales.pedidosya.dto.PedidoResponse;
import com.andersonmorales.pedidosya.entity.DetallePedido;
import com.andersonmorales.pedidosya.entity.Pedido;
import com.andersonmorales.pedidosya.entity.Producto;
import com.andersonmorales.pedidosya.entity.Usuario;
import com.andersonmorales.pedidosya.enums.EstadoPedido;
import com.andersonmorales.pedidosya.enums.Rol;
import com.andersonmorales.pedidosya.exception.InsufficientStockException;
import com.andersonmorales.pedidosya.exception.InvalidStatusException;
import com.andersonmorales.pedidosya.exception.ResourceNotFoundException;
import com.andersonmorales.pedidosya.repository.PedidoRepository;
import com.andersonmorales.pedidosya.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio principal encargado de la gestión de pedidos y flujo de delivery.
 * Implementa concurrencia segura mediante bloqueo pesimista, control de stock y máquina de estados.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    public static final BigDecimal COSTO_ENVIO_FIJO = new BigDecimal("15.00");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;

    /**
     * Crea un nuevo pedido para un cliente autenticado.
     * Bloquea pesimistamente los productos involucrados para evitar condiciones de carrera en el stock.
     *
     * @param emailCliente Correo electrónico del cliente que realiza la orden.
     * @param request Datos del pedido con la lista de productos y cantidades.
     * @return DTO {@link PedidoResponse} con el pedido generado y sus detalles.
     */
    @Transactional
    public PedidoResponse crearPedido(String emailCliente, PedidoRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
        }

        Usuario cliente = usuarioService.obtenerPorEmail(emailCliente);

        // Agrupar cantidades por producto si se repiten en el request
        Map<Long, Integer> cantidadesPorProducto = new LinkedHashMap<>();
        for (ItemPedidoRequest item : request.items()) {
            cantidadesPorProducto.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        // Bloqueo pesimista de escritura para los productos seleccionados
        List<Producto> productos = productoRepository.findAllByIdForUpdate(cantidadesPorProducto.keySet());
        Map<Long, Producto> productoMap = productos.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        // Validar que todos los productos existen
        for (Long productoId : cantidadesPorProducto.keySet()) {
            if (!productoMap.containsKey(productoId)) {
                throw new ResourceNotFoundException("Producto", productoId);
            }
        }

        // Validar comercio abierto, disponibilidad y stock
        Long primerComercioId = productos.get(0).getComercio().getId();
        for (Producto prod : productos) {
            // Verificar que pertenezcan al mismo comercio
            if (!prod.getComercio().getId().equals(primerComercioId)) {
                throw new IllegalArgumentException("Todos los productos del pedido deben pertenecer al mismo comercio.");
            }
            // Verificar si el comercio está abierto
            if (!Boolean.TRUE.equals(prod.getComercio().getAbierto())) {
                throw new InvalidStatusException("El comercio " + prod.getComercio().getNombre() + " se encuentra actualmente cerrado.");
            }
            // Verificar disponibilidad del producto
            if (!Boolean.TRUE.equals(prod.getDisponible())) {
                throw new InvalidStatusException("El producto " + prod.getNombre() + " no está disponible para ordenar.");
            }
            // Verificar stock suficiente
            int cantidadSolicitada = cantidadesPorProducto.get(prod.getId());
            if (prod.getStock() < cantidadSolicitada) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto: " + prod.getNombre() +
                        ". Disponible: " + prod.getStock() + ", Solicitado: " + cantidadSolicitada);
            }
        }

        // Construir la cabecera del pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .repartidor(null)
                .fechaPedido(LocalDateTime.now())
                .costoEnvio(COSTO_ENVIO_FIJO)
                .estado(EstadoPedido.PENDIENTE)
                .montoTotal(BigDecimal.ZERO)
                .build();

        // Descontar inventario, calcular subtotales y agregar detalles
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Producto prod = productoMap.get(entry.getKey());
            int cantidad = entry.getValue();

            // Descuento de inventario
            prod.setStock(prod.getStock() - cantidad);

            BigDecimal subtotal = prod.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            subtotalAcumulado = subtotalAcumulado.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .producto(prod)
                    .cantidad(cantidad)
                    .precioUnitario(prod.getPrecio())
                    .subtotal(subtotal)
                    .build();

            pedido.agregarDetalle(detalle);
        }

        // Monto total = suma de subtotales + costo de envío
        pedido.setMontoTotal(subtotalAcumulado.add(COSTO_ENVIO_FIJO));

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return PedidoResponse.from(pedidoGuardado);
    }

    /**
     * Obtiene los detalles completos de un pedido por su identificador.
     *
     * @param id Identificador único del pedido.
     * @return DTO {@link PedidoResponse}.
     * @throws ResourceNotFoundException si el pedido no existe.
     */
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        return PedidoResponse.from(pedido);
    }

    /**
     * Lista todos los pedidos pertenecientes a un cliente dado su correo.
     *
     * @param emailCliente Correo electrónico del cliente.
     * @return Lista de pedidos ordenados por fecha de creación descendente.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMisPedidos(String emailCliente) {
        Usuario cliente = usuarioService.obtenerPorEmail(emailCliente);
        return pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(cliente.getId()).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    /**
     * Lista los pedidos disponibles para ser tomados por cualquier repartidor.
     * Se consideran disponibles aquellos pedidos en estado PENDIENTE o EN_PREPARACION sin repartidor asignado.
     *
     * @return Lista de pedidos en orden cronológico ascendente.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDisponiblesParaRepartidor() {
        return pedidoRepository.findByRepartidorIsNullAndEstadoInOrderByFechaPedidoAsc(
                List.of(EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION)
        ).stream().map(PedidoResponse::from).toList();
    }

    /**
     * Asigna un pedido a un repartidor disponible.
     *
     * @param pedidoId ID del pedido a tomar.
     * @param emailRepartidor Correo del repartidor autenticado.
     * @return DTO {@link PedidoResponse} actualizado.
     */
    @Transactional
    public PedidoResponse tomarPedido(Long pedidoId, String emailRepartidor) {
        Pedido pedido = pedidoRepository.findWithDetallesById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idOrThrow(pedidoId)));

        if (pedido.getRepartidor() != null) {
            throw new InvalidStatusException("El pedido #" + pedidoId + " ya tiene un repartidor asignado.");
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new InvalidStatusException("No es posible tomar un pedido en estado " + pedido.getEstado());
        }

        Usuario repartidor = usuarioService.obtenerPorEmail(emailRepartidor);
        if (repartidor.getRol() != Rol.REPARTIDOR && repartidor.getRol() != Rol.ADMIN) {
            throw new InvalidStatusException("El usuario con email " + emailRepartidor + " no tiene rol de repartidor.");
        }

        pedido.setRepartidor(repartidor);
        // Si estaba pendiente, avanza a preparación/en camino
        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            pedido.setEstado(EstadoPedido.EN_PREPARACION);
        }

        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    /**
     * Actualiza el estado de un pedido según la máquina de estados de PedidosYa.
     * Si el pedido es cancelado, reintegra automáticamente el stock de los productos.
     *
     * @param pedidoId ID del pedido a actualizar.
     * @param nuevoEstado Nuevo estado deseado para el pedido.
     * @return DTO {@link PedidoResponse} actualizado.
     */
    @Transactional
    public PedidoResponse actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findWithDetallesById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idOrThrow(pedidoId)));

        EstadoPedido actual = pedido.getEstado();

        if (actual == EstadoPedido.ENTREGADO) {
            throw new InvalidStatusException("No se puede modificar un pedido que ya fue ENTREGADO.");
        }

        if (actual == EstadoPedido.CANCELADO) {
            throw new InvalidStatusException("No se puede modificar un pedido que ya fue CANCELADO.");
        }

        if (actual == nuevoEstado) {
            return PedidoResponse.from(pedido);
        }

        // Validaciones de transición de estados
        validarTransicion(actual, nuevoEstado);

        // Si se cancela, restaurar el inventario de los productos
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto prod = detalle.getProducto();
                prod.setStock(prod.getStock() + detalle.getCantidad());
            }
        }

        pedido.setEstado(nuevoEstado);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    /**
     * Lista todos los pedidos registrados en el sistema (vista administrativa).
     *
     * @return Lista completa de pedidos ordenada cronológicamente de forma descendente.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc().stream()
                .map(PedidoResponse::from)
                .toList();
    }

    /**
     * Lista los pedidos asignados a un repartidor en específico.
     *
     * @param emailRepartidor Correo electrónico del repartidor.
     * @return Lista de pedidos asignados al repartidor.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorRepartidor(String emailRepartidor) {
        Usuario repartidor = usuarioService.obtenerPorEmail(emailRepartidor);
        return pedidoRepository.findByRepartidorIdOrderByFechaPedidoDesc(repartidor.getId()).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        boolean valida = switch (actual) {
            case PENDIENTE -> nuevo == EstadoPedido.EN_PREPARACION || nuevo == EstadoPedido.CANCELADO;
            case EN_PREPARACION -> nuevo == EstadoPedido.EN_CAMINO || nuevo == EstadoPedido.CANCELADO;
            case EN_CAMINO -> nuevo == EstadoPedido.ENTREGADO || nuevo == EstadoPedido.CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };

        if (!valida) {
            throw new InvalidStatusException(
                    "Transición no permitida: no se puede cambiar el estado de " + actual + " a " + nuevo);
        }
    }

    private Long idOrThrow(Long id) {
        return id != null ? id : 0L;
    }
}

