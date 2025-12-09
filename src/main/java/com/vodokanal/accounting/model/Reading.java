package com.vodokanal.accounting.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reading")
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "value", nullable = false, precision = 8, scale = 3)
    private BigDecimal value;

    public Reading() {
    }

    public long getId() {
        return id;
    }

    public Meter getMeter() {
        return meter;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
