DROP DATABASE IF EXISTS university_assets;
CREATE DATABASE university_assets CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE university_assets;

CREATE TABLE property (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  inventory_number VARCHAR(60) NOT NULL UNIQUE,
  condition_state VARCHAR(40) NOT NULL,
  total_units INT NOT NULL,
  available_units INT NOT NULL,
  criticality INT NOT NULL,
  CHECK (total_units >= 0),
  CHECK (available_units >= 0 AND available_units <= total_units),
  CHECK (criticality BETWEEN 0 AND 100)
);

CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  role_name VARCHAR(60) NOT NULL
);

CREATE TABLE property_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  property_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  planned_return_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  timeliness_score INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
  integral_index DECIMAL(6,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_request_property FOREIGN KEY (property_id) REFERENCES property(id),
  CHECK (quantity > 0),
  CHECK (timeliness_score BETWEEN 0 AND 100),
  CHECK (integral_index BETWEEN 0 AND 100),
  CHECK (status IN ('CREATED','APPROVED','ISSUED','RETURNED'))
);

CREATE TABLE transfer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id BIGINT NOT NULL,
  issued_at TIMESTAMP NULL,
  returned_at TIMESTAMP NULL,
  notes VARCHAR(500),
  CONSTRAINT fk_transfer_request FOREIGN KEY (request_id) REFERENCES property_request(id)
);

INSERT INTO property(name, inventory_number, condition_state, total_units, available_units, criticality) VALUES
('Ноутбук Lenovo ThinkPad', 'INV-001', 'СПРАВНИЙ', 10, 8, 90),
('Проєктор Epson', 'INV-002', 'СПРАВНИЙ', 5, 3, 85),
('Подовжувач мережевий', 'INV-003', 'СПРАВНИЙ', 20, 18, 35),
('Мікрофон USB', 'INV-004', 'ПОТРЕБУЄ ОГЛЯДУ', 6, 4, 60);

INSERT INTO app_user(full_name, email, role_name) VALUES
('Іваненко Іван Іванович', 'i.ivanenko@example.edu.ua', 'СТУДЕНТ'),
('Петренко Марія Олександрівна', 'm.petrenko@example.edu.ua', 'ВИКЛАДАЧ'),
('Коваленко Андрій Сергійович', 'a.kovalenko@example.edu.ua', 'ЛАБОРАНТ');

INSERT INTO property_request(user_id, property_id, quantity, planned_return_date, timeliness_score, status, integral_index)
VALUES (1, 1, 1, '2026-11-20', 80, 'CREATED', 81.50);
