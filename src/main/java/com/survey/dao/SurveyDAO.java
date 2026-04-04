package com.survey.dao;

import com.survey.model.Survey;
import com.survey.model.Question;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 * SurveyDAO - Demonstrates: JDBC API (Unit 3), Exception Handling (Unit 1),
 * PreparedStatements, Transactions, Best DB Practices.
 */
public class SurveyDAO {
    private static final Logger LOGGER = Logger.getLogger(SurveyDAO.class.getName());
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── CREATE ──────────────────────────────────────────────────────────────

    public int insertSurvey(Survey survey) {
        String sql = "INSERT INTO surveys (title, description, category) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, survey.getTitle());
            ps.setString(2, survey.getDescription());
            ps.setString(3, survey.getCategory());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                survey.setId(id);
                return id;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Insert survey failed", e);
        }
        return -1;
    }

    public int insertQuestion(Question q) {
        String optionsStr = String.join("|", q.getOptions());
        String sql = "INSERT INTO questions (survey_id, question_text, question_type, options, required, order_index) VALUES (?,?,?,?,?,?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, q.getSurveyId());
            ps.setString(2, q.getText());
            ps.setString(3, q.getType().name());
            ps.setString(4, optionsStr);
            ps.setInt(5, q.isRequired() ? 1 : 0);
            ps.setInt(6, q.getOrderIndex());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Insert question failed", e);
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Survey> getAllSurveys() {
        List<Survey> list = new ArrayList<>();
        String sql = "SELECT * FROM surveys WHERE active = 1 ORDER BY created_at DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Survey s = new Survey();
                s.setId(rs.getInt("id"));
                s.setTitle(rs.getString("title"));
                s.setDescription(rs.getString("description"));
                s.setCategory(rs.getString("category"));
                s.setActive(rs.getInt("active") == 1);
                list.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Get all surveys failed", e);
        }
        return list;
    }

    public List<Question> getQuestionsForSurvey(int surveyId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE survey_id = ? ORDER BY order_index";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setSurveyId(rs.getInt("survey_id"));
                q.setText(rs.getString("question_text"));
                q.setType(Question.QuestionType.valueOf(rs.getString("question_type")));
                q.setRequired(rs.getInt("required") == 1);
                q.setOrderIndex(rs.getInt("order_index"));
                String opts = rs.getString("options");
                if (opts != null && !opts.isEmpty()) {
                    for (String o : opts.split("\\|")) q.addOption(o);
                }
                list.add(q);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Get questions failed", e);
        }
        return list;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean deleteSurvey(int surveyId) {
        // Demonstrates Transactions (Unit 3)
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM responses WHERE survey_id = ?");
                 PreparedStatement ps2 = conn.prepareStatement("DELETE FROM questions WHERE survey_id = ?");
                 PreparedStatement ps3 = conn.prepareStatement("DELETE FROM surveys WHERE id = ?")) {

                ps1.setInt(1, surveyId); ps1.executeUpdate();
                ps2.setInt(1, surveyId); ps2.executeUpdate();
                ps3.setInt(1, surveyId); ps3.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Delete survey failed", e);
            return false;
        }
    }

    public int getSurveyCount() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM surveys WHERE active=1")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Count failed", e); }
        return 0;
    }

    public int getResponseCount() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT respondent_name || survey_id) FROM responses")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Count failed", e); }
        return 0;
    }
}
