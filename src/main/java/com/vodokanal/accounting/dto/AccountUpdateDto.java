package com.vodokanal.accounting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AccountUpdateDto(
        @Schema(
                description = "Плательщик",
                example = "Сидоров В.В."
        )
        String payer,
        boolean isActive
) {
}
