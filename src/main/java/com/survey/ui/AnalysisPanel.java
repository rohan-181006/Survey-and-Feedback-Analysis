package com.survey.ui;

import com.survey.beans.SurveyBean;
import com.survey.dao.ResponseDAO;
import com.survey.util.AnalyticsWorker;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.survey.ui.SurveyApp.*;

/**
 * AnalysisPanel - Demonstrates: Swing custom painting, JScrollPane,
 * JProgressBar (Unit 4 - Progress Bars), Threading (Unit 1), MVC (Unit 4)
 */
public class AnalysisPanel extends JPanel {

    private final ResponseDAO responseDAO;
    private final SurveyBean surveyBean;

    // Sub-panels
    private JLabel surveyTitleLabel;
    private JLabel respondentLabel;
    private JLabel scoreLabel;
    private JPanel chartsPanel;
    private JPanel textPanel;
    private JProgressBar loadingBar;
    private JLabel emptyLabel;

    public AnalysisPanel(ResponseDAO responseDAO, SurveyBean surveyBean) {
        this.responseDAO = responseDAO;
        this.surveyBean = surveyBean;
        setLayout(new BorderLayout(12, 12));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setBackground(BG_DARK);

        surveyTitleLabel = new JLabel("Select a survey to view analysis");
        surveyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        surveyTitleLabel.setForeground(TEXT_MAIN);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        metaRow.setBackground(BG_DARK);
        respondentLabel = makeMetaLabel("👥 Respondents: —");
        scoreLabel = makeMetaLabel("⭐ Overall Score: —");
        metaRow.add(respondentLabel);
        metaRow.add(scoreLabel);

        // Loading progress bar (Unit 4 - Progress Bars)
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setForeground(ACCENT);
        loadingBar.setBackground(BG_CARD);
        loadingBar.setString("Computing analytics...");
        loadingBar.setStringPainted(true);

        header.add(surveyTitleLabel, BorderLayout.NORTH);
        header.add(metaRow, BorderLayout.CENTER);
        header.add(loadingBar, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Empty state
        emptyLabel = new JLabel("← Select a survey from the sidebar", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emptyLabel.setForeground(TEXT_DIM);
        add(emptyLabel, BorderLayout.CENTER);

        // Charts container - initially hidden
        chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.setBackground(BG_DARK);

        // Text responses panel
        textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(BG_DARK);
        textPanel.setVisible(false);
    }

    private JLabel makeMetaLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DIM);
        return l;
    }

    /**
     * Load analysis using background thread (Unit 1 - Multithreading)
     */
    public void loadAnalysis(int surveyId, String title) {
        surveyTitleLabel.setText(title);
        respondentLabel.setText("👥 Respondents: Loading...");
        scoreLabel.setText("⭐ Overall Score: Loading...");

        // Show progress bar (Unit 4 - Progress Bars)
        loadingBar.setVisible(true);
        emptyLabel.setVisible(false);
        chartsPanel.setVisible(false);
        remove(chartsPanel);
        remove(emptyLabel);

        // Background computation (Unit 1 - Multithreaded Programs)
        AnalyticsWorker worker = new AnalyticsWorker(surveyId, new AnalyticsWorker.AnalyticsCallback() {
            @Override
            public void onComplete(AnalyticsWorker.AnalyticsResult result) {
                // Update UI on EDT (Swing thread safety)
                SwingUtilities.invokeLater(() -> {
                    loadingBar.setVisible(false);
                    renderAnalysis(result);
                });
            }

            @Override
            public void onError(Exception e) {
                SwingUtilities.invokeLater(() -> {
                    loadingBar.setVisible(false);
                    surveyTitleLabel.setText("Error loading analysis: " + e.getMessage());
                });
            }
        });
        AnalyticsWorker.submit(worker);
    }

    private void renderAnalysis(AnalyticsWorker.AnalyticsResult result) {
        respondentLabel.setText("👥 Respondents: " + result.respondentCount);
        scoreLabel.setText(String.format("⭐ Overall Score: %.2f / 5.0", result.overallScore));

        chartsPanel.removeAll();

        // ── Rating Charts ────────────────────────────────────────────────────
        if (!result.averageRatings.isEmpty()) {
            chartsPanel.add(makeSectionLabel("Rating Questions — Average Scores"));
            for (Map.Entry<String, Double> entry : result.averageRatings.entrySet()) {
                chartsPanel.add(Box.createVerticalStrut(8));
                chartsPanel.add(makeRatingBar(entry.getKey(), entry.getValue()));
            }
            chartsPanel.add(Box.createVerticalStrut(16));
        }

        // ── Choice Distribution Charts ────────────────────────────────────────
        if (!result.choiceDistribution.isEmpty()) {
            chartsPanel.add(makeSectionLabel("Choice / Yes-No Questions — Distribution"));
            for (Map.Entry<String, Map<String, Integer>> entry : result.choiceDistribution.entrySet()) {
                chartsPanel.add(Box.createVerticalStrut(10));
                chartsPanel.add(makeBarChart(entry.getKey(), entry.getValue()));
            }
            chartsPanel.add(Box.createVerticalStrut(16));
        }

        // ── Text Responses ────────────────────────────────────────────────────
        if (!result.textResponses.isEmpty()) {
            chartsPanel.add(makeSectionLabel("Text Responses"));
            for (String resp : result.textResponses) {
                chartsPanel.add(makeTextResponseCard(resp));
                chartsPanel.add(Box.createVerticalStrut(4));
            }
        }

        if (result.averageRatings.isEmpty() && result.choiceDistribution.isEmpty()) {
            JLabel noData = new JLabel("  No responses yet for this survey. Fill the survey to generate analysis.", SwingConstants.LEFT);
            noData.setForeground(TEXT_DIM);
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            chartsPanel.add(noData);
        }

        JScrollPane scroll = new JScrollPane(chartsPanel);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
        chartsPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(ACCENT);
        l.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return l;
    }

    /**
     * Rating progress bar row (Unit 4 - Sliders / Progress Bars)
     */
    private JPanel makeRatingBar(String questionText, double avgRating) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_CARD);
        row.setBorder(new EmptyBorder(10, 16, 10, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Truncate long question text
        String display = questionText.length() > 55 ? questionText.substring(0, 52) + "..." : questionText;
        JLabel ql = new JLabel(display);
        ql.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ql.setForeground(TEXT_MAIN);
        ql.setPreferredSize(new Dimension(300, 20));

        JProgressBar bar = new JProgressBar(0, 500);
        bar.setValue((int) (avgRating * 100));
        bar.setForeground(avgRating >= 4.0 ? SUCCESS : avgRating >= 3.0 ? WARNING : DANGER);
        bar.setBackground(BG_DARK);
        bar.setStringPainted(false);
        bar.setBorderPainted(false);

        JLabel score = new JLabel(String.format("%.2f / 5", avgRating));
        score.setFont(new Font("Segoe UI", Font.BOLD, 13));
        score.setForeground(avgRating >= 4.0 ? SUCCESS : avgRating >= 3.0 ? WARNING : DANGER);
        score.setPreferredSize(new Dimension(70, 20));

        row.add(ql, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(score, BorderLayout.EAST);
        return row;
    }

    /**
     * Custom bar chart panel using paintComponent (Unit 4 - Swing custom painting)
     */
    private JPanel makeBarChart(String question, Map<String, Integer> distribution) {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setBackground(BG_CARD);
        container.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1), new EmptyBorder(12, 16, 12, 16)));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        String display = question.length() > 60 ? question.substring(0, 57) + "..." : question;
        JLabel qLabel = new JLabel(display);
        qLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        qLabel.setForeground(TEXT_MAIN);
        container.add(qLabel, BorderLayout.NORTH);

        // Chart uses custom Swing painting
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int maxVal = distribution.values().stream().mapToInt(Integer::intValue).max().orElse(1);
                String[] keys = distribution.keySet().toArray(new String[0]);
                int n = keys.length;
                int w = getWidth(); int h = getHeight();
                int barW = Math.max(20, (w - 40) / n - 12);

                Color[] palette = {ACCENT, SUCCESS, WARNING, DANGER, ACCENT2,
                    new Color(255, 200, 100), new Color(100, 220, 200)};

                for (int i = 0; i < n; i++) {
                    int count = distribution.get(keys[i]);
                    int barH = (int) ((double) count / maxVal * (h - 40));
                    int x = 20 + i * (barW + 12);
                    int y = h - 30 - barH;

                    g2.setColor(palette[i % palette.length]);
                    g2.fillRoundRect(x, y, barW, barH, 6, 6);

                    // Count label
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.setColor(TEXT_MAIN);
                    g2.drawString(String.valueOf(count), x + barW / 2 - 4, y - 4);

                    // X-axis label
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.setColor(TEXT_DIM);
                    String lbl = keys[i].length() > 10 ? keys[i].substring(0, 9) + "." : keys[i];
                    g2.drawString(lbl, x, h - 8);
                }
            }
        };
        chart.setBackground(BG_CARD);
        chart.setPreferredSize(new Dimension(0, 110));
        container.add(chart, BorderLayout.CENTER);
        return container;
    }

    private JPanel makeTextResponseCard(String text) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1), new EmptyBorder(8, 14, 8, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MAIN);
        card.add(l, BorderLayout.WEST);
        return card;
    }
}
