package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.entity.ServiceEntity;
import com.vodokanal.accounting.entity.TariffEntity;
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

    public AccountEntity mapAccountUpdate(AccountUpdateDto accountUpdateDto, AccountEntity accountEntity) {
        accountEntity.setPayer(accountUpdateDto.payer());
        accountEntity.setIsActive(accountUpdateDto.isActive());

        return accountEntity;
    }

    public TariffEntity mapTariffDtoEntity(TariffDto tariffDto, ServiceEntity serviceEntity) {
        TariffEntity tariffEntity = new TariffEntity();
        tariffEntity.setService(serviceEntity);
        tariffEntity.setImplementationDate(tariffDto.implementationDate());
        tariffEntity.setRate(tariffDto.rate());

        return tariffEntity;
    }

    public TariffDto mapTariffEntityDto(TariffEntity tariffEntity) {

        return new TariffDto(
                tariffEntity.getId(),
                tariffEntity.getService().getId(),
                tariffEntity.getImplementationDate(),
                tariffEntity.getRate()
        );
    }
}
