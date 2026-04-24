
```markdown
# 🎲 Лотерейный бэкенд (Lottery Backend)

Бэкенд-система для проведения лотерейных тиражей с покупкой билетов и mock-оплатой.  
Реализован **Сценарий 2: Лотерея с оплатой** согласно учебному кейсу.

## 📌 Функциональность

- **Аутентификация и авторизация** (JWT, роли `USER` и `ADMIN`).
- **Управление тиражами** (создание, активация, завершение) — только для `ADMIN`.
- **Покупка лотерейных билетов** пользователями.
- **Mock-оплата билетов** с фиксацией успешного/неуспешного результата.
- **Генерация выигрышной комбинации** при завершении тиража.
- **Автоматическое определение выигрыша** и обновление статусов билетов (`WIN` / `LOSE`).
- **Просмотр истории билетов и результатов** для авторизованных пользователей.

## 🛠 Технологический стек

- **Java 17**
- **Jetty 11** (встроенный HTTP-сервер, без Spring)
- **PostgreSQL 15**
- **HikariCP** (пул соединений)
- **JWT** (io.jsonwebtoken)
- **BCrypt** (хеширование паролей)
- **Jackson** (JSON-сериализация)
- **Maven** (сборка)
- **Docker / docker-compose** (контейнеризация)



## 🚀 Запуск приложения

### 🔧 Локально (без Docker)

**Требования:** Java 17, Maven, PostgreSQL (локальный).

1. **Создайте базу данных**  
   Подключитесь к PostgreSQL и выполните:
   ```sql
   CREATE USER lottery_user WITH PASSWORD 'lottery_pass';
   CREATE DATABASE lottery OWNER lottery_user;
   ```

2. **Примените схему**  
   Выполните скрипт `src/main/resources/db/migration/V1__init.sql`:
   ```bash
   psql -U lottery_user -d lottery -f src/main/resources/db/migration/V1__init.sql
   ```

3. **Соберите и запустите приложение**
   ```bash
   mvn clean package
   java -jar target/lottery-backend-1.0-SNAPSHOT.jar
   ```
   Сервер запустится на `http://localhost:8080`.

4. **Проверьте работу**
   ```bash
   curl http://localhost:8080/api/draws
   ```
   Ожидаемый ответ: `[]` (если тиражей нет) или ошибка 401 (требуется авторизация).

### 🐳 Через Docker Compose (рекомендуется)

1. Убедитесь, что Docker и docker-compose установлены.
2. Выполните команду в корне проекта:
   ```bash
   docker-compose up -d
   ```
3. Приложение будет доступно на порту `8080`.  
   База данных PostgreSQL инициализируется автоматически.

4. Остановка контейнеров:
   ```bash
   docker-compose down
   ```

## 📡 API Endpoints

### 🔐 Аутентификация

| Метод | URL                    | Доступ       | Описание                           |
|-------|------------------------|--------------|------------------------------------|
| POST  | `/api/auth/register`   | Публичный    | Регистрация нового пользователя    |
| POST  | `/api/auth/login`      | Публичный    | Вход, возвращает JWT токен         |

**Пример регистрации:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "pass123"
}
```

**Пример входа:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "pass123"
}
```
Ответ:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> Все остальные эндпоинты требуют заголовок `Authorization: Bearer <token>` (кроме `/api/payments`).

### 🎰 Тиражи

| Метод | URL                              | Роль   | Описание                            |
|-------|----------------------------------|--------|-------------------------------------|
| GET   | `/api/draws`                     | Любая  | Список активных тиражей             |
| GET   | `/api/draws/{id}`                | Любая  | Информация о тираже                 |
| POST  | `/api/draws`                     | ADMIN  | Создание нового тиража              |
| POST  | `/api/draws/{id}/activate`       | ADMIN  | Активация тиража (статус → ACTIVE)  |
| POST  | `/api/draws/{id}/complete`       | ADMIN  | Завершение тиража и розыгрыш        |

**Пример создания тиража (ADMIN):**
```http
POST /api/draws
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Новогодний тираж",
  "ticketPrice": 100.00,
  "startTime": "2026-05-01T10:00:00",
  "endTime": "2026-05-10T18:00:00"
}
```

### 🎫 Билеты

| Метод | URL                    | Роль  | Описание                               |
|-------|------------------------|-------|----------------------------------------|
| POST  | `/api/tickets`         | USER  | Покупка билета для активного тиража    |
| GET   | `/api/tickets`         | USER  | Список всех билетов текущего пользователя |
| GET   | `/api/tickets/{id}`    | USER  | Проверка результата конкретного билета |

**Пример покупки билета:**
```http
POST /api/tickets
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "drawId": 1,
  "combination": "5,12,23,34,45"
}
```

### 💳 Платежи

| Метод | URL               | Доступ    | Описание                     |
|-------|-------------------|-----------|------------------------------|
| POST  | `/api/payments`   | Публичный | Mock-оплата билета (имитация) |

**Пример успешной оплаты:**
```http
POST /api/payments
Content-Type: application/json

{
  "ticketId": 1,
  "success": true
}
```

## 🧪 Полный сценарий тестирования (cURL)

Ниже приведены команды для проверки полного цикла работы системы.  
Предполагается, что сервер запущен на `localhost:8080`.

### 1. Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass123"}'
```

### 2. Вход под администратором (используйте заранее созданного admin/admin123)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
Сохраните полученный `token` в переменную `ADMIN_TOKEN`.

### 3. Создание тиража (ADMIN)
```bash
curl -X POST http://localhost:8080/api/draws \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Тестовый тираж", "ticketPrice": 100.00, "startTime": "2026-04-20T10:00:00", "endTime": "2026-04-25T18:00:00"}'
```

### 4. Активация тиража
```bash
curl -X POST http://localhost:8080/api/draws/1/activate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 5. Вход обычным пользователем
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass123"}'
```
Сохраните `token` в `USER_TOKEN`.

### 6. Покупка билета
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"drawId": 1, "combination": "5,12,23,34,45"}'
```

### 7. Имитация оплаты
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"ticketId": 1, "success": true}'
```

### 8. Завершение тиража (ADMIN)
```bash
# Сначала измените дату окончания на прошедшую (через psql или аналогично)
curl -X POST http://localhost:8080/api/draws/1/complete \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 9. Проверка результата билета
```bash
curl -X GET http://localhost:8080/api/tickets/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

## 🔒 Безопасность и особенности

- Пароли хешируются с помощью **BCrypt** (cost = 10).
- JWT токен подписывается алгоритмом **HS256**, секрет задаётся в `application.properties` (для продакшена должен быть надёжным).
- По умолчанию в БД создаётся администратор `admin` / `admin123` (пароль захеширован в `V1__init.sql`).
- Mock-оплата не требует авторизации — в реальном проекте этот эндпоинт должен быть защищён.

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
```
