# BookClub

Книжный клуб с рекомендациями. Запускается локально.

## Что использовано
- Java 21, Spring Boot 3.2.5
- Spring Security + JWT токены
- Hibernate, Spring Data JPA
- MariaDB
- HTML, CSS, JavaScript (всё в одном index.html)

## Как запустить

1. Установить MariaDB, создать базу `bookclub`, пользователь и пароль в application.properties
2. Залить тестовые данные (100 книг) скриптом books.sql:
mariadb -u bookclub_user -p bookclub < books.sql
3.Запустить приложение:
mvn spring-boot:run
4. Открыть http://localhost:8080/
5. Можно зарегестрировать разных пользователей и потестировать работоспособность. 
Специально для удобства тестирования роли выбираются при регистрации.
