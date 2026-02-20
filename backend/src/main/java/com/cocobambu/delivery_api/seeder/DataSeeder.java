package com.cocobambu.delivery_api.seeder;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import com.cocobambu.delivery_api.dto.ItemDTO;
import com.cocobambu.delivery_api.dto.OrderDetailsDTO;
import com.cocobambu.delivery_api.dto.PaymentDTO;
import com.cocobambu.delivery_api.dto.PedidoImportDTO;
import com.cocobambu.delivery_api.dto.StatusDTO;
import com.cocobambu.delivery_api.entity.Order;
import com.cocobambu.delivery_api.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        if (orderRepository.count() > 0) {
            System.out.println("Banco de dados já populado. Pulando o Seeder.");
            return;
        }

        InputStream inputStream = getClass().getResourceAsStream("/pedidos.json");

        if (inputStream == null) {
            System.out.println("Arquivo pedidos.json não encontrado.");
            return;
        }

        try {
            List<PedidoImportDTO> pedidosLidos = objectMapper.readValue(
                inputStream, new TypeReference<List<PedidoImportDTO>>() {}
            );

            System.out.println("Leitura concluída com sucesso. Total: " + pedidosLidos.size());
            
            if (!pedidosLidos.isEmpty()) {
                List<Order> ordersParaSalvar = new ArrayList<>();
                
                for (PedidoImportDTO dto : pedidosLidos) {
                    Order order = new Order();

                    order.setStoreId(dto.getStoreId());
                    order.setId(dto.getOrderId());

                    OrderDetailsDTO details = dto.getOrder();
                    if (details != null) {
                        if (details.getTotalPrice() != null) {
                            order.setTotalPrice(java.math.BigDecimal.valueOf(details.getTotalPrice()));
                        }
                        order.setLastStatusName(details.getLastStatusName());
                        order.setCreatedAt(details.getCreatedAt());
                    
                        if (details.getItems() != null) {
                            List<com.cocobambu.delivery_api.entity.OrderItem> items = new ArrayList<>();
                            for (ItemDTO itemDto : details.getItems()) {
                                com.cocobambu.delivery_api.entity.OrderItem item = new com.cocobambu.delivery_api.entity.OrderItem();

                                item.setName(itemDto.getName());
                                if (itemDto.getPrice() != null) {
                                    item.setPrice(java.math.BigDecimal.valueOf(itemDto.getPrice()));
                                }
                                if (itemDto.getTotalPrice() != null) {
                                    item.setTotalPrice(java.math.BigDecimal.valueOf(itemDto.getTotalPrice()));
                                }
                                item.setQuantity(itemDto.getQuantity());
                                item.setObservations(itemDto.getObservations());

                                item.setOrder(order); 
                                items.add(item);
                            }
                            order.setItems(items);
                        }
                    
                        if (details.getPayments() != null) {
                            List<com.cocobambu.delivery_api.entity.OrderPayment> payments = new ArrayList<>();
                            for (PaymentDTO payDto : details.getPayments()) {
                                com.cocobambu.delivery_api.entity.OrderPayment payment = new com.cocobambu.delivery_api.entity.OrderPayment();

                                if (payDto.getValue() != null) {
                                    payment.setValue(java.math.BigDecimal.valueOf(payDto.getValue()));
                                }
                                payment.setOrigin(payDto.getOrigin());
                                payment.setPrepaid(payDto.getPrepaid());

                                payment.setOrder(order); 
                                payments.add(payment);
                            }
                            order.setPayments(payments);
                        }
                    
                        if (details.getStatuses() != null) {
                            List<com.cocobambu.delivery_api.entity.OrderStatus> statuses = new ArrayList<>();
                            for (StatusDTO statusDto : details.getStatuses()) {
                                com.cocobambu.delivery_api.entity.OrderStatus status = new com.cocobambu.delivery_api.entity.OrderStatus();

                                // A CORREÇÃO DO ENUM ESTÁ AQUI
                                if (statusDto.getName() != null) {
                                    status.setName(com.cocobambu.delivery_api.entity.Status.valueOf(statusDto.getName().toUpperCase()));
}
                                status.setCreatedAt(statusDto.getCreatedAt());

                                status.setOrder(order); 
                                statuses.add(status);
                            }
                            order.setStatuses(statuses);
                        }
                    }

                    ordersParaSalvar.add(order);
                }

                orderRepository.saveAll(ordersParaSalvar);
                System.out.println("Pedidos salvos no banco de dados com sucesso!");
            }

        } catch (Exception e) {
            System.out.println("Falha ao ler o arquivo ou salvar no banco: " + e.getMessage());
            e.printStackTrace(); // Vai nos ajudar a ver o erro exato se falhar novamente
        }
    }
}