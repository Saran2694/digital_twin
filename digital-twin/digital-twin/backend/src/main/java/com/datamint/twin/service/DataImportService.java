package com.datamint.twin.service;

import com.datamint.twin.dto.Dtos.ImportResult;
import com.datamint.twin.model.Alert;
import com.datamint.twin.model.ComponentInspection;
import com.datamint.twin.model.Machine;
import com.datamint.twin.repository.AlertRepository;
import com.datamint.twin.repository.MachineRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Loads Metal_component-LAST_6_MONTHS_DATA.xlsx into MySQL, creates the machines and derives alerts. */
@Service
public class DataImportService {
    /** x / y position (%) of each machine on the factory image; A, B, C match the reference screenshot. */
    private static final double[][] POS = {{22.5, 45.3}, {50.8, 24.6}, {80.1, 53.1}, {36.4, 26.2}, {42, 33},
            {63.5, 48.4}, {67.3, 40.6}, {35.3, 66}, {52, 69}, {48.6, 82}, {9.4, 48.4}, {92, 45}};
    private static final List<String> REQUIRED = List.of("Component_ID", "Inspection_Date", "Shift", "Line", "Material",
            "Part_Type", "Batch_ID", "Pour_Temp_C", "Mold_Temp_C", "Pressure_bar", "Cycle_Time_s", "Weight_g",
            "Quality", "Defect_Type", "Severity", "Disposition");

    private final JdbcTemplate jdbc;
    private final MachineRepository machines;
    private final AlertRepository alerts;
    @Value("${twin.status.temp-sigma:3.0}") private double tempSigma;

    public DataImportService(JdbcTemplate jdbc, MachineRepository machines, AlertRepository alerts) {
        this.jdbc = jdbc; this.machines = machines; this.alerts = alerts;
    }

    @Transactional
    public ImportResult importWorkbook(InputStream in) throws IOException {
        List<ComponentInspection> rows = parse(in);
        Map<String, Machine> byKey = ensureMachines(rows);
        for (ComponentInspection r : rows) r.setMachineId(byKey.get(r.getLine() + "|" + r.getMaterial()).getId());
        upsert(rows);
        return new ImportResult(rows.size(), byKey.size(), generateAlerts());
    }

    // ---------------------------------------------------------------- parsing
    private List<ComponentInspection> parse(InputStream in) throws IOException {
        DataFormatter fmt = new DataFormatter();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sh = wb.getSheetAt(0);
            Row header = sh.getRow(sh.getFirstRowNum());
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) idx.put(fmt.formatCellValue(c).trim(), c.getColumnIndex());
            for (String col : REQUIRED)
                if (!idx.containsKey(col)) throw new IllegalArgumentException("Missing column: " + col);

            List<ComponentInspection> out = new ArrayList<>();
            for (int i = sh.getFirstRowNum() + 1; i <= sh.getLastRowNum(); i++) {
                Row r = sh.getRow(i);
                if (r == null) continue;
                String id = str(fmt, r, idx, "Component_ID");
                if (id == null) continue;
                ComponentInspection c = new ComponentInspection();
                c.setComponentId(id);
                c.setInspectionDate(date(fmt, r, idx, "Inspection_Date"));
                c.setShift(str(fmt, r, idx, "Shift"));
                c.setLine(str(fmt, r, idx, "Line"));
                c.setMaterial(str(fmt, r, idx, "Material"));
                c.setPartType(str(fmt, r, idx, "Part_Type"));
                c.setBatchId(str(fmt, r, idx, "Batch_ID"));
                c.setPourTempC(num(fmt, r, idx, "Pour_Temp_C"));
                c.setMoldTempC(num(fmt, r, idx, "Mold_Temp_C"));
                c.setPressureBar(num(fmt, r, idx, "Pressure_bar"));
                c.setCycleTimeS(num(fmt, r, idx, "Cycle_Time_s"));
                c.setWeightG(num(fmt, r, idx, "Weight_g"));
                c.setQuality(str(fmt, r, idx, "Quality"));
                c.setDefectType(str(fmt, r, idx, "Defect_Type"));
                c.setSeverity(str(fmt, r, idx, "Severity"));
                c.setDisposition(str(fmt, r, idx, "Disposition"));
                if (c.getInspectionDate() == null || c.getLine() == null || c.getMaterial() == null) continue;
                out.add(c);
            }
            return out;
        }
    }

    private static String str(DataFormatter f, Row r, Map<String, Integer> idx, String col) {
        Cell c = r.getCell(idx.get(col));
        if (c == null) return null;
        String s = f.formatCellValue(c).trim();
        return s.isEmpty() ? null : s;
    }

    private static Double num(DataFormatter f, Row r, Map<String, Integer> idx, String col) {
        Cell c = r.getCell(idx.get(col));
        if (c == null) return null;
        if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
        String s = f.formatCellValue(c).trim();
        try { return s.isEmpty() ? null : Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private static LocalDate date(DataFormatter f, Row r, Map<String, Integer> idx, String col) {
        Cell c = r.getCell(idx.get(col));
        if (c == null) return null;
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c))
            return c.getLocalDateTimeCellValue().toLocalDate();
        try { return LocalDate.parse(f.formatCellValue(c).trim().substring(0, 10)); } catch (Exception e) { return null; }
    }

    // ---------------------------------------------------------------- machines
    private Map<String, Machine> ensureMachines(List<ComponentInspection> rows) {
        TreeSet<String> keys = rows.stream().map(r -> r.getLine() + "|" + r.getMaterial())
                .collect(Collectors.toCollection(TreeSet::new));
        Map<String, Machine> result = new LinkedHashMap<>();
        for (String k : keys) {
            String[] p = k.split("\\|", 2);
            result.put(k, machines.findByLineAndMaterial(p[0], p[1]).orElseGet(() -> createMachine(p[0], p[1])));
        }
        return result;
    }

    private Machine createMachine(String line, String material) {
        int index = (int) machines.count();
        Machine m = new Machine();
        m.setCode(index < 26 ? String.valueOf((char) ('A' + index)) : "M" + (index + 1));
        m.setName("Machine " + m.getCode());
        m.setMachineType("Casting Machine");
        m.setLine(line);
        m.setMaterial(material);
        m.setPosX(index < POS.length ? POS[index][0] : 10.0 + (index * 7) % 80);
        m.setPosY(index < POS.length ? POS[index][1] : 20.0 + (index * 11) % 60);
        return machines.save(m);
    }

    private void upsert(List<ComponentInspection> rows) {
        String sql = "insert into component_inspections (component_id, inspection_date, shift, line, material, part_type, " +
                "batch_id, pour_temp_c, mold_temp_c, pressure_bar, cycle_time_s, weight_g, quality, defect_type, severity, " +
                "disposition, machine_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key update " +
                "inspection_date=values(inspection_date), shift=values(shift), line=values(line), material=values(material), " +
                "part_type=values(part_type), batch_id=values(batch_id), pour_temp_c=values(pour_temp_c), " +
                "mold_temp_c=values(mold_temp_c), pressure_bar=values(pressure_bar), cycle_time_s=values(cycle_time_s), " +
                "weight_g=values(weight_g), quality=values(quality), defect_type=values(defect_type), " +
                "severity=values(severity), disposition=values(disposition), machine_id=values(machine_id)";
        jdbc.batchUpdate(sql, rows, 500, (ps, r) -> {
            ps.setString(1, r.getComponentId());
            ps.setObject(2, java.sql.Date.valueOf(r.getInspectionDate()));
            ps.setString(3, r.getShift());
            ps.setString(4, r.getLine());
            ps.setString(5, r.getMaterial());
            ps.setString(6, r.getPartType());
            ps.setString(7, r.getBatchId());
            ps.setObject(8, r.getPourTempC());
            ps.setObject(9, r.getMoldTempC());
            ps.setObject(10, r.getPressureBar());
            ps.setObject(11, r.getCycleTimeS());
            ps.setObject(12, r.getWeightG());
            ps.setString(13, r.getQuality());
            ps.setString(14, r.getDefectType());
            ps.setString(15, r.getSeverity());
            ps.setString(16, r.getDisposition());
            ps.setObject(17, r.getMachineId());
        });
    }

    // ---------------------------------------------------------------- alerts
    /** Critical/Major defects and pour-temperature outliers become alerts (idempotent). */
    @Transactional
    public int generateAlerts() {
        LocalDate asOf = jdbc.queryForObject("select max(inspection_date) from component_inspections", LocalDate.class);
        if (asOf == null) return 0;
        Set<String> existing = new HashSet<>();
        jdbc.query("select type, component_id from alert where component_id is not null",
                (RowCallbackHandler) rs -> existing.add(rs.getString(1) + ":" + rs.getString(2)));
        Map<Long, String> names = new HashMap<>();
        for (Machine m : machines.findAll()) names.put(m.getId(), m.getName());
        List<Alert> out = new ArrayList<>();

        jdbc.query("select component_id, machine_id, inspection_date, shift, part_type, material, defect_type, " +
                   "severity, disposition from component_inspections where quality='Damaged' and severity in ('Critical','Major')",
                (RowCallbackHandler) rs -> {
                    String id = rs.getString(1);
                    if (existing.contains("DEFECT:" + id)) return;
                    boolean crit = "Critical".equals(rs.getString(8));
                    Alert a = new Alert();
                    a.setMachineId(rs.getLong(2));
                    a.setType("DEFECT");
                    a.setSeverity(crit ? "CRITICAL" : "WARNING");
                    a.setTitle(names.get(a.getMachineId()) + " – " + (crit ? "Critical " : "Major ") + pretty(rs.getString(7)) + " defect");
                    a.setMessage(rs.getString(5) + " (" + rs.getString(6) + ") " + id + " rejected, disposition: " + rs.getString(9));
                    a.setComponentId(id);
                    a.setCreatedAt(TimeUtil.readingTime(rs.getObject(3, LocalDate.class), rs.getString(4), id));
                    status(a, asOf);
                    out.add(a);
                });

        Map<String, double[]> stats = new LinkedHashMap<>();
        jdbc.query("select material, avg(pour_temp_c), stddev_pop(pour_temp_c) from component_inspections group by material",
                (RowCallbackHandler) rs -> stats.put(rs.getString(1), new double[]{rs.getDouble(2), rs.getDouble(3)}));
        for (Map.Entry<String, double[]> e : stats.entrySet()) {
            double mean = e.getValue()[0], sd = e.getValue()[1];
            if (sd <= 0) continue;
            jdbc.query("select component_id, machine_id, inspection_date, shift, pour_temp_c from component_inspections " +
                       "where material=? and abs(pour_temp_c-?)>?",
                    (RowCallbackHandler) rs -> {
                        String id = rs.getString(1);
                        if (existing.contains("TEMPERATURE:" + id)) return;
                        double t = rs.getDouble(5);
                        Alert a = new Alert();
                        a.setMachineId(rs.getLong(2));
                        a.setType("TEMPERATURE");
                        a.setSeverity("WARNING");
                        a.setTitle(names.get(a.getMachineId()) + " – " + (t > mean ? "High" : "Low") + " Pour Temperature");
                        a.setMessage(String.format("Pour temperature reached %.1f°C (normal %.0f ± %.0f)", t, mean, tempSigma * sd));
                        a.setComponentId(id);
                        a.setCreatedAt(TimeUtil.readingTime(rs.getObject(3, LocalDate.class), rs.getString(4), id));
                        status(a, asOf);
                        out.add(a);
                    }, e.getKey(), mean, tempSigma * sd);
        }
        alerts.saveAll(out);
        return out.size();
    }

    private static void status(Alert a, LocalDate asOf) {
        if (a.getCreatedAt().toLocalDate().isAfter(asOf.minusDays(3))) {
            a.setStatus("OPEN");
        } else {
            a.setStatus("RESOLVED");
            a.setResolvedAt(a.getCreatedAt().plusHours(4));
        }
    }

    private static String pretty(String s) {
        if (s == null) return "quality";
        String t = s.replace('_', ' ');
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
