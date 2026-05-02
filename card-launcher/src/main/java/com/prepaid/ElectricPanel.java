package com.prepaid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ElectricPanel extends JPanel {

    private String snapshotBefore = null;
    private JTextField moneyField;
    private JTextField kwhField;
    private JTextArea infoArea;
    private JButton readButton;
    private JButton loadButton;

    public ElectricPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Electric"));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        readButton = new JButton("Read");
        JLabel moneyLabel = new JLabel("Amount:");
        moneyField = new JTextField(10);
        loadButton = new JButton("Load");
        JLabel kwhLabel = new JLabel("KWH:");
        kwhField = new JTextField(8);
        kwhField.setEditable(false);
        JButton dbButton = new JButton("DB Viewer");
        JButton compareBtn = new JButton("Compare DB");

        controls.add(readButton);
        controls.add(moneyLabel);
        controls.add(moneyField);
        controls.add(loadButton);
        controls.add(kwhLabel);
        controls.add(kwhField);
        controls.add(dbButton);
        controls.add(compareBtn);

        infoArea = new JTextArea(10, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Card Info"));

        add(controls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        readButton.addActionListener(e -> onRead());
        loadButton.addActionListener(e -> onLoad());
        dbButton.addActionListener(e -> onDbViewer());
        compareBtn.addActionListener(e -> onCompare());

        JButton testBtn = new JButton("Test Memo");
        controls.add(testBtn);
//        testBtn.addActionListener(e -> onTestMemo());
    }

    private void onRead() {
        try {
            javax.swing.JFrame topFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.setAlwaysOnTop(false);

            if (!NanometController.isPopupOpen()) {
                NanometController.clickPowerPurchase();
                Thread.sleep(1500);
            }

            if (!NanometController.isPopupOpen()) {
                infoArea.setText("Could not open Power Purchase window.");
                topFrame.setAlwaysOnTop(true);
                topFrame.toFront();
                return;
            }

            NanometController.demotePopup();
            NanometController.clickReadCard();
            Thread.sleep(2000);
            String result = NanometController.readMemoViaClipboard();
            infoArea.setText(result);

            topFrame.setAlwaysOnTop(true);
            topFrame.toFront();
        } catch (Exception ex) {
            infoArea.append("Exception: " + ex.getMessage() + "\n");
        }
    }

    private void onCompare() {
        if (snapshotBefore == null) {
            snapshotBefore = NanometController.fullSnapshot();
            infoArea.setText("=== SNAPSHOT TAKEN ===\n" + snapshotBefore +
                "\n\nNow insert card and click Read Card in Nanomet.\nThen click Compare DB again to see what changed.");
        } else {
            String after = NanometController.fullSnapshot();
            StringBuilder diff = new StringBuilder();
            boolean anyChange = false;
            for (String afterLine : after.split("\n")) {
                if (afterLine.trim().isEmpty()) continue;
                String table = afterLine.split(":")[0];
                String beforeLine = snapshotBefore.lines()
                    .filter(l -> l.startsWith(table + ":"))
                    .findFirst().orElse("");
                if (!afterLine.equals(beforeLine)) {
                    diff.append("CHANGED: ").append(afterLine)
                        .append("  (was: ").append(beforeLine).append(")\n");
                    anyChange = true;
                }
            }
            infoArea.setText(anyChange ? diff.toString() : "(No changes detected in any table)");
            snapshotBefore = null;
        }
    }

    private void onLoad() {
        String amount = moneyField.getText().trim();
        if (amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount.");
            return;
        }
        try {
            javax.swing.JFrame topFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
            boolean wasOnTop = topFrame.isAlwaysOnTop();
            if (!NanometController.isPopupOpen()) {
                topFrame.setAlwaysOnTop(false);
                NanometController.clickPowerPurchase();
                Thread.sleep(800);
            }
            if (!NanometController.isPopupOpen()) {
                infoArea.setText("Could not open Power Purchase window.");
                topFrame.setAlwaysOnTop(wasOnTop);
                return;
            }
            NanometController.demotePopup();
            NanometController.setAmount(amount);
            Thread.sleep(200);
            NanometController.clickLoad();
            topFrame.setAlwaysOnTop(wasOnTop);
            topFrame.toFront();
        } catch (Exception ex) {
            infoArea.setText("Error: " + ex.getMessage());
        }
    }

    private void onDbViewer() {
        JDialog dialog = new JDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), "Database Viewer", false);
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
    }

    public void setKwh(String kwh) {
        kwhField.setText(kwh);
    }
}