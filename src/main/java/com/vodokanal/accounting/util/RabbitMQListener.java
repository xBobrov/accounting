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
        Map<String, String> requestMap = mappingUtil.parseJsonToHashMap(request);
        String operation = requestMap.get("operation");

        log.info("Получен запрос из Телеграм: {}", request);

        if (operation.equals(Operation.START.getOperation())) {
            long chatID = Long.parseLong(requestMap.get("chatID"));

            return accountService.getAccountByTelegramID(chatID);
        } else if (operation.equals(Operation.BINDING_ID.getOperation())) {
            long chatID = Long.parseLong(requestMap.get("chatID"));
            String accountNumber = requestMap.get("accountNumber");

            return accountService.bindTelegramID(chatID, accountNumber);
        }

        return "";
    }
}
