package com.prepaid;

import java.awt.Robot;
import java.awt.event.InputEvent;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;

public class NanometController {

    private static final String MAIN_WINDOW = "Prepaid Ordinary Card-Meter Charging Management System";
    private static final String POPUP_WINDOW = "Power purchase";
    private static final int POWER_PURCHASE_X = 36;
    private static final int POWER_PURCHASE_Y = 315;

    public static void launchNanomet() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("D:\\Prepaid Ordinary Card-Meter Charging Management System\\ELWORKS\\Nanomet.exe");
        pb.directory(new java.io.File("D:\\Prepaid Ordinary Card-Meter Charging Management System\\ELWORKS"));
        pb.start();
    }

    public static boolean isNanometRunning() {
        return User32.INSTANCE.FindWindow(null, MAIN_WINDOW) != null;
    }

public static void clickPowerPurchase() throws Exception {
        if (isPopupOpen()) return;
        HWND hwnd = User32.INSTANCE.FindWindow(null, MAIN_WINDOW);
        if (hwnd == null) return;

        int style = User32.INSTANCE.GetWindowLong(hwnd, -16);
        boolean minimized = (style & 0x20000000) != 0;

        IntByReference pid = new IntByReference();
        int targetThread = User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
        int currentThread = Kernel32.INSTANCE.GetCurrentThreadId();

        User32.INSTANCE.AttachThreadInput(new DWORD(currentThread), new DWORD(targetThread), true);
        User32.INSTANCE.ShowWindow(hwnd, 9);
        Thread.sleep(300);
        User32.INSTANCE.ShowWindow(hwnd, 3);
        User32.INSTANCE.SetForegroundWindow(hwnd);
        User32.INSTANCE.AttachThreadInput(new DWORD(currentThread), new DWORD(targetThread), false);

        if (minimized) Thread.sleep(1000);

        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);

        int clickX = rect.left + 46;
        int clickY = rect.top + 430;

        Robot robot = new Robot();
        robot.mouseMove(clickX, clickY);
        Thread.sleep(500);

        // First click to focus
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(150);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);

        // Second click to actually hit the button
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(150);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
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
        if (popup == null) return false;
        // Check WS_VISIBLE style directly instead of IsWindowVisible
        int style = User32.INSTANCE.GetWindowLong(popup, -16); // GWL_STYLE
        return (style & 0x10000000) != 0; // WS_VISIBLE
    }

public static void demotePopup() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return;
        // Remove always-on-top from popup
        HWND HWND_NOTOPMOST = new HWND(new com.sun.jna.Pointer(-2));
        User32.INSTANCE.SetWindowPos(popup, HWND_NOTOPMOST, 0, 0, 0, 0, 0x0001 | 0x0002); // SWP_NOMOVE | SWP_NOSIZE
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