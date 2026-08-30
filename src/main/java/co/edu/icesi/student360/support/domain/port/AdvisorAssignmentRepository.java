package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.AdvisorAssignment;
import java.util.List;

public interface AdvisorAssignmentRepository {

  List<AdvisorAssignment> findByAdvisorReference(String advisorReference);

  List<AdvisorAssignment> findByAdvisorReferenceAndStudentReference(
      String advisorReference, String studentReference);
}
