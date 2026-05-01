package com.prepaid;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class User32Util {
    public static HWND findPopup() {
        return User32.INSTANCE.FindWindow(null, "Power purchase");
    }
}