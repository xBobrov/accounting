package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.exception.DataNotFoundException;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.MappingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing customer accounts and billing document generation.
 * <p>
 * This service coordinates account lifecycle operations, including registration,
 * telegram ID binding, and email updates. It also serves as the primary engine
 * for retrieving billing data and triggering PDF invoice creation.
 * </p>
 */
@Service
public class AccountService {
    private final DatabaseRepository databaseRepository;
    private final MappingUtil mappingUtil;
    private final DocumentService documentService;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public AccountService(DatabaseRepository databaseRepository, MappingUtil mappingUtil, DocumentService documentService) {
        this.databaseRepository = databaseRepository;
        this.mappingUtil = mappingUtil;
        this.documentService = documentService;
    }

    /**
     * Adds a single account.
     *
     * @param accountDto the account data transfer object.
     * @return the persisted {@link AccountDto}.
     */
    public AccountDto addAccount(AccountDto accountDto) {
        AccountEntity entity = mappingUtil.mapAccountDtoToEntity(accountDto);
        AccountEntity saved = databaseRepository.addAccount(entity);

        return mappingUtil.mapAccountEntityToDto(saved);
    }

    /**
     * Adds a list of accounts.
     *
     * @param accountDtoList list of accounts to be persisted.
     * @return list of persisted {@link AccountDto} objects.
     */
    public List<AccountDto> addAccountList(List<AccountDto> accountDtoList) {
        List<AccountEntity> entities = accountDtoList.stream()
                .map(mappingUtil::mapAccountDtoToEntity)
                .toList();

        List<AccountEntity> savedEntities = databaseRepository.addAccountList(entities);

        return savedEntities.stream()
                .map(mappingUtil::mapAccountEntityToDto)
                .toList();
    }

    /**
     * Updates an existing account's details identified by its database ID.
     *
     * @param id               the unique database identifier of the account.
     * @param accountUpdateDto DTO containing fields to be updated (e.g., payer name, status).
     * @return the updated {@link AccountUpdateDto}.
     */
    public AccountUpdateDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return databaseRepository.updateAccount(id, accountUpdateDto);
    }

    /**
     * Retrieves account information associated with a specific Telegram user in JSON format.
     *
     * @param chatID the unique Telegram chat identifier.
     * @return a JSON string containing account details for the bot's response.
     */
    public String getAccountData(long chatID) {
        return databaseRepository.getAccountData(chatID);
    }

    /**
     * Links a Telegram Chat ID to a utility account number.
     * <p>
     * This method establishes a persistent connection between the user's social
     * identifier and their billing record, enabling self-service features.
     * </p>
     *
     * @param chatID        the Telegram chat identifier.
     * @param accountNumber the utility account number (format: ####-###-#).
     * @return a JSON string with the updated account data or an empty string if not found.
     */
    public String bindTelegramID(long chatID, String accountNumber) {
        return databaseRepository.bindTelegramID(chatID, accountNumber);
    }

    /**
     * Updates the email address for a specific user.
     *
     * @param chatID the unique Telegram chat identifier.
     * @param email  the new email address (or "0" to remove existing binding).
     * @return the updated email string as confirmed by the database.
     */
    public String changeEmail(long chatID, String email) {
        return databaseRepository.changeEmail(chatID, email);
    }

    /**
     * Retrieves the currently linked email address for a user.
     *
     * @param chatID the unique Telegram chat identifier.
     * @return the email address string or an empty string if not linked.
     */
    public String getEmail(long chatID) {
        return databaseRepository.getEmail(chatID);
    }

    /**
     * Manages the billing document download process.
     *
     * @param request DTO containing account number and billing period.
     * @return a byte array representing the generated PDF invoice.
     * @throws DataNotFoundException if no billing data exists for the requested period.
     */
    public byte[] downloadBill(BillRequestDto request) {
        String accountNumber = request.number();
        LocalDate billPeriod = LocalDate.parse(request.period() + "-01");
        LocalDate readingDate = billPeriod.minusMonths(1);

        log.info("Generating bill for account {} and period {}", accountNumber, billPeriod);

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
            billMeterDtoList.add(new BillMeterDto(
                    "",
                    "",
                    "",
                    null,
                    null,
                    null));
        }

        return documentService.createBill(billCalculationDTOList, billMeterDtoList, billPeriod);
    }
}