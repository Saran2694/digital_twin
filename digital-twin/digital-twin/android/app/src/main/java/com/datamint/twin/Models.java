package com.datamint.twin;

import java.util.List;

/** JSON objects exchanged with the Spring Boot REST API. */
public final class Models {
    private Models() {}

    public static class LoginRequest {
        public String username, password;
        public LoginRequest(String u, String p) { username = u; password = p; }
    }
    public static class User { public long id; public String username, fullName, role; public boolean enabled; }
    public static class LoginResponse { public String token; public User user; }
    public static class UserForm { public String username, password, fullName, role; }

    public static class Summary { public int total, running, warning, offline; public long activeAlerts; public String asOf; }

    public static class Machine {
        public long id, openAlerts;
        public String code, name, machineType, line, material, status, statusReason, lastUpdated;
        public Double temperature, moldTemp, pressure, runningHours, defectRate, posX, posY;
    }
    public static class TrendPoint { public String label; public double temperature, pressure; }

    public static class Alert {
        public long id, machineId;
        public String machineName, type, severity, title, message, componentId, status, createdAt, acknowledgedBy;
    }
    public static class PageResult<T> { public List<T> items; public long total; }

    public static class Maint {
        public long id, machineId;
        public String machineName, title, description, priority, status, requestedBy, assignedTo, createdAt, dueDate;
    }
    public static class MaintForm { public long machineId; public String title, description, priority; }

    public static class Overview {
        public long total, good, damaged, scrap, rework;
        public double yieldPct, avgCycle, avgPourTemp, avgPressure;
        public String from, to;
    }
    public static class GroupRow { public String name; public long total, damaged; }
    public static class TrendRow { public String period; public long total, damaged; public double avgPourTemp, avgPressure; }
}
