package com.prepaid;

import java.awt.Robot;
import java.awt.event.InputEvent;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;

public class ClickPowerPurchase1 {
    public static void main(String[] args) throws Exception {

        // Save current mouse position
        POINT originalPos = new POINT();
        User32.INSTANCE.GetCursorPos(originalPos);

        // Move to target and click
        User32.INSTANCE.SetCursorPos(46, 520);
        Thread.sleep(10);

        Robot robot = new Robot();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        // Return mouse to original position
        Thread.sleep(10);
        User32.INSTANCE.SetCursorPos(originalPos.x, originalPos.y);

        System.out.println("Done!");
    }
}