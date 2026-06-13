package com.vodokanal.accounting.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "calc_method")
public class CalcMethodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "method")
    private List<CalculationEntity> calculations;

    public CalcMethodEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<CalculationEntity> getCalculations() {
        return calculations;
    }

    public void setCalculations(List<CalculationEntity> calculations) {
        this.calculations = calculations;
    }

    public void setName(String name) {
        this.name = name;
    }
}
