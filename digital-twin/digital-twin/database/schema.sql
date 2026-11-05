-- OPTIONAL: Spring Boot creates these tables automatically (spring.jpa.hibernate.ddl-auto=update).
-- Run this only if you prefer to create the schema manually.
CREATE DATABASE IF NOT EXISTS digital_twin CHARACTER SET utf8mb4;
USE digital_twin;

CREATE TABLE IF NOT EXISTS machine (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(10) NOT NULL UNIQUE,
  name VARCHAR(60) NOT NULL,
  machine_type VARCHAR(60),
  line VARCHAR(30) NOT NULL,
  material VARCHAR(60) NOT NULL,
  pos_x DOUBLE, pos_y DOUBLE,
  UNIQUE KEY uk_machine_line_material (line, material)
);

CREATE TABLE IF NOT EXISTS component_inspections (
  component_id VARCHAR(20) PRIMARY KEY,
  inspection_date DATE NOT NULL,
  shift VARCHAR(5), line VARCHAR(30), material VARCHAR(60), part_type VARCHAR(40), batch_id VARCHAR(30),
  pour_temp_c DOUBLE, mold_temp_c DOUBLE, pressure_bar DOUBLE, cycle_time_s DOUBLE, weight_g DOUBLE,
  quality VARCHAR(20), defect_type VARCHAR(40), severity VARCHAR(20), disposition VARCHAR(20),
  machine_id BIGINT,
  INDEX idx_ci_machine (machine_id), INDEX idx_ci_date (inspection_date)
);

CREATE TABLE IF NOT EXISTS alert (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  machine_id BIGINT NOT NULL, type VARCHAR(20), severity VARCHAR(15), title VARCHAR(150), message VARCHAR(500),
  component_id VARCHAR(20), status VARCHAR(15), created_at DATETIME(6) NOT NULL,
  acknowledged_by VARCHAR(60), resolved_at DATETIME(6),
  INDEX idx_alert_status (status), INDEX idx_alert_created (created_at)
);

CREATE TABLE IF NOT EXISTS maintenance_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  machine_id BIGINT NOT NULL, title VARCHAR(150) NOT NULL, description VARCHAR(600),
  priority VARCHAR(10), status VARCHAR(15), requested_by VARCHAR(60), assigned_to VARCHAR(60),
  auto_generated BIT NOT NULL DEFAULT 0, created_at DATETIME(6), due_date DATE, completed_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(60) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100), role VARCHAR(20), enabled BIT NOT NULL DEFAULT 1
);
