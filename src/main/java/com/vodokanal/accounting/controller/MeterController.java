package com.vodokanal.accounting.controller;

import com.vodokanal.accounting.dto.MeterDto;
import com.vodokanal.accounting.dto.MeterUpdateDto;
import com.vodokanal.accounting.service.MeterService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for metering devices adding and updating.
 *
 */
@RestController
@RequestMapping("/api/v1/meters")
public class MeterController {
    private final MeterService meterService;

    private static final Logger log = LoggerFactory.getLogger(MeterController.class);

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    /**
     * Create a new metering device
     *
     * @param meterDto Data Transfer Object containing meter details.
     * @return {@link ResponseEntity} with the created {@link MeterDto} and HTTP 201 status.
     */
    @PostMapping
    public ResponseEntity<MeterDto> addMeter(@RequestBody @Valid MeterDto meterDto) {
        log.info("Requested metering device adding, serial number: {}", meterDto.serialNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.addMeter(meterDto));
    }

    /**
     * * Updates metering device verification date.
     *
     * @param meterUpdateDto DTO containing meter details and a new verification date
     * @return @link ResponseEntity} with the updated data and HTTP 200 status.
     */
    @PatchMapping
    public ResponseEntity<MeterUpdateDto> updateMeter(@RequestBody @Valid MeterUpdateDto meterUpdateDto) {
        log.info("Requested metering device updating, serial number: {}", meterUpdateDto.serialNumber());

        return ResponseEntity.status(HttpStatus.OK).body(meterService.updateMeter(meterUpdateDto));
    }
}
