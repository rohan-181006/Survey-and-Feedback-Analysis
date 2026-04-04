package com.survey.dao;

import com.survey.model.Response;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 * ResponseDAO - Demonstrates: JDBC PreparedStatements, SQL aggregation (Unit 3)
 */
public class ResponseDAO {
    private static final Logger LOGGER = Logger.getLogger(ResponseDAO.class.getName());
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean insertResponses(List<Response> responses) {
        // Batch insert - Unit 3 best practice
        String sql = "INSERT INTO responses (survey_id, question_id, respondent_name, answer_text, rating_value) VALUES (?,?,?,?,?)";
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Response r : responses) {
                    ps.setInt(1, r.getSurveyId());
                    ps.setInt(2, r.getQuestionId());
                    ps.setString(3, r.getRespondentName());
                    ps.setString(4, r.getAnswerText());
                    ps.setInt(5, r.getRatingValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Batch insert responses failed", e);
            return false;
        }
    }

    /**
     * Get average rating per question for a survey
     */
    public Map<String, Double> getAverageRatings(int surveyId) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = """
            SELECT q.question_text, AVG(r.rating_value) as avg_rating
            FROM responses r
            JOIN questions q ON r.question_id = q.id
            WHERE r.survey_id = ? AND q.question_type = 'RATING' AND r.rating_value > 0
            GROUP BY r.question_id, q.question_text
            ORDER BY q.order_index
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("question_text"), rs.getDouble("avg_rating"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Get avg ratings failed", e);
        }
        return result;
    }

    /**
     * Get choice distribution for multiple-choice questions
     */
    public Map<String, Map<String, Integer>> getChoiceDistribution(int surveyId) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String sql = """
            SELECT q.question_text, r.answer_text, COUNT(*) as count
            FROM responses r
            JOIN questions q ON r.question_id = q.id
            WHERE r.survey_id = ? AND q.question_type IN ('MULTIPLE_CHOICE','YES_NO')
            GROUP BY r.question_id, q.question_text, r.answer_text
            ORDER BY q.order_index, count DESC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String question = rs.getString("question_text");
                String answer = rs.getString("answer_text");
                int count = rs.getInt("count");
                result.computeIfAbsent(question, k -> new LinkedHashMap<>()).put(answer, count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Get choice distribution failed", e);
        }
        return result;
    }

    /**
     * Get text responses for a survey
     */
    public List<String> getTextResponses(int surveyId) {
        List<String> list = new ArrayList<>();
        String sql = """
            SELECT r.respondent_name, q.question_text, r.answer_text
            FROM responses r
            JOIN questions q ON r.question_id = q.id
            WHERE r.survey_id = ? AND q.question_type = 'TEXT'
            ORDER BY r.submitted_at DESC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("respondent_name") + " — " + rs.getString("answer_text"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Get text responses failed", e);
        }
        return list;
    }

    /**
     * Total respondents for a survey
     */
    public int getRespondentCount(int surveyId) {
        String sql = "SELECT COUNT(DISTINCT respondent_name) FROM responses WHERE survey_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Count failed", e); }
        return 0;
    }

    /**
     * Overall satisfaction score across all rating questions for a survey
     */
    public double getOverallScore(int surveyId) {
        String sql = """
            SELECT AVG(r.rating_value) as overall
            FROM responses r
            JOIN questions q ON r.question_id = q.id
            WHERE r.survey_id = ? AND q.question_type = 'RATING' AND r.rating_value > 0
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Score failed", e); }
        return 0.0;
    }
}
