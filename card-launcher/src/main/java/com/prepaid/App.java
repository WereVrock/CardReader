package com.prepaid;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        DialogDismisser.loadRules();
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}