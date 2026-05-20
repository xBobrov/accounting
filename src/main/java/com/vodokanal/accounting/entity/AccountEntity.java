package com.vodokanal.accounting.entity;

import com.vodokanal.accounting.dto.BillCalculationDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @SequenceGenerator(name = "my_seq", sequenceName = "my_sequence", allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    private long id;

    @Column(name = "number", nullable = false, unique = true, length = 10)
    private String number;

    @NotNull
    @Column(name = "address", nullable = false, unique = true)
    private String address;

    @NotNull
    @Column(name = "payer", nullable = false)
    private String payer;

    @ColumnDefault("''")
    @Column(name = "email")
    private String email = "";

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive;

    @Column(name = "is_normed", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isNormed;

    @Column(name = "telegram_id")
    private Long telegramID;

    @Column(name = "balance", precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10, 2) DEFAULT 0.0")
    private BigDecimal balance;

    @Column(name = "resident_regd")
    private int residentRegd;

    @OneToMany(mappedBy = "account")
    private List<MeterEntity> meters;

    @OneToMany(mappedBy = "account")
    private List<TransactionEntity> transactions;

    @OneToMany(mappedBy = "account")
    private List<CalculationEntity> calculations;

    public AccountEntity() {
        this.isActive = true;
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

    public boolean getIsActive() {
        return isActive;
    }

    public Long getTelegramID() {
        return telegramID;
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

    public void setIsActive(boolean active) {
        this.isActive = active;
    }

    public void setTelegramID(Long telegramID) {
        this.telegramID = telegramID;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getResidentRegd() {
        return residentRegd;
    }

    public void setResidentRegd(int residentRegd) {
        this.residentRegd = residentRegd;
    }
}
