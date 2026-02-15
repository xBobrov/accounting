package com.vodokanal.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meter")
public class MeterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 10)
    private String serialNumber;

    @Column(name = "valid_thru", nullable = false)
    private LocalDate validThru;

    @Column(name = "initial_value", precision = 10, scale = 3,
            columnDefinition = "DECIMAL(10, 3) DEFAULT 0.0")
    private BigDecimal initialValue;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    public MeterEntity() {
    }

    public long getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public BigDecimal getInitialValue() {
        return initialValue;
    }

    public ServiceEntity getService() {
        return service;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public void setInitialValue(BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }
}
