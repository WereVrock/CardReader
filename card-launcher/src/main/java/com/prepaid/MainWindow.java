package com.prepaid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
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
        topBar.add(launchNanomet);
        topBar.add(alwaysOnTop);
        topBar.add(dialogManagerBtn);

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