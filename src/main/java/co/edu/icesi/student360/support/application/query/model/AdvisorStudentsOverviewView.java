package co.edu.icesi.student360.support.application.query.model;

import java.util.List;

public record AdvisorStudentsOverviewView(
    String advisorReference, List<AdvisorStudentRow> students, List<String> unavailableSources) {}
