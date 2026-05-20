package com.vodokanal.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="reading", uniqueConstraints = {
        @UniqueConstraint(name = "uc_reading", columnNames = {"date", "meter_id"})
})
public class ReadingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "meter_id", nullable = false)
    private MeterEntity meter;

    @Column(name = "value", precision = 10, scale = 3,
            columnDefinition = "DECIMAL(10, 3) DEFAULT 0.0")
    private BigDecimal value;

    @Column(name = "consumption", precision = 10, scale = 3,
            columnDefinition = "DECIMAL(10, 3) DEFAULT 0.0")
    private BigDecimal consumption;

    public ReadingEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
