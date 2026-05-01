package com.prepaid;

import java.awt.Robot;
import java.awt.event.InputEvent;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;

public class NanometController {

    private static final String MAIN_WINDOW = "Prepaid Ordinary Card-Meter Charging Management System";
    private static final String POPUP_WINDOW = "Power purchase";
    private static final int POWER_PURCHASE_X = 46;
    private static final int POWER_PURCHASE_Y = 520;

    public static void launchNanomet() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("D:\\Prepaid Ordinary Card-Meter Charging Management System\\ELWORKS\\Nanomet.exe");
        pb.directory(new java.io.File("D:\\Prepaid Ordinary Card-Meter Charging Management System\\ELWORKS"));
        pb.start();
    }

    public static boolean isNanometRunning() {
        return User32.INSTANCE.FindWindow(null, MAIN_WINDOW) != null;
    }

    public static void clickPowerPurchase() throws Exception {
        POINT originalPos = new POINT();
        User32.INSTANCE.GetCursorPos(originalPos);
        User32.INSTANCE.SetCursorPos(POWER_PURCHASE_X, POWER_PURCHASE_Y);
        Thread.sleep(10);
        Robot robot = new Robot();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(10);
        User32.INSTANCE.SetCursorPos(originalPos.x, originalPos.y);
    }

    public static void clickReadCard() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return;
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] text = new char[512];
            User32.INSTANCE.GetWindowText(child, text, 512);
            if (new String(text).trim().equals("Read card")) {
                User32.INSTANCE.SendMessage(child, 0x00F5, new WPARAM(0), new LPARAM(0));
                return false;
            }
            return true;
        }, null);
    }

    public static void setAmount(String amount) {
    HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
    if (popup == null) return;
    User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
        char[] cls = new char[512];
        User32.INSTANCE.GetClassName(child, cls, 512);
        if (new String(cls).trim().equals("TDBNumberEditEh")) {
            User32Ex.INSTANCE.SetWindowTextW(child, amount);
            return false;
        }
        return true;
    }, null);
}

    
    public static boolean isPopupOpen() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        return popup != null && User32.INSTANCE.IsWindowVisible(popup);
    }
    public static void clickLoad() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return;
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] text = new char[512];
            User32.INSTANCE.GetWindowText(child, text, 512);
            if (new String(text).trim().equals("Power purchase")) {
                User32.INSTANCE.SendMessage(child, 0x00F5, new WPARAM(0), new LPARAM(0));
                return false;
            }
            return true;
        }, null);
    }

    public static String readMemo() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return "";
        final String[] result = {""};
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] cls = new char[512];
            User32.INSTANCE.GetClassName(child, cls, 512);
            if (new String(cls).trim().equals("TMemo")) {
                char[] text = new char[2048];
                User32.INSTANCE.GetWindowText(child, text, 2048);
                result[0] = new String(text).trim();
                return false;
            }
            return true;
        }, null);
        return result[0];
    }
}