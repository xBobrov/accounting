package com.vodokanal.accounting.model;

import com.vodokanal.accounting.entity.AccountEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "meter")
public class Meter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "serial_number", nullable = false)
    private Long serialNumber;

    @Column(name = "validity", nullable = false)
    private LocalDate validity;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "init_value", nullable = false, precision = 8, scale = 3)
    private BigDecimal initialValue;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @OneToMany(mappedBy = "meter")
    private List<Reading> readings;

    public Meter() {
    }

    public long getId() {
        return id;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getValidity() {
        return validity;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public BigDecimal getInitialValue() {
        return initialValue;
    }

    public Service getService() {
        return service;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setValidity(LocalDate validity) {
        this.validity = validity;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public void setInitialValue(BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
