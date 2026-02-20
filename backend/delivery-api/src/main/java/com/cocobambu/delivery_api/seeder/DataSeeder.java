package com.cocobambu.delivery_api.seeder;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.cocobambu.delivery_api.dto.PedidoImportDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ObjectMapper objectMapper;

    public DataSeeder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/pedidos.json");

        if (inputStream == null) {
            System.out.println("Arquivo pedidos.json nao encontrado.");
            return;
        }

        try {
            List<PedidoImportDTO> pedidosLidos = objectMapper.readValue(
                    inputStream, 
                    new TypeReference<List<PedidoImportDTO>>() {}
            );

            System.out.println("Leitura concluida com sucesso. Total: " + pedidosLidos.size());
            
            if (!pedidosLidos.isEmpty()) {
                System.out.println("ID do primeiro pedido: " + pedidosLidos.get(0).getOrderId());
            }

        } catch (Exception e) {
            System.out.println("Falha ao ler o arquivo: " + e.getMessage());
        }
    }
}