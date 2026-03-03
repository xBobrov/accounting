package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.MeterDto;
import com.vodokanal.accounting.dto.MeterUpdateDto;
import com.vodokanal.accounting.exception.DataNotFoundException;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.HttpRequestProducer;
import com.vodokanal.accounting.util.MappingUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MeterService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;
    private final HttpRequestProducer httpRequestProducer;

    @Value("${service.fgis.url}")
    private String fgisUrl;

    @Value("${service.fgis.params}")
    private String fgisParams;

    public MeterService(
            DatabaseRepository databaseRepository,
            MappingUtil mappingUtil,
            HttpRequestProducer httpRequestProducer) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
        this.httpRequestProducer = httpRequestProducer;
    }

    public MeterDto addMeter(MeterDto meterDto) {
        LocalDate validThru = getValidThruDate(meterDto.verificationDate(), meterDto.serialNumber());

        return databaseRepository.addMeter(meterDto, validThru);
    }

    private LocalDate getValidThruDate(String verificationDate, String serialNumber) {
        String fgisResponse = httpRequestProducer.get(fgisUrl + fgisParams.formatted(verificationDate, serialNumber));
        String validThruString = mappingUtil.parseFGISResponse(fgisResponse);

        if (validThruString.isEmpty()) {
            throw new DataNotFoundException("ИПУ не обнаружено во ФГИС \"Аршин\"");
        }

        return mappingUtil.parseLocalDate(validThruString);
    }

    public MeterUpdateDto updateMeter(MeterUpdateDto meterUpdateDto) {
        LocalDate validThru = getValidThruDate(meterUpdateDto.verificationDate(), meterUpdateDto.serialNumber());

        return databaseRepository.updateMeter(meterUpdateDto, validThru);
    }
}
