package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.AlertView;
import com.datamint.twin.dto.Dtos.PageResult;
import com.datamint.twin.service.AlertService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) { this.service = service; }

    @GetMapping
    public PageResult<AlertView> search(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) String severity,
                                        @RequestParam(required = false) Long machineId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return service.search(blank(status), blank(severity), machineId, page, size);
    }

    @GetMapping("/recent")
    public List<AlertView> recent(@RequestParam(defaultValue = "3") int limit) { return service.recent(limit); }

    @PutMapping("/{id}/acknowledge")
    public AlertView acknowledge(@PathVariable Long id, Authentication auth) { return service.acknowledge(id, auth.getName()); }

    @PutMapping("/{id}/resolve")
    public AlertView resolve(@PathVariable Long id, Authentication auth) { return service.resolve(id, auth.getName()); }

    private static String blank(String s) { return s == null || s.isBlank() ? null : s; }
}
