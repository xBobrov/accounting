package com.vodokanal.accounting.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record MeterDataDto(
        Long id,
        String number,
        String service,
        Date valid,
        BigDecimal lastReading
) {
}
