package com.cocobambu.delivery_api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Integer code;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    
    private String observations;
    private BigDecimal discount;
}