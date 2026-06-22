package com.vodokanal.accounting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BillRequestDto(
        @NotBlank(message = "Не указан номер лицевого счета")
        @Schema(
                description = "Номер лицевого счета",
                example = "9876-963-1"
        )
        @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String number,

        @NotNull(message = "Не указан период за который требуется квитанция")
        @Schema(
                description = "Период за который требуется квитанция",
                example = "2026-04-01"
        )
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
                message = "Указанный период не соответствует формату: ГГГГ-ММ")
        String period
) {
}
