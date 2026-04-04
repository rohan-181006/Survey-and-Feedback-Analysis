package com.survey.ui;

import com.survey.beans.*;
import com.survey.dao.*;
import com.survey.model.*;
import com.survey.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * SurveyApp - Main Application Window
 * Demonstrates: Swing & MVC (Unit 4), Layout Management, Menus,
 * Dialogs, Events, JavaBeans (Unit 2), Threading (Unit 1),
 * JDBC (Unit 3), Exception Handling (Unit 1), Packages (Unit 1)
 */
public class SurveyApp extends JFrame {

    // ── MVC: Model (Bean) ─────────────────────────────────────────────────────
    private final SurveyBean surveyBean = new SurveyBean();

    // ── DAO Layer ─────────────────────────────────────────────────────────────
    private final SurveyDAO surveyDAO = new SurveyDAO();
    private final ResponseDAO responseDAO = new ResponseDAO();

    // ── UI Components ─────────────────────────────────────────────────────────
    private JList<Survey> surveyList;
    private DefaultListModel<Survey> surveyListModel;
    private JTabbedPane mainTabs;
    private AnalysisPanel analysisPanel;
    private JLabel statusLabel;
    private JLabel surveyCountLabel, responseCountLabel;

    // ── Color Palette ─────────────────────────────────────────────────────────
    static final Color BG_DARK = new Color(15, 17, 26);
    static final Color BG_PANEL = new Color(22, 26, 40);
    static final Color BG_CARD = new Color(30, 36, 55);
    static final Color ACCENT = new Color(99, 179, 237);
    static final Color ACCENT2 = new Color(154, 117, 234);
    static final Color SUCCESS = new Color(72, 199, 142);
    static final Color WARNING = new Color(255, 183, 77);
    static final Color DANGER = new Color(252, 100, 100);
    static final Color TEXT_MAIN = new Color(226, 232, 245);
    static final Color TEXT_DIM = new Color(140, 153, 180);
    static final Color BORDER_CLR = new Color(45, 54, 80);

    public SurveyApp() {
        super("Survey & Feedback Analysis System");
        setupLookAndFeel();
        initComponents();
        buildMenu();
        loadSurveys();
        setupBeanListeners();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(SurveyApp.this,
                        "Exit the application?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    AnalyticsWorker.shutdown();
                    DatabaseManager.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });

        setSize(1280, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Look and Feel ─────────────────────────────────────────────────────────
    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            /* use default */ }
        getContentPane().setBackground(BG_DARK);
    }

    // ── Menu Bar (Unit 4 - Menus, Keyboard Mnemonics, Accelerators) ──────────
    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_PANEL);
        menuBar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));

        // File Menu
        JMenu fileMenu = createMenu("File", KeyEvent.VK_F);
        JMenuItem newSurvey = createMenuItem("New Survey", KeyEvent.VK_N,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
                e -> showCreateSurveyDialog());
        JMenuItem exportItem = createMenuItem("Export Report", KeyEvent.VK_E,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK),
                e -> exportReport());
        JMenuItem exitItem = createMenuItem("Exit", KeyEvent.VK_X, null,
                e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(newSurvey);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Survey Menu
        JMenu surveyMenu = createMenu("Survey", KeyEvent.VK_S);
        JMenuItem fillSurvey = createMenuItem("Fill Survey", KeyEvent.VK_F,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
                e -> showFillSurveyDialog());
        JMenuItem deleteSurvey = createMenuItem("Delete Survey", KeyEvent.VK_D, null,
                e -> deleteSelectedSurvey());
        surveyMenu.add(fillSurvey);
        surveyMenu.addSeparator();
        surveyMenu.add(deleteSurvey);

        // View Menu with CheckBox items (Unit 4 - Check box in Menu Items)
        JMenu viewMenu = createMenu("View", KeyEvent.VK_V);
        JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark Mode", true);
        darkMode.setForeground(TEXT_MAIN);
        darkMode.setBackground(BG_PANEL);
        darkMode.addActionListener(e -> setStatus("Dark mode: " + darkMode.isSelected()));
        viewMenu.add(darkMode);

        // Help Menu
        JMenu helpMenu = createMenu("Help", KeyEvent.VK_H);
        JMenuItem aboutItem = createMenuItem("About", KeyEvent.VK_A, null, e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(surveyMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JMenu createMenu(String text, int mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setForeground(TEXT_MAIN);
        menu.setBackground(BG_PANEL);
        return menu;
    }

    private JMenuItem createMenuItem(String text, int mnemonic, KeyStroke accelerator, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        if (accelerator != null)
            item.setAccelerator(accelerator);
        item.addActionListener(action);
        item.setForeground(TEXT_MAIN);
        item.setBackground(BG_PANEL);
        return item;
    }

    // ── Main Layout (Unit 4 - Border Layout) ─────────────────────────────────
    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        add(buildHeader(), BorderLayout.NORTH);

        // Left sidebar - Survey List
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Content - Tabbed Pane (Unit 4 - Tabbed Panes)
        mainTabs = new JTabbedPane();
        mainTabs.setBackground(BG_DARK);
        mainTabs.setForeground(TEXT_MAIN);
        mainTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Dashboard Tab
        mainTabs.addTab("📊  Dashboard", buildDashboardPanel());

        // Analysis Tab
        analysisPanel = new AnalysisPanel(responseDAO, surveyBean);
        mainTabs.addTab("🔍  Analysis", analysisPanel);

        // Fill Survey Tab
        mainTabs.addTab("✏️  Fill Survey", buildFillSurveyTab());

        // Create Survey Tab
        mainTabs.addTab("➕  Create Survey", buildCreateSurveyTab());

        add(mainTabs, BorderLayout.CENTER);

        // Status Bar
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header Panel ──────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));
        header.setPreferredSize(new Dimension(0, 64));

        JLabel title = new JLabel("  📋  Survey & Feedback Analysis System");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 16));
        stats.setOpaque(false);
        surveyCountLabel = makeStatLabel("Surveys: 0");
        responseCountLabel = makeStatLabel("Responses: 0");
        stats.add(surveyCountLabel);
        stats.add(responseCountLabel);
        header.add(stats, BorderLayout.EAST);

        return header;
    }

    private JLabel makeStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(ACCENT);
        return l;
    }

    // ── Left Sidebar ──────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_PANEL);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_CLR));

        JLabel header = new JLabel("  SURVEYS");
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(TEXT_DIM);
        header.setBorder(new EmptyBorder(16, 12, 8, 12));
        header.setOpaque(true);
        header.setBackground(BG_PANEL);
        sidebar.add(header, BorderLayout.NORTH);

        // Survey JList (Unit 4 - Advance Swing: List)
        surveyListModel = new DefaultListModel<>();
        surveyList = new JList<>(surveyListModel);
        surveyList.setBackground(BG_PANEL);
        surveyList.setForeground(TEXT_MAIN);
        surveyList.setSelectionBackground(new Color(40, 50, 80));
        surveyList.setSelectionForeground(ACCENT);
        surveyList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        surveyList.setFixedCellHeight(44);
        surveyList.setCellRenderer(new SurveyListRenderer());
        surveyList.setBorder(new EmptyBorder(4, 0, 4, 0));

        surveyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Survey selected = surveyList.getSelectedValue();
                if (selected != null) {
                    surveyBean.setSelectedSurveyId(selected.getId());
                    surveyBean.setSelectedSurveyTitle(selected.getTitle());
                    surveyBean.fireSurveySelected(selected.getId(), selected.getTitle());
                    setStatus("Survey selected: " + selected.getTitle());
                }
            }
        });

        // Right-click popup menu (Unit 4 - Pop-up Menus)
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(BG_PANEL);
        JMenuItem fillItem = new JMenuItem("Fill Survey");
        JMenuItem deleteItem = new JMenuItem("Delete Survey");
        JMenuItem exportItem = new JMenuItem("Export Report");
        for (JMenuItem mi : new JMenuItem[] { fillItem, deleteItem, exportItem }) {
            mi.setBackground(BG_PANEL);
            mi.setForeground(TEXT_MAIN);
            popup.add(mi);
        }
        fillItem.addActionListener(e -> showFillSurveyDialog());
        deleteItem.addActionListener(e -> deleteSelectedSurvey());
        exportItem.addActionListener(e -> exportReport());
        surveyList.setComponentPopupMenu(popup);

        sidebar.add(new JScrollPane(surveyList), BorderLayout.CENTER);

        // Sidebar buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setBackground(BG_PANEL);
        btnPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton newBtn = makeButton("+ New", ACCENT);
        newBtn.addActionListener(e -> showCreateSurveyDialog());
        JButton refreshBtn = makeButton("↺ Refresh", TEXT_DIM);
        refreshBtn.addActionListener(e -> loadSurveys());

        btnPanel.add(newBtn);
        btnPanel.add(refreshBtn);
        sidebar.add(btnPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // ── Dashboard Panel ───────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Dashboard Overview");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(TEXT_MAIN);
        panel.add(heading, BorderLayout.NORTH);

        // Cards using GridBagLayout (Unit 4 - GridBag Layout)
        JPanel cards = new JPanel(new GridBagLayout());
        cards.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.4;
        cards.add(makeDashCard("📋", "Total Surveys", "" + surveyDAO.getSurveyCount(), ACCENT), gbc);

        gbc.gridx = 1;
        cards.add(makeDashCard("👥", "Total Responses", "" + surveyDAO.getResponseCount(), SUCCESS), gbc);

        gbc.gridx = 2;
        cards.add(makeDashCard("📈", "Active Surveys", "" + surveyDAO.getSurveyCount(), WARNING), gbc);

        gbc.gridx = 3;
        cards.add(makeDashCard("⭐", "Avg Satisfaction", "4.1 / 5", ACCENT2), gbc);

        // Info panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weighty = 0.6;
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(235, 240, 250));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR), new EmptyBorder(20, 20, 20, 20)));

        JLabel infoTitle = new JLabel("Getting Started");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoTitle.setForeground(TEXT_MAIN);
        infoPanel.add(infoTitle, BorderLayout.NORTH);

        JTextArea info = new JTextArea(
                "\n  1. Select a survey from the left panel to view its analysis.\n" +
                        "  2. Use 'Fill Survey' tab or right-click a survey to submit responses.\n" +
                        "  3. Use 'Create Survey' tab to add a new custom survey.\n" +
                        "  4. Use File > Export Report to save results to a text file.\n" +
                        "  5. Right-click any survey in the sidebar for quick actions.\n");
        info.setEditable(false);
        info.setBackground(new Color(235, 240, 250));
        info.setForeground(Color.BLACK);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setBorder(null);
        infoPanel.add(info, BorderLayout.CENTER);
        cards.add(infoPanel, gbc);

        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeDashCard(String icon, String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accent, 1),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel iconLabel = new JLabel(icon + "  " + label);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        iconLabel.setForeground(TEXT_DIM);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Fill Survey Tab ───────────────────────────────────────────────────────
    private JPanel buildFillSurveyTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("Select a survey from the left panel, then click 'Fill Survey' button or use Ctrl+F");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_DIM);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        JButton fillBtn = makeButton("✏️  Fill Selected Survey", ACCENT);
        fillBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fillBtn.setPreferredSize(new Dimension(260, 48));
        fillBtn.addActionListener(e -> showFillSurveyDialog());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG_DARK);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        fillBtn.setAlignmentX(CENTER_ALIGNMENT);
        center.add(Box.createVerticalGlue());
        center.add(lbl);
        center.add(Box.createVerticalStrut(20));
        center.add(fillBtn);
        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ── Create Survey Tab ─────────────────────────────────────────────────────
    private JPanel buildCreateSurveyTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Create New Survey");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(TEXT_MAIN);
        panel.add(heading, BorderLayout.NORTH);

        // Form using GridBagLayout (Unit 4)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR), new EmptyBorder(24, 24, 24, 24)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField titleField = makeTextField(30);
        JTextField descField = makeTextField(30);
        String[] categories = { "Customer Service", "HR", "Product", "Education", "Marketing", "Other" };
        JComboBox<String> catBox = new JComboBox<>(categories); // Unit 4 - Combo Boxes
        catBox.setBackground(BG_CARD);
        catBox.setForeground(TEXT_MAIN);

        addFormRow(form, gbc, 0, "Survey Title *", titleField);
        addFormRow(form, gbc, 1, "Description", descField);
        addFormRow(form, gbc, 2, "Category", catBox);

        // Questions section
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel qLabel = new JLabel("Questions:");
        qLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qLabel.setForeground(TEXT_MAIN);
        form.add(qLabel, gbc);

        DefaultListModel<String> questionListModel = new DefaultListModel<>();
        JList<String> questionDisplayList = new JList<>(questionListModel);
        questionDisplayList.setBackground(BG_DARK);
        questionDisplayList.setForeground(TEXT_MAIN);
        questionDisplayList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane qScroll = new JScrollPane(questionDisplayList);
        qScroll.setPreferredSize(new Dimension(480, 120));
        qScroll.getViewport().setBackground(BG_DARK);

        gbc.gridy = 4;
        form.add(qScroll, gbc);

        // Add question controls
        JPanel addQPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        addQPanel.setBackground(BG_CARD);
        JTextField qText = makeTextField(20);
        qText.setToolTipText("Enter question text");
        JComboBox<Question.QuestionType> qType = new JComboBox<>(Question.QuestionType.values());
        qType.setBackground(BG_CARD);
        qType.setForeground(TEXT_MAIN);
        JButton addQBtn = makeButton("+ Add", SUCCESS);
        addQBtn.addActionListener(e -> {
            String qt = qText.getText().trim();
            if (!qt.isEmpty()) {
                questionListModel.addElement("[" + qType.getSelectedItem() + "] " + qt);
                qText.setText("");
            }
        });
        addQPanel.add(new JLabel(" Q Text: "));
        addQPanel.add(qText);
        addQPanel.add(new JLabel(" Type: "));
        addQPanel.add(qType);
        addQPanel.add(addQBtn);

        // Fix label colors
        for (Component c : addQPanel.getComponents()) {
            if (c instanceof JLabel)
                ((JLabel) c).setForeground(TEXT_DIM);
        }

        gbc.gridy = 5;
        form.add(addQPanel, gbc);

        // Save button
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = makeButton("💾  Save Survey", ACCENT);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            if (t.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Survey title is required!", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (questionListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Add at least one question!", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Save survey (Unit 3 - JDBC)
            Survey s = new Survey(0, t, descField.getText().trim(), (String) catBox.getSelectedItem());
            int id = surveyDAO.insertSurvey(s);
            if (id > 0) {
                // Serialize backup (Unit 1 - Serialization)
                SerializationUtil.serializeSurvey(s, "survey_" + id);
                loadSurveys();
                titleField.setText("");
                descField.setText("");
                questionListModel.clear();
                JOptionPane.showMessageDialog(this, "Survey created successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                setStatus("Survey '" + t + "' created.");
            }
        });
        form.add(saveBtn, gbc);

        panel.add(new JScrollPane(form), BorderLayout.CENTER);
        return panel;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel l = new JLabel(label + ":");
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(l, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    // ── Fill Survey Dialog (Unit 4 - Dialog Boxes) ───────────────────────────
    private void showFillSurveyDialog() {
        Survey selected = surveyList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a survey first.", "No Survey Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Question> questions = surveyDAO.getQuestionsForSurvey(selected.getId());
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This survey has no questions.", "Empty Survey",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        FillSurveyDialog dialog = new FillSurveyDialog(this, selected, questions, responseDAO);
        dialog.setVisible(true);
        if (dialog.isSubmitted()) {
            setStatus("Response submitted for: " + selected.getTitle());
            updateHeaderStats();
            // Refresh analysis if this survey is open
            if (surveyBean.getSelectedSurveyId() == selected.getId()) {
                analysisPanel.loadAnalysis(selected.getId(), selected.getTitle());
            }
        }
    }

    private void showCreateSurveyDialog() {
        mainTabs.setSelectedIndex(3); // Switch to Create Survey tab
    }

    // ── Export Report ─────────────────────────────────────────────────────────
    private void exportReport() {
        Survey s = surveyList.getSelectedValue();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a survey first.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Respondents: ").append(responseDAO.getRespondentCount(s.getId())).append("\n");
        sb.append("Overall Score: ").append(String.format("%.2f", responseDAO.getOverallScore(s.getId())))
                .append("/5\n\n");
        sb.append("--- AVERAGE RATINGS ---\n");
        responseDAO.getAverageRatings(s.getId())
                .forEach((q, avg) -> sb.append(q).append(": ").append(String.format("%.2f", avg)).append("\n"));
        sb.append("\n--- CHOICE DISTRIBUTION ---\n");
        responseDAO.getChoiceDistribution(s.getId()).forEach((q, choices) -> {
            sb.append(q).append(":\n");
            choices.forEach((choice, count) -> sb.append("  ").append(choice).append(": ").append(count).append("\n"));
        });

        SerializationUtil.exportToTextFile(s.getTitle(), sb.toString());
        JOptionPane.showMessageDialog(this,
                "Report exported to: " + SerializationUtil.getBackupDir(), "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Delete Survey ─────────────────────────────────────────────────────────
    private void deleteSelectedSurvey() {
        Survey s = surveyList.getSelectedValue();
        if (s == null)
            return;
        int r = JOptionPane.showConfirmDialog(this,
                "Delete survey '" + s.getTitle() + "' and all its responses?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            surveyDAO.deleteSurvey(s.getId());
            loadSurveys();
            setStatus("Survey deleted: " + s.getTitle());
        }
    }

    // ── Bean Listeners (Unit 2 - JavaBeans Event Model) ──────────────────────
    private void setupBeanListeners() {
        surveyBean.addSurveySelectionListener(event -> {
            analysisPanel.loadAnalysis(event.getSurveyId(), event.getSurveyTitle());
            mainTabs.setSelectedIndex(1); // Switch to Analysis tab
        });

        surveyBean.addPropertyChangeListener("selectedSurveyTitle",
                evt -> setTitle("Survey App — " + evt.getNewValue()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void loadSurveys() {
        surveyListModel.clear();
        List<Survey> surveys = surveyDAO.getAllSurveys();
        for (Survey s : surveys)
            surveyListModel.addElement(s);
        updateHeaderStats();
    }

    private void updateHeaderStats() {
        surveyCountLabel.setText("Surveys: " + surveyDAO.getSurveyCount());
        responseCountLabel.setText("Responses: " + surveyDAO.getResponseCount());
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));
        bar.setPreferredSize(new Dimension(0, 28));

        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_DIM);
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel version = new JLabel("Survey Analysis System v1.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_DIM);
        bar.add(version, BorderLayout.EAST);
        return bar;
    }

    void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    static JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JTextField makeTextField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(BG_DARK);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR), new EmptyBorder(4, 8, 4, 8)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return f;
    }

    // ── About Dialog ──────────────────────────────────────────────────────────
    private void showAbout() {
        // Unit 4 - Option Dialogs
        JOptionPane.showMessageDialog(this,
                "Survey & Feedback Analysis System\n\n" +
                        "Demonstrates Java concepts from:\n" +
                        "Unit 1: OOP, Exception Handling, Threading, I/O Serialization\n" +
                        "Unit 2: JavaBeans, GUI, Event Model\n" +
                        "Unit 3: JDBC, SQL, Transactions\n" +
                        "Unit 4: Swing, MVC, Layouts, Menus, Dialogs\n\n" +
                        "Built with Java Swing + SQLite",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Main Entry Point ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Unit 4 - Swing thread safety via SwingUtilities
        SwingUtilities.invokeLater(SurveyApp::new);
    }
}
