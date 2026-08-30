package co.edu.icesi.student360.support.application;

/** Every state change this service makes feeds the warehouse through the outbox. */
public final class SupportEvents {

  public static final String WELLBEING_ENTRY_RECORDED = "WELLBEING_ENTRY_RECORDED";
  public static final String ALERT_GENERATED = "ALERT_GENERATED";
  public static final String ALERT_STATUS_CHANGED = "ALERT_STATUS_CHANGED";
  public static final String INTERVENTION_PLAN_CREATED = "INTERVENTION_PLAN_CREATED";
  public static final String INTERVENTION_PLAN_UPDATED = "INTERVENTION_PLAN_UPDATED";
  public static final String SUPPORT_REPORT_ADDED = "SUPPORT_REPORT_ADDED";
  public static final String SUPPORT_REQUEST_CREATED = "SUPPORT_REQUEST_CREATED";
  public static final String SUPPORT_REQUEST_UPDATED = "SUPPORT_REQUEST_UPDATED";

  public static final String AGGREGATE_STUDENT = "STUDENT";
  public static final String AGGREGATE_ALERT = "ALERT";
  public static final String AGGREGATE_PLAN = "INTERVENTION_PLAN";
  public static final String AGGREGATE_REQUEST = "SUPPORT_REQUEST";

  private SupportEvents() {}
}
