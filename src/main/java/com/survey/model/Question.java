package com.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Question Model - Demonstrates: OOP, Interfaces, Serialization (Unit 1)
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 2L;

    // Enum demonstrates Java type system (Unit 1)
    public enum QuestionType {
        MULTIPLE_CHOICE("Multiple Choice"),
        TEXT("Text Response"),
        RATING("Rating (1-5)"),
        YES_NO("Yes / No");

        private final String displayName;
        QuestionType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }

    private int id;
    private int surveyId;
    private String text;
    private QuestionType type;
    private List<String> options;
    private boolean required;
    private int orderIndex;

    public Question() {
        this.options = new ArrayList<>();
        this.required = true;
    }

    public Question(int id, int surveyId, String text, QuestionType type) {
        this();
        this.id = id;
        this.surveyId = surveyId;
        this.text = text;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSurveyId() { return surveyId; }
    public void setSurveyId(int surveyId) { this.surveyId = surveyId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public void addOption(String option) { this.options.add(option); }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    @Override
    public String toString() { return text; }
}
