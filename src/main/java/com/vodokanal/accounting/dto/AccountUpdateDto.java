package com.vodokanal.accounting.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountUpdateDto(
        @NotBlank(message = "Не указан плательщик")
        String payer,

        boolean isActive
) {
}
