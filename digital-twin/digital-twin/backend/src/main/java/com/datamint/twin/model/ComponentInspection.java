package com.datamint.twin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

/** One row of Metal_component-LAST_6_MONTHS_DATA.xlsx */
@Entity
@Table(name = "component_inspections", indexes = {
        @Index(name = "idx_ci_machine", columnList = "machine_id"),
        @Index(name = "idx_ci_date", columnList = "inspection_date")})
@Getter @Setter @NoArgsConstructor
public class ComponentInspection {
    @Id @Column(name = "component_id", length = 20)
    private String componentId;
    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;
    @Column(length = 5)  private String shift;
    @Column(length = 30) private String line;
    @Column(length = 60) private String material;
    @Column(name = "part_type", length = 40) private String partType;
    @Column(name = "batch_id", length = 30)  private String batchId;
    @Column(name = "pour_temp_c")    private Double pourTempC;
    @Column(name = "mold_temp_c")    private Double moldTempC;
    @Column(name = "pressure_bar")   private Double pressureBar;
    @Column(name = "cycle_time_s")   private Double cycleTimeS;
    @Column(name = "weight_g")       private Double weightG;
    @Column(length = 20) private String quality;
    @Column(name = "defect_type", length = 40) private String defectType;
    @Column(length = 20) private String severity;
    @Column(length = 20) private String disposition;
    @Column(name = "machine_id") private Long machineId;
}
