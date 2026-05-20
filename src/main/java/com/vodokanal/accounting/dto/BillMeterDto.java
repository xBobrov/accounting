package com.vodokanal.accounting.dto;

import java.math.BigDecimal;
import java.util.Date;

public record BillMeterDto(
        String account,
        String meterNumber,
        String service,
        BigDecimal value,
        Date valid,
        BigDecimal consumption
) {
    public BillMeterDto {
        if (service.equals("1")) {
            service = "ХВ";
        } else if (service.equals("2")) {
            service = "ГВ";
        } else {
            service = " ";
        }
    }
}
