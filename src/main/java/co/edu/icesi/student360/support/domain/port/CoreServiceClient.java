package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.source.AcademicStatus;
import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import co.edu.icesi.student360.support.domain.model.source.StudentProfile;
import co.edu.icesi.student360.support.domain.model.source.StudentSummary;
import java.util.Collection;
import java.util.List;

/**
 * Port: the official student records, fetched synchronously from core-service on behalf of the
 * current user. Every method throws {@link SourceUnavailableException} when the source cannot be
 * reached or answers with an error; callers degrade.
 */
public interface CoreServiceClient {

  StudentProfile fetchStudentProfile(String studentReference);

  AcademicStatus fetchAcademicStatus(String studentReference);

  FinancialStatus fetchFinancialStatus(String studentReference);

  List<StudentSummary> fetchStudentSummaries(Collection<String> studentReferences);
}
