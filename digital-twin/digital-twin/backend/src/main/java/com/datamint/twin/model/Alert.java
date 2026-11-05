package com.datamint.twin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert", indexes = {@Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_created", columnList = "created_at")})
@Getter @Setter @NoArgsConstructor
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "machine_id", nullable = false) private Long machineId;
    @Column(length = 20) private String type;          // DEFECT, TEMPERATURE, OFFLINE, SYSTEM
    @Column(length = 15) private String severity;      // CRITICAL, WARNING, INFO
    @Column(length = 150) private String title;
    @Column(length = 500) private String message;
    @Column(name = "component_id", length = 20) private String componentId;
    @Column(length = 15) private String status;        // OPEN, ACKNOWLEDGED, RESOLVED
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "acknowledged_by", length = 60) private String acknowledgedBy;
    @Column(name = "resolved_at") private LocalDateTime resolvedAt;
}
