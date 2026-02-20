package com.cocobambu.delivery_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_statuses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private Status name;

    @Column(name = "created_at")
    private Long createdAt;
}