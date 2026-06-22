package com.vodokanal.accounting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for updating metering devices verification date after a successful verification.
 *
 * @param serialNumber     The serial number of the metering device.
 * @param verificationDate The date when the meter was last verified (format: YYYY-MM-DD).
 * @param validThru        The expiration date of the current verification.
 *                         Must be {@code null} in requests as it is calculated by the server.
 * @param accountNumber    The account number the meter is assigned to.
 */
public record MeterUpdateDto(
        @NotBlank(message = "Не указан номер ИПУ")
        @Schema(
                description = "Заводской номер прибора учета",
                example = "5174263"
        )
        String serialNumber,

        @NotBlank(message = "Не указана дата поверки ИПУ")
        @Schema(
                description = "Дата предыдущей поверки прибора учета",
                example = "2024-05-13"
        )
        @Pattern(regexp = "(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)",
                message = "Дата поверки ИПУ должна иметь формат: гггг-мм-дд")
        String verificationDate,

        @Null(message = "Дата очередной поверки не должна быть указана")
        @Schema(
                description = "Дата очередной поверки (не должена быть указана, запрашивается из ФГИС Аршин",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "2029-05-13"
        )
        String validThru,

        @NotNull(message = "Не указан номер лицевого счета") @Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        @Schema(
                description = "Номер лицевого счета",
                example = "9876-963-1"
        )
        String accountNumber
) {

}
