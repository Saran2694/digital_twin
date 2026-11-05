package com.datamint.twin.service;

import com.datamint.twin.dto.Dtos.AlertView;
import com.datamint.twin.dto.Dtos.PageResult;
import com.datamint.twin.model.Alert;
import com.datamint.twin.model.Machine;
import com.datamint.twin.repository.AlertRepository;
import com.datamint.twin.repository.MachineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlertService {
    private final AlertRepository alerts;
    private final MachineRepository machines;

    public AlertService(AlertRepository alerts, MachineRepository machines) {
        this.alerts = alerts; this.machines = machines;
    }

    public PageResult<AlertView> search(String status, String severity, Long machineId, int page, int size) {
        Page<Alert> p = alerts.search(status, severity, machineId, PageRequest.of(page, Math.min(size, 200)));
        Map<Long, String> names = names();
        return new PageResult<>(p.getContent().stream().map(a -> view(a, names)).toList(),
                p.getTotalElements(), page, size);
    }

    public java.util.List<AlertView> recent(int limit) {
        Map<Long, String> names = names();
        return alerts.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(a -> view(a, names)).getContent();
    }

    public AlertView acknowledge(Long id, String user) {
        Alert a = find(id);
        if ("OPEN".equals(a.getStatus())) { a.setStatus("ACKNOWLEDGED"); a.setAcknowledgedBy(user); alerts.save(a); }
        return view(a, names());
    }

    public AlertView resolve(Long id, String user) {
        Alert a = find(id);
        a.setStatus("RESOLVED");
        if (a.getAcknowledgedBy() == null) a.setAcknowledgedBy(user);
        a.setResolvedAt(LocalDateTime.now());
        alerts.save(a);
        return view(a, names());
    }

    private Alert find(Long id) {
        return alerts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
    }

    private Map<Long, String> names() {
        Map<Long, String> m = new HashMap<>();
        for (Machine x : machines.findAll()) m.put(x.getId(), x.getName());
        return m;
    }

    private AlertView view(Alert a, Map<Long, String> names) {
        return new AlertView(a.getId(), a.getMachineId(), names.get(a.getMachineId()), a.getType(), a.getSeverity(),
                a.getTitle(), a.getMessage(), a.getComponentId(), a.getStatus(), a.getCreatedAt(),
                a.getAcknowledgedBy(), a.getResolvedAt());
    }
}
