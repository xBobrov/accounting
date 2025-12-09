package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

public record AccountDto(
        @Null(message = "Индитификационный номер не должен быть указан")
        Long id,

        @NotBlank(message = "Не указан номер лицевого счета")
        @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String number,

        @NotBlank(message = "Не указан адрес")
        String address,

        @NotBlank(message = "Не указан плательщик")
        String payer
) {
}
