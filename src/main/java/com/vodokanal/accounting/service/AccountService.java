package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.MappingUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;

    public AccountService(DatabaseRepository databaseRepository, MappingUtil mappingUtil) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
    }

    public List<AccountDto> addAccount(List<AccountDto> accountDtoList) {
        List<AccountEntity> accountEntityList = accountDtoList.stream()
                .map(mappingUtil::mapAccountDtoEntity)
                .toList();

        accountEntityList = databaseRepository.addAccount(accountEntityList);

        return accountEntityList.stream()
                .map(mappingUtil::mapAccountEntityDto)
                .toList();
    }

    public AccountDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return databaseRepository.updateAccount(id, accountUpdateDto);
    }
}

