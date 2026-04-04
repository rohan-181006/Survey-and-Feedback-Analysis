package com.survey.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Response Model - Demonstrates: OOP, Serialization (Unit 1)
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 3L;

    private int id;
    private int surveyId;
    private int questionId;
    private String respondentName;
    private String answerText;
    private int ratingValue;     // for RATING type
    private Date submittedAt;

    public Response() {
        this.submittedAt = new Date();
    }

    public Response(int surveyId, int questionId, String respondentName, String answerText) {
        this();
        this.surveyId = surveyId;
        this.questionId = questionId;
        this.respondentName = respondentName;
        this.answerText = answerText;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSurveyId() { return surveyId; }
    public void setSurveyId(int surveyId) { this.surveyId = surveyId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getRespondentName() { return respondentName; }
    public void setRespondentName(String name) { this.respondentName = name; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public int getRatingValue() { return ratingValue; }
    public void setRatingValue(int ratingValue) { this.ratingValue = ratingValue; }

    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }

    @Override
    public String toString() {
        return respondentName + ": " + answerText;
    }
}
