# Лотерейный бэкенд

Бэкенд-система для проведения лотерейных тиражей с покупкой билетов и mock-оплатой.  
Реализован **Сценарий 2: Лотерея с оплатой** согласно учебному кейсу.

## Функциональность

- **Аутентификация и авторизация** (JWT, роли `USER` и `ADMIN`).
- **Управление тиражами** (создание, завершение) — только для `ADMIN`.
- **Покупка лотерейных билетов** пользователями.
- **Mock-оплата билетов** с фиксацией успешного/неуспешного результата.
- **Генерация выигрышной комбинации** при завершении тиража.
- **Автоматическое определение выигрыша** и обновление статусов билетов (`WIN` / `LOSE`).
- **Просмотр истории билетов и результатов** для авторизованных пользователей.

## Технологический стек

- **Java 17**
- **Jetty 11** (встроенный HTTP-сервер, без Spring)
- **PostgreSQL 15**
- **HikariCP** (пул соединений)
- **JWT** (io.jsonwebtoken)
- **BCrypt** (хеширование паролей)
- **Jackson** (JSON-сериализация)
- **Maven** (сборка)
- **Docker / docker-compose** (контейнеризация)

## ER-диаграммма
```mermaid
erDiagram
    USERS ||--o{ DRAWS : creates
    USERS ||--o{ TICKETS : buys
    DRAWS ||--o{ TICKETS : contains
    TICKETS ||--o{ PAYMENTS : has

    USERS {
        BIGINT id PK
        VARCHAR username
        VARCHAR password_hash
        VARCHAR role
        TIMESTAMP created_at
    }

    DRAWS {
        BIGINT id PK
        VARCHAR name
        DECIMAL ticket_price
        TIMESTAMP start_time
        TIMESTAMP end_time
        VARCHAR status
        VARCHAR winning_combination
        BIGINT created_by FK
        TIMESTAMP created_at
    }

    TICKETS {
        BIGINT id PK
        BIGINT draw_id FK
        BIGINT user_id FK
        VARCHAR ticket_number
        VARCHAR combination
        VARCHAR status
        TIMESTAMP created_at
    }

    PAYMENTS {
        BIGINT id PK
        BIGINT ticket_id FK
        DECIMAL amount
        VARCHAR status
        TIMESTAMP payment_time
        VARCHAR external_id
        TIMESTAMP created_at
    }
```

## Запуск приложения

Выполните команду в корне проекта:
   ```bash
   docker-compose up -d
   ```
Сервер запустится на `http://localhost:8080`.

База данных PostgreSQL инициализируется автоматически.

Swagger будет доступен  на `http://localhost:8080/swagger.html`

API приложения описано в openapi.yaml

![img.png](img/swagger.png)

## 🧪 Полный сценарий тестирования

### 1. Вход под администратором (используйте заранее созданного admin/admin123)
![img.png](img/img.png)

### 2. Создание тиража (ADMIN)
![img_1.png](img/img_1.png)
После выполнения метода будет создан тираж в статусе CREATED и 30 билетов в статусе AVAILABLE.

### 3. Регистрация пользователя
![img_2.png](img/img_2.png)

### 4. Вход обычным пользователем
![img_3.png](img/img_3.png)

### 5. Получение списка доступных тиражей
![img_4.png](img/img_4.png)

### 5. Резервирование билета за пользователем
![img_5.png](img/img_5.png)
После выполнения метода билет перейдет в статус RESERVED.

### 6. Оплета билета
![img_6.png](img/img_6.png)
При передаче success = true/false билет перейдет в статус PAID/CANCELLED соответственно.

### 7. Завершение тиража (ADMIN)
![img_7.png](img/img_7.png)

### 8. Проверка результата билетов
![img_8.png](img/img_8.png)
В данной реализации билет считается выигрышным,
если хотя бы одно число из его комбинации оказалось в выигрышной комбинации.

Проверим корректность определения выигрыша, получив информацию о тираже
![img_9.png](img/img_9.png)

### 9. Просмотр истории тиражей
![img_10.png](img/img_10.png)

## 📝 Примечания по реализации

- Архитектура разделена на слои: **controller → service → repository**.
- Для работы с БД используется чистый **JDBC** без ORM.
- Транзакции управляются вручную с явными `commit/rollback`.
- Валидация JWT выполняется в `JwtAuthHandler` (наследник `HandlerWrapper`).
- Приложение не использует Spring, что соответствует техническим ограничениям кейса.

## 👥 Авторство

Разработано в рамках учебного хакатона.
```markdown
- @lavrik0904
- @Conststruktor
- @XuTpoKoT
- @Lucy_Korotkova
- @kskskseniia
- @Gertoce
```
