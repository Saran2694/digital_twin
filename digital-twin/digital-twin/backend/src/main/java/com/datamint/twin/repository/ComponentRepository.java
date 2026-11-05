package com.datamint.twin.repository;

import com.datamint.twin.model.ComponentInspection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComponentRepository extends JpaRepository<ComponentInspection, String> {
    List<ComponentInspection> findByMachineIdOrderByComponentIdDesc(Long machineId, Pageable pageable);
    Optional<ComponentInspection> findFirstByMachineIdOrderByComponentIdDesc(Long machineId);
}
