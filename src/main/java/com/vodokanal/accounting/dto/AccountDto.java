package com.vodokanal.accounting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

public record AccountDto(
        @Null(message = "Индитификационный номер не должен быть указан")
        @Schema(
                description = "Уникальный идентификатор записи в базе данных (генерируется автоматически, не заполнять при создании)",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "1"
        )
        Long id,

        @NotBlank(message = "Не указан номер лицевого счета")
        @Schema(
                description = "Номер лицевого счета",
                example = "9876-963-1"
        )
        @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String number,

        @NotBlank(message = "Не указан адрес")
        @Schema(
                description = "Адрес помещения",
                example = "612270б г.Орлов, ул.Кирова, д.14, кв.1"
        )
        String address,

        @NotBlank(message = "Не указан плательщик")
        @Schema(
                description = "Плательщик",
                example = "Сидоров В.В."
        )
        String payer,

        @Schema(
                description = "Количество человек зарегистрированных в жилом помещении",
                example = "2"
        )
        @Min(value = 0, message = "Количество зарегистрированных не может быть меньше 0")
        int residentRegd
) {
}
