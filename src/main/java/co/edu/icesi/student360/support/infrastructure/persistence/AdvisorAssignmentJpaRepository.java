package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.AdvisorAssignment;
import co.edu.icesi.student360.support.domain.port.AdvisorAssignmentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvisorAssignmentJpaRepository
    extends JpaRepository<AdvisorAssignment, Integer>, AdvisorAssignmentRepository {}
