package com.datamint.twin.repository;

import com.datamint.twin.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("select a from Alert a where (:status is null or a.status = :status) " +
           "and (:severity is null or a.severity = :severity) " +
           "and (:machineId is null or a.machineId = :machineId) order by a.createdAt desc")
    Page<Alert> search(@Param("status") String status, @Param("severity") String severity,
                       @Param("machineId") Long machineId, Pageable pageable);

    long countByStatusIn(Collection<String> statuses);
}
