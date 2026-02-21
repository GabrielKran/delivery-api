package com.cocobambu.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryAddressDTO {
    private String reference;
    @JsonProperty("street_name")
    private String streetName;

    @JsonProperty("postal_code")
    private String postalCode;

    private String country;
    private String city;
    private String neighborhood;
    
    @JsonProperty("street_number")
    private String streetNumber;

    private String state;
    private CoordinatesDTO coordinates;
}