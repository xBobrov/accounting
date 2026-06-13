package com.vodokanal.accounting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.service.TariffService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TariffController.class)
public class TariffControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TariffService tariffService;

    @Test
    @DisplayName("POST /api/v1/tariffs: new tariff successful creation")
    void shouldCreateTariff() throws Exception {
        TariffDto requestDto = new TariffDto(
                null,
                1L,
                "2026-05-01",
                "15.50"
        );

        TariffDto responseDto = new TariffDto(
                1L,
                1L,
                "2026-05-01",
                "15.50"
        );

        when(tariffService.addTariff(any(TariffDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.implementationDate").value("2026-05-01"))
                .andExpect(jsonPath("$.rate").value("15.50"));
    }

    @Test
    @DisplayName("POST /api/v1/tariffs: error at incorrect implementation date format")
    void shouldReturn400WhenImplementationDateIsInvalid() throws Exception {
        TariffDto invalidDto = new TariffDto(
                null,
                1L,
                "2026.05.01",
                "15.50"
        );

        mockMvc.perform(post("/api/v1/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
