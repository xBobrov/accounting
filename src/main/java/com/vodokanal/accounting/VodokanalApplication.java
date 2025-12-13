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

        databaseRepository.addService(serviceEntityList);
    }
}
