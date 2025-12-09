package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.List;

@RestController
public class RequestHandler {
    private final AccountService accountService;

    public RequestHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account/add/list")
    public ResponseEntity<List<AccountDto>> addAccountList(@RequestBody @Valid List<AccountDto> accountList) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountList);
    }

    @PostMapping("/account/add/single")
    public ResponseEntity<AccountDto> addAccount(@RequestBody @Valid AccountDto accountDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.addAccount(accountDto));
    }

    @GetMapping()
    public String idleEcho() throws SQLException {
       return "baba" + "kuka" + "posa";
    }

    private String testQuery() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/vodokanal";
        String user = "postgres";
        String password = "admin";

        Connection connection = DriverManager.getConnection(url, user, password);
        Statement statement = connection.createStatement();

        String sql = "SELECT * FROM service";
        ResultSet resultSet = statement.executeQuery(sql);

        String response = "";

        while (resultSet.next()) {
            response += resultSet.getString("service") + "\n";
        }

        return response;
    }
}