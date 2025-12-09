package com.vodokanal.accounting.model;

import com.vodokanal.accounting.entity.AccountEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "charge")
public class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "charge", nullable = false, precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10, 2) DEFAULT 0.0")
    private BigDecimal charge;

    public Charge() {
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public Service getService() {
        return service;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }
}
