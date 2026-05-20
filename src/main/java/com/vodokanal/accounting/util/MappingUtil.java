package com.vodokanal.accounting.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.entity.MeterEntity;
import com.vodokanal.accounting.entity.TariffEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                accountEntity.getPayer(),
                accountEntity.getResidentRegd()
        );
    }

    public AccountEntity mapAccountToUpdate(AccountUpdateDto accountUpdateDto, AccountEntity accountEntity) {
        accountEntity.setPayer(accountUpdateDto.payer());
        accountEntity.setIsActive(accountUpdateDto.isActive());

        return accountEntity;
    }

    public TariffEntity mapTariffDtoToEntity(TariffDto tariffDto) {
        TariffEntity tariffEntity = new TariffEntity();
        tariffEntity.setImplementationDate(LocalDate.parse(tariffDto.implementationDate()));
        tariffEntity.setRate(new BigDecimal(tariffDto.rate()));

        return tariffEntity;
    }

    public TariffDto mapTariffEntityToDto(TariffEntity tariffEntity) {
        return new TariffDto(
                tariffEntity.getId(),
                tariffEntity.getService().getId(),
                tariffEntity.getImplementationDate().toString(),
                tariffEntity.getRate().toString()
        );
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

    public MeterEntity mapMeterDtoToEntity(MeterDto meterDto) {
        MeterEntity meterEntity = new MeterEntity();
        meterEntity.setSerialNumber(meterDto.serialNumber());
        meterEntity.setInitialValue(new BigDecimal(meterDto.initialValue()));
        meterEntity.setVerificationDate(LocalDate.parse(meterDto.verificationDate()));

        return meterEntity;
    }

    public MeterDto mapMeterEntityToDto(MeterEntity meterEntity) {
        return new MeterDto(
                meterEntity.getId(),
                meterEntity.getSerialNumber(),
                meterEntity.getVerificationDate().toString(),
                meterEntity.getValidThru().toString(),
                meterEntity.getInitialValue().toString(),
                meterEntity.getService().getId(),
                meterEntity.getAccount().getNumber()

        );
    }

    public String parseFGISResponse(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return rootNode.path("result").path("items").path(0).path("valid_date").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDate parseLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(date, formatter);
    }

    public MeterUpdateDto mapMeterEntityToUpdateDto(MeterEntity meterEntity) {
        return new MeterUpdateDto(
                meterEntity.getSerialNumber(),
                meterEntity.getVerificationDate().toString(),
                meterEntity.getValidThru().toString(),
                meterEntity.getAccount().getNumber()
        );
    }
}
