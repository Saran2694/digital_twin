package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.*;
import com.datamint.twin.service.MonitoringService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Dashboard KPIs, digital factory view data and live equipment monitoring. */
@RestController
@RequestMapping("/api")
public class MonitoringController {
    private final MonitoringService monitoring;

    public MonitoringController(MonitoringService monitoring) { this.monitoring = monitoring; }

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboard() { return monitoring.dashboard(); }

    @GetMapping("/machines")
    public List<MachineSummary> machines() { return monitoring.summaries(); }

    @GetMapping("/machines/{id}")
    public MachineSummary machine(@PathVariable Long id) { return monitoring.summary(id); }

    @GetMapping("/machines/{id}/trend")
    public List<TrendPoint> trend(@PathVariable Long id, @RequestParam(defaultValue = "1h") String range) {
        return monitoring.trend(id, range);
    }
}
