package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.MaintenanceForm;
import com.datamint.twin.dto.Dtos.MaintenanceView;
import com.datamint.twin.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) { this.service = service; }

    @GetMapping
    public List<MaintenanceView> list(@RequestParam(required = false) String status,
                                      @RequestParam(defaultValue = "0") int limit) {
        return service.list(status == null || status.isBlank() ? null : status, limit);
    }

    @PostMapping
    public MaintenanceView create(@Valid @RequestBody MaintenanceForm form, Authentication auth) {
        return service.create(form, auth.getName());
    }

    @PutMapping("/{id}")
    public MaintenanceView update(@PathVariable Long id, @Valid @RequestBody MaintenanceForm form) {
        return service.update(id, form);
    }

    @PutMapping("/{id}/status")
    public MaintenanceView status(@PathVariable Long id, @RequestParam String status) {
        return service.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
