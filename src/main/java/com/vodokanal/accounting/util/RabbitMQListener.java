package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.CustomerBotRequestDto;
import com.vodokanal.accounting.dto.CustomerBotResponseDto;
import com.vodokanal.accounting.enums.Operation;
import com.vodokanal.accounting.service.AccountService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;


@Component
public class RabbitMQListener {
    private final AccountService accountService;
    private final MappingUtil mappingUtil;

    public RabbitMQListener(AccountService accountService, MappingUtil mappingUtil) {
        this.accountService = accountService;
        this.mappingUtil = mappingUtil;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    @SendTo
    public String receiveMessage(String request) {
        CustomerBotRequestDto customerBotRequestDto = mappingUtil.mapJsonToRequestDto(request);
        long chatID = customerBotRequestDto.chatID();
        String data = customerBotRequestDto.data();
        Operation operation = customerBotRequestDto.operation();

        if (operation == Operation.SIGNIN) {

            return mappingUtil.mapResponseDtoToJson(new CustomerBotResponseDto(
                    accountService.isUserSignedUp(chatID)));
        } else if (operation == Operation.SIGNUP) {


            return mappingUtil.mapResponseDtoToJson(new CustomerBotResponseDto(
                    accountService.bindTelegramID(chatID, data)));
        }

        return null;
    }
}
