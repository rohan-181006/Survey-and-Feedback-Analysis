package com.survey.beans;

import java.beans.*;
import java.io.Serializable;

public class SurveyBean implements Serializable {
    private static final long serialVersionUID = 10L;

    // PropertyChangeSupport for Bound Properties (Unit 2 - JavaBeans)
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String selectedSurveyTitle = "";
    private int selectedSurveyId = -1;
    private int responseCount = 0;
    private double satisfactionScore = 0.0;
    private boolean analysisDirty = false;

    // ── Bound Property: selectedSurveyTitle ─────────────────────────────────
    public String getSelectedSurveyTitle() { return selectedSurveyTitle; }
    public void setSelectedSurveyTitle(String newTitle) {
        String old = this.selectedSurveyTitle;
        this.selectedSurveyTitle = newTitle;
        pcs.firePropertyChange("selectedSurveyTitle", old, newTitle);
    }

    // ── Bound Property: selectedSurveyId ────────────────────────────────────
    public int getSelectedSurveyId() { return selectedSurveyId; }
    public void setSelectedSurveyId(int newId) {
        int old = this.selectedSurveyId;
        this.selectedSurveyId = newId;
        this.analysisDirty = true;
        pcs.firePropertyChange("selectedSurveyId", old, newId);
    }

    // ── Bound Property: responseCount ────────────────────────────────────────
    public int getResponseCount() { return responseCount; }
    public void setResponseCount(int count) {
        int old = this.responseCount;
        this.responseCount = count;
        pcs.firePropertyChange("responseCount", old, count);
    }

    // ── Bound Property: satisfactionScore ────────────────────────────────────
    public double getSatisfactionScore() { return satisfactionScore; }
    public void setSatisfactionScore(double score) {
        double old = this.satisfactionScore;
        this.satisfactionScore = score;
        pcs.firePropertyChange("satisfactionScore", old, score);
    }

    public boolean isAnalysisDirty() { return analysisDirty; }
    public void setAnalysisDirty(boolean dirty) { this.analysisDirty = dirty; }

    // ── PropertyChangeListener support (Unit 2 - Event Model) ────────────────
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    // ── Custom Business Event (Unit 2 - Adding Custom Event Types) ────────────
    private final java.util.List<SurveySelectionListener> selectionListeners = new java.util.ArrayList<>();

    public void addSurveySelectionListener(SurveySelectionListener l) {
        selectionListeners.add(l);
    }

    public void removeSurveySelectionListener(SurveySelectionListener l) {
        selectionListeners.remove(l);
    }

    public void fireSurveySelected(int surveyId, String title) {
        SurveySelectionEvent event = new SurveySelectionEvent(this, surveyId, title);
        for (SurveySelectionListener l : selectionListeners) {
            l.surveySelected(event);
        }
    }

    @Override
    public String toString() {
        return "SurveyBean[id=" + selectedSurveyId + ", title=" + selectedSurveyTitle + "]";
    }
}
