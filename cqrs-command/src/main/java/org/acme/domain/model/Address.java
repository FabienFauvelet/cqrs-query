package org.acme.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Address {
    private UUID id;
    private String street;
    private String zipCode;
    private String city;
}
