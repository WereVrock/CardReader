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

JButton dbButton = new JButton("DB Viewer");
controls.add(dbButton);
dbButton.addActionListener(e -> onDbViewer());
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
Thread.sleep(3000);
infoArea.setText(NanometController.readCardInfo());
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

