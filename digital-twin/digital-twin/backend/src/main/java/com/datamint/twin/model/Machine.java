package com.datamint.twin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One monitored equipment = one (production line + material) combination found in the data. */
@Entity
@Table(name = "machines")
@Getter @Setter @NoArgsConstructor
public class Machine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "machine_code", nullable = false, unique = true, length = 255)
    private String code;                 // A, B, C ...
    @Column(name = "machine_name", nullable = false, length = 255)
    private String name;
    // Machine A
    @Column(name = "machine_type", length = 60)
    private String machineType;
    @Column(nullable = false, length = 30)
    private String line;
    @Column(nullable = false, length = 60)
    private String material;
    @Column(name = "pos_x")
    private Double posX;                 // % position on the factory image
    @Column(name = "pos_y")
    private Double posY;
}
