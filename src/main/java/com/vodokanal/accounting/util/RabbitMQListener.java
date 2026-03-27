package com.vodokanal.accounting.util;

import com.vodokanal.accounting.enums.Operation;
import com.vodokanal.accounting.service.AccountService;
import com.vodokanal.accounting.service.MeterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class RabbitMQListener {
    private final AccountService accountService;
    private final MeterService meterService;
    private final MappingUtil mappingUtil;

    private static final Logger log = LoggerFactory.getLogger(RabbitMQListener.class);

    public RabbitMQListener(AccountService accountService, MeterService meterService, MappingUtil mappingUtil) {
        this.accountService = accountService;
        this.meterService = meterService;
        this.mappingUtil = mappingUtil;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    @SendTo
    public String receiveMessage(String request) {
        log.info("Получен запрос из Телеграм: {}", request);

        Map<String, String> requestMap = mappingUtil.parseJsonToHashMap(request);
        String operation = requestMap.get("operation");
        long chatID = Long.parseLong(requestMap.get("chatID"));

        if (operation.equals(Operation.START.getOperation())) {
            return accountService.getAccountData(chatID);

        } else if (operation.equals(Operation.BIND_ID.getOperation())) {
            String accountNumber = requestMap.get("accountNumber");
            return accountService.bindTelegramID(chatID, accountNumber);

        } else if (operation.equals(Operation.EMAIL_INFO.getOperation())) {
            return accountService.getEmail(chatID);

        } else if (operation.equals(Operation.METER_INFO.getOperation())) {
            return meterService.getAllMetersData(chatID);

        } else if (operation.equals(Operation.CHANGE_EMAIL.getOperation())) {
            String email = requestMap.get("email");
            return accountService.changeEmail(chatID, email);

        } else if (operation.equals(Operation.METER_VALIDATION.getOperation())) {
            String meterNumber = requestMap.get("meterNumber");
            return meterService.getMeterData(chatID, meterNumber);

        } else if (operation.equals(Operation.READING_TRANSMIT.getOperation())) {
            String meterNumber = requestMap.get("meterNumber");
            String currentReading = requestMap.get("currentReading");
            String consumption = requestMap.get("consumption");

            return meterService.saveReading(chatID, meterNumber, currentReading, consumption);
        }

        return "";
    }
}
