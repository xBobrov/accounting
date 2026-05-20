package com.vodokanal.accounting.service;

import com.vodokanal.accounting.util.DatabaseRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@EnableScheduling
public class SchedulerService {
    private final DatabaseRepository databaseRepository;

    public SchedulerService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @Scheduled(cron = "0 0 0 1 * ?") //"0 0 0 1 * ?" //"0 * * * * *"
    public void fulfillCalculation() {
        LocalDate readingDate = LocalDate.now().minusMonths(1);
        databaseRepository.fulfillCalculation(readingDate);
    }
}
