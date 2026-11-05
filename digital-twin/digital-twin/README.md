# Digital Twin Monitoring Portal (Datamint Aarvix)

Java 17 · Spring Boot 3 · REST API · MySQL · React (web) · Android (Java, native)

```
digital-twin/
├── backend/    Spring Boot + MySQL REST API   (open backend/pom.xml in IntelliJ)
├── frontend/   React web app (Vite)           (npm install && npm run dev)
├── android/    Android app (Java, Retrofit)   (open the android/ folder in IntelliJ / Android Studio)
├── database/   schema.sql (optional – tables are auto-created)
└── docs/       PROJECT_ANALYSIS.md  (requirements → modules → features → API → DB)
```

## 1. Database
Install MySQL 8, then edit `backend/src/main/resources/application.properties`
(`spring.datasource.username / password`). The database `digital_twin` and all tables are created automatically.

## 2. Backend (IntelliJ)
1. `File ▸ Open` → select `backend/pom.xml` → *Open as Project*. JDK **17**.
2. `Settings ▸ Build ▸ Compiler ▸ Annotation Processors` → **Enable annotation processing** (Lombok).
3. Run `TwinApplication`. On the first start it
   * creates the admin user **admin / admin123** (shown as "John Doe – Admin"),
   * imports `Metal_component-LAST_6_MONTHS_DATA.xlsx` (9,753 rows) into MySQL,
   * creates 12 machines, the alerts and the first maintenance requests.
4. API runs on `http://localhost:8080/api`.

## 3. Web app
```
cd frontend
npm install
npm run dev        # http://localhost:5173
```
API address is in `frontend/.env` (`VITE_API_URL`). Production build: `npm run build`.

## 4. Android app
1. Open the **android/** folder in IntelliJ IDEA (Android plugin) or Android Studio, let Gradle sync (JDK 17).
2. Emulator: works as-is (`10.0.2.2` = your PC). Real phone: type `http://<PC-IP>:8080/api/` into the *Server URL* box on the login screen (phone and PC on the same Wi-Fi).
3. Run ▶ or `Build ▸ Build APK(s)`.

## Logins
`admin / admin123` – create more users in *Settings (Admin Dashboard)*. Change the password and `twin.jwt.secret` before production.
