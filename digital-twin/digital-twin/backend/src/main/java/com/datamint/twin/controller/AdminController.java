package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.*;
import com.datamint.twin.model.AppUser;
import com.datamint.twin.model.Machine;
import com.datamint.twin.repository.*;
import com.datamint.twin.service.DataImportService;
import com.datamint.twin.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Admin dashboard: users, machines, data import, system stats (ROLE_ADMIN only, see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Set<String> ROLES = Set.of("ADMIN", "ENGINEER", "OPERATOR");

    private final UserRepository users;
    private final MachineRepository machines;
    private final PasswordEncoder encoder;
    private final DataImportService importer;
    private final MaintenanceService maintenance;
    private final JdbcTemplate jdbc;

    public AdminController(UserRepository users, MachineRepository machines, PasswordEncoder encoder,
                           DataImportService importer, MaintenanceService maintenance, JdbcTemplate jdbc) {
        this.users = users; this.machines = machines; this.encoder = encoder;
        this.importer = importer; this.maintenance = maintenance; this.jdbc = jdbc;
    }

    // ---- users
    @GetMapping("/users")
    public List<UserInfo> users() { return users.findAll().stream().map(AuthController::info).toList(); }

    @PostMapping("/users")
    public UserInfo createUser(@RequestBody UserForm f) {
        if (f.username() == null || f.username().isBlank() || f.password() == null || f.password().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and a password of 6+ characters are required");
        if (users.findByUsername(f.username()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        AppUser u = new AppUser();
        u.setUsername(f.username().trim());
        u.setPasswordHash(encoder.encode(f.password()));
        fill(u, f);
        return AuthController.info(users.save(u));
    }

    @PutMapping("/users/{id}")
    public UserInfo updateUser(@PathVariable Long id, @RequestBody UserForm f) {
        AppUser u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (f.password() != null && !f.password().isBlank()) u.setPasswordHash(encoder.encode(f.password()));
        fill(u, f);
        return AuthController.info(users.save(u));
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id, Authentication auth) {
        AppUser u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getUsername().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        users.delete(u);
    }

    private void fill(AppUser u, UserForm f) {
        if (f.fullName() != null) u.setFullName(f.fullName());
        if (f.role() != null) {
            if (!ROLES.contains(f.role())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
            u.setRole(f.role());
        } else if (u.getRole() == null) u.setRole("OPERATOR");
        if (f.enabled() != null) u.setEnabled(f.enabled());
    }

    // ---- machines
    @PutMapping("/machines/{id}")
    public Machine updateMachine(@PathVariable Long id, @RequestBody MachineUpdate f) {
        Machine m = machines.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine not found"));
        if (f.name() != null && !f.name().isBlank()) m.setName(f.name());
        if (f.machineType() != null) m.setMachineType(f.machineType());
        if (f.posX() != null) m.setPosX(f.posX());
        if (f.posY() != null) m.setPosY(f.posY());
        return machines.save(m);
    }

    // ---- data
    @PostMapping("/import")
    public ImportResult importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            ImportResult r = importer.importWorkbook(file.getInputStream());
            maintenance.autoGenerate();
            return r;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/regenerate")
    public Map<String, Integer> regenerate() {
        return Map.of("alertsCreated", importer.generateAlerts(), "maintenanceCreated", maintenance.autoGenerate());
    }

    @GetMapping("/stats")
    public AdminStats stats() {
        return new AdminStats(count("component_inspections"), count("machine"), count("alert"),
                count("maintenance_request"), count("app_user"));
    }

    private long count(String table) {
        Long n = jdbc.queryForObject("select count(*) from " + table, Long.class);
        return n == null ? 0 : n;
    }
}
