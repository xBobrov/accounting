package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.MeterDto;
import com.vodokanal.accounting.dto.MeterUpdateDto;
import com.vodokanal.accounting.exception.DataNotFoundException;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.HttpRequestProducer;
import com.vodokanal.accounting.util.MappingUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for managing metering devices and their verification status.
 * <p>
 * This service integrates with the "FGIS Arshin" external system to
 * validate meter verification dates and retrieve legally binding
 * "valid until" dates for every device submission.
 * </p>
 */
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

    /**
     * Registers a new metering device in the billing system.
     * <p>
     * Before persisting, it requests the legal expiration date (validThru)
     * from the federal registry based on the provided verification date.
     * </p>
     *
     * @param meterDto the data transfer object containing meter information.
     * @return the persisted {@link MeterDto} including the system-calculated expiration date.
     */
    public MeterDto addMeter(MeterDto meterDto) {
        LocalDate validThru = getValidThruDate(meterDto.verificationDate(), meterDto.serialNumber());

        return databaseRepository.addMeter(meterDto, validThru);
    }

    /**
     * Synchronizes verification data with the "FGIS Arshin" federal registry.
     *
     * @param verificationDate the date the meter was serviced.
     * @param serialNumber     the physical serial number of the device.
     * @return a {@link LocalDate} representing the new legal expiration date.
     * @throws DataNotFoundException if the device record is missing from the federal registry.
     */
    private LocalDate getValidThruDate(String verificationDate, String serialNumber) {
        String fgisResponse = httpRequestProducer.get(fgisUrl + fgisParams.formatted(verificationDate, serialNumber));
        String validThruString = mappingUtil.parseFGISResponse(fgisResponse);

        if (validThruString.isEmpty()) {
            throw new DataNotFoundException("ИПУ не обнаружено во ФГИС \"Аршин\"");
        }

        return mappingUtil.parseLocalDate(validThruString);
    }

    /**
     * Updates an existing meter's verification details.
     * <p>
     * Re-validates the verification status through "FGIS Arshin" and
     * updates the device's eligibility period.
     * </p>
     *
     * @param meterUpdateDto DTO containing updated verification date and identifiers.
     * @return the updated {@link MeterUpdateDto}.
     */
    public MeterUpdateDto updateMeter(MeterUpdateDto meterUpdateDto) {
        LocalDate validThru = getValidThruDate(meterUpdateDto.verificationDate(), meterUpdateDto.serialNumber());

        return databaseRepository.updateMeter(meterUpdateDto, validThru);
    }

    /**
     * Retrieves all meters associated with a specific Telegram user.
     *
     * @param chatID the unique Telegram chat identifier.
     * @return a JSON string containing a list of all assigned meters and their metadata.
     */
    public String getAllMetersData(long chatID) {
        return databaseRepository.getAllMetersData(chatID);
    }

    /**
     * Retrieves detailed information for a specific meter.
     *
     * @param chatID      the Telegram chat identifier for ownership verification.
     * @param meterNumber the serial number of the meter.
     * @return a JSON string with the meter's current status and last readings.
     */
    public String getMeterData(long chatID, String meterNumber) {
        return databaseRepository.getMetersData(chatID, meterNumber);
    }

    /**
     * Persists a new consumption reading for a specific meter.
     * <p>
     * Converts raw string input into {@link BigDecimal} and associates the reading
     * with the first day of the current month to maintain billing consistency.
     * </p>
     *
     * @param chatID         the Telegram chat identifier.
     * @param meterNumber    the serial number of the meter.
     * @param currentReading the current value displayed on the meter.
     * @param consumption    the calculated consumption for the period.
     * @return a string representation of the rows affected in the database (usually "1" for success).
     */
    public String saveReading(long chatID, String meterNumber, String currentReading, String consumption) {
        BigDecimal currentReadingBigDecimal = new BigDecimal(currentReading);
        BigDecimal consumptionBigDecimal = new BigDecimal(consumption);

        return databaseRepository.saveReading(
                chatID,
                meterNumber,
                currentReadingBigDecimal,
                consumptionBigDecimal,
                createDate());
    }

    /**
     * Generates a billing reference date.
     *
     * @return the first day of the current month.
     */
    private LocalDate createDate() {
        LocalDate date = LocalDate.now();
        return date.withDayOfMonth(1);
    }
}
