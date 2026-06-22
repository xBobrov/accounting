package com.vodokanal.accounting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record TariffDto(
        @Null(message = "Индитификационный номер не должен быть указан")
        @Schema(
                description = "Уникальный идентификатор записи в базе данных (генерируется автоматически, не заполнять" +
                        " при создании)",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "1"
        )
        Long id,

        @NotNull(message = "Не указана услуга")
        @Schema(
                description = "Индетификатор услуги, по умолчанию 1 - холодное водоснабжение, 2 - горячее" +
                        " водоснабжение, 3 - водоотведение",
                example = "2"
        )
        Long service,

        @NotBlank(message = "Не указана дата установки тарифа")
        @Schema(
                description = "Дата начала действия тарифа",
                example = "2026-01-01"
        )
        @Pattern(regexp = "(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)",
                message = "Неверный формат даты установки тарифа, дата должна иметь формат: гггг-мм-дд")
        String implementationDate,

        @NotBlank(message = "Не указана ставка тарифа")
        @Schema(
                description = "Ставка тарифа, должна иметь денежный формат с разделителем .",
                example = "54.25"
        )
        @Pattern(regexp = "^\\d+\\.\\d{2}$",
                message = "Ставка тарифа имеет неверный формат," +
                        " десятичная часть числа должна иметь два знака и отделятся точкой")
        String rate
) {
}
