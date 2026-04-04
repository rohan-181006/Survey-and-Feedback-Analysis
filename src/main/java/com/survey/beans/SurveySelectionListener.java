package com.survey.beans;

import java.util.EventListener;

/**
 * SurveySelectionListener - Demonstrates: Custom Event Listener (Unit 2 - JavaBeans)
 */
public interface SurveySelectionListener extends EventListener {
    void surveySelected(SurveySelectionEvent event);
}
