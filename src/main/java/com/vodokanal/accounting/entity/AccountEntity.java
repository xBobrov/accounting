package com.vodokanal.accounting.entity;

import com.vodokanal.accounting.model.Charge;
import com.vodokanal.accounting.model.Meter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "number", nullable = false, unique = true, length = 10)
    private String number;

    //@NotNull
    @Column(name = "address", nullable = false, unique = true)
    private String address;

   // @NotNull
    @Column(name = "payer", nullable = false)
    private String payer;

    @Column(name = "email")
    private String email;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "telegram")
    private String telegram;

    @Column(name = "balance", precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10, 2) DEFAULT 0.0")
    private BigDecimal balance;

    @OneToMany(mappedBy = "account")
    private List<Meter> meters;

    @OneToMany(mappedBy = "account")
    private List<Charge> charges;

    public AccountEntity() {
        this.active = true;
        this.balance = BigDecimal.valueOf(0.0);
    }

    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getAddress() {
        return address;
    }

    public String getPayer() {
        return payer;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public String getTelegram() {
        return telegram;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNumber(String nunber) {
        this.number = nunber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }
}
