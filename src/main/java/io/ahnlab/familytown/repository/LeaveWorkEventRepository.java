package io.ahnlab.familytown.repository;

import io.ahnlab.familytown.entity.LeaveWorkEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveWorkEventRepository extends JpaRepository<LeaveWorkEvent, Long> {
}
