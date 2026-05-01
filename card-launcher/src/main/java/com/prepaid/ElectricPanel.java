package com.prepaid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
            Thread.sleep(1000);
            infoArea.setText(NanometController.readMemo());
            topFrame.setAlwaysOnTop(true);
            topFrame.toFront();
        } catch (Exception ex) {
            infoArea.append("Exception: " + ex.getMessage() + "\n");
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

private void scanChildren(com.sun.jna.platform.win32.WinDef.HWND parent, int depth) {
String indent = "  ".repeat(depth);
com.sun.jna.platform.win32.User32.INSTANCE.EnumChildWindows(parent, (child, data) -> {
char[] text = new char[512];
char[] cls = new char[512];
com.sun.jna.platform.win32.User32.INSTANCE.GetWindowText(child, text, 512);
com.sun.jna.platform.win32.User32.INSTANCE.GetClassName(child, cls, 512);
String t = new String(text).trim();
String c = new String(cls).trim();
infoArea.append(indent + "Class: " + c + " | Text: " + t + "\n");
scanChildren(child, depth + 1);
return true;
}, null);
}

public void setKwh(String kwh) {
kwhField.setText(kwh);
}
}
