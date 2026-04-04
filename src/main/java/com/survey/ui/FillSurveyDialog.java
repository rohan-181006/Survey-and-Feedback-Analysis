package com.survey.ui;

import com.survey.dao.ResponseDAO;
import com.survey.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.survey.ui.SurveyApp.*;

/**
 * FillSurveyDialog - Demonstrates: Swing Dialog Boxes (Unit 4),
 * Choice Components (CheckBoxes, Radio Buttons, Sliders),
 * Text Components (TextFields, TextAreas), ScrollPane,
 * Exception Handling (Unit 1)
 */
public class FillSurveyDialog extends JDialog {

    private final Survey survey;
    private final List<Question> questions;
    private final ResponseDAO responseDAO;
    private boolean submitted = false;

    // Question answer components map
    private final Map<Integer, JComponent> answerComponents = new LinkedHashMap<>();

    private JTextField nameField;

    public FillSurveyDialog(Frame owner, Survey survey, List<Question> questions, ResponseDAO responseDAO) {
        super(owner, "Fill Survey: " + survey.getTitle(), true); // Modal dialog (Unit 4)
        this.survey = survey;
        this.questions = questions;
        this.responseDAO = responseDAO;
        buildUI();
        pack();
        setMinimumSize(new Dimension(580, 500));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DARK);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel(survey.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JLabel desc = new JLabel(survey.getDescription() != null ? survey.getDescription() : "");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_DIM);

        header.add(title, BorderLayout.NORTH);
        header.add(desc, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Questions panel inside a scroll pane (Unit 4 - Scroll Pane)
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBackground(BG_DARK);
        questionsPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Name field (Text Field - Unit 4)
        JPanel nameRow = buildSection("Your Name");
        nameField = makeTextField(24);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nameRow.add(nameField);
        questionsPanel.add(nameRow);
        questionsPanel.add(Box.createVerticalStrut(12));

        // Build question inputs
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel section = buildSection((i + 1) + ". " + q.getText() + (q.isRequired() ? " *" : ""));

            JComponent input = buildInputForQuestion(q);
            answerComponents.put(q.getId(), input);
            section.add(input);

            questionsPanel.add(section);
            questionsPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(questionsPanel);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(BG_PANEL);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton cancel = makeButton("Cancel", TEXT_DIM);
        cancel.addActionListener(e -> dispose());

        JButton submit = makeButton("✔  Submit", SUCCESS);
        submit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submit.addActionListener(e -> submitResponses());

        footer.add(cancel);
        footer.add(submit);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildSection(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR), new EmptyBorder(12, 16, 12, 16)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MAIN);
        label.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        return panel;
    }

    /**
     * Build appropriate Swing input component for each question type.
     * Demonstrates: Text Fields, Text Areas, Radio Buttons, Check Boxes, Sliders (Unit 4)
     */
    private JComponent buildInputForQuestion(Question q) {
        switch (q.getType()) {
            case TEXT: {
                // Text Area (Unit 4 - Text Areas)
                JTextArea ta = new JTextArea(3, 30);
                ta.setBackground(BG_DARK);
                ta.setForeground(TEXT_MAIN);
                ta.setCaretColor(TEXT_MAIN);
                ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                ta.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_CLR), new EmptyBorder(6, 8, 6, 8)));
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                ta.setAlignmentX(Component.LEFT_ALIGNMENT);
                return ta;
            }
            case RATING: {
                // Slider (Unit 4 - Sliders)
                JPanel rPanel = new JPanel(new BorderLayout(8, 4));
                rPanel.setBackground(BG_CARD);

                JSlider slider = new JSlider(1, 5, 3);
                slider.setMajorTickSpacing(1);
                slider.setSnapToTicks(true);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                slider.setBackground(BG_CARD);
                slider.setForeground(TEXT_DIM);

                JLabel valueDisplay = new JLabel("3 / 5  ★★★☆☆", SwingConstants.CENTER);
                valueDisplay.setForeground(ACCENT);
                valueDisplay.setFont(new Font("Segoe UI", Font.BOLD, 14));

                slider.addChangeListener(e -> {
                    int v = slider.getValue();
                    String stars = "★".repeat(v) + "☆".repeat(5 - v);
                    valueDisplay.setText(v + " / 5  " + stars);
                });

                rPanel.add(slider, BorderLayout.CENTER);
                rPanel.add(valueDisplay, BorderLayout.EAST);
                rPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Store slider as the component
                rPanel.putClientProperty("slider", slider);
                return rPanel;
            }
            case MULTIPLE_CHOICE: {
                // Radio Buttons (Unit 4 - Radio Buttons)
                JPanel rPanel = new JPanel();
                rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.Y_AXIS));
                rPanel.setBackground(BG_CARD);
                ButtonGroup group = new ButtonGroup();

                for (String option : q.getOptions()) {
                    JRadioButton rb = new JRadioButton(option);
                    rb.setBackground(BG_CARD);
                    rb.setForeground(TEXT_MAIN);
                    rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    rb.setAlignmentX(Component.LEFT_ALIGNMENT);
                    group.add(rb);
                    rPanel.add(rb);
                    rPanel.add(Box.createVerticalStrut(4));
                }

                rPanel.putClientProperty("buttonGroup", group);
                rPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                return rPanel;
            }
            case YES_NO: {
                // Check Boxes (Unit 4 - Check Boxes)
                JPanel ynPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                ynPanel.setBackground(BG_CARD);
                ButtonGroup bg = new ButtonGroup();

                JRadioButton yes = new JRadioButton("Yes");
                JRadioButton no = new JRadioButton("No");
                for (JRadioButton rb : new JRadioButton[]{yes, no}) {
                    rb.setBackground(BG_CARD);
                    rb.setForeground(TEXT_MAIN);
                    rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    bg.add(rb);
                    ynPanel.add(rb);
                }

                ynPanel.putClientProperty("buttonGroup", bg);
                ynPanel.putClientProperty("yesButton", yes);
                ynPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                return ynPanel;
            }
            default:
                return new JLabel("Unknown type");
        }
    }

    /**
     * Collect answers and save via JDBC (Unit 3), with validation (Unit 1 - Exception Handling)
     */
    private void submitResponses() {
        try {
            String respondentName = nameField.getText().trim();
            if (respondentName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your name.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Response> responses = new ArrayList<>();

            for (Question q : questions) {
                JComponent input = answerComponents.get(q.getId());
                String answer = extractAnswer(q, input);

                if (q.isRequired() && (answer == null || answer.isEmpty())) {
                    JOptionPane.showMessageDialog(this,
                        "Please answer: " + q.getText(), "Required Field", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Response r = new Response(survey.getId(), q.getId(), respondentName, answer);
                if (q.getType() == Question.QuestionType.RATING) {
                    try { r.setRatingValue(Integer.parseInt(answer)); } catch (Exception ignored) {}
                }
                responses.add(r);
            }

            // Save all responses (Unit 3 - JDBC Batch Insert / Transactions)
            boolean success = responseDAO.insertResponses(responses);
            if (success) {
                submitted = true;
                JOptionPane.showMessageDialog(this,
                    "Thank you, " + respondentName + "! Your response has been saved.",
                    "Submitted", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save responses.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            // Unit 1 - Exception Handling
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractAnswer(Question q, JComponent input) {
        return switch (q.getType()) {
            case TEXT -> ((JTextArea) input).getText().trim();
            case RATING -> {
                JSlider slider = (JSlider) input.getClientProperty("slider");
                yield slider != null ? String.valueOf(slider.getValue()) : "3";
            }
            case MULTIPLE_CHOICE, YES_NO -> {
                ButtonGroup bg = (ButtonGroup) input.getClientProperty("buttonGroup");
                if (bg != null) {
                    Enumeration<AbstractButton> buttons = bg.getElements();
                    while (buttons.hasMoreElements()) {
                        AbstractButton btn = buttons.nextElement();
                        if (btn.isSelected()) yield btn.getText();
                    }
                }
                yield "";
            }
        };
    }

    public boolean isSubmitted() { return submitted; }
}
