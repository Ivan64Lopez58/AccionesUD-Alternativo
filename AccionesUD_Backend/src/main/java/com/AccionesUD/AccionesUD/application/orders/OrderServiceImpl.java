package com.AccionesUD.AccionesUD.application.orders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.AccionesUD.AccionesUD.domain.model.Order;
import com.AccionesUD.AccionesUD.dto.orders.OrderRequestDTO;
import com.AccionesUD.AccionesUD.dto.orders.OrderResponseDTO;
import com.AccionesUD.AccionesUD.repository.OrderRepository;
import com.AccionesUD.AccionesUD.repository.UserRepository;
import com.AccionesUD.AccionesUD.utilities.orders.OrderStatus;
import com.AccionesUD.AccionesUD.utilities.orders.OrderValidator;

import org.springframework.context.ApplicationEventPublisher;
import com.AccionesUD.AccionesUD.domain.model.notification.NotificationEvent;
import com.AccionesUD.AccionesUD.domain.model.notification.NotificationType;
import com.AccionesUD.AccionesUD.alpacaStock.application.StockService;
import com.AccionesUD.AccionesUD.alpacaStock.domain.model.StockInfo;


@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final StockService stockService;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ModelMapper modelMapper, 
    ApplicationEventPublisher eventPublisher, StockService stockService, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
        this.stockService = stockService;
        this.userRepository = userRepository;
    }

      @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO, String username) {

        OrderValidator.validate(requestDTO);
        StockInfo quote = stockService.getLatestTrade(requestDTO.getSymbol());
        Order orderEntity = modelMapper.map(requestDTO, Order.class);
    
        orderEntity.setUsername(username);
        orderEntity.setMarketPrice(quote.getPrice());
        orderEntity.setMarket(quote.getSymbol());
        orderEntity.setStatus(OrderStatus.PENDING);
        orderEntity.setCreatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(orderEntity);
        eventPublisher.publishEvent(new NotificationEvent(
            this,
            username,
            NotificationType.ORDEN_CREADA,
            "Orden creada",
            "Has creado una orden del tipo " + requestDTO.getOrderType()
        ));

        return modelMapper.map(saved, OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO ejecutarOrden(Long orderId) {
        Order orden = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId));

        orden.setStatus(OrderStatus.EXECUTED);
        Order actualizada = orderRepository.save(orden);

        eventPublisher.publishEvent(
            new NotificationEvent(
                this,
                actualizada.getUsername(),
                NotificationType.ORDEN_EJECUTADA,
                "Orden ejecutada",
                "Tu orden para el símbolo " + actualizada.getSymbol() + " fue ejecutada correctamente."
            )
        );

        return modelMapper.map(actualizada, OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO rechazarOrdenPorLimite(Long orderId) {
        Order orden = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId));

        orden.setStatus(OrderStatus.REJECTED);
        Order actualizada = orderRepository.save(orden);

        eventPublisher.publishEvent(
            new NotificationEvent(
                this,
                actualizada.getUsername(),
                NotificationType.ORDEN_RECHAZADA,
                "Orden rechazada",
                "Tu orden para el símbolo " + actualizada.getSymbol() + " fue rechazada por superar tu límite diario de órdenes."
            )
        );

        return modelMapper.map(actualizada, OrderResponseDTO.class);
    }

   @Override
    public List<OrderResponseDTO> listarTodasLasOrdenes() {
        return orderRepository.findAll()
            .stream()
            .map(o -> modelMapper.map(o, OrderResponseDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> listarOrdenesPorUsuario(String username) {
        return orderRepository.findByUsername(username)
            .stream()
            .map(o -> modelMapper.map(o, OrderResponseDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public long contarOrdenesPorUsuario(String username) {
        return orderRepository.countByUsername(username);
    }

    @Override
    public List<OrderResponseDTO> listarOrdenesPorMercado(String market) {
        return orderRepository.findByMarket(market)
            .stream()
            .map(o -> modelMapper.map(o, OrderResponseDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> listarOrdenesPorCompany(String company) {
        return orderRepository.findByCompany(company)
            .stream()
            .map(o -> modelMapper.map(o, OrderResponseDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO actualizarEstadoOrden(Long orderId, OrderStatus newStatus) {
        Order o = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));
        OrderStatus current = o.getStatus();
        if (current == OrderStatus.PENDING || current == OrderStatus.SENT) {
            if (newStatus != OrderStatus.EXECUTED
             && newStatus != OrderStatus.CANCELLED
             && newStatus != OrderStatus.REJECTED
             && newStatus != OrderStatus.EXPIRED) {
                throw new IllegalArgumentException(
                    "Transición no válida: no se puede cambiar de " 
                    + current + " a " + newStatus);
            }
        } else {
            throw new IllegalArgumentException(
                "No se puede cambiar el estado desde " + current);
        }
        o.setStatus(newStatus);
        Order saved = orderRepository.save(o);
        return modelMapper.map(saved, OrderResponseDTO.class);
    }

}
