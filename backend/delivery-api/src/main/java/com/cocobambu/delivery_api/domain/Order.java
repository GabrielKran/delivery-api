package com.cocobambu.delivery_api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "last_status_name")
    private String lastStatusName;

    @Column(name = "created_at")
    private Long createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderPayment> payments;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderStatus> statuses;
}