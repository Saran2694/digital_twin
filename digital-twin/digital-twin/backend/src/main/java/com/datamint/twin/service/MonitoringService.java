package com.datamint.twin.service;

import com.datamint.twin.dto.Dtos.*;
import com.datamint.twin.model.ComponentInspection;
import com.datamint.twin.model.Machine;
import com.datamint.twin.repository.AlertRepository;
import com.datamint.twin.repository.ComponentRepository;
import com.datamint.twin.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Live equipment monitoring: status rules, KPIs and sensor trends computed from the inspection data. */
@Service
public class MonitoringService {
    private static final List<String> ACTIVE = List.of("OPEN", "ACKNOWLEDGED");
    private static final DateTimeFormatter MD = DateTimeFormatter.ofPattern("MM-dd");

    private final JdbcTemplate jdbc;
    private final MachineRepository machines;
    private final ComponentRepository components;
    private final AlertRepository alerts;

    @Value("${twin.status.window-days:7}") private int windowDays;
    @Value("${twin.status.warn-defect-rate:0.10}") private double warnRate;
    @Value("${twin.status.temp-sigma:3.0}") private double tempSigma;
    @Value("${twin.status.offline-days:2}") private int offlineDays;

    public MonitoringService(JdbcTemplate jdbc, MachineRepository machines,
                             ComponentRepository components, AlertRepository alerts) {
        this.jdbc = jdbc; this.machines = machines; this.components = components; this.alerts = alerts;
    }

    /** "Now" of the digital twin = newest inspection date in the database. */
    public LocalDate asOf() {
        return jdbc.queryForObject("select max(inspection_date) from component_inspections", LocalDate.class);
    }

    public List<MachineSummary> summaries() {
        LocalDate asOf = asOf();
        if (asOf == null) return List.of();
        LocalDate from = asOf.minusDays(windowDays - 1L);

        Map<Long, long[]> win = new HashMap<>();
        jdbc.query("select machine_id, count(*), sum(quality='Damaged'), sum(severity='Critical') " +
                   "from component_inspections where inspection_date between ? and ? group by machine_id",
                (RowCallbackHandler) rs -> win.put(rs.getLong(1),
                        new long[]{rs.getLong(2), rs.getLong(3), rs.getLong(4)}), from, asOf);

        Map<Long, LocalDate> lastDates = new HashMap<>();
        Map<Long, Double> cycleSum = new HashMap<>();
        jdbc.query("select machine_id, max(inspection_date), sum(cycle_time_s) from component_inspections group by machine_id",
                (RowCallbackHandler) rs -> {
                    lastDates.put(rs.getLong(1), rs.getObject(2, LocalDate.class));
                    cycleSum.put(rs.getLong(1), rs.getDouble(3));
                });

        Map<String, double[]> matStats = new HashMap<>();
        jdbc.query("select material, avg(pour_temp_c), stddev_pop(pour_temp_c) from component_inspections group by material",
                (RowCallbackHandler) rs -> matStats.put(rs.getString(1), new double[]{rs.getDouble(2), rs.getDouble(3)}));

        Map<Long, Long> openAlerts = new HashMap<>();
        jdbc.query("select machine_id, count(*) from alert where status in ('OPEN','ACKNOWLEDGED') group by machine_id",
                (RowCallbackHandler) rs -> openAlerts.put(rs.getLong(1), rs.getLong(2)));

        List<MachineSummary> out = new ArrayList<>();
        for (Machine m : machines.findAll(Sort.by("code"))) {
            ComponentInspection last = components.findFirstByMachineIdOrderByComponentIdDesc(m.getId()).orElse(null);
            long[] w = win.getOrDefault(m.getId(), new long[3]);
            LocalDate lastDate = lastDates.get(m.getId());
            double rate = w[0] == 0 ? 0 : (double) w[1] / w[0];

            List<String> reasons = new ArrayList<>();
            String status;
            if (lastDate == null || lastDate.isBefore(asOf.minusDays(offlineDays - 1L))) {
                status = "OFFLINE";
                reasons.add("No readings since " + lastDate);
            } else {
                if (w[2] > 0) reasons.add(w[2] + " critical defect(s) in last " + windowDays + " days");
                if (rate >= warnRate) reasons.add(String.format("Defect rate %.1f%% in last %d days", rate * 100, windowDays));
                double[] st = matStats.get(m.getMaterial());
                if (last != null && last.getPourTempC() != null && st != null && st[1] > 0
                        && Math.abs(last.getPourTempC() - st[0]) > tempSigma * st[1]) {
                    reasons.add("Pour temperature out of range");
                }
                status = reasons.isEmpty() ? "RUNNING" : "WARNING";
            }
            out.add(new MachineSummary(m.getId(), m.getCode(), m.getName(), m.getMachineType(), m.getLine(),
                    m.getMaterial(), status, reasons.isEmpty() ? "Operating normally" : String.join("; ", reasons),
                    last == null ? null : last.getPourTempC(), last == null ? null : last.getMoldTempC(),
                    last == null ? null : last.getPressureBar(),
                    r1(cycleSum.getOrDefault(m.getId(), 0.0) / 3600.0),
                    last == null ? null : TimeUtil.readingTime(last.getInspectionDate(), last.getShift(), last.getComponentId()),
                    r1(rate * 100), openAlerts.getOrDefault(m.getId(), 0L), m.getPosX(), m.getPosY()));
        }
        return out;
    }

    public MachineSummary summary(Long id) {
        return summaries().stream().filter(s -> s.id().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine not found"));
    }

    public DashboardSummary dashboard() {
        List<MachineSummary> all = summaries();
        int running = 0, warning = 0, offline = 0;
        for (MachineSummary s : all) {
            switch (s.status()) { case "RUNNING" -> running++; case "WARNING" -> warning++; default -> offline++; }
        }
        return new DashboardSummary(all.size(), running, warning, offline, alerts.countByStatusIn(ACTIVE), asOf());
    }

    /** range: 1h | 6h | 24h = last 20 / 60 / 120 readings, 7d = daily averages of the last 7 days. */
    public List<TrendPoint> trend(Long machineId, String range) {
        if ("7d".equals(range)) {
            LocalDate asOf = asOf();
            if (asOf == null) return List.of();
            List<TrendPoint> pts = new ArrayList<>();
            jdbc.query("select inspection_date, avg(pour_temp_c), avg(pressure_bar) from component_inspections " +
                       "where machine_id=? and inspection_date between ? and ? group by inspection_date order by inspection_date",
                    (RowCallbackHandler) rs -> pts.add(new TrendPoint(
                            rs.getObject(1, LocalDate.class).format(MD), r1(rs.getDouble(2)), r1(rs.getDouble(3)))),
                    machineId, asOf.minusDays(6), asOf);
            return pts;
        }
        int n = switch (range == null ? "" : range) { case "6h" -> 60; case "24h" -> 120; default -> 20; };
        List<ComponentInspection> rows = new ArrayList<>(
                components.findByMachineIdOrderByComponentIdDesc(machineId, PageRequest.of(0, n)));
        Collections.reverse(rows);
        List<TrendPoint> pts = new ArrayList<>();
        for (ComponentInspection c : rows) {
            pts.add(new TrendPoint(c.getInspectionDate().format(MD) + " " + c.getShift(),
                    c.getPourTempC() == null ? 0 : c.getPourTempC(),
                    c.getPressureBar() == null ? 0 : c.getPressureBar()));
        }
        return pts;
    }

    private static double r1(double v) { return Math.round(v * 10.0) / 10.0; }
}
