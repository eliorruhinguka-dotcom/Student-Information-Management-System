package rw.university.sims.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import rw.university.sims.dto.response.*;
import java.util.List;

@Repository
public class JdbcReportQueryRepository implements ReportQueryRepository {
    private final JdbcTemplate jdbc;

    public JdbcReportQueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<EnrolmentByProgrammeRow> enrolmentByProgramme(
            Long semesterId, Long departmentId, Long lecturerId) {
        String sql = """
            SELECT p.id AS programme_id, p.code AS programme_code, p.name AS programme_name,
                   d.id AS department_id, d.code AS department_code, d.name AS department_name,
                   COUNT(DISTINCT CASE WHEN s.status = 'ACTIVE' AND (CAST(? AS BIGINT) IS NULL OR co.id IS NOT NULL) THEN s.id END) AS active_student_count,
                   COUNT(DISTINCT CASE WHEN s.status = 'ACTIVE' AND co.id IS NOT NULL THEN s.id END) AS registered_student_count,
                   COUNT(DISTINCT CASE WHEN s.status = 'ACTIVE' AND co.id IS NOT NULL THEN cr.id END) AS registration_count
            FROM programme p
            JOIN department d ON d.id = p.department_id
            LEFT JOIN student s ON s.programme_id = p.id
            LEFT JOIN course_registrations cr
                   ON cr.student_id = s.id AND cr.status = 'REGISTERED'
            LEFT JOIN course_offerings co
                   ON co.id = cr.course_offering_id
                  AND co.semester_id = ?
                  AND (CAST(? AS BIGINT) IS NULL OR co.lecturer_id = ?)
            WHERE p.active = TRUE AND d.active = TRUE
              AND (CAST(? AS BIGINT) IS NULL OR d.id = ?)
            GROUP BY p.id, p.code, p.name, d.id, d.code, d.name
            ORDER BY d.code, p.code
            """;
        return jdbc.query(sql, (rs, row) -> new EnrolmentByProgrammeRow(
                rs.getLong("programme_id"), rs.getString("programme_code"), rs.getString("programme_name"),
                rs.getLong("department_id"), rs.getString("department_code"), rs.getString("department_name"),
                rs.getLong("active_student_count"), rs.getLong("registered_student_count"),
                rs.getLong("registration_count")),
                lecturerId, semesterId, lecturerId, lecturerId, departmentId, departmentId);
    }

    @Override
    public List<OfferingCapacityRow> offeringCapacity(
            Long semesterId, Long departmentId, Long lecturerId) {
        String sql = """
            SELECT co.id AS offering_id, c.course_code, c.title AS course_title,
                   d.code AS department_code, d.name AS department_name, co.section,
                   ua.full_name AS lecturer_name, co.capacity, co.registered_count,
                   (co.capacity - co.registered_count) AS seats_left,
                   CASE WHEN co.capacity = 0 THEN 0
                        ELSE ROUND((co.registered_count * 100.0 / co.capacity)::numeric, 2)
                   END AS percent_used
            FROM course_offerings co
            JOIN course c ON c.id = co.course_id
            JOIN department d ON d.id = c.department_id
            JOIN user_accounts ua ON ua.id = co.lecturer_id
            WHERE co.semester_id = ?
              AND (CAST(? AS BIGINT) IS NULL OR d.id = ?)
              AND (CAST(? AS BIGINT) IS NULL OR co.lecturer_id = ?)
            ORDER BY d.code, c.course_code, co.section
            """;
        return jdbc.query(sql, (rs, row) -> new OfferingCapacityRow(
                rs.getLong("offering_id"), rs.getString("course_code"), rs.getString("course_title"),
                rs.getString("department_code"), rs.getString("department_name"), rs.getString("section"),
                rs.getString("lecturer_name"), rs.getInt("capacity"), rs.getInt("registered_count"),
                rs.getInt("seats_left"), rs.getBigDecimal("percent_used")),
                semesterId, departmentId, departmentId, lecturerId, lecturerId);
    }

    @Override
    public List<OutstandingFeesStudentRow> outstandingFees(Long semesterId) {
        String sql = """
            WITH due AS (
                SELECT student_id, currency, SUM(amount_due) AS total_due
                FROM fee_obligation
                WHERE semester_id = ? AND status = 'ACTIVE' AND counts_toward_eligibility = TRUE
                GROUP BY student_id, currency
            ), paid AS (
                SELECT student_id, currency, SUM(amount) AS total_paid
                FROM payment
                WHERE semester_id = ? AND status = 'CONFIRMED'
                GROUP BY student_id, currency
            ), student_currency_keys AS (
                SELECT student_id, currency FROM due
                UNION
                SELECT student_id, currency FROM paid
            )
            SELECT s.id AS student_id, s.registration_number, s.first_name, s.last_name, s.email,
                   student_currency_keys.currency,
                   COALESCE(due.total_due, 0) AS total_due,
                   COALESCE(paid.total_paid, 0) AS total_paid,
                   COALESCE(due.total_due, 0) - COALESCE(paid.total_paid, 0) AS outstanding_balance,
                   CASE WHEN COALESCE(due.total_due, 0) = 0 THEN 0
                        ELSE ROUND((COALESCE(paid.total_paid, 0) * 100.0 / due.total_due)::numeric, 2)
                   END AS percent_paid
            FROM student_currency_keys
            JOIN student s ON s.id = student_currency_keys.student_id
            LEFT JOIN due ON due.student_id = student_currency_keys.student_id
                         AND due.currency = student_currency_keys.currency
            LEFT JOIN paid ON paid.student_id = student_currency_keys.student_id
                          AND paid.currency = student_currency_keys.currency
            ORDER BY s.registration_number, student_currency_keys.currency
            """;
        return jdbc.query(sql, (rs, row) -> new OutstandingFeesStudentRow(
                rs.getLong("student_id"), rs.getString("registration_number"), rs.getString("first_name"),
                rs.getString("last_name"), rs.getString("email"), rs.getString("currency").trim(),
                rs.getBigDecimal("total_due"), rs.getBigDecimal("total_paid"),
                rs.getBigDecimal("outstanding_balance"), rs.getBigDecimal("percent_paid")),
                semesterId, semesterId);
    }

    @Override
    public List<GradeDistributionRow> gradeDistribution(Long offeringId) {
        String sql = """
            SELECT r.grade, COUNT(*) AS grade_count,
                   ROUND((COUNT(*) * 100.0 / NULLIF(SUM(COUNT(*)) OVER (), 0))::numeric, 2) AS percentage
            FROM result r
            JOIN course_registrations cr ON cr.id = r.course_registration_id
            JOIN course_offerings co ON co.id = cr.course_offering_id
            WHERE co.id = ? AND r.status = 'PUBLISHED'
            GROUP BY r.grade
            ORDER BY r.grade
            """;
        return jdbc.query(sql, (rs, row) -> new GradeDistributionRow(
                rs.getString("grade"), rs.getLong("grade_count"), rs.getBigDecimal("percentage")), offeringId);
    }
}
