package com.vodokanal.accounting;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import com.vodokanal.accounting.dto.Tmp;
import com.vodokanal.accounting.entity.ServiceEntity;
import com.vodokanal.accounting.util.DatabaseRepository;
import com.vodokanal.accounting.util.QRCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class VodokanalApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(VodokanalApplication.class, args);

        //добавление услуг в базу данных
        List<ServiceEntity> serviceEntityList = new ArrayList<>();

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setName("Холодное водоснабжение");
        serviceEntityList.add(serviceEntity);

        serviceEntity = new ServiceEntity();
        serviceEntity.setName("Горячее водоснабжение");
        serviceEntityList.add(serviceEntity);

        serviceEntity = new ServiceEntity();
        serviceEntity.setName("Водоотведенее");
        serviceEntityList.add(serviceEntity);

        DatabaseRepository databaseRepository = context.getBean(DatabaseRepository.class);
        //  databaseRepository.addService(serviceEntityList);
        //String s = databaseRepository.getMeterData(689050626);
       // System.out.println(databaseRepository.testUpdate());
    }
}
