package com.vodokanal.accounting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("POST /api/v1/accounts: successful creation of a single account")
    void shouldCreateSingleAccount() throws Exception {
        AccountDto requestDto = new AccountDto(
                null,
                "1234-567-8",
                "ул. Ленина, 1",
                "Иванов И.И.",
                2
        );

        AccountDto responseDto = new AccountDto(
                1L,
                "1234-567-8",
                "ул. Ленина, 1",
                "Иванов И.И.",
                2);

        when(accountService.addAccount(any(AccountDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("1234-567-8"))
                .andExpect(jsonPath("$.address").value("ул. Ленина, 1"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts: error at incorrect account number")
    void shouldReturn400WhenAccountNumberIsWrong() throws Exception {
        AccountDto invalidDto = new AccountDto(
                null,
                "123-567-8",
                "ул. Ленина, 1",
                "Иванов И.И.",
                2
        );

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{id}: successful update of account")
    void shouldUpdateAccount() throws Exception {
        long id = 1L;
        AccountUpdateDto updateDto = new AccountUpdateDto(
                "Петров П.П.",
                false);

        when(accountService.updateAccount(eq(id), any(AccountUpdateDto.class))).thenReturn(updateDto);

        mockMvc.perform(patch("/api/v1/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payer").value("Петров П.П."))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/bil: successful bill downloading")
    void shouldDownloadBill() throws Exception {
        String number = "1234-567-8";
        String period = "2026-05";
        byte[] mockPdf = new byte[]{1, 2, 3, 4};

        when(accountService.downloadBill(any())).thenReturn(mockPdf);

        mockMvc.perform(get("/api/v1/accounts/bill")
                        .param("number", number)
                        .param("period", period))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"bill-1234-567-8-2026-05.pdf\""))
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/bill: error at incorrect period format")
    void shouldReturn400WhenPeriodFormatIsWrong() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/bill")
                        .param("number", "1234-567-8")
                        .param("period", "12.2023"))
                .andExpect(status().isBadRequest());
    }
}