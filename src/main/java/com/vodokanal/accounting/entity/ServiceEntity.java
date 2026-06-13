package com.vodokanal.accounting.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "service")
    private List<TariffEntity> tariffs;

    @OneToMany(mappedBy = "service")
    private List<MeterEntity> meters;

    @OneToMany(mappedBy = "service")
    private List<NormEntity> norms;

    @OneToMany(mappedBy = "service")
    private List<CalculationEntity> calculations;

    public ServiceEntity() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
