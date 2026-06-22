## Серверное приложение информационной системы ресурсоснабжающей организации (Pet-project)

Backend-составляющая информационной системы организации оказывающей услуги водоснабжения. Сервис осуществляет ведение учета личных счетов абонентов, прием и обработку показаний ИПУ (счетчиков), интеграцию с государственными реестрами, начисления оплаты услуг, формирование платежных документов. Взаимодействие с приложением со стороны абонентов осуществляется через Telegram-bot, являющийся частью общей информационной системы и представленый в репозитории https://github.com/xBobrov/customerbot. Проект разрабатывается в учебных целях для демонстрации навыков построения **микросервисной архитектуры** и взаимодействия с **СУБД PosgreSQL**.

## 🎯 Основные функции
*   **Управление учетными данными по REST API (CRUD)**: Внесение и изменение данных лицевых счетов абанентов, индивидуальных приборов учета, тарифов и нормативов.
*   **Интеграция с Telegram через брокер сообщений**: Обмен данными с пользовательским чат-ботом: прием показаний приборов учета, информация по состоянию лицевого счета и изменение реквизитов. 
*   **Регламентные расчеты начислений** Периодические расчеты начислений за потребленные услуги с использованием нативного SQL для обеспечения максимальной производительности.  
*   **Интеграция с ФГИС «Аршин»**: Автоматическая верификация проведения поверок ИПУ через HTTP-запросы к государственному реестру.
*   **Генерация документов**: Динамическое формирование квитанций в формате PDF на основе данных о потреблении и текущих тарифах.

## 🛠 Технологический стек
*   **Core:** Java 22, Spring Boot 3.4.
*   **Data Layer:** Hibernate (Native SessionFactory), PostgreSQL.
*   **Integration:** Spring WebClient / RestTemplate для внешних API.
*   **Reporting:** Библиотека для генерации PDF (JasperReports).
*   **Testing:** JUnit 5, Mockito.
*   **Deployment** Docker Compose

## 📊 Схема базы данных

![Схема базы данных](https://github.com/xBobrov/accounting/blob/master/assets/accountind_scheme.png)

<details>
<summary><ins>📌Описание таблиц базы данных (нажмите, чтобы раскрыть) 📌</ins></summary>

* **account** - лицевые счета (л/с) абонентов
  - number - номер л/с
  - address - адрес помещения
  - payer - ФИО плательщика
  - email - электронная почта
  - telegram_id - уникальный идентификатор аккаунта Telegram
  - resident-regd - количество зарегистрированных человек в жилом помещении
  - is_normed - начисляется ли оплата по нормативу потребления (или по показаниям ИПУ)
  - is_active - является ли лицевой счет действующим (оказываются услуги, производится начисление оплаты)
 
* **meter** - индивидуальные приборы учета (счетчики воды)
  - serial_number - заводской номер
  - verification_date - дата проведенной первичной/очередной поверки
  - valid_thru - дата следующей поверки
  - initial_value - начальное показание счетчика при вводе в эксплуатацию
 
* **reading** - показания индивидуальных приборов учета
  - date - дата принятия показания
  - value - показание
  - consumption - потребление услуги с даты предыдущего показания

* **calculation** - начисления
  - date - дата начисления
  - ammount - размер начисления

* **transaction** - движение денежных средств по лицевому счету
  - date - дата
  - sum - сумма

* **service** - услуги
  - name - наименование услуги (холодное водоснабжение/горячее водоснабжение/водоотведение)

* **tariff** - тарифы
  - impl_date - дата введения
  - rate - ставка руб за м³

 * **norm** - месячный норматив потребления 
   - impl_date - дата введения
   - rate - норматив м³ на человека

* **calc_method** - вид начисления
  - name - наименование вида начисления (по нормативу/по показаниям ИПУ)
</details>

## 🔌 Описание API

Проект задокументирован с помощью OpenAPI 3. Вы можете просмотреть структуру эндпоинтов, DTO-моделей и отправить тестовые запросы из браузера.
* Интерактивный UI: http://localhost:8080/swagger-ui.html
* Спецификация в формате JSON: http://localhost:8080/v3/api-docs

## 🚦 Запуск проекта

Данный проект является составной частью информационной системы наряду с crud-сервисом, представленным в отдельном репозитории: https://github.com/xBobrov/accounting.
Система спроектированна для запуска на платформе Docker и предполагает работу в связке с брокером сообщений RabbitMQ и базой данных PostgreSQL.

![Схема микросервисной архетиктуры](https://github.com/xBobrov/customerbot/blob/master/assets/project_scheme.png)

### Docker-compose для полной информационной системы

```yml
services:
  # База данных PostgreSQL
  postgres-db:
    image: postgres:16-alpine
    container_name: postgres_db
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
      POSTGRES_DB: vodokanal
    ports:
      - "5432:5432"
    volumes:
      - ./src/main/resources/db_backup.sql:/docker-entrypoint-initdb.d/01-init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user -d crud_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Очередь сообщений RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 5s
      timeout: 5s
      retries: 5

  # CRUD Сервис (Maven) (данный проект)
  accounting:
    build:
      context: ./accounting # Путь к папке с CRUD проектом
    container_name: accounting
    ports:
      - "8080:8080"
    depends_on:
      postgres-db:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-db:5432/vodokanal
      SPRING_RABBITMQ_HOST: rabbitmq

  # Telegram Бот (Gradle) (https://github.com/xBobrov/accounting)
  customerbot:
    build:
      context: ./customerbot # Путь к папке с Telegram-bot проектом 
    container_name: customerbot
    depends_on:
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_RABBITMQ_HOST: rabbitmq
      TELEGRAM_BOT_TOKEN: "***" # Токен Telegram
```

## 💾 Запуск с тестовой базой данных

В репозиторий включен дамп тестовой базы данных (`db_backup.sql`), содержащий демонстрационный набор данных.

### Вариант 1. Автоматический запуск (рекомендуемый)
Скрипт бэкапа автоматически монтируется в Docker-контейнер и разворачивается при первом запуске инфраструктуры:
```bash
# Очищаем старые тома (если они были) и поднимаем проект с демонстрационной БД
docker-compose down -v
docker-compose up --build
```

### Вариант 2. Ручное восстановление дампа
Если вы запускаете PostgreSQL локально (без Docker), вы можете накатить бэкап вручную через CLI:
```bash
psql -U postgres -d vodokanal -f src/main/resources/db_backup.sql
```

---
*Проект создается в учебных целях для демонстрации навыков Trainee/Junior Java Developer.*
