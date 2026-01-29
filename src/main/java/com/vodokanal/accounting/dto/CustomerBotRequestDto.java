package com.vodokanal.accounting.dto;


import com.vodokanal.accounting.enums.Operation;

public record CustomerBotRequestDto(
        long chatID,
        String data,
        Operation operation
) {}
