package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusDTO {
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    private String name;
    
    @JsonProperty("order_id")
    private String orderId;
}