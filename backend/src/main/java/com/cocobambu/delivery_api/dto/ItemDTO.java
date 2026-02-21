package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {
    private String name;
    private Integer quantity;
    private Double price;
    
    @JsonProperty("total_price")
    private Double totalPrice;
    
    private String observations;

    private Integer code;
    private Double discount;
    private java.util.List<String> condiments = new java.util.ArrayList<>();
}