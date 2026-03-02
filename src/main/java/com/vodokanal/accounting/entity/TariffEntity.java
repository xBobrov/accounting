package com.vodokanal.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tariff")
public class TariffEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(name = "impl_date", nullable = false)
    private LocalDate implementationDate;

    @Column(name = "rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    public TariffEntity() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public void setImplementationDate(LocalDate implementationDate) {
        this.implementationDate = implementationDate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public long getId() {
        return id;
    }

    public ServiceEntity getService() {
        return service;
    }

    public LocalDate getImplementationDate() {
        return implementationDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "TariffEntity{" +
                "id=" + id +
                ", service=" + service.getName() +
                ", implementationDate=" + implementationDate +
                ", rate=" + rate +
                '}';
    }
}
