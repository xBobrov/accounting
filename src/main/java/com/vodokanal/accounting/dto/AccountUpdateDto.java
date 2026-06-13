package com.vodokanal.accounting.dto;

public record AccountUpdateDto(
        String payer,
        boolean isActive
) {
}
