package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailsDTO {
    
    @JsonProperty("total_price")
    private Double totalPrice;
    
    @JsonProperty("last_status_name")
    private String lastStatusName;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    private List<ItemDTO> items;
    private List<PaymentDTO> payments;
    private List<StatusDTO> statuses;
}