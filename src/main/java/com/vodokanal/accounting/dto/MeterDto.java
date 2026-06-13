package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for registering a new metering device.
 * <p>
 * This DTO encapsulates all necessary information required to link a physical
 * device to a customer account, including initial readings and service types.
 * It enforces strict validation rules for dates and numeric formats.
 * </p>
 *
 * @param id               The unique technical identifier. Must be {@code null} during creation.
 * @param serialNumber     The physical serial number of the device.
 * @param verificationDate The date of the last official verification (format: YYYY-MM-DD).
 * @param validThru        The calculated expiration date. Must be {@code null} in requests
 *                         as it is retrieved from the federal registry (FGIS Arshin).
 * @param initialValue     The starting reading value at the moment of installation
 *                         (supports up to 3 decimal places).
 * @param service          The unique identifier of the utility service (e.g., Cold Water, Heating).
 * @param accountNumber    The utility account number the meter belongs to.
 */
public record MeterDto(
        @Null(message = "Идентификатор записи не должен быть указан")
        Long id,

        @NotBlank(message = "Не указан номер ИПУ")
        String serialNumber,

        @NotBlank(message = "Не указана дата поверки ИПУ")
        @Pattern(regexp = "(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)",
                message = "Дата поверки ИПУ должна иметь формат: гггг-мм-дд")
        String verificationDate,

        @Null(message = "Дата очередной поверки не должена быть указана")
        String validThru,

        @NotNull(message = "Не указано текущее показание ИПУ")
        @Pattern(regexp = "^\\d+([.]\\d{1,3})?$",
                message = "Текущее показание имеет неверный формат," +
                        " десятичная часть числа должна иметь не более трех знаков и отделяться точкой")
        String initialValue,

        @NotNull(message = "Не указан идентификатор услуги")
        Long service,

        @NotNull(message = "Не указан номер лицевого счета")@Pattern(regexp = "\\d{4}-\\d{3}-\\d",
                message = "Номер лицевого счета не соответствует установленному формату")
        String accountNumber
) {
}
