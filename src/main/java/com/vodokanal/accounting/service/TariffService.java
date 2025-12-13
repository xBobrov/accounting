package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.util.DatabaseRepository;
import org.springframework.stereotype.Service;

@Service
public class TariffService {
    private final DatabaseRepository databaseRepository;

    public TariffService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public TariffDto addTariff(TariffDto tariffDto) {
       return databaseRepository.addTariff(tariffDto);
    }
}
