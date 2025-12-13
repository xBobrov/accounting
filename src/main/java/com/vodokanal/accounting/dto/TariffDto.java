package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TariffDto(
        @Null(message = "Индитификационный номер не должен быть указан")
        Long id,

        @NotNull
        Long service,

        @NotBlank(message = "Не указана дата установки тарифа")
        LocalDate implementationDate,

        @NotNull
        BigDecimal rate
) {
}
