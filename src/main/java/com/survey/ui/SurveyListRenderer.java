package com.survey.ui;

import com.survey.model.Survey;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.survey.ui.SurveyApp.*;

/**
 * SurveyListRenderer - Demonstrates: Custom Swing cell renderer (Unit 4 - Advance Swing: List)
 */
public class SurveyListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        JPanel cell = new JPanel(new BorderLayout(8, 2));
        cell.setBorder(new EmptyBorder(8, 14, 8, 14));

        if (value instanceof Survey s) {
            JLabel name = new JLabel(s.getTitle());
            name.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JLabel cat = new JLabel(s.getCategory());
            cat.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            if (isSelected) {
                cell.setBackground(new Color(40, 50, 80));
                name.setForeground(ACCENT);
                cat.setForeground(new Color(100, 130, 180));
            } else {
                cell.setBackground(index % 2 == 0 ? BG_PANEL : new Color(25, 30, 48));
                name.setForeground(TEXT_MAIN);
                cat.setForeground(TEXT_DIM);
            }

            cell.add(name, BorderLayout.CENTER);
            cell.add(cat, BorderLayout.SOUTH);
        }
        return cell;
    }
}
