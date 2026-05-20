package com.vodokanal.accounting.dto;

import java.math.BigDecimal;

public record BillCalculationDTO(

        String service,
        Boolean isNormed,
        String account,
        String payer,
        String address,
        Integer residents,
        String norm,
        BigDecimal tariff,
        String consumption,
        BigDecimal sum,
        Long code
) {


    public BillCalculationDTO {
        if (service.equals("1")) {
            service = "Холодное Водоснабжение";
        } else if (service.equals("2")) {
            service = "Горячее Водоснабжение";
        } else {
            service = "Водоотведение";
        }

        if (code == 1) {
            norm = "";
        } else {
            consumption = "";
        }
    }
}
