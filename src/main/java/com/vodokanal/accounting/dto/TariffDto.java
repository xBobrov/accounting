package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.*;

public record TariffDto(
        @Null(message = "Индитификационный номер не должен быть указан")
        Long id,

        @NotNull(message = "Не указана услуга")
        Long service,

        @NotBlank(message = "Не указана дата установки тарифа")
        @Pattern(regexp = "(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)",
                message = "Неверный формат даты установки тарифа, дата должна иметь формат: гггг-мм-дд")
        String implementationDate,

        @NotBlank(message = "Не указана ставка тарифа")
        @Pattern(regexp = "^\\d+\\.\\d{2}$",
                message = "Ставка тарифа имеет неверный формат," +
                        " десятичная часть числа должна иметь два знака и отделятся точкой")
        String rate
) {
}
