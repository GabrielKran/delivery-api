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

                        if (details.getStore() != null) {
                            order.setStoreName(details.getStore().getName());
                        }

                        order.setCreatedAt(details.getCreatedAt());

                        // MAPEAMENTO DO CLIENTE
                        if (details.getCustomer() != null) {
                            com.cocobambu.delivery_api.entity.CustomerEmbeddable ce = new com.cocobambu.delivery_api.entity.CustomerEmbeddable();
                            ce.setName(details.getCustomer().getName());
                            ce.setTemporaryPhone(details.getCustomer().getTemporaryPhone());
                            order.setCustomer(ce);
                        }

                        // MAPEAMENTO DO ENDEREÇO
                        if (details.getDeliveryAddress() != null) {
                            com.cocobambu.delivery_api.entity.AddressEmbeddable ae = new com.cocobambu.delivery_api.entity.AddressEmbeddable();
                            ae.setCountry(details.getDeliveryAddress().getCountry());
                            ae.setReference(details.getDeliveryAddress().getReference());
                            ae.setStreetName(details.getDeliveryAddress().getStreetName());
                            ae.setPostalCode(details.getDeliveryAddress().getPostalCode());
                            ae.setCity(details.getDeliveryAddress().getCity());
                            ae.setNeighborhood(details.getDeliveryAddress().getNeighborhood());
                            ae.setStreetNumber(details.getDeliveryAddress().getStreetNumber());
                            ae.setState(details.getDeliveryAddress().getState());

                            if (details.getDeliveryAddress().getCoordinates() != null) {
                                ae.setLongitude(details.getDeliveryAddress().getCoordinates().getLongitude());
                                ae.setLatitude(details.getDeliveryAddress().getCoordinates().getLatitude());
                                ae.setCoordId(details.getDeliveryAddress().getCoordinates().getId());
                            }
                            order.setDeliveryAddress(ae);
                        }
                    
                        if (details.getItems() != null) {
                            List<com.cocobambu.delivery_api.entity.OrderItem> items = new ArrayList<>();

                            for (ItemDTO itemDto : details.getItems()) {
                                // 1. PRIMEIRO criamos a entidade OrderItem
                                com.cocobambu.delivery_api.entity.OrderItem item = new com.cocobambu.delivery_api.entity.OrderItem();

                                // 2. DEPOIS passamos os dados do DTO para a Entidade (usando item.set... e não itemDto.set...)
                                item.setCode(itemDto.getCode());
                                if (itemDto.getDiscount() != null) {
                                    item.setDiscount(java.math.BigDecimal.valueOf(itemDto.getDiscount()));
                                }
                                
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

                                if (statusDto.getName() != null) {
                                    status.setName(com.cocobambu.delivery_api.entity.Status.valueOf(statusDto.getName().toUpperCase()));
}
                                status.setCreatedAt(statusDto.getCreatedAt());
                                status.setOrigin(statusDto.getOrigin());

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