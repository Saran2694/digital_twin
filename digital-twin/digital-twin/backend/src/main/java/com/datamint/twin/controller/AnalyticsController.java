package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Historical data analytics on the 6-month inspection history. */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private static final Map<String, String> GROUPS = Map.of("line", "line", "material", "material", "shift", "shift",
            "partType", "part_type", "defectType", "defect_type", "severity", "severity",
            "disposition", "disposition", "quality", "quality", "batch", "batch_id");

    private final JdbcTemplate jdbc;

    public AnalyticsController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static class Filter {
        final StringBuilder sql = new StringBuilder(" where 1=1");
        final List<Object> args = new ArrayList<>();
        Filter(LocalDate from, LocalDate to, Long machineId) {
            if (from != null) { sql.append(" and inspection_date >= ?"); args.add(from); }
            if (to != null) { sql.append(" and inspection_date <= ?"); args.add(to); }
            if (machineId != null) { sql.append(" and machine_id = ?"); args.add(machineId); }
        }
    }

    @GetMapping("/overview")
    public Overview overview(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                             @RequestParam(required = false) Long machineId) {
        Filter f = new Filter(from, to, machineId);
        return jdbc.queryForObject("select count(*), sum(quality='Good'), sum(quality='Damaged'), sum(disposition='Scrap'), " +
                "sum(disposition='Rework'), avg(cycle_time_s), avg(pour_temp_c), avg(pressure_bar), min(inspection_date), " +
                "max(inspection_date) from component_inspections" + f.sql, (rs, i) -> {
            long total = rs.getLong(1), good = rs.getLong(2);
            return new Overview(total, good, rs.getLong(3), total == 0 ? 0 : round(good * 100.0 / total), rs.getLong(4),
                    rs.getLong(5), round(rs.getDouble(6)), round(rs.getDouble(7)), round(rs.getDouble(8)),
                    rs.getObject(9, LocalDate.class), rs.getObject(10, LocalDate.class));
        }, f.args.toArray());
    }

    /** by = line | material | shift | partType | defectType | severity | disposition | quality | batch */
    @GetMapping("/group")
    public List<GroupRow> group(@RequestParam String by,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                @RequestParam(required = false) Long machineId) {
        String col = GROUPS.get(by);
        if (col == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown group: " + by);
        Filter f = new Filter(from, to, machineId);
        return jdbc.query("select " + col + ", count(*), sum(quality='Damaged'), avg(pour_temp_c), avg(pressure_bar), " +
                "avg(cycle_time_s) from component_inspections" + f.sql + " and " + col + " is not null group by " + col +
                " order by count(*) desc limit 100",
                (rs, i) -> new GroupRow(rs.getString(1), rs.getLong(2), rs.getLong(3), round(rs.getDouble(4)),
                        round(rs.getDouble(5)), round(rs.getDouble(6))), f.args.toArray());
    }

    /** granularity = day | week | month */
    @GetMapping("/trend")
    public List<TrendRow> trend(@RequestParam(defaultValue = "day") String granularity,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                @RequestParam(required = false) Long machineId) {
        String expr = switch (granularity) {
            case "month" -> "date_format(inspection_date,'%Y-%m')";
            case "week" -> "date_format(date_sub(inspection_date, interval weekday(inspection_date) day),'%Y-%m-%d')";
            default -> "date_format(inspection_date,'%Y-%m-%d')";
        };
        Filter f = new Filter(from, to, machineId);
        return jdbc.query("select " + expr + " as p, count(*), sum(quality='Damaged'), avg(pour_temp_c), avg(pressure_bar) " +
                "from component_inspections" + f.sql + " group by p order by p",
                (rs, i) -> new TrendRow(rs.getString(1), rs.getLong(2), rs.getLong(3), round(rs.getDouble(4)),
                        round(rs.getDouble(5))), f.args.toArray());
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
