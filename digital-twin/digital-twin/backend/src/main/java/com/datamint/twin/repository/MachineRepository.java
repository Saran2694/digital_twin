package com.datamint.twin.repository;

import com.datamint.twin.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByLineAndMaterial(String line, String material);
}
