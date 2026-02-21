package com.cocobambu.delivery_api;

// Importamos o DTO que o Service agora devolve
import com.cocobambu.delivery_api.dto.PedidoImportDTO; 
import com.cocobambu.delivery_api.entity.Order;
import com.cocobambu.delivery_api.entity.Status;
import com.cocobambu.delivery_api.repository.OrderRepository;
import com.cocobambu.delivery_api.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    private Order pedidoMock;

    @BeforeEach
    void setUp() {
        // Inicializa um pedido default no estado RECEIVED para ser usado nos testes
        pedidoMock = new Order();
        pedidoMock.setId("mock-id-123");
        pedidoMock.setLastStatusName(Status.RECEIVED.name());
        pedidoMock.setStatuses(new ArrayList<>());
    }

    @Test
    void devePermitirTransicaoDeReceivedParaConfirmed() {
        // Mocka a busca do banco
        when(orderRepository.findById("mock-id-123")).thenReturn(Optional.of(pedidoMock));
        
        // MÁGICA AQUI: Diz ao Mockito para retornar o exato objeto que tentamos salvar, 
        // com todas as modificações (incluindo o status adicionado na lista).
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoImportDTO atualizado = orderService.updateOrderStatus("mock-id-123", "CONFIRMED");

        // Validações
        assertNotNull(atualizado.getOrder(), "O objeto order não deveria ser nulo no DTO");
        assertEquals(Status.CONFIRMED.name(), atualizado.getOrder().getLastStatusName());
        assertFalse(atualizado.getOrder().getStatuses().isEmpty(), "O histórico de status não pode estar vazio");
    }

    @Test
    void naoDevePermitirPularEtapasDeReceivedParaDelivered() {
        // Tenta fazer um salto inválido na máquina de estados (RECEIVED direto para DELIVERED).
        // Espera que o service barre a ação lançando uma exceção de argumento inválido.
        when(orderRepository.findById("mock-id-123")).thenReturn(Optional.of(pedidoMock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus("mock-id-123", "DELIVERED");
        });

        assertTrue(exception.getMessage().contains("Transição de status inválida"));
    }

    @Test
    void naoDevePermitirAlterarPedidoQueJaEstaCancelado() {
        // Força o pedido inicial para CANCELED e valida se o sistema bloqueia 
        // qualquer tentativa de mudança para um status anterior (ex: RECEIVED).
        pedidoMock.setLastStatusName(Status.CANCELED.name());
        when(orderRepository.findById("mock-id-123")).thenReturn(Optional.of(pedidoMock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus("mock-id-123", "RECEIVED");
        });

        assertTrue(exception.getMessage().contains("Transição de status inválida"));
    }
}