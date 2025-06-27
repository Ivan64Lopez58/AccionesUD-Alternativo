package com.AccionesUD.AccionesUD.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AccionesUD.AccionesUD.application.orders.OrderService;
import com.AccionesUD.AccionesUD.dto.orders.OrderRequestDTO;
import com.AccionesUD.AccionesUD.dto.orders.OrderResponseDTO;
import com.AccionesUD.AccionesUD.utilities.orders.OrderStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        try {
            List<OrderResponseDTO> all = orderService.listarTodasLasOrdenes();
            return ResponseEntity.ok(all);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al listar órdenes.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO dto, Principal principal) {
        try {
            String username = principal.getName();
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            OrderResponseDTO resp = orderService.createOrder(dto, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al crear orden.");
        }
                                 
    }

    @GetMapping("/user")
    public ResponseEntity<?> listarPorUsuario() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<OrderResponseDTO> list = orderService.listarOrdenesPorUsuario(username);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al listar por usuario.");
        }
    }

    @GetMapping("/me")
public ResponseEntity<?> listarMisOrdenes(Principal principal) {
    try {
        String username = principal.getName();
        List<OrderResponseDTO> list = orderService.listarOrdenesPorUsuario(username);
        return ResponseEntity.ok(list);
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error interno al listar tus órdenes.");
    }
}

    @GetMapping("/user/{username}/count")
    public ResponseEntity<?> contarPorUsuario(@PathVariable String username) {
        try {
            long count = orderService.contarOrdenesPorUsuario(username);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al contar órdenes.");
        }
    }

    @GetMapping("/market/{market}")
    public ResponseEntity<?> listarPorMercado(@PathVariable String market) {
        try {
            List<OrderResponseDTO> list = orderService.listarOrdenesPorMercado(market);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al listar por mercado.");
        }
    }

    @GetMapping("/company/{company}")
    public ResponseEntity<?> listarPorCompany(@PathVariable String company) {
        try {
            List<OrderResponseDTO> list = orderService.listarOrdenesPorCompany(company);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al listar por compañía.");
        }
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable Long id) {
        try {
            OrderResponseDTO resp = orderService.ejecutarOrden(id);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al ejecutar orden.");
        }
    }

    @PutMapping("/{id}/rejectByLimit")
    public ResponseEntity<?> rejectByLimit(@PathVariable Long id) {
        try {
            OrderResponseDTO resp = orderService.rechazarOrdenPorLimite(id);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al rechazar orden.");
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> actualizarEstado(
        @PathVariable Long id,
        @RequestParam("status") String statusStr
    ) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            OrderResponseDTO resp = orderService.actualizarEstadoOrden(id, newStatus);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            // incluye errores de parseo de enum o validación de transición
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error interno al actualizar estado.");
        }
    }

}