package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.entity.AccountEntity;
import org.springframework.stereotype.Service;

@Service
public class MappingUtil {

    // dto to entity
    public AccountEntity mapAccountDtoEntity(AccountDto accountDto) {
        AccountEntity accountEntity = new AccountEntity();

        accountEntity.setNumber(accountDto.number());
        accountEntity.setAddress(accountDto.address());
        accountEntity.setPayer(accountDto.payer());

        return accountEntity;
    }

    // entity to dto
    public AccountDto mapAccountEntityDto(AccountEntity accountEntity) {

        return new AccountDto(
                accountEntity.getId(),
                accountEntity.getNumber(),
                accountEntity.getAddress(),
                accountEntity.getPayer()
        );
    }
}
