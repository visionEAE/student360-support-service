package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertJpaRepository extends JpaRepository<Alert, UUID>, AlertRepository {}
