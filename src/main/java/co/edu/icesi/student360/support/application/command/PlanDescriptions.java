package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.InterventionType;

/** The suggested plan text per type; the advisor edits it into a real plan. */
final class PlanDescriptions {

  private PlanDescriptions() {}

  static String describe(InterventionType type) {
    return switch (type) {
      case INTEGRAL_SUPPORT ->
          "Agenda un seguimiento de bienestar en las próximas 48 horas y conecta a la estudiante"
              + " con Bienestar Financiero para revisar el saldo vencido y los planes de pago"
              + " disponibles. Notifica al acompañante académico para revisar una carga académica"
              + " más liviana el próximo semestre.";
      case ACADEMIC_FOLLOW_UP ->
          "Seguimiento con la acompañante en los próximos 10 días hábiles; revisar juntas la"
              + " carga académica y las entregas recientes.";
    };
  }
}
