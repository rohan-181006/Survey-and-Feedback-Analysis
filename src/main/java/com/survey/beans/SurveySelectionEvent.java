package com.survey.beans;

import java.util.EventObject;
import java.util.EventListener;

/**
 * SurveySelectionEvent - Demonstrates: Custom Event Types (Unit 2 - JavaBeans)
 */
public class SurveySelectionEvent extends EventObject {
    private static final long serialVersionUID = 11L;

    private final int surveyId;
    private final String surveyTitle;

    public SurveySelectionEvent(Object source, int surveyId, String surveyTitle) {
        super(source);
        this.surveyId = surveyId;
        this.surveyTitle = surveyTitle;
    }

    public int getSurveyId() { return surveyId; }
    public String getSurveyTitle() { return surveyTitle; }
}
