package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BillRequestDto(
        @NotBlank(message = "Не указан номер лицевого счета")
        @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String number,

        @NotNull(message = "Не указан период требуемого счета")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
                message = "Указаный период не соответствует формату: мм.гггг")
        String period
) {
}
