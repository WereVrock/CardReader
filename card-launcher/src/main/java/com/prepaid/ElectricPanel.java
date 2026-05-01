package com.prepaid;

import javax.swing.*;
import java.awt.*;

public class ElectricPanel extends JPanel {

    private JTextField moneyField;
    private JTextField kwhField;
    private JTextArea infoArea;
    private JButton readButton;
    private JButton loadButton;

    public ElectricPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Electric"));

        // Top controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        readButton = new JButton("Read");
        JLabel moneyLabel = new JLabel("Amount:");
        moneyField = new JTextField(10);
        loadButton = new JButton("Load");
        JLabel kwhLabel = new JLabel("KWH:");
        kwhField = new JTextField(8);
        kwhField.setEditable(false);

        controls.add(readButton);
        controls.add(moneyLabel);
        controls.add(moneyField);
        controls.add(loadButton);
        controls.add(kwhLabel);
        controls.add(kwhField);

        // Info area
        infoArea = new JTextArea(10, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Card Info"));

        add(controls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Wire buttons
        readButton.addActionListener(e -> onRead());
        loadButton.addActionListener(e -> onLoad());
    }

    private void onRead() {
        try {
            if (User32Util.findPopup() == null) {
                NanometController.clickPowerPurchase();
                Thread.sleep(500);
            }
            NanometController.clickReadCard();
            Thread.sleep(1000);
            String info = NanometController.readMemo();
            infoArea.setText(info);
        } catch (Exception ex) {
            infoArea.setText("Error: " + ex.getMessage());
        }
    }

    private void onLoad() {
        String amount = moneyField.getText().trim();
        if (amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount.");
            return;
        }
        try {
            NanometController.setAmount(amount);
            Thread.sleep(200);
            NanometController.clickLoad();
        } catch (Exception ex) {
            infoArea.setText("Error: " + ex.getMessage());
        }
    }

    public void setKwh(String kwh) {
        kwhField.setText(kwh);
    }
}