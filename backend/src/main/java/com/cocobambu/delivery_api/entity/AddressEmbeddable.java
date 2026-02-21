package com.cocobambu.delivery_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class AddressEmbeddable {

    @Column(name = "delivery_country")
    private String country;

    @Column(name = "delivery_reference")
    private String reference;

    @Column(name = "delivery_street")
    private String streetName;

    @Column(name = "delivery_postal_code")
    private String postalCode;

    @Column(name = "delivery_city")
    private String city;

    @Column(name = "delivery_neighborhood")
    private String neighborhood;

    @Column(name = "delivery_street_number")
    private String streetNumber;

    @Column(name = "delivery_state")
    private String state;
    
    @Column(name = "coord_longitude")
    private Double longitude;

    @Column(name = "coord_latitude")
    private Double latitude;

    @Column(name = "coord_id")
    private Long coordId;
}