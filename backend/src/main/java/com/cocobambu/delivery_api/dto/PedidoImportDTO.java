package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoImportDTO {
    
    @JsonProperty("store_id")
    private String storeId;
    
    @JsonProperty("order_id")
    private String orderId;
    
    @JsonProperty("order")
    private OrderDetailsDTO order;
}