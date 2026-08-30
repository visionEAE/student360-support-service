package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportReportJpaRepository
    extends JpaRepository<SupportReport, UUID>, SupportReportRepository {}
