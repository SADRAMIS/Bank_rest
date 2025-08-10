# 🚀 Система Управления Банковскими Картами

Backend-приложение на Java (Spring Boot) для управления банковскими картами с поддержкой аутентификации, авторизации и транзакций.

## 📋 Возможности

### 👤 Пользователи
- Просмотр своих карт (поиск + пагинация)
- Запрос блокировки карты
- Переводы между своими картами
- Просмотр баланса

### 🔐 Администраторы
- Создание, блокировка, активация, удаление карт
- Управление пользователями
- Просмотр всех карт

## 🏗️ Архитектура

- **Spring Boot 3.2.0** - основной фреймворк
- **Spring Security + JWT** - аутентификация и авторизация
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - база данных
- **Liquibase** - миграции базы данных
- **Swagger/OpenAPI** - документация API
- **Docker** - контейнеризация

## 🚀 Быстрый старт

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 15+
- Docker & Docker Compose (опционально)

### Запуск с Docker Compose

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd Bank_REST
```

2. Запустите приложение с помощью Docker Compose:
```bash
docker-compose up -d
```

3. Приложение будет доступно по адресу: http://localhost:8080
4. Swagger UI: http://localhost:8080/swagger-ui.html

### Запуск локально

1. Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE bankcards;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE bankcards TO postgres;
```

2. Настройте подключение к базе данных в `src/main/resources/application.yml`

3. Соберите и запустите приложение:
```bash
mvn clean install
mvn spring-boot:run
```

## 🔐 Аутентификация

### Тестовые пользователи

**Администратор:**
- Username: `admin`
- Password: `admin123`
- Роль: `ADMIN`

**Пользователь:**
- Username: `user`
- Password: `user123`
- Роль: `USER`

### Получение JWT токена

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

## 📚 API Endpoints

### Аутентификация
- `POST /api/auth/login` - вход в систему
- `POST /api/auth/register` - регистрация

### Карты
- `GET /api/cards` - получение карт пользователя
- `POST /api/cards` - создание карты (ADMIN)
- `GET /api/cards/{id}` - получение информации о карте
- `PUT /api/cards/{id}/status` - обновление статуса карты
- `DELETE /api/cards/{id}` - удаление карты (ADMIN)
- `GET /api/cards/all` - получение всех карт (ADMIN)

### Транзакции
- `POST /api/transactions` - создание транзакции
- `GET /api/transactions` - получение транзакций пользователя
- `GET /api/transactions/{id}` - получение информации о транзакции
- `POST /api/transactions/{id}/cancel` - отмена транзакции
- `GET /api/transactions/all` - получение всех транзакций (ADMIN)

### Пользователи
- `GET /api/users/{id}` - получение информации о пользователе
- `GET /api/users` - получение всех пользователей (ADMIN)
- `PUT /api/users/{id}` - обновление пользователя (ADMIN)
- `DELETE /api/users/{id}` - удаление пользователя (ADMIN)

## 🗄️ База данных

### Схема

- **users** - пользователи системы
- **cards** - банковские карты
- **transactions** - транзакции между картами

### Миграции

Миграции выполняются автоматически при запуске приложения через Liquibase.

## 🔒 Безопасность

- Шифрование номеров карт (AES)
- Маскирование номеров карт для отображения
- JWT токены для аутентификации
- Ролевая авторизация (ADMIN/USER)
- Валидация входных данных

## 🧪 Тестирование

Запуск тестов:
```bash
mvn test
```

## 📦 Сборка

Создание JAR файла:
```bash
mvn clean package
```

## 🐳 Docker

Сборка Docker образа:
```bash
docker build -t bankcards .
```

## 📄 Лицензия

MIT License

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit изменения (`git commit -m 'Add some AmazingFeature'`)
4. Push в branch (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request
