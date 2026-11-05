# Project analysis – Digital Twin Monitoring System

## 1. Requirement modules → features

| # | Module (requirement) | Feature delivered (your 6 features) | Web page | Android screen |
|---|---|---|---|---|
| 1 | Virtual facility dashboard | **Digital factory view** – factory picture with live colour-coded machine markers, KPI cards (total / running / warning / offline / active alerts), machine details | Dashboard, Factory View | Dashboard, Factory |
| 2 | Equipment status monitoring | **Live equipment monitoring** – status per machine, temperature, mold temperature, pressure, running hours, defect rate, 10-second auto refresh, sensor trend (1h/6h/24h/7d) | Factory View | Factory, Machine detail |
| 3 | Maintenance requirement management | **Maintenance management** – create / edit / status workflow (Pending → In Progress → Completed), priority, assignee, due date, auto-created requests for WARNING / OFFLINE machines | Maintenance | Maintenance |
| 4 | Performance monitoring | **Historical data analytics** – yield %, defects by type / line / material / shift / severity, scrap & rework, production vs damaged trend (day/week/month), CSV reports | Analytics, Reports | Analytics |
| 5 | Alert notification | **Alert management** – alerts from Critical/Major defects and pour-temperature outliers, bell + badge counter, acknowledge / resolve, filters | Alerts (+ bell, sidebar badge) | Alerts |
| – | Administration | **Admin dashboard** – users & roles, machine names/types/map positions, Excel import, system counters | Settings | Admin (menu) |

## 2. How your Excel database is used
The workbook (`metal_component_6month`, 9,753 rows, 2026-05-01 → 2026-10-31) is a *casting-inspection* history:
Component_ID, Inspection_Date, Shift, Line, Material, Part_Type, Batch_ID, Pour_Temp_C, Mold_Temp_C, Pressure_bar, Cycle_Time_s, Weight_g, Quality, Defect_Type, Severity, Disposition.
It has **no** machine, vibration, alert or maintenance columns, so the twin is derived from it:

| Twin concept | Derived from the data |
|---|---|
| Machine (12) | each *Line × Material* combination (3 lines × 4 materials) → Machine A … L |
| "Now" | newest `Inspection_Date` in the DB (the data is historical) |
| Temperature (card / label / chart) | latest `Pour_Temp_C` of the machine |
| Vibration slot of the reference design | replaced by **Pressure (bar)** – the data has no vibration |
| Running hours | Σ `Cycle_Time_s` / 3600 for the machine (whole data set) |
| Status | **Offline**: no reading in the last 2 days · **Warning**: critical defect in last 7 days, or defect rate ≥ 10 %, or pour temp > 3σ from the material average · else **Normal** (all thresholds in `application.properties`) |
| Alerts | Critical → CRITICAL, Major → WARNING defects; pour temperature > 3σ → WARNING. Alerts of the last 3 data-days start OPEN, older ones RESOLVED |
| Sensor-trend ranges | data only has date + shift, so 1h/6h/24h = last 20/60/120 readings, 7d = daily averages |
| Reading time | shift start (A 06:00, B 14:00, C 22:00) + sequence minutes |
| Maintenance | no source data → table starts with auto-generated requests for WARNING/OFFLINE machines, then users add their own |

Machine type is shown as "Casting Machine" (editable in Admin ▸ Machines). Marker positions on the factory image are stored in the DB (`pos_x`, `pos_y`) and editable.

## 3. Architecture
React (web) and Android (Retrofit) → REST/JSON + JWT → Spring Boot (controller → service → repository) → MySQL.
Roles: ADMIN, ENGINEER, OPERATOR (admin pages are ADMIN-only).

## 4. MySQL tables
`machine` · `component_inspections` (the Excel) · `alert` · `maintenance_request` · `app_user`  (see `database/schema.sql`).

## 5. REST API (`/api`, Bearer token except login)
| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login`, `GET /auth/me` |
| Dashboard / equipment | `GET /dashboard/summary`, `GET /machines`, `GET /machines/{id}`, `GET /machines/{id}/trend?range=1h\|6h\|24h\|7d` |
| Alerts | `GET /alerts?status&severity&machineId&page&size`, `GET /alerts/recent`, `PUT /alerts/{id}/acknowledge`, `PUT /alerts/{id}/resolve` |
| Maintenance | `GET /maintenance?status&limit`, `POST /maintenance`, `PUT /maintenance/{id}`, `PUT /maintenance/{id}/status?status=`, `DELETE /maintenance/{id}` |
| Analytics | `GET /analytics/overview`, `/analytics/group?by=line\|material\|shift\|partType\|defectType\|severity\|disposition\|quality\|batch`, `/analytics/trend?granularity=day\|week\|month` (all accept `from`, `to`, `machineId`) |
| Reports | `GET /reports/inspections.csv`, `/reports/alerts.csv`, `/reports/maintenance.csv` |
| Admin | `GET/POST/PUT/DELETE /admin/users`, `PUT /admin/machines/{id}`, `POST /admin/import` (xlsx), `POST /admin/regenerate`, `GET /admin/stats` |
