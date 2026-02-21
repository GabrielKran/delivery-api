package com.cocobambu.delivery_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderSummaryDTO {
    private String id;
    private String customerName;
    private BigDecimal totalPrice;
    private String lastStatusName;
    private Long createdAt;
}