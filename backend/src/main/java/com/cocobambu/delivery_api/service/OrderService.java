package com.cocobambu.delivery_api.service;

import com.cocobambu.delivery_api.dto.CoordinatesDTO;
import com.cocobambu.delivery_api.dto.CustomerDTO;
import com.cocobambu.delivery_api.dto.DeliveryAddressDTO;
import com.cocobambu.delivery_api.dto.ItemDTO;
import com.cocobambu.delivery_api.dto.OrderDetailsDTO;
import com.cocobambu.delivery_api.dto.OrderSummaryDTO;
import com.cocobambu.delivery_api.dto.PaymentDTO;
import com.cocobambu.delivery_api.dto.PedidoImportDTO;
import com.cocobambu.delivery_api.dto.StatusDTO;
import com.cocobambu.delivery_api.dto.StoreDTO;
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

            if (order.getCustomer() != null && order.getCustomer().getName() != null) {
                dto.setCustomerName(order.getCustomer().getName());
            } else {
                dto.setCustomerName("Cliente não informado");
            }

            dto.setTotalPrice(order.getTotalPrice());
            dto.setLastStatusName(order.getLastStatusName());
            dto.setCreatedAt(order.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public PedidoImportDTO findById(String id) {
        Order order = repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
        return convertToDto(order);
    }

    public PedidoImportDTO create(Order order) {
        if (order.getItems() != null && order.getTotalPrice() != null) {
            double somaCalculada = order.getItems().stream()
                    .mapToDouble(item -> {
                        double preco = item.getPrice() != null ? item.getPrice().doubleValue() : 0.0;
                        int qtd = item.getQuantity() != null ? item.getQuantity() : 1;
                        return preco * qtd;
                    })
                    .sum();

            if (Math.abs(somaCalculada - order.getTotalPrice().doubleValue()) > 0.01) {
                throw new IllegalArgumentException(
                        String.format("Tentativa de fraude ou erro de cálculo! O total enviado foi R$ %.2f, mas a soma dos itens é R$ %.2f", 
                        order.getTotalPrice().doubleValue(), somaCalculada)
                );
            }
        }

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

        Order savedOrder = repository.save(order);
        return convertToDto(savedOrder);
    }

    public void delete(String id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para exclusão!"));

        // Apenas pedidos cancelados podem ser apagados
        if (!Status.CANCELED.name().equals(order.getLastStatusName())) {
            throw new IllegalArgumentException("Regra de Negócio: Apenas pedidos com estado CANCELED podem ser excluídos do sistema.");
        }

        repository.deleteById(id);
    }

    public PedidoImportDTO updateOrderStatus(String orderId, String novoStatusStr) {
        Optional<Order> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Pedido não encontrado!");
        }
        Order order = orderOpt.get();

        Status statusAtual = Status.valueOf(order.getLastStatusName().toUpperCase());
        Status novoStatus = Status.valueOf(novoStatusStr.toUpperCase());

        processChange(statusAtual, novoStatus);

        // Atualiza o status principal
        order.setLastStatusName(novoStatus.name());

        // Prevenção contra NullPointerException (boa prática)
        if (order.getStatuses() == null) {
            order.setStatuses(new java.util.ArrayList<>());
        }

        // Cria o registro no histórico
        OrderStatus statusHistory = new OrderStatus();
        statusHistory.setName(novoStatus);
        statusHistory.setCreatedAt(System.currentTimeMillis());
        statusHistory.setOrder(order);
        
        // A LINHA CRÍTICA QUE NÃO PODE FALTAR:
        order.getStatuses().add(statusHistory);

        Order savedOrder = repository.save(order);
        return convertToDto(savedOrder);
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

    private PedidoImportDTO convertToDto(Order order) {
        PedidoImportDTO response = new PedidoImportDTO();
        response.setOrderId(order.getId());
        response.setStoreId(order.getStoreId());

        OrderDetailsDTO details = new OrderDetailsDTO();
        details.setTotalPrice(order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : null);
        details.setLastStatusName(order.getLastStatusName());
        details.setCreatedAt(order.getCreatedAt());

        if (order.getStoreName() != null) {
            StoreDTO storeDTO = new StoreDTO();
            storeDTO.setId(order.getStoreId());
            storeDTO.setName(order.getStoreName());
            details.setStore(storeDTO);
        }

        if (order.getCustomer() != null) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setName(order.getCustomer().getName());
            customerDTO.setTemporaryPhone(order.getCustomer().getTemporaryPhone());
            details.setCustomer(customerDTO);
        }

        if (order.getDeliveryAddress() != null) {
            DeliveryAddressDTO addressDTO = new DeliveryAddressDTO();
            addressDTO.setReference(order.getDeliveryAddress().getReference());
            addressDTO.setStreetName(order.getDeliveryAddress().getStreetName());
            addressDTO.setPostalCode(order.getDeliveryAddress().getPostalCode());
            addressDTO.setCountry(order.getDeliveryAddress().getCountry());
            addressDTO.setCity(order.getDeliveryAddress().getCity());
            addressDTO.setNeighborhood(order.getDeliveryAddress().getNeighborhood());
            addressDTO.setStreetNumber(order.getDeliveryAddress().getStreetNumber());
            addressDTO.setState(order.getDeliveryAddress().getState());

            CoordinatesDTO coords = new CoordinatesDTO();
            coords.setLongitude(order.getDeliveryAddress().getLongitude());
            coords.setLatitude(order.getDeliveryAddress().getLatitude());
            coords.setId(order.getDeliveryAddress().getCoordId());
            addressDTO.setCoordinates(coords);
            
            details.setDeliveryAddress(addressDTO);
        }

        if (order.getItems() != null) {
            details.setItems(order.getItems().stream().map(item -> {
                ItemDTO i = new ItemDTO();
                i.setName(item.getName());
                i.setQuantity(item.getQuantity());
                i.setPrice(item.getPrice() != null ? item.getPrice().doubleValue() : null);
                i.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null);
                i.setObservations(item.getObservations());
                i.setCode(item.getCode());
                i.setDiscount(item.getDiscount() != null ? item.getDiscount().doubleValue() : 0.0);
                return i;
            }).collect(java.util.stream.Collectors.toList()));
        }

        if (order.getPayments() != null) {
            details.setPayments(order.getPayments().stream().map(pay -> {
                PaymentDTO p = new PaymentDTO();
                p.setPrepaid(pay.getPrepaid());
                p.setValue(pay.getValue() != null ? pay.getValue().doubleValue() : null);
                p.setOrigin(pay.getOrigin());
                return p;
            }).collect(java.util.stream.Collectors.toList()));
        }

        if (order.getStatuses() != null) {
            details.setStatuses(order.getStatuses().stream().map(st -> {
                StatusDTO s = new StatusDTO();
                s.setName(st.getName().name());
                s.setCreatedAt(st.getCreatedAt());
                s.setOrderId(order.getId());
                s.setOrigin(st.getOrigin());
                return s;
            }).collect(java.util.stream.Collectors.toList()));
        }

        response.setOrder(details);
        return response;
    }
}