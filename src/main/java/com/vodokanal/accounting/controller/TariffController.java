package com.vodokanal.accounting.controller;

import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.service.TariffService;

import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing services' tariffs.
 * <p>
 * Provides endpoints for setting tariff rates on a certain date.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tariffs")
public class TariffController {
    private final TariffService tariffService;
    private static final Logger log = LoggerFactory.getLogger(TariffController.class);

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    /**
     * Creates a new tariff for a specific service.
     *
     * @param tariffDto Data Transfer Object containing tariff details.
     * @return {@link ResponseEntity} with the created {@link TariffDto} and HTTP 201 status.
     */
    @PostMapping
    public ResponseEntity<TariffDto> addTariff(@RequestBody @Valid TariffDto tariffDto) {
        log.info("Requested tariff rate adding, service: {}", tariffDto.service());

        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.addTariff(tariffDto));
    }
}
