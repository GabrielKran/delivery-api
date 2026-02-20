package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {
    private Boolean prepaid;
    private Double value;
    private String origin;
}