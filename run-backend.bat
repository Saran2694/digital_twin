@echo off
echo Starting Digital Twin Backend (Spring Boot)...
cd /d "%~dp0digital-twin\digital-twin\backend"
java -jar target\digital-twin-backend-1.0.0.jar --spring.datasource.password=root
pause

