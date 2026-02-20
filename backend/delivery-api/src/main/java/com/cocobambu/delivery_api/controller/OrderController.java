package com.cocobambu.delivery_api.controller;

import com.cocobambu.delivery_api.entity.Order;
import com.cocobambu.delivery_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*") // Permite que o seu frontend local faça requisições para esta API
public class OrderController {

    @Autowired
    private OrderService service;

    // 1. Buscar todos os pedidos (GET /orders)
    @GetMapping
    public ResponseEntity<List<Order>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // 2. Buscar um pedido específico (GET /orders/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // 3. Criar um novo pedido (POST /orders)
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        Order novoPedido = service.create(order);
        return ResponseEntity.ok(novoPedido);
    }

    // 4. Deletar um pedido (DELETE /orders/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Atualizar o Status - A MÁQUINA DE ESTADOS (PATCH /orders/{id}/status?newStatus=CONFIRMED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestParam String newStatus) {
        Order pedidoAtualizado = service.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(pedidoAtualizado);
    }
}