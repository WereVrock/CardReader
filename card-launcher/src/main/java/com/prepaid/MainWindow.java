package com.prepaid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MainWindow extends JFrame {

public MainWindow() {
        setTitle("Prepaid Card Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox launchNanomet = new JCheckBox("Launch Nanomet on start");
        JCheckBox alwaysOnTop = new JCheckBox("Always on Top");
        JButton dialogManagerBtn = new JButton("Dialog Manager");
        JButton dbViewerBtn = new JButton("DB Viewer");
        topBar.add(launchNanomet);
        topBar.add(alwaysOnTop);
        topBar.add(dialogManagerBtn);
        topBar.add(dbViewerBtn);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Electric", new ElectricPanel());
        tabs.addTab("Water", new JPanel());

        // Permanent log area
        JTextArea permanentLog = new JTextArea(4, 0);
        permanentLog.setEditable(false);
        permanentLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(permanentLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Dialog Log"));

        DialogDismisser.setOnLog(() -> SwingUtilities.invokeLater(() -> {
            permanentLog.setText("");
            DialogDismisser.getLog().forEach(line -> permanentLog.append(line + "\n"));
            permanentLog.setCaretPosition(permanentLog.getDocument().getLength());
        }));
        DialogDismisser.start();

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);

        // Always on top toggle
        alwaysOnTop.addActionListener(e -> setAlwaysOnTop(alwaysOnTop.isSelected()));

        // Dialog Manager button opens a dialog
        dialogManagerBtn.addActionListener(e -> {
            JDialog dm = new JDialog(this, "Dialog Manager", false);
            dm.setSize(600, 300);
            dm.setLocationRelativeTo(this);
            dm.add(new DialogManagerPanel(false));
            dm.setVisible(true);
        });

        dbViewerBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Database Viewer", false);
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(8, 8));

            JTextField sqlField = new JTextField("SELECT TOP 50 C_Name, C_MNo, C_Left, C_Times, C_State FROM Customer ORDER BY C_Name");
            JButton runBtn = new JButton("Run");
            JPanel top = new JPanel(new BorderLayout(4, 4));
            top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
            top.add(new JLabel("SQL:"), BorderLayout.WEST);
            top.add(sqlField, BorderLayout.CENTER);
            top.add(runBtn, BorderLayout.EAST);

            JTextArea results = new JTextArea();
            results.setEditable(false);
            results.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane scroll = new JScrollPane(results);
            scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            for (String[] q : new String[][]{
                {"Customers",        "SELECT TOP 50 C_Name, C_MNo, C_Left, C_Times, C_State FROM Customer ORDER BY C_Name"},
                {"Recent Purchases", "SELECT TOP 20 B_Date, B_MNo, B_AmountW, B_Left, B_State FROM BuyE ORDER BY B_Date DESC"},
                {"Meters",           "SELECT TOP 50 M_No, M_CNo, M_State, M_Left FROM Meters"}
            }) {
                JButton btn = new JButton(q[0]);
                String queryCopy = q[1];
                btn.addActionListener(ev -> {
                    sqlField.setText(queryCopy);
                    results.setText(NanometController.queryDatabase(queryCopy));
                });
                quickPanel.add(btn);
            }

            runBtn.addActionListener(ev -> results.setText(NanometController.queryDatabase(sqlField.getText().trim())));
            sqlField.addActionListener(ev -> results.setText(NanometController.queryDatabase(sqlField.getText().trim())));

            dialog.add(top, BorderLayout.NORTH);
            dialog.add(scroll, BorderLayout.CENTER);
            dialog.add(quickPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

            results.setText(NanometController.queryDatabase(sqlField.getText().trim()));
        });

        // Launch Nanomet toggle
        launchNanomet.addActionListener(e -> {
            if (launchNanomet.isSelected()) {
                try {
                    NanometController.launchNanomet();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to launch Nanomet: " + ex.getMessage());
                }
            }
        });
    }

}