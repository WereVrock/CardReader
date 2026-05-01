package com.prepaid;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

public interface User32Ex extends StdCallLibrary {
    User32Ex INSTANCE = Native.load("user32", User32Ex.class);
    boolean SetWindowTextW(HWND hWnd, String lpString);
}