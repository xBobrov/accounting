package com.vodokanal.accounting.service;

import com.vodokanal.accounting.dto.MeterDto;
import com.vodokanal.accounting.exception.DataNotFoundException;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.HttpRequestProducer;
import com.vodokanal.accounting.util.MappingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeterServiceTest {

    @Mock
    private DatabaseRepository databaseRepository;
    @Mock
    private MappingUtil mappingUtil;
    @Mock
    private HttpRequestProducer httpRequestProducer;
    @InjectMocks
    private MeterService meterService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(meterService, "fgisUrl", "http://test-url");
        ReflectionTestUtils.setField(meterService, "fgisParams", "?date=%s&sn=%s");
    }

    @Test
    @DisplayName("POST /api/v1/meters: successful meter device adding")
    void shouldAddMeterSuccessfully() {
        // given
        MeterDto requestDto = new MeterDto(
                null,
                "544316",
                "2024-06-01",
                null,
                "36.130",
                1L,
                "1234-567-8"
        );

        String mockJsonResponse = "{ \"result\": \"ok\" }";
        String validThru = "2029-05-31";
        LocalDate expectedDate = LocalDate.of(2029, 5, 31);

        when(httpRequestProducer.get(anyString())).thenReturn(mockJsonResponse);
        when(mappingUtil.parseFGISResponse(mockJsonResponse)).thenReturn(validThru);
        when(mappingUtil.parseLocalDate(validThru)).thenReturn(expectedDate);
        when(databaseRepository.addMeter(requestDto, expectedDate)).thenReturn(requestDto);

        // when
        MeterDto result = meterService.addMeter(requestDto);

        // then
        assertNotNull(result);
        verify(httpRequestProducer).get(contains("544316"));
        verify(databaseRepository).addMeter(requestDto, expectedDate);
    }

    @Test
    @DisplayName("POST /api/v1/meters: exception throws if meter is not registered in the FGIS Arshin")
    void shouldThrowExceptionWhenMeterNotFoundInFGIS() {
        // given
        MeterDto requestDto = new MeterDto(
                null,
                "544316",
                "2024-06-01",
                null,
                "36.130",
                1L,
                "1234-567-8"
        );

        when(httpRequestProducer.get(anyString())).thenReturn("empty response");
        when(mappingUtil.parseFGISResponse(anyString())).thenReturn("");

        // when & then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () ->
                meterService.addMeter(requestDto)
        );

        assertTrue(exception.getMessage().contains("ИПУ не обнаружено во ФГИС \"Аршин\""));
        verify(databaseRepository, never()).addMeter(any(), any());
    }
}
