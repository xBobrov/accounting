package com.vodokanal.accounting.controller;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.dto.BillRequestDto;
import com.vodokanal.accounting.service.AccountService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing customer accounts and billing operations.
 * <p>
 * Provides endpoints for creating and updating account data
 * and generating billing documents in PDF format.
 * All endpoints are prefixed with {@code /api/v1/accounts}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Creates a new single customer account.
     *
     * @param accountDto Data Transfer Object containing account details.
     * @return {@link ResponseEntity} with the created {@link AccountDto} and HTTP 201 status.
     */
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody @Valid AccountDto accountDto) {
        log.info("Requested single account adding, account number: {}", accountDto.number());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.addAccount(accountDto));
    }

    /**
     * Performs a batch upload of multiple customer accounts.
     *
     * @param accountDtoList List of {@link AccountDto} objects to be created.
     * @return {@link ResponseEntity} with the list of created accounts and HTTP 201 status.
     */
    @PostMapping("/batch")
    public ResponseEntity<List<AccountDto>> addAccountList(@RequestBody @Valid List<AccountDto> accountDtoList) {
        log.info("Requested a batch of {} accounts adding", accountDtoList.size());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.addAccountList(accountDtoList));
    }

    /**
     * Updates specific fields of an existing account identified by its ID.
     *
     * @param id               Unique identifier of the account in the database.
     * @param accountUpdateDto DTO containing updated account information.
     * @return {@link ResponseEntity} with the updated data and HTTP 200 status.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AccountUpdateDto> updateAccount(@PathVariable long id,
                                                          @RequestBody @Valid AccountUpdateDto accountUpdateDto) {
        log.info("Requested account update, account id: {}", id);

        return ResponseEntity.status(HttpStatus.OK).body(accountService.updateAccount(id, accountUpdateDto));
    }

    /**
     * Generates a PDF bill for a specific account and period.
     *
     * @param billRequestDto DTO containing account number and billing period (format: YYYY-MM).
     * @return {@link ResponseEntity} containing the PDF file as a {@link Resource}.
     */
    @GetMapping("/bill")
    public ResponseEntity<Resource> downloadBill(@Valid BillRequestDto billRequestDto) {

        log.info("Requested a bill downloading, account number: {}, period: {}",
                billRequestDto.number(), billRequestDto.period());

        byte[] billPdf = accountService.downloadBill(billRequestDto);
        ByteArrayResource resource = new ByteArrayResource(billPdf);
        String filename = String.format("bill-%s-%s.pdf", billRequestDto.number(), billRequestDto.period());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(billPdf.length)
                .body(resource);
    }
}