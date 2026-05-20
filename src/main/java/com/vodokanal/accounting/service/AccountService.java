package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.exception.DataNotFoundException;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.MappingUtil;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;
    private final DocumentService documentService;

    public AccountService(DatabaseRepository databaseRepository, MappingUtil mappingUtil, DocumentService documentService) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
        this.documentService = documentService;
    }

    public List<AccountDto> addAccount(List<AccountDto> accountDtoList) {
        List<AccountEntity> accountEntityList = accountDtoList.stream()
                .map(mappingUtil::mapAccountDtoToEntity)
                .toList();

        accountEntityList = databaseRepository.addAccount(accountEntityList);

        return accountEntityList.stream()
                .map(mappingUtil::mapAccountEntityToDto)
                .toList();
    }

    public AccountUpdateDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return databaseRepository.updateAccount(id, accountUpdateDto);
    }

    public String getAccountData(long chatID) {
        return databaseRepository.getAccountData(chatID);
    }

    public String bindTelegramID(long chatID, String accountNumber) {
        return databaseRepository.bindTelegramID(chatID, accountNumber);
    }

    public String changeEmail(long chatID, String email) {
        return databaseRepository.changeEmail(chatID, email);
    }

    public String getEmail(long chatID) {
        return databaseRepository.getEmail(chatID);
    }

    public byte[] downloadBill(BillRequestDto request) {
        String accountNumber = request.number();
        LocalDate billPeriod = LocalDate.parse(request.period() + "-01");
        LocalDate readingDate = billPeriod.minusMonths(1);

        List<BillCalculationDTO> billCalculationDTOList = databaseRepository.getBillCalculation(
                accountNumber,
                billPeriod,
                readingDate);

        if (billCalculationDTOList.isEmpty()) {
            throw new DataNotFoundException("Для лицевого счета %s за период %s счета на оплату не найдено"
                    .formatted(accountNumber, request.period()));
        }

        List<BillMeterDto> billMeterDtoList;

        if (!billCalculationDTOList.getFirst().isNormed()) {
            billMeterDtoList = databaseRepository.getBillMeter(accountNumber, readingDate);
        } else {
            billMeterDtoList = new ArrayList<>();
            billMeterDtoList.add(new BillMeterDto("", "", "", null, null, null));
        }

        return documentService.createBill(billCalculationDTOList, billMeterDtoList, billPeriod);
    }
}

