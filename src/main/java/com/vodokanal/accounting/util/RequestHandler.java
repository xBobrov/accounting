package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.List;

@RestController
public class RequestHandler {
    private final AccountService accountService;

    public RequestHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account/add/list")
    public ResponseEntity<List<AccountDto>> addAccountList(@RequestBody @Valid List<AccountDto> accountDtoList) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.addAccount(accountDtoList));
    }

    @PostMapping("/account/add/single")
    public ResponseEntity<AccountDto> addAccount(@RequestBody @Valid AccountDto accountDto) {
        List<AccountDto> accountDtoList = accountService.addAccount(List.of(accountDto));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountDtoList.getFirst());
    }

    @PatchMapping("/account/update/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable long id,
                                                       @RequestBody @Valid AccountUpdateDto accountUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.updateAccount(id, accountUpdateDto));
    }

}