package co.edu.icesi.student360.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.icesi.student360.common.identity.IdentityHeaders;
import co.edu.icesi.student360.common.logging.Correlation;
import co.edu.icesi.student360.common.security.ServiceIdentity;
import co.edu.icesi.student360.common.security.ServiceTokenProvider;
import co.edu.icesi.student360.common.security.local.LocalServiceTokenValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Contract v2: a low entry (all three dimensions DIFFICULT) for the at-risk student produces an
 * explainable HIGH alert; drafts save and restore without evaluating the rule; the advisor overview
 * classifies risk per student; alerts, plans, reports and requests are authorized by active
 * assignment; every write leaves its outbox event and audit record.
 */
@SpringBootTest(
    properties = {
      "SUPPORT_DB_PASSWORD=unused-overridden-by-testcontainers",
      "SERVICE_TOKEN_SECRET=" + SupportFlowIntegrationTest.SECRET,
      "PSEUDONYM_SECRET=pseudonym-secret-for-tests-0123456789abcdef"
    })
@AutoConfigureMockMvc
@Testcontainers
class SupportFlowIntegrationTest {

  static final String SECRET = "0123456789abcdef0123456789abcdef-test-only";

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16").withInitScript("db/test-init.sql");

  static final MockWebServer SOURCES = new MockWebServer();

  private static final UUID MARIA = UUID.fromString("11111111-1111-1111-1111-000000001003");
  private static final UUID ANA = UUID.fromString("11111111-1111-1111-1111-000000001001");
  private static final UUID CARLOS = UUID.fromString("22222222-2222-2222-2222-000000002001");
  private static final UUID DIANA = UUID.fromString("22222222-2222-2222-2222-000000002002");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private ServiceTokenProvider tokens;
  @Autowired private ObjectMapper json;

  @BeforeAll
  static void startSources() throws IOException {
    SOURCES.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            String path = request.getPath() == null ? "" : request.getPath();
            if (path.startsWith("/api/core/students/summaries")) {
              return json(
                  "[{\"id\":\"S-1003\",\"code\":\"2025145032\","
                      + "\"fullName\":\"María Rojas\","
                      + "\"program\":{\"code\":\"PSI\",\"name\":\"Psychology\"},"
                      + "\"currentSemester\":7,"
                      + "\"academicStanding\":\"AT_RISK\",\"overdue\":true,\"daysOverdue\":62,"
                      + "\"outstandingBalance\":1240000.00,"
                      + "\"updatedAt\":\"2026-08-30T10:00:00Z\"}]");
            }
            if (path.startsWith("/api/core/students/S-1003/financial-status")) {
              return json(
                  "{\"studentId\":\"S-1003\",\"overdueBalance\":4100000.00,"
                      + "\"daysOverdue\":62,\"financialHold\":true,"
                      + "\"outstandingBalance\":6800000.00}");
            }
            if (path.startsWith("/api/core/students/S-1001/financial-status")) {
              return json(
                  "{\"studentId\":\"S-1001\",\"overdueBalance\":0.00,\"daysOverdue\":0,"
                      + "\"financialHold\":false,\"outstandingBalance\":0.00}");
            }
            if (path.startsWith("/api/lms/students/S-1003/signals")) {
              return json(
                  "{\"studentId\":\"S-1003\",\"daysSinceLastAccess\":21,"
                      + "\"onTimeSubmissionRate\":0.40,\"coursesWithoutActivity\":2,"
                      + "\"activeCourses\":3}");
            }
            if (path.startsWith("/api/lms/students/S-1001/signals")) {
              return json(
                  "{\"studentId\":\"S-1001\",\"daysSinceLastAccess\":1,"
                      + "\"onTimeSubmissionRate\":1.00,\"coursesWithoutActivity\":0,"
                      + "\"activeCourses\":3}");
            }
            return new MockResponse().setResponseCode(404);
          }
        });
    SOURCES.start();
  }

  @AfterAll
  static void stopSources() throws IOException {
    SOURCES.shutdown();
  }

  @DynamicPropertySource
  static void pointClientsAtTheMock(DynamicPropertyRegistry registry) {
    registry.add("student360.clients.core-service.url", () -> SOURCES.url("/").toString());
    registry.add("student360.clients.lms-service.url", () -> SOURCES.url("/").toString());
  }

  @BeforeEach
  void clean() throws InterruptedException {
    while (SOURCES.takeRequest(50, TimeUnit.MILLISECONDS) != null) {
      // drain requests recorded by earlier tests
    }
    jdbc.update("DELETE FROM audit.audit_record");
    jdbc.update("DELETE FROM support.outbox_event");
    jdbc.update("DELETE FROM support.support_request");
    jdbc.update("DELETE FROM support.support_report");
    jdbc.update("DELETE FROM support.intervention_plan");
    jdbc.update("DELETE FROM support.alert");
    jdbc.update("DELETE FROM support.wellbeing_entry_dimension");
    jdbc.update("DELETE FROM support.wellbeing_entry");
  }

  @Test
  void shouldGenerateAnExplainableHighAlertFromConvergentSignals() throws Exception {
    int before = SOURCES.getRequestCount();

    String body =
        mockMvc
            .perform(
                as(MARIA, "STUDENT", "S-1003", post(entriesPath("S-1003")), "gate5-entry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sentEntry("DIFFICULT", "DIFFICULT", "DIFFICULT")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.alertGenerated").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String alertId = json.readTree(body).path("alertId").asText();

    // both sources were consulted synchronously, on behalf of the student, with a service token
    assertThat(SOURCES.getRequestCount() - before).isEqualTo(2);
    for (int i = 0; i < 2; i++) {
      RecordedRequest call = SOURCES.takeRequest(2, TimeUnit.SECONDS);
      assertThat(call).isNotNull();
      assertThat(call.getHeader(Correlation.REQUEST_ID_HEADER)).isEqualTo("gate5-entry");
      assertThat(call.getHeader(IdentityHeaders.EXTERNAL_REFERENCE)).isEqualTo("S-1003");
      String audience = call.getPath().startsWith("/api/core") ? "core-service" : "lms-service";
      ServiceIdentity caller =
          new LocalServiceTokenValidator(
                  audience, SECRET.getBytes(StandardCharsets.UTF_8), Clock.systemUTC())
              .validate(call.getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length()));
      assertThat(caller.issuer()).isEqualTo("support-service");
    }

    Map<String, Object> alert =
        jdbc.queryForMap("SELECT * FROM support.alert WHERE id = ?::uuid", alertId);
    assertThat(alert)
        .containsEntry("severity", "HIGH")
        .containsEntry("status", "OPEN")
        .containsEntry("student_reference", "S-1003");
    assertThat(alert.get("created_by")).as("the rule, not an advisor, raised it").isNull();
    JsonNode signals = json.readTree(alert.get("triggering_signals").toString());
    assertThat(signals.path("wellbeingLevel").asInt()).isEqualTo(1);
    assertThat(signals.path("daysSinceLastAccess").asInt()).isEqualTo(21);
    assertThat(signals.path("overdueBalance").decimalValue()).isEqualByComparingTo("4100000.00");
    assertThat(json.convertValue(signals.path("firedConditions"), List.class))
        .containsExactly(
            "LOW_WELLBEING",
            "NO_RECENT_LMS_ACCESS",
            "LOW_ON_TIME_SUBMISSION_RATE",
            "OVERDUE_BALANCE");
    assertThat(
            jdbc.queryForMap(
                "SELECT type, status FROM support.intervention_plan WHERE alert_id = ?::uuid",
                alertId))
        .containsEntry("type", "INTEGRAL_SUPPORT")
        .containsEntry("status", "PROPOSED");

    // the entry is stored under a pseudonym, never the student id; notes never leave the table
    Map<String, Object> entry =
        jdbc.queryForMap("SELECT student_pseudonym, level, status FROM support.wellbeing_entry");
    assertThat(entry.get("student_pseudonym").toString()).hasSize(64).doesNotContain("S-1003");
    assertThat(entry).containsEntry("status", "SENT");
    assertThat(jdbc.queryForList("SELECT dimension, mood FROM support.wellbeing_entry_dimension"))
        .hasSize(3);

    List<String> events =
        jdbc.queryForList(
            "SELECT event_type FROM support.outbox_event ORDER BY created_at", String.class);
    assertThat(events)
        .as("events in the order they happened")
        .containsExactly(
            "WELLBEING_ENTRY_RECORDED", "ALERT_GENERATED", "INTERVENTION_PLAN_CREATED");
    String payload =
        jdbc.queryForObject(
            "SELECT payload::text FROM support.outbox_event WHERE event_type = 'ALERT_GENERATED'",
            String.class);
    JsonNode envelope = json.readTree(payload);
    assertThat(envelope.path("requestId").asText()).isEqualTo("gate5-entry");
    assertThat(envelope.path("data").path("severity").asText()).isEqualTo("HIGH");

    Map<String, Object> audit = jdbc.queryForMap("SELECT * FROM audit.audit_record");
    assertThat(audit)
        .containsEntry("action", "RECORD_WELLBEING_ENTRY")
        .containsEntry("record_type", "STATE_CHANGE")
        .containsEntry("authorization_basis", "SELF")
        .containsEntry("outcome", "ALLOWED");
  }

  @Test
  void shouldSaveAndRestoreADraftWithoutEvaluatingTheRule() throws Exception {
    int before = SOURCES.getRequestCount();
    mockMvc
        .perform(
            as(MARIA, "STUDENT", "S-1003", post(entriesPath("S-1003")), "gate5-draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(draftEntry()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.alertGenerated").value(false));

    assertThat(SOURCES.getRequestCount() - before)
        .as("a draft never consults the sources")
        .isZero();
    assertThat(jdbc.queryForList("SELECT id FROM support.alert")).isEmpty();

    String restored =
        mockMvc
            .perform(
                as(
                    MARIA,
                    "STUDENT",
                    "S-1003",
                    get("/api/support/students/S-1003/wellbeing-entries/draft"),
                    "gate5-draft-get"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(json.readTree(restored).path("status").asText()).isEqualTo("DRAFT");
    assertThat(json.readTree(restored).path("dimensions").size()).isEqualTo(1);
  }

  @Test
  void shouldSendAnExistingDraftAndThenRejectFurtherEdits() throws Exception {
    String created =
        mockMvc
            .perform(
                as(MARIA, "STUDENT", "S-1003", post(entriesPath("S-1003")), "gate5-draft-2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(draftEntry()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String entryId = json.readTree(created).path("entryId").asText();

    mockMvc
        .perform(
            as(
                    MARIA,
                    "STUDENT",
                    "S-1003",
                    put("/api/support/students/S-1003/wellbeing-entries/" + entryId),
                    "gate5-send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentEntry("GOOD", "GOOD", "GOOD")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SENT"))
        .andExpect(jsonPath("$.alertGenerated").value(false));

    mockMvc
        .perform(
            as(
                    MARIA,
                    "STUDENT",
                    "S-1003",
                    put("/api/support/students/S-1003/wellbeing-entries/" + entryId),
                    "gate5-resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentEntry("GOOD", "GOOD", "GOOD")))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldShowTheAlertOnlyInTheAssignedAdvisorsInboxAndAuditTheDetailAccess() throws Exception {
    String alertId = lowEntryFor(MARIA, "S-1003");
    jdbc.update("DELETE FROM audit.audit_record");

    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/alerts"),
                "gate5-inbox-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(alertId))
        .andExpect(jsonPath("$[0].severity").value("HIGH"));
    mockMvc
        .perform(
            as(DIANA, "ADVISOR", "A-2002", get("/api/support/advisors/me/alerts"), "gate5-inbox-b"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/alerts/" + alertId),
                "gate5-detail"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentId").value("S-1003"))
        .andExpect(jsonPath("$.triggeringSignals.firedConditions.length()").value(4))
        .andExpect(jsonPath("$.interventionPlan.type").value("INTEGRAL_SUPPORT"));

    // negative scenario A: an advisor without an active assignment (A-2002's is expired) → 403
    mockMvc
        .perform(
            as(
                DIANA,
                "ADVISOR",
                "A-2002",
                get("/api/support/advisors/me/alerts/" + alertId),
                "gate5-denied"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.requestId").value("gate5-denied"));

    List<Map<String, Object>> trail =
        jdbc.queryForList(
            "SELECT action, outcome, authorization_basis, request_id, actor_id, subject_id"
                + " FROM audit.audit_record ORDER BY id");
    assertThat(trail)
        .extracting("action")
        .containsExactly(
            "LIST_ALERT_INBOX", "LIST_ALERT_INBOX", "READ_ALERT_DETAIL", "READ_ALERT_DETAIL");
    assertThat(trail.get(2))
        .containsEntry("outcome", "ALLOWED")
        .containsEntry("authorization_basis", "ASSIGNMENT")
        .containsEntry("actor_id", CARLOS)
        .containsEntry("subject_id", alertId);
    assertThat(trail.get(3))
        .containsEntry("outcome", "DENIED")
        .containsEntry("authorization_basis", "NONE")
        .containsEntry("actor_id", DIANA)
        .containsEntry("request_id", "gate5-denied");
  }

  @Test
  void shouldClassifyTheAdvisorOverviewFromConvergentAndOfficialSignals() throws Exception {
    lowEntryFor(MARIA, "S-1003");

    String body =
        mockMvc
            .perform(
                as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    get("/api/support/advisors/me/students"),
                    "gate5-overview"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode students = json.readTree(body).path("students");
    JsonNode maria = firstWithId(students, "S-1003");
    assertThat(maria.path("academicStatus").asText()).isEqualTo("AT_RISK");
    assertThat(maria.path("financialStatus").asText()).isEqualTo("AT_RISK");
    assertThat(maria.path("emotionalStatus").asText()).isEqualTo("AT_RISK");
    assertThat(maria.path("overallRisk").asText()).isEqualTo("HIGH");
    assertThat(maria.path("openAlertId").isTextual()).isTrue();
    // sorted highest risk first
    assertThat(students.get(0).path("studentId").asText()).isEqualTo("S-1003");
  }

  @Test
  void shouldExposeTheFullStudentCaseWithPartialDegradation() throws Exception {
    lowEntryFor(MARIA, "S-1003");

    String body =
        mockMvc
            .perform(
                as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    get("/api/support/advisors/me/students/S-1003"),
                    "gate5-case"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode view = json.readTree(body);
    assertThat(view.path("activeAlert").path("severity").asText()).isEqualTo("HIGH");
    assertThat(view.path("wellbeing").path("currentLevelLabel").asText()).isEqualTo("LOW");
    // the profile source is not stubbed in this test: the section degrades, the rest still answers
    assertThat(view.has("student")).as("null sections are omitted, not null-valued").isFalse();
    assertThat(json.convertValue(view.path("unavailableSources"), List.class))
        .contains("core-service");
  }

  @Test
  void shouldNotAlertWhenSignalsDoNotConverge() throws Exception {
    mockMvc
        .perform(
            as(ANA, "STUDENT", "S-1001", post(entriesPath("S-1001")), "gate5-ana")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentEntry("DIFFICULT", "GOOD", "GOOD")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.alertGenerated").value(false));

    assertThat(jdbc.queryForList("SELECT id FROM support.alert")).isEmpty();
    assertThat(jdbc.queryForList("SELECT event_type FROM support.outbox_event", String.class))
        .containsExactly("WELLBEING_ENTRY_RECORDED");
  }

  @Test
  void shouldDegradeWhenASourceIsDownInsteadOfRejectingTheEntry() throws Exception {
    // S-1002 is unknown to the mock: both sources answer 404 → evaluated in degraded mode.
    mockMvc
        .perform(
            as(
                    UUID.randomUUID(),
                    "STUDENT",
                    "S-1002",
                    post(entriesPath("S-1002")),
                    "gate5-degraded")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentEntry("FAIR", "FAIR", "FAIR")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.alertGenerated").value(false));

    assertThat(jdbc.queryForList("SELECT level FROM support.wellbeing_entry", Integer.class))
        .containsExactly(2);
  }

  @Test
  void shouldRefuseAStudentRecordingForAnotherStudent() throws Exception {
    mockMvc
        .perform(
            as(ANA, "STUDENT", "S-1001", post(entriesPath("S-1003")), "gate5-forbidden")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentEntry("DIFFICULT", "DIFFICULT", "DIFFICULT")))
        .andExpect(status().isForbidden());

    assertThat(jdbc.queryForList("SELECT id FROM support.wellbeing_entry")).isEmpty();
    assertThat(jdbc.queryForMap("SELECT outcome FROM audit.audit_record"))
        .containsEntry("outcome", "DENIED");
  }

  @Test
  void shouldRecordASupportReportAndAcknowledgeTheAlert() throws Exception {
    String alertId = lowEntryFor(MARIA, "S-1003");

    mockMvc
        .perform(
            as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    post("/api/support/advisors/me/alerts/" + alertId + "/reports"),
                    "gate5-report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"content\":\"Met the student; agreed on a payment plan"
                        + " and weekly check-ins.\"}"))
        .andExpect(status().isCreated());

    assertThat(jdbc.queryForMap("SELECT status FROM support.alert WHERE id = ?::uuid", alertId))
        .containsEntry("status", "ACKNOWLEDGED");
    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/alerts/" + alertId),
                "gate5-report-read"))
        .andExpect(jsonPath("$.reports.length()").value(1))
        .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/reports"),
                "gate5-reports-list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].studentId").value("S-1003"));
  }

  @Test
  void shouldLetAnAdvisorRaiseAManualAlertAndCloseIt() throws Exception {
    String body =
        mockMvc
            .perform(
                as(
                        CARLOS,
                        "ADVISOR",
                        "A-2001",
                        post("/api/support/advisors/me/students/S-1003/alerts"),
                        "gate5-manual")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"severity\":\"MEDIUM\","
                            + "\"reason\":\"Missed two consecutive check-ins.\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String alertId = json.readTree(body).path("id").asText();

    Map<String, Object> alert =
        jdbc.queryForMap("SELECT * FROM support.alert WHERE id = ?::uuid", alertId);
    assertThat(alert).containsEntry("source", "ADVISOR").containsEntry("created_by", "A-2001");
    JsonNode signals = json.readTree(alert.get("triggering_signals").toString());
    assertThat(json.convertValue(signals.path("firedConditions"), List.class))
        .containsExactly("ADVISOR_JUDGEMENT");
    assertThat(signals.path("reason").asText()).contains("check-ins");

    mockMvc
        .perform(
            as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    patch("/api/support/advisors/me/alerts/" + alertId),
                    "gate5-close")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CLOSED\"}"))
        .andExpect(status().isOk());
    assertThat(jdbc.queryForMap("SELECT status FROM support.alert WHERE id = ?::uuid", alertId))
        .containsEntry("status", "CLOSED");

    // Diana is only assigned to S-1006, not S-1003: she may not raise an alert on her.
    mockMvc
        .perform(
            as(
                    DIANA,
                    "ADVISOR",
                    "A-2002",
                    post("/api/support/advisors/me/students/S-1003/alerts"),
                    "gate5-manual-denied")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"severity\":\"MEDIUM\",\"reason\":\"x\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldCreateAndManageAnInterventionPlanWithoutAnAlert() throws Exception {
    String body =
        mockMvc
            .perform(
                as(
                        CARLOS,
                        "ADVISOR",
                        "A-2001",
                        post("/api/support/advisors/me/students/S-1003/intervention-plans"),
                        "gate5-plan")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"type\":\"ACADEMIC_FOLLOW_UP\",\"description\":\"Weekly tutoring.\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String planId = json.readTree(body).path("id").asText();
    assertThat(
            jdbc.queryForMap(
                "SELECT alert_id, status FROM support.intervention_plan WHERE id = ?::uuid",
                planId))
        .containsEntry("alert_id", null)
        .containsEntry("status", "PROPOSED");

    mockMvc
        .perform(
            as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    patch("/api/support/advisors/me/intervention-plans/" + planId),
                    "gate5-accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/intervention-plans"),
                "gate5-plans-list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$[0].studentId").value("S-1003"));
  }

  @Test
  void shouldCreateAndResolveASupportRequest() throws Exception {
    String body =
        mockMvc
            .perform(
                as(
                        CARLOS,
                        "ADVISOR",
                        "A-2001",
                        post("/api/support/advisors/me/students/S-1003/requests"),
                        "gate5-request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"type\":\"FINANCIAL_WELLBEING_REFERRAL\","
                            + "\"description\":\"Referral for a payment plan.\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String requestId = json.readTree(body).path("id").asText();

    mockMvc
        .perform(
            as(
                    CARLOS,
                    "ADVISOR",
                    "A-2001",
                    patch("/api/support/advisors/me/requests/" + requestId),
                    "gate5-resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RESOLVED\",\"resolution\":\"Payment plan approved.\"}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            as(
                CARLOS,
                "ADVISOR",
                "A-2001",
                get("/api/support/advisors/me/requests"),
                "gate5-requests-list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("RESOLVED"))
        .andExpect(jsonPath("$[0].resolution").value("Payment plan approved."));

    List<String> events =
        jdbc.queryForList(
            "SELECT event_type FROM support.outbox_event ORDER BY created_at", String.class);
    assertThat(events).containsExactly("SUPPORT_REQUEST_CREATED", "SUPPORT_REQUEST_UPDATED");
  }

  private String lowEntryFor(UUID userId, String studentReference) throws Exception {
    String body =
        mockMvc
            .perform(
                as(
                        userId,
                        "STUDENT",
                        studentReference,
                        post(entriesPath(studentReference)),
                        "gate5-setup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sentEntry("DIFFICULT", "DIFFICULT", "DIFFICULT")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(body).path("alertId").asText();
  }

  private static String entriesPath(String studentReference) {
    return "/api/support/students/" + studentReference + "/wellbeing-entries";
  }

  private static String sentEntry(String economic, String academic, String emotional) {
    return "{\"status\":\"SENT\",\"dimensions\":["
        + dimension("ECONOMIC", economic, "[\"PAYMENT_PLAN\"]")
        + ","
        + dimension("ACADEMIC", academic, "[\"TUTORING\"]")
        + ","
        + dimension("EMOTIONAL", emotional, "[\"TALK_TO_SOMEONE\"]")
        + "]}";
  }

  private static String draftEntry() {
    return "{\"status\":\"DRAFT\",\"dimensions\":[" + dimension("ECONOMIC", "FAIR", "[]") + "]}";
  }

  private static String dimension(String dimension, String mood, String needs) {
    return "{\"dimension\":\""
        + dimension
        + "\",\"mood\":\""
        + mood
        + "\",\"needs\":"
        + needs
        + "}";
  }

  private static MockResponse json(String body) {
    return new MockResponse().setBody(body).addHeader("Content-Type", "application/json");
  }

  private static JsonNode firstWithId(JsonNode array, String studentId) {
    for (JsonNode node : array) {
      if (studentId.equals(node.path("studentId").asText())) {
        return node;
      }
    }
    throw new AssertionError("No row for " + studentId + " in " + array);
  }

  private MockHttpServletRequestBuilder as(
      UUID userId,
      String role,
      String reference,
      MockHttpServletRequestBuilder request,
      String requestId) {
    return request
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.tokenFor("support-service"))
        .header(Correlation.REQUEST_ID_HEADER, requestId)
        .header(IdentityHeaders.USER_ID, userId.toString())
        .header(IdentityHeaders.USER_ROLES, role)
        .header(IdentityHeaders.EXTERNAL_REFERENCE, reference);
  }
}
