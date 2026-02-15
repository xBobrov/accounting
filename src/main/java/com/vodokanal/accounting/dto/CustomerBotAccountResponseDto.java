package com.vodokanal.accounting.dto;

import java.math.BigDecimal;

public record CustomerBotAccountResponseDto(
        String number,
        BigDecimal balance
) {}
