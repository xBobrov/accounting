package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.MappingUtil;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;

    public AccountService(DatabaseRepository databaseRepository, MappingUtil mappingUtil) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
    }

    public AccountDto addAccount(AccountDto accountDto) {
        AccountEntity accountEntity = mappingUtil.mapAccountDtoEntity(accountDto);
        accountEntity = databaseRepository.addAccount(accountEntity);

        return mappingUtil.mapAccountEntityDto(accountEntity);
    }
}

