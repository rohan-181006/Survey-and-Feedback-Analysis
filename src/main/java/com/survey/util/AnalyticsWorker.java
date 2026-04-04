package com.survey.util;

import com.survey.dao.ResponseDAO;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * AnalyticsWorker - Demonstrates: Multithreading, Thread Life Cycle (Unit 1)
 * Uses ExecutorService for background analytics computation.
 */
public class AnalyticsWorker implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(AnalyticsWorker.class.getName());

    public interface AnalyticsCallback {
        void onComplete(AnalyticsResult result);
        void onError(Exception e);
    }

    private final int surveyId;
    private final ResponseDAO responseDAO;
    private final AnalyticsCallback callback;

    // Thread pool (Unit 1 - Thread Life Cycle)
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AnalyticsWorker(int surveyId, AnalyticsCallback callback) {
        this.surveyId = surveyId;
        this.responseDAO = new ResponseDAO();
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("AnalyticsWorker started for survey " + surveyId + " on thread: " + Thread.currentThread().getName());

            // Simulate compute time
            Thread.sleep(300);

            AnalyticsResult result = new AnalyticsResult();
            result.surveyId = surveyId;
            result.respondentCount = responseDAO.getRespondentCount(surveyId);
            result.overallScore = responseDAO.getOverallScore(surveyId);
            result.averageRatings = responseDAO.getAverageRatings(surveyId);
            result.choiceDistribution = responseDAO.getChoiceDistribution(surveyId);
            result.textResponses = responseDAO.getTextResponses(surveyId);

            LOGGER.info("AnalyticsWorker completed for survey " + surveyId);
            callback.onComplete(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "AnalyticsWorker interrupted", e);
            callback.onError(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "AnalyticsWorker error", e);
            callback.onError(e);
        }
    }

    /**
     * Submit this worker to the thread pool (Unit 1 - Creating Multithreaded Programs)
     */
    public static void submit(AnalyticsWorker worker) {
        executor.submit(worker);
    }

    public static void shutdown() {
        executor.shutdown();
    }

    /**
     * Inner class - encapsulates analytics result data
     */
    public static class AnalyticsResult {
        public int surveyId;
        public int respondentCount;
        public double overallScore;
        public Map<String, Double> averageRatings = new LinkedHashMap<>();
        public Map<String, Map<String, Integer>> choiceDistribution = new LinkedHashMap<>();
        public List<String> textResponses = new ArrayList<>();
    }
}
