package com.cocobambu.delivery_api.controller;

import com.cocobambu.delivery_api.dto.OrderSummaryDTO;
import com.cocobambu.delivery_api.dto.PedidoImportDTO; // IMPORTANTE: Adicionamos o import aqui!
import com.cocobambu.delivery_api.entity.Order;
import com.cocobambu.delivery_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService service;

    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // A MUDANÇA ESTÁ AQUI: Trocamos <Order> por <PedidoImportDTO>
    @GetMapping("/{id}")
    public ResponseEntity<PedidoImportDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        Order novoPedido = service.create(order);
        return ResponseEntity.ok(novoPedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Atualizar o Status - A MÁQUINA DE ESTADOS (PATCH /orders/{id}/status?newStatus=CONFIRMED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestParam String newStatus) {
        Order pedidoAtualizado = service.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(pedidoAtualizado);
    }
}