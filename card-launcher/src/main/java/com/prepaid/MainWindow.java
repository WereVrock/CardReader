package com.prepaid;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Prepaid Card Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox launchNanomet = new JCheckBox("Launch Nanomet on start");
        topBar.add(launchNanomet);

        // Tabs for Electric and Water
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Electric", new ElectricPanel());
        tabs.addTab("Water", new JPanel()); // placeholder for later

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(new DialogManagerPanel(), BorderLayout.SOUTH);

        // Launch Nanomet if checked
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