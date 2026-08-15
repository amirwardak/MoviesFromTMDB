# Каталог фильмов (MoviesFromTMDB)

JavaFX-приложение, которое позволяет искать информацию о фильмах через **TMDB API** и сохранять понравившиеся фильмы в локальную базу данных **PostgreSQL**.

## Требования

- **JDK 17** или новее
- **PostgreSQL** (запущенный локально или доступный по сети)
- **API-ключ TMDB** — получить можно на странице настроек аккаунта: https://www.themoviedb.org/settings/api

## Установка

### 1. Клонируйте репозиторий

```bash
git clone <ссылка-на-репозиторий>
cd MoviesFromTMDB
```

### 2. Настройте подключение к базе данных

Файлы конфигурации не хранятся в репозитории (добавлены в `.gitignore`, чтобы не засветить пароли и ключи). Вместо них в проекте лежат шаблоны с суффиксом `.example`.

Скопируйте шаблон подключения к БД:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Откройте `db.properties` и замените значения на свои:

```properties
db.url=jdbc:postgresql://localhost:5432/movies_db
db.user=postgres
db.password=ваш_пароль
```

- `db.url` — адрес сервера PostgreSQL и имя базы данных (порт `5432` — стандартный, менять нужно только если вы его меняли при установке).
- `db.user` — имя пользователя PostgreSQL.
- `db.password` — пароль этого пользователя.

> Базу данных создавать заранее не обязательно — приложение создаст её само при первом запуске (см. раздел «Автонастройка БД» ниже).

### 3. Настройте API-ключ TMDB

Скопируйте шаблон конфигурации TMDB:

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Откройте `config.properties` и вставьте ваш ключ:

```properties
tmdb.api.key=ваш_JWT_ключ
tmdb.base.url=https://api.themoviedb.org/3
tmdb.image.base.url=https://image.tmdb.org/t/p/w500
```

- `tmdb.api.key` — ваш API-ключ (Access Token в формате JWT) с сайта TMDB.
- Остальные два параметра менять не нужно.

## Запуск

Через Maven Wrapper (Maven не требуется устанавливать отдельно):

```bash
./mvnw clean javafx:run
```

Windows:

```bash
mvnw.cmd clean javafx:run
```

Или через установленный глобально Maven:

```bash
mvn clean javafx:run
```

## Автонастройка БД

При первом запуске приложение автоматически:

1. создаст базу данных `movies_db`, если её ещё нет;
2. создаст таблицу `movies`, если её ещё нет.

Для автоматического создания базы данных у пользователя из `db.properties` должно быть право `CREATEDB` (у роли `postgres` по умолчанию оно есть).

Если вы предпочитаете создать базу вручную:

```sql
CREATE DATABASE movies_db;
```

## Схема таблицы

Таблица `movies` создаётся приложением автоматически по файлу `schema.sql`:

| Поле          | Тип           | Описание                                  |
|---------------|---------------|-------------------------------------------|
| `id`          | SERIAL        | Первичный ключ                            |
| `tmdb_id`     | INTEGER       | ID фильма в TMDB, уникальный, NOT NULL    |
| `title`       | VARCHAR(255)  | Название фильма, NOT NULL                 |
| `overview`    | TEXT          | Краткое описание                          |
| `release_date`| DATE          | Дата выхода                               |
| `vote_average`| DECIMAL(3,1)  | Средняя оценка (0.0 – 10.0)               |
| `poster_path` | VARCHAR(512)  | Путь к постеру                            |
| `created_at`  | TIMESTAMP     | Дата добавления записи в базу, по умолчанию сейчас |
