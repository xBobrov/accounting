package com.vodokanal.accounting.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.entity.ServiceEntity;
import com.vodokanal.accounting.entity.TariffEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class MappingUtil {
    private final ObjectMapper objectMapper;

    public MappingUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // dto to entity
    public AccountEntity mapAccountDtoToEntity(AccountDto accountDto) {
        AccountEntity accountEntity = new AccountEntity();

        accountEntity.setNumber(accountDto.number());
        accountEntity.setAddress(accountDto.address());
        accountEntity.setPayer(accountDto.payer());

        return accountEntity;
    }

    // entity to dto
    public AccountDto mapAccountEntityToDto(AccountEntity accountEntity) {

        return new AccountDto(
                accountEntity.getId(),
                accountEntity.getNumber(),
                accountEntity.getAddress(),
                accountEntity.getPayer()
        );
    }

    public AccountEntity mapAccountToUpdate(AccountUpdateDto accountUpdateDto, AccountEntity accountEntity) {
        accountEntity.setPayer(accountUpdateDto.payer());
        accountEntity.setIsActive(accountUpdateDto.isActive());

        return accountEntity;
    }

    public TariffEntity mapTariffDtoToEntity(TariffDto tariffDto, ServiceEntity serviceEntity) {
        TariffEntity tariffEntity = new TariffEntity();
        tariffEntity.setService(serviceEntity);
        tariffEntity.setImplementationDate(tariffDto.implementationDate());
        tariffEntity.setRate(tariffDto.rate());

        return tariffEntity;
    }

    public TariffDto mapTariffEntityToDto(TariffEntity tariffEntity) {

        return new TariffDto(
                tariffEntity.getId(),
                tariffEntity.getService().getId(),
                tariffEntity.getImplementationDate(),
                tariffEntity.getRate()
        );
    }

    public CustomerBotRequestDto mapJsonToRequestDto(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, CustomerBotRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String mapResponseDtoToJson(CustomerBotResponseDto responseDto) {
        try {
            return objectMapper.writeValueAsString(responseDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String mapObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T parseJsonToObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, String> parseJsonToHashMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<HashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
