# Курсова робота — варіант 15

**Тема:** Інформаційна система видачі та повернення університетського майна.  
**Студентка:** Шевченко Лілія Сергіївна. **Група:** D2TH.  
**Дисципліна:** Кросплатформене програмування. **Рік:** 2026–2027.

## Структура
- `database.sql` — створення MySQL БД і демонстраційні дані.
- `server/` — Java 21, Maven WAR, Servlet API `javax.servlet` 4.0.1, Jackson, JDBC, Service, моделі, тести JUnit 5.
- `client/` — JavaFX GUI, HTTP/JSON-клієнт, фоновий CSV-імпорт через `Task`.
- `client/sample-data/inventory.csv` — приклад інвентарного CSV.

## Запуск
1. Встановити JDK 21, Maven, MySQL 8+, Apache Tomcat 9.
2. Виконати `database.sql` у MySQL.
3. За потреби задати змінні середовища `ASSETS_DB_URL`, `ASSETS_DB_USER`, `ASSETS_DB_PASSWORD`.
4. У `server`: `mvn clean test package`; WAR `target/university-assets.war` розгорнути в Tomcat 9.
5. У `client`: `mvn clean javafx:run`.
6. За потреби змінити `ASSETS_API_BASE` (за замовчуванням `http://localhost:8080/university-assets/api`).

## API
- `GET /api/assets|users|requests|transfers`
- `POST /api/{resource}` — створення.
- `PUT /api/{resource}/{id}` — редагування.
- `DELETE /api/{resource}/{id}` — видалення.
- `POST /api/requests/{id}/transition` з JSON `{"to":"APPROVED"|"ISSUED"|"RETURNED"}`.

## Стани заявки
`CREATED → APPROVED → ISSUED → RETURNED` відповідають українським станам із завдання: `СТВОРЕНО → СХВАЛЕНО → ВИДАНО → ПОВЕРНУТО`.

## Формула
`I = 0.40·F1 + 0.35·F2 + 0.25·F3`, де фактори нормуються до 0–100. Для дефіциту використано `F3 = 100·(N−A)/N`, де `N` — загальна, `A` — доступна кількість одиниць.

Працездатність програмного комплексу перевірено шляхом запуску клієнтської та серверної частин, тестування API, перевірки підключення до бази даних MySQL та виконання 19 автоматизованих тестів JUnit. Перевірка підтвердила коректність роботи основних компонентів системи
