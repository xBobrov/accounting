package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

public record MeterUpdateDto(
        @NotBlank(message = "Не указан номер ИПУ")
        String serialNumber,

        @NotBlank(message = "Не указана дата поверки ИПУ")
        @Pattern(regexp = "(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)",
                message = "Дата поверки ИПУ должна иметь формат: гггг-мм-дд")
        String verificationDate,

        @Null(message = "Дата очередной поверки не должена быть указана")
        String validThru,

        @NotNull(message = "Не указан номер лицевого счета") @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String accountNumber
) {

}
