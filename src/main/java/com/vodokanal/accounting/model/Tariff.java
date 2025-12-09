package com.vodokanal.accounting.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tariff")
public class Tariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "impl_date", nullable = false)
    private LocalDate implementationDate;

    @Column(name = "rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal rate;

    public Tariff() {
    }

    public long getId() {
        return id;
    }

    public Service getService() {
        return service;
    }

    public LocalDate getImplementationDate() {
        return implementationDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setImplementationDate(LocalDate implementationDate) {
        this.implementationDate = implementationDate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
