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
    private JTextField priceField;
    private JTextField nameField;
    private javax.swing.JTextPane infoArea;
    private JButton readButton;
    private JButton loadButton;

public ElectricPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Electric"));

        // Row 1: name
        nameField = new JTextField();
        nameField.setEditable(false);
        nameField.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameField.setForeground(new java.awt.Color(0, 100, 180));
        nameField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        nameField.setOpaque(false);

        // Row 2: controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        readButton = new JButton("Read");
        JButton testBtn = new JButton("Test Memo");
        JLabel moneyLabel = new JLabel("Amount:");
        moneyField = new JTextField(8);
        loadButton = new JButton("Load");
        JLabel kwhLabel = new JLabel("kWh:");
        kwhField = new JTextField(7);
        kwhField.setEditable(false);
        JLabel priceLabel = new JLabel("Price/kWh:");
        priceField = new JTextField(7);
        priceField.setEditable(false);
        JButton compareBtn = new JButton("Compare DB");

        controls.add(readButton);
        controls.add(testBtn);
        controls.add(moneyLabel);
        controls.add(moneyField);
        controls.add(loadButton);
        controls.add(kwhLabel);
        controls.add(kwhField);
        controls.add(priceLabel);
        controls.add(priceField);
        controls.add(compareBtn);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(nameField, BorderLayout.NORTH);
        topSection.add(controls, BorderLayout.SOUTH);

        infoArea = new javax.swing.JTextPane();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Card Info"));

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        moneyField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateKwh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateKwh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateKwh(); }
            private void updateKwh() {
                try {
                    String p = priceField.getText().trim();
                    String a = moneyField.getText().trim();
                    if (p.isEmpty() || p.equals("N/A") || a.isEmpty()) { kwhField.setText(""); return; }
                    double price = Double.parseDouble(p.replace(",", "."));
                    double amount = Double.parseDouble(a.replace(",", "."));
                    if (price == 0) { kwhField.setText(""); return; }
                    double result = amount / price;
                    System.out.println("DEBUG kwh: " + amount + " / " + price + " = " + result);
                    kwhField.setText(String.format("%.2f", result));
                } catch (Exception ex) {
                    System.out.println("DEBUG kwh error: " + ex.getMessage());
                    kwhField.setText("");
                }
            }
        });

        readButton.addActionListener(e -> onRead());
        loadButton.addActionListener(e -> onLoad());
        compareBtn.addActionListener(e -> onCompare());
        testBtn.addActionListener(e -> onTestMemo());
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
                displayMemoText("Could not open Power Purchase window.");
                topFrame.setAlwaysOnTop(true);
                topFrame.toFront();
                return;
            }

            NanometController.demotePopup();
            NanometController.clickReadCard();
            Thread.sleep(2000);
            String raw = NanometController.readMemoViaClipboard();
            displayParsedInfo(raw);

            topFrame.setAlwaysOnTop(true);
            topFrame.toFront();
        } catch (Exception ex) {
            displayMemoText("Exception: " + ex.getMessage());
        }
    }

    private void onCompare() {
        if (snapshotBefore == null) {
            snapshotBefore = NanometController.fullSnapshot();
            displayMemoText("=== SNAPSHOT TAKEN ===\n" + snapshotBefore +
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
            displayMemoText(anyChange ? diff.toString() : "(No changes detected in any table)");
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
                displayMemoText("Could not open Power Purchase window.");
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
            displayMemoText("Error: " + ex.getMessage());
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

    private void displayParsedInfo(String raw) {
        javax.swing.text.StyledDocument doc = infoArea.getStyledDocument();
        try { doc.remove(0, doc.getLength()); } catch (Exception ignored) {}

        javax.swing.text.Style normal = infoArea.addStyle("normal", null);
        javax.swing.text.StyleConstants.setForeground(normal, java.awt.Color.BLACK);
        javax.swing.text.StyleConstants.setFontFamily(normal, "Monospaced");
        javax.swing.text.StyleConstants.setFontSize(normal, 12);

        javax.swing.text.Style nameStyle = infoArea.addStyle("name", null);
        javax.swing.text.StyleConstants.setForeground(nameStyle, new java.awt.Color(0, 100, 180));
        javax.swing.text.StyleConstants.setBold(nameStyle, true);
        javax.swing.text.StyleConstants.setFontSize(nameStyle, 14);
        javax.swing.text.StyleConstants.setFontFamily(nameStyle, "Monospaced");

        javax.swing.text.Style errorStyle = infoArea.addStyle("error", null);
        javax.swing.text.StyleConstants.setForeground(errorStyle, java.awt.Color.RED);
        javax.swing.text.StyleConstants.setBold(errorStyle, true);
        javax.swing.text.StyleConstants.setFontFamily(errorStyle, "Monospaced");
        javax.swing.text.StyleConstants.setFontSize(errorStyle, 12);

        String userName = extract(raw, "User code");
        String meterNo = extract(raw, "Meter No");
        String lastAmount = extract(raw, "Amount of power purchase");
        String lastDate = extract(raw, "Date of power purchase");
        // Extract kWh from DB section only (after "Previous power purchase information")
        String lastQty = extractFromSection(raw, "Previous power purchase information", "Quantity of power purchase");

        String dbName = NanometController.lookupCustomerName(userName);
        if (dbName == null) dbName = NanometController.lookupCustomerByMeter(meterNo);

        java.util.List<String> errors = new java.util.ArrayList<>();
        for (String line : raw.split("\n")) {
            String lower = line.toLowerCase();
            if (lower.contains("error") || lower.contains("has not been plugged in")) {
                errors.add(line.trim());
            }
        }

        String price = lastQty.equals("N/A") || lastAmount.equals("N/A") ? "N/A" :
            formatPrice(lastAmount, lastQty);
        final String priceFinal = price;
        SwingUtilities.invokeLater(() -> priceField.setText(priceFinal));

        try {
            String displayName = dbName != null ? dbName : userName;
            SwingUtilities.invokeLater(() -> nameField.setText(displayName));
            doc.insertString(doc.getLength(), displayName + "\n\n", nameStyle);
            appendStyled(doc, "Meter No    : " + meterNo + "\n", normal);
            appendStyled(doc, "User No     : " + userName + "\n", normal);
            appendStyled(doc, "\nLast Load\n", normal);
            appendStyled(doc, "  Date      : " + lastDate + "\n", normal);
            appendStyled(doc, "  Amount    : " + lastAmount + "\n", normal);
            appendStyled(doc, "  kWh       : " + lastQty + "\n", normal);

            if (!errors.isEmpty()) {
                appendStyled(doc, "\nISSUES:\n", errorStyle);
                for (String err : errors) {
                    appendStyled(doc, "  " + err + "\n", errorStyle);
                }
            }
        } catch (Exception ignored) {}
    }

    private String formatPrice(String amount, String kwh) {
        try {
            double a = Double.parseDouble(amount.trim());
            double k = Double.parseDouble(kwh.replaceAll("[^0-9.]", "").trim());
            if (k == 0) return "N/A";
            return String.format("%.4f", a / k);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String extractFromSection(String text, String sectionHeader, String key) {
        boolean inSection = false;
        for (String line : text.split("\n")) {
            if (line.toLowerCase().contains(sectionHeader.toLowerCase())) {
                inSection = true;
            }
            if (inSection && line.toLowerCase().contains(key.toLowerCase())) {
                int colon = line.indexOf(':');
                if (colon >= 0) return line.substring(colon + 1).trim();
            }
        }
        return "N/A";
    }

    private String extract(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.toLowerCase().contains(key.toLowerCase())) {
                int colon = line.indexOf(':');
                if (colon >= 0) return line.substring(colon + 1).trim();
            }
        }
        return "N/A";
    }

    private void appendStyled(javax.swing.text.StyledDocument doc, String text, javax.swing.text.Style style) {
        try { doc.insertString(doc.getLength(), text, style); } catch (Exception ignored) {}
    }

    private void displayMemoText(String text) {
        javax.swing.text.StyledDocument doc = infoArea.getStyledDocument();
        try { doc.remove(0, doc.getLength()); } catch (Exception ignored) {}

        javax.swing.text.Style normal = infoArea.addStyle("normal", null);
        javax.swing.text.StyleConstants.setForeground(normal, java.awt.Color.BLACK);

        javax.swing.text.Style error = infoArea.addStyle("error", null);
        javax.swing.text.StyleConstants.setForeground(error, java.awt.Color.RED);
        javax.swing.text.StyleConstants.setBold(error, true);

        javax.swing.text.Style warning = infoArea.addStyle("warning", null);
        javax.swing.text.StyleConstants.setForeground(warning, new java.awt.Color(180, 100, 0));
        javax.swing.text.StyleConstants.setBold(warning, true);

        for (String line : text.split("\n")) {
            javax.swing.text.Style style = normal;
            String lower = line.toLowerCase();
            if (lower.contains("error") || lower.contains("has not been plugged in")) {
                style = error;
            } else if (lower.contains("warning") || lower.contains("alarm")) {
                style = warning;
            }
            try {
                doc.insertString(doc.getLength(), line + "\n", style);
            } catch (Exception ignored) {}
        }
    }

    private void onTestMemo() {
        try {
            if (!NanometController.isPopupOpen()) {
                String debug = NanometController.queryDatabase(
                    "SELECT C_No, C_Name, C_MNo FROM Customer WHERE C_No LIKE '%7%' OR C_MNo LIKE '%78098%'");
                displayMemoText("=== DB Debug ===\n" + debug);
                return;
            }
            String raw = NanometController.readMemoViaClipboard();
            displayParsedInfo(raw);
        } catch (Exception ex) {
            displayMemoText("Error: " + ex.getMessage());
        }
    }

    private void scanChildren(com.sun.jna.platform.win32.WinDef.HWND parent, int depth) {
        String indent = "  ".repeat(depth);
        com.sun.jna.platform.win32.User32.INSTANCE.EnumChildWindows(parent, (child, data) -> {
            char[] text = new char[512];
            char[] cls = new char[512];
            com.sun.jna.platform.win32.User32.INSTANCE.GetWindowText(child, text, 512);
            com.sun.jna.platform.win32.User32.INSTANCE.GetClassName(child, cls, 512);
            String t = new String(text).trim();
            String c = new String(cls).trim();
            javax.swing.text.StyledDocument doc = infoArea.getStyledDocument();
            javax.swing.text.Style normal = infoArea.getStyle("normal");
            if (normal == null) normal = infoArea.addStyle("normal", null);
            appendStyled(doc, indent + "Class: " + c + " | Text: " + t + "\n", normal);
            scanChildren(child, depth + 1);
            return true;
        }, null);
    }

    public void setKwh(String kwh) {
        kwhField.setText(kwh);
    }
}