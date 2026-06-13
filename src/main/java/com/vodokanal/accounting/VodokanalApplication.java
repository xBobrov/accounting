package com.vodokanal.accounting;

import com.vodokanal.accounting.entity.ServiceEntity;
import com.vodokanal.accounting.util.DatabaseRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class VodokanalApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(VodokanalApplication.class, args);
    }
}
