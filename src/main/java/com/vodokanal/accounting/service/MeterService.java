package com.vodokanal.accounting.service;

import com.vodokanal.accounting.entity.MeterEntity;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.MappingUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MeterService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;

    public MeterService(DatabaseRepository databaseRepository, MappingUtil mappingUtil) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
    }

    public String addMeter(long chatID, String meterJson) {
        return databaseRepository.addMeter(chatID, meterJson);
    }
}


