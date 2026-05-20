package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.service.AccountService;
import com.vodokanal.accounting.service.MeterService;
import com.vodokanal.accounting.service.TariffService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
public class HttpRequestHandler {
    private final AccountService accountService;
    private final TariffService tariffService;
    private final MeterService meterService;

    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    public HttpRequestHandler(AccountService accountService, TariffService tariffService, MeterService meterService) {
        this.accountService = accountService;
        this.tariffService = tariffService;
        this.meterService = meterService;
    }

    @PostMapping("/account/add/list")
    public ResponseEntity<List<AccountDto>> addAccountList(@RequestBody @Valid List<AccountDto> accountDtoList) {
        log.info("Вызван метод addAccountList с телом запроса: {}", accountDtoList);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.addAccount(accountDtoList));
    }

    @PostMapping("/account/add/single")
    public ResponseEntity<AccountDto> addAccount(@RequestBody @Valid AccountDto accountDto) {
        log.info("Вызван метод addAccount с телом запроса: {}", accountDto);

        List<AccountDto> accountDtoList = accountService.addAccount(List.of(accountDto));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountDtoList.getFirst());
    }

    @PatchMapping("/account/update/{id}")
    public ResponseEntity<AccountUpdateDto> updateAccount(@PathVariable long id,
                                                          @RequestBody @Valid AccountUpdateDto accountUpdateDto) {
        log.info("Вызван метод updateAccount с телом запроса: {}", accountUpdateDto);

        return ResponseEntity.status(HttpStatus.OK).body(accountService.updateAccount(id, accountUpdateDto));
    }

    @GetMapping("/account/download")
    public ResponseEntity<Resource> downloadBill(@Valid BillRequestDto request) {

        byte[] billPdf =  accountService.downloadBill(request);
        ByteArrayResource resource = new ByteArrayResource(billPdf);
        String filename = String.format("bill-%s-%s.pdf", request.number(), request.period());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(billPdf.length)
                .body(resource);
    }

    @PostMapping("/tariff/add")
    public ResponseEntity<TariffDto> addTariff(@RequestBody @Valid TariffDto tariffDto) {
        log.info("Вызван метод addTariff с телом запроса: {}", tariffDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.addTariff(tariffDto));
    }

    @PostMapping("/meter/add")
    public ResponseEntity<MeterDto> addMetter(@RequestBody @Valid MeterDto meterDto) {
        log.info("Вызван метод addMeter с телом запроса: {}", meterDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.addMeter(meterDto));
    }

    @PostMapping("/meter/update")
    public ResponseEntity<MeterUpdateDto> updateMetter(@RequestBody @Valid MeterUpdateDto meterUpdateDto) {
        log.info("Вызван метод updateMetter с телом запроса: {}", meterUpdateDto);

        return ResponseEntity.status(HttpStatus.OK).body(meterService.updateMeter(meterUpdateDto));
    }

    @GetMapping
    public LocalDate test(){
        return LocalDate.now();
    }

}