package com.cocobambu.delivery_api.service;

import com.cocobambu.delivery_api.dto.OrderSummaryDTO;
import com.cocobambu.delivery_api.entity.Order;
import com.cocobambu.delivery_api.entity.OrderStatus;
import com.cocobambu.delivery_api.entity.Status;
import com.cocobambu.delivery_api.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository repository;

    public List<OrderSummaryDTO> findAll() {
        List<Order> orders = repository.findAll();
        
        return orders.stream().map(order -> {
            OrderSummaryDTO dto = new OrderSummaryDTO();
            dto.setId(order.getId());
            dto.setTotalPrice(order.getTotalPrice());
            dto.setLastStatusName(order.getLastStatusName());
            dto.setCreatedAt(order.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public Order findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }

    public Order create(Order order) {
        order.setId(java.util.UUID.randomUUID().toString());
        order.setCreatedAt(System.currentTimeMillis());

        order.setLastStatusName(Status.RECEIVED.name());

        OrderStatus initialStatus = new OrderStatus();
        initialStatus.setName(Status.RECEIVED);
        initialStatus.setCreatedAt(System.currentTimeMillis());
        initialStatus.setOrder(order);

        if (order.getStatuses() == null) {
            order.setStatuses(new java.util.ArrayList<>());
        }
        order.getStatuses().add(initialStatus);

        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }
        if (order.getPayments() != null) {
            order.getPayments().forEach(payment -> payment.setOrder(order));
        }

        return repository.save(order);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Pedido não encontrado para exclusão!");
        }
        repository.deleteById(id);
    }

    public Order updateOrderStatus(String orderId, String novoStatusStr) {
        Optional<Order> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Pedido não encontrado!");
        }
        Order order = orderOpt.get();

        Status statusAtual = Status.valueOf(order.getLastStatusName().toUpperCase());
        Status novoStatus = Status.valueOf(novoStatusStr.toUpperCase());

        processChange(statusAtual, novoStatus);

        order.setLastStatusName(novoStatus.name());

        OrderStatus statusHistory = new OrderStatus();
        statusHistory.setName(novoStatus);
        statusHistory.setCreatedAt(System.currentTimeMillis());
        statusHistory.setOrder(order);
        
        order.getStatuses().add(statusHistory);

        return repository.save(order);
    }

    private void processChange(Status statusAtual, Status novoStatus) {

        if (statusAtual == Status.DELIVERED || statusAtual == Status.CANCELED) {
            throw new IllegalArgumentException("Transição de status inválida de " + statusAtual + " para " + novoStatus);
        }
        
        boolean isValid = switch (statusAtual) {
            case RECEIVED -> List.of(Status.CONFIRMED, Status.CANCELED).contains(novoStatus);
            
            case CONFIRMED -> List.of(Status.DISPATCHED, Status.CANCELED).contains(novoStatus);
            
            case DISPATCHED -> List.of(Status.DELIVERED, Status.CANCELED).contains(novoStatus);

            default -> false;
        };

        if (!isValid) {
            throw new IllegalArgumentException("Transição de status inválida de " + statusAtual + " para " + novoStatus);
        }
    }
}