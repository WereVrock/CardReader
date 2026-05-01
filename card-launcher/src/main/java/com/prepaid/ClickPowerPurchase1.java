package com.prepaid;

import java.awt.Robot;
import java.awt.event.InputEvent;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;

public class ClickPowerPurchase1 {
    public static void main(String[] args) throws Exception {
        HWND hwnd = User32.INSTANCE.FindWindow(null, "Prepaid Ordinary Card-Meter Charging Management System");

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

        System.out.println("Done!");
    }
}