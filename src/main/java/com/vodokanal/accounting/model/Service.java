package com.vodokanal.accounting.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "service")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "service", nullable = false)
    private String service;

    @OneToMany(mappedBy = "service")
    private List<Tariff> tariffs;

    @OneToMany(mappedBy = "service")
    private List<Meter> meters;

    @OneToMany(mappedBy = "service")
    private List<Charge> charges;

    public Service() {
    }

    public long getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setService(String service) {
        this.service = service;
    }
}


