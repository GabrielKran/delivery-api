package com.cocobambu.delivery_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class CustomerEmbeddable {
    @Column(name = "customer_name")
    private String name;
    
    @Column(name = "customer_phone")
    private String temporaryPhone;
}