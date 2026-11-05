package com.datamint.twin.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CSV exports for the Reports page. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/inspections.csv")
    public void inspections(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            @RequestParam(required = false) Long machineId, HttpServletResponse resp) throws IOException {
        StringBuilder sql = new StringBuilder("select c.component_id, c.inspection_date, m.name as machine, c.shift, c.line, " +
                "c.material, c.part_type, c.batch_id, c.pour_temp_c, c.mold_temp_c, c.pressure_bar, c.cycle_time_s, c.weight_g, " +
                "c.quality, c.defect_type, c.severity, c.disposition from component_inspections c " +
                "join machine m on m.id = c.machine_id where 1=1");
        List<Object> args = new ArrayList<>();
        if (from != null) { sql.append(" and c.inspection_date >= ?"); args.add(from); }
        if (to != null) { sql.append(" and c.inspection_date <= ?"); args.add(to); }
        if (machineId != null) { sql.append(" and c.machine_id = ?"); args.add(machineId); }
        sql.append(" order by c.component_id");
        csv(resp, "inspections.csv", sql.toString(), args.toArray());
    }

    @GetMapping("/alerts.csv")
    public void alerts(HttpServletResponse resp) throws IOException {
        csv(resp, "alerts.csv", "select a.id, m.name as machine, a.type, a.severity, a.title, a.message, a.component_id, " +
                "a.status, a.created_at, a.acknowledged_by, a.resolved_at from alert a join machine m on m.id = a.machine_id " +
                "order by a.created_at desc");
    }

    @GetMapping("/maintenance.csv")
    public void maintenance(HttpServletResponse resp) throws IOException {
        csv(resp, "maintenance.csv", "select r.id, m.name as machine, r.title, r.description, r.priority, r.status, " +
                "r.requested_by, r.assigned_to, r.created_at, r.due_date, r.completed_at from maintenance_request r " +
                "join machine m on m.id = r.machine_id order by r.created_at desc");
    }

    private void csv(HttpServletResponse resp, String filename, String sql, Object... args) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=" + filename);
        PrintWriter w = resp.getWriter();
        boolean[] headerDone = {false};
        jdbc.query(sql, (RowCallbackHandler) rs -> {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            if (!headerDone[0]) {
                StringBuilder h = new StringBuilder();
                for (int i = 1; i <= n; i++) h.append(i > 1 ? "," : "").append(md.getColumnLabel(i));
                w.println(h);
                headerDone[0] = true;
            }
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= n; i++) line.append(i > 1 ? "," : "").append(escape(rs.getString(i)));
            w.println(line);
        }, args);
        w.flush();
    }

    private static String escape(String v) {
        if (v == null) return "";
        return v.contains(",") || v.contains("\"") || v.contains("\n") ? "\"" + v.replace("\"", "\"\"") + "\"" : v;
    }
}
