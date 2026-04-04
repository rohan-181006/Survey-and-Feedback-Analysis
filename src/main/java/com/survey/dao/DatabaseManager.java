package com.survey.dao;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DatabaseManager - Demonstrates: JDBC (Unit 3), Exception Handling (Unit 1)
 * Uses SQLite (embedded) so no external DB server is needed.
 * Design follows Best Practices for Programming for Databases (Unit 3).
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:survey_app.db";
    private static DatabaseManager instance;
    private Connection connection;

    // Singleton pattern (OOP Design Pattern)
    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Get or reopen connection - demonstrates JDBC connection management (Unit 3)
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC Driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database", e);
        }
        return connection;
    }

    /**
     * Initialize DB schema - demonstrates DDL via JDBC (Unit 3)
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Surveys table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS surveys (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    category TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    active INTEGER DEFAULT 1
                )
            """);

            // Questions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    survey_id INTEGER NOT NULL,
                    question_text TEXT NOT NULL,
                    question_type TEXT NOT NULL,
                    options TEXT,
                    required INTEGER DEFAULT 1,
                    order_index INTEGER DEFAULT 0,
                    FOREIGN KEY (survey_id) REFERENCES surveys(id)
                )
            """);

            // Responses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS responses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    survey_id INTEGER NOT NULL,
                    question_id INTEGER NOT NULL,
                    respondent_name TEXT,
                    answer_text TEXT,
                    rating_value INTEGER DEFAULT 0,
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (survey_id) REFERENCES surveys(id),
                    FOREIGN KEY (question_id) REFERENCES questions(id)
                )
            """);

            seedSampleData(conn);
            LOGGER.info("Database initialized successfully.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
        }
    }

    /**
     * Seed sample data for demonstration
     */
    private void seedSampleData(Connection conn) throws SQLException {
        // Check if already seeded
        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM surveys")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        // Insert sample surveys
        String[] surveys = {
            "INSERT INTO surveys (title, description, category) VALUES ('Customer Satisfaction Survey', 'Help us improve our services by sharing your experience.', 'Customer Service')",
            "INSERT INTO surveys (title, description, category) VALUES ('Employee Engagement Survey', 'Annual employee feedback to improve workplace culture.', 'HR')",
            "INSERT INTO surveys (title, description, category) VALUES ('Product Feedback Form', 'Tell us what you think about our latest product.', 'Product')"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : surveys) stmt.execute(sql);
        }

        // Insert sample questions for survey 1
        String[][] questions = {
            {"1", "How satisfied are you with our service?", "RATING", ""},
            {"1", "Which feature do you use most?", "MULTIPLE_CHOICE", "Support|Dashboard|Reports|Settings"},
            {"1", "Would you recommend us to a friend?", "YES_NO", ""},
            {"1", "Any additional comments?", "TEXT", ""},
            {"2", "How would you rate your work-life balance?", "RATING", ""},
            {"2", "What motivates you most at work?", "MULTIPLE_CHOICE", "Salary|Growth|Culture|Recognition"},
            {"2", "Do you feel valued by your team?", "YES_NO", ""},
            {"3", "Rate the product's ease of use.", "RATING", ""},
            {"3", "Which improvements would you prioritize?", "MULTIPLE_CHOICE", "Performance|UI|Features|Documentation"},
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO questions (survey_id, question_text, question_type, options, order_index) VALUES (?,?,?,?,?)")) {
            for (int i = 0; i < questions.length; i++) {
                ps.setInt(1, Integer.parseInt(questions[i][0]));
                ps.setString(2, questions[i][1]);
                ps.setString(3, questions[i][2]);
                ps.setString(4, questions[i][3]);
                ps.setInt(5, i);
                ps.executeUpdate();
            }
        }

        // Insert sample responses
        String[][] responses = {
            {"1", "1", "Alice", "5"}, {"1", "1", "Bob", "4"}, {"1", "1", "Carol", "3"},
            {"1", "1", "David", "5"}, {"1", "1", "Eve", "4"}, {"1", "1", "Frank", "2"},
            {"1", "2", "Alice", "Dashboard"}, {"1", "2", "Bob", "Reports"},
            {"1", "2", "Carol", "Support"}, {"1", "2", "David", "Dashboard"},
            {"1", "3", "Alice", "Yes"}, {"1", "3", "Bob", "Yes"},
            {"1", "3", "Carol", "No"}, {"1", "3", "David", "Yes"},
            {"2", "5", "Tom", "4"}, {"2", "5", "Sara", "3"}, {"2", "5", "Mike", "5"},
            {"2", "5", "Anna", "4"}, {"2", "5", "John", "2"},
            {"2", "6", "Tom", "Growth"}, {"2", "6", "Sara", "Culture"},
            {"2", "6", "Mike", "Salary"}, {"2", "6", "Anna", "Recognition"},
            {"3", "8", "User1", "5"}, {"3", "8", "User2", "4"}, {"3", "8", "User3", "3"},
        };

        String[] names = {"Alice","Bob","Carol","David","Eve","Frank","Tom","Sara","Mike","Anna","John","User1","User2","User3"};
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO responses (survey_id, question_id, respondent_name, answer_text, rating_value) VALUES (?,?,?,?,?)")) {
            for (String[] r : responses) {
                ps.setInt(1, Integer.parseInt(r[0]));
                ps.setInt(2, Integer.parseInt(r[1]));
                ps.setString(3, r[2]);
                ps.setString(4, r[3]);
                try { ps.setInt(5, Integer.parseInt(r[3])); } catch (NumberFormatException e) { ps.setInt(5, 0); }
                ps.executeUpdate();
            }
        }
    }

    /**
     * Close connection - resource management (Unit 3 best practice)
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing connection", e);
        }
    }
}
