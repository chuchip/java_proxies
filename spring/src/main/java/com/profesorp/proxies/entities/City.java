package com.profesorp.proxies.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class City {
    @Id
    @GeneratedValue
    int id;

    String name;
    String postCode;
}
