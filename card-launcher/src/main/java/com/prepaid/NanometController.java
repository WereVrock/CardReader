package com.prepaid;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.ptr.IntByReference;

public class NanometController {

private static final String MAIN_WINDOW = "Prepaid Ordinary Card-Meter Charging Management System";
private static final String POPUP_WINDOW = "Power purchase";

// =========================================================================
// DATABASE ACCESS (discovered via ILSpy decompilation of Nanomet.exe)
// =========================================================================
// Settings DB : D:\Prepaid Ordinary Card-Meter Charging Management System\ELWORKS\Settings.mdb
// Settings PWD: ElworkS132Nanomet (plaintext in Nanomet.cs)
// Main DB path: Settings ID=1 + ID=2 → D:\...\data\SXYFF.mdb
// Main DB PWD : Settings ID=3, DES-CBC encrypted, key="A0D1nX0Q", IV={33,67,86,135,16,253,234,28}
// Encryption  : Nanomet.Tools.Encryptor (DES/CBC/PKCS5, UTF-8)
// DB format   : Microsoft Access (.mdb), Jet OLEDB 4.0, read via UCanAccess 5.0.1
//
// Key tables in SXYFF.mdb:
//   Customer : C_No, C_Name, C_address, C_MNo(meter), C_Left(remaining kWh),
//              C_Times, C_State, C_Update, C_Limit, C_Alarm, C_Tamount
//   BuyE     : B_No, B_CNo(→Customer.C_No), B_MNo(meter), B_AmountW(amount TL),
//              B_Date, B_Times, B_State, B_Left, B_Cost, B_Staff
//   Meters   : M_No, M_CNo(→Customer.C_No), M_State, M_Datein, M_Left
//   Other    : Areas, Users, EPrice, MeterKind, ST, ChangeM, Duc, Huan, Huifu, Ting, zhuxiao
//
// NOTE: DLB.exe (the actual prepaid UI) is native Delphi — not decompilable with ILSpy.
// Card data is painted via GDI, never stored in DB on plain read, only on purchase.
// Card reader is held exclusively by DLB.exe — javax.smartcardio cannot access it while DLB runs.
// =========================================================================
private static final String SETTINGS_DB  = "D:\\Prepaid Ordinary Card-Meter Charging Management System\\ELWORKS\\Settings.mdb";
private static final String SETTINGS_PWD = "ElworkS132Nanomet";
private static final String MAIN_DB      = "D:\\Prepaid Ordinary Card-Meter Charging Management System\\data\\SXYFF.mdb";
private static final String ENC_MAIN_PWD = "G/gTP7CZWkIrfPtC8oguJxclM3orimk6";

private static String desDecrypt(String encrypted) {
try {
String script =
"$iv = [byte[]](33,67,86,135,16,253,234,28);" +
"$key = [System.Text.Encoding]::UTF8.GetBytes('A0D1nX0Q');" +
"$data = [System.Convert]::FromBase64String('" + encrypted + "');" +
"$des = New-Object System.Security.Cryptography.DESCryptoServiceProvider;" +
"$des.Mode = [System.Security.Cryptography.CipherMode]::CBC;" +
"$des.Padding = [System.Security.Cryptography.PaddingMode]::PKCS7;" +
"$dec = $des.CreateDecryptor($key,$iv);" +
"$ms = New-Object System.IO.MemoryStream(,$data);" +
"$cs = New-Object System.Security.Cryptography.CryptoStream($ms,$dec,[System.Security.Cryptography.CryptoStreamMode]::Read);" +
"$sr = New-Object System.IO.StreamReader($cs);" +
"Write-Output $sr.ReadToEnd()";
ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script);
pb.redirectErrorStream(true);
Process proc = pb.start();
String result = new String(proc.getInputStream().readAllBytes()).trim();
proc.waitFor();
return result;
} catch (Exception e) {
return "DECRYPT_ERROR: " + e.getMessage();
}
}

public static Connection openMainDb() throws Exception {
Properties props = new Properties();
props.put("password", desDecrypt(ENC_MAIN_PWD));
return DriverManager.getConnection("jdbc:ucanaccess://" + MAIN_DB, props);
}

public static String queryDatabase(String sql) {
try {
Connection conn = openMainDb();
ResultSet rs = conn.createStatement().executeQuery(sql);
java.sql.ResultSetMetaData meta = rs.getMetaData();
int cols = meta.getColumnCount();
StringBuilder sb = new StringBuilder();
for (int i = 1; i <= cols; i++)
sb.append(String.format("%-20s", meta.getColumnName(i)));
sb.append("\n").append("-".repeat(cols * 20)).append("\n");
while (rs.next()) {
for (int i = 1; i <= cols; i++)
sb.append(String.format("%-20s", rs.getString(i) == null ? "" : rs.getString(i)));
sb.append("\n");
}
conn.close();
return sb.toString();
} catch (Exception e) {
return "DB Error: " + e.getMessage();
}
}

public static String fullSnapshot() {
try {
Connection conn = openMainDb();
String[] tables = {"Customer", "BuyE", "Meters", "ST", "Duc", "Huan",
"Huifu", "Ting", "zhuxiao", "ChangeM", "buka"};
StringBuilder sb = new StringBuilder();
for (String table : tables) {
try {
ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table);
java.sql.ResultSetMetaData meta = rs.getMetaData();
int cols = meta.getColumnCount();
StringBuilder rows = new StringBuilder();
while (rs.next()) {
for (int i = 1; i <= cols; i++)
rows.append(rs.getString(i)).append("|");
rows.append("\n");
}
sb.append(table).append(":").append(rows.toString().hashCode()).append("\n");
} catch (Exception ex) {
sb.append(table).append(": ERROR\n");
}
}
conn.close();
return sb.toString();
} catch (Exception e) {
return "DB Error: " + e.getMessage();
}
}

// =========================================================================
// NANOMET WINDOW CONTROL
// =========================================================================

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

Robot robot = new Robot();
robot.mouseMove(rect.left + 46, rect.top + 430);
Thread.sleep(500);
robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
Thread.sleep(150);
robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
Thread.sleep(500);
robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
Thread.sleep(150);
robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
}

public static String clickReadCard() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return null;
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] text = new char[512];
            User32.INSTANCE.GetWindowText(child, text, 512);
            if (new String(text).trim().equals("Read card")) {
                User32.INSTANCE.SendMessage(child, 0x00F5, new WPARAM(0), new LPARAM(0));
                return false;
            }
            return true;
        }, null);
        return waitForResultWindow(3000);
    }

    public static String clickLoad() {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return null;
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] text = new char[512];
            User32.INSTANCE.GetWindowText(child, text, 512);
            if (new String(text).trim().equals("Power purchase")) {
                User32.INSTANCE.SendMessage(child, 0x00F5, new WPARAM(0), new LPARAM(0));
                return false;
            }
            return true;
        }, null);
        return waitForResultWindow(5000);
    }

    // Watches for any new top-level window appearing after an action.
    // Returns the window text if found, null if timeout.
    private static String waitForResultWindow(int timeoutMs) {
        // Snapshot existing windows before
        java.util.Set<Long> before = new java.util.HashSet<>();
        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            before.add(Pointer.nativeValue(hwnd.getPointer()));
            return true;
        }, null);

        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try { Thread.sleep(200); } catch (Exception ignored) {}
            final String[] found = {null};
            User32.INSTANCE.EnumWindows((hwnd, data) -> {
                long id = Pointer.nativeValue(hwnd.getPointer());
                if (!before.contains(id) && User32.INSTANCE.IsWindowVisible(hwnd)) {
                    char[] txt = new char[512];
                    User32.INSTANCE.GetWindowText(hwnd, txt, 512);
                    String t = new String(txt).trim();
                    if (!t.isEmpty()) {
                        // Also grab child text for message content
                        final StringBuilder content = new StringBuilder(t).append(": ");
                        User32.INSTANCE.EnumChildWindows(hwnd, (child, d) -> {
                            char[] ct = new char[512];
                            User32.INSTANCE.GetWindowText(child, ct, 512);
                            String cs = new String(ct).trim();
                            if (!cs.isEmpty()) content.append(cs).append(" ");
                            return true;
                        }, null);
                        found[0] = content.toString().trim();
                        return false;
                    }
                }
                return true;
            }, null);
            if (found[0] != null) return found[0];
        }
        return null;
    }

public static void setAmount(String amount) {
        HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
        if (popup == null) return;

        final HWND[] fieldHwnd = {null};
        User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
            char[] cls = new char[512];
            User32.INSTANCE.GetClassName(child, cls, 512);
            if (new String(cls).trim().equals("TDBNumberEditEh")) {
                fieldHwnd[0] = child;
                return false;
            }
            return true;
        }, null);

        if (fieldHwnd[0] == null) return;

        try {
            // Click the field to focus it
            RECT r = new RECT();
            User32.INSTANCE.GetWindowRect(fieldHwnd[0], r);
            int cx = r.left + (r.right - r.left) / 2;
            int cy = r.top + (r.bottom - r.top) / 2;

            Robot robot = new Robot();
            robot.mouseMove(cx, cy);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(200);

            // Triple click to select all existing content
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);

            // Type the amount
            for (char c : amount.toCharArray()) {
                int keyCode = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(c);
                if (keyCode != java.awt.event.KeyEvent.VK_UNDEFINED) {
                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);
                    Thread.sleep(40);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public static boolean isPopupOpen() {
HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
if (popup == null) return false;
int style = User32.INSTANCE.GetWindowLong(popup, -16);
return (style & 0x10000000) != 0;
}

public static void demotePopup() {
HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
if (popup == null) return;
HWND HWND_NOTOPMOST = new HWND(new Pointer(-2));
User32.INSTANCE.SetWindowPos(popup, HWND_NOTOPMOST, 0, 0, 0, 0, 0x0001 | 0x0002);
}

public static String readMemoViaClipboard() {
HWND popup = User32.INSTANCE.FindWindow(null, POPUP_WINDOW);
if (popup == null) return "Power purchase window not found.";

final HWND[] memoHwnd = {null};
User32.INSTANCE.EnumChildWindows(popup, (child, data) -> {
char[] cls = new char[512];
User32.INSTANCE.GetClassName(child, cls, 512);
if (new String(cls).trim().equals("TMemo")) {
memoHwnd[0] = child;
return false;
}
return true;
}, null);

if (memoHwnd[0] == null) return "TMemo not found.";

try {
// Select all text via EM_SETSEL (works without focus or keyboard)
User32.INSTANCE.SendMessage(memoHwnd[0], 0x00B1, new WPARAM(0), new LPARAM(-1)); // EM_SETSEL 0,-1

// Copy via WM_COPY
User32.INSTANCE.SendMessage(memoHwnd[0], 0x0301, new WPARAM(0), new LPARAM(0)); // WM_COPY

Thread.sleep(200);
String clip = (String) java.awt.Toolkit.getDefaultToolkit()
.getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
if (clip != null && !clip.trim().isEmpty()) return clip.trim();

} catch (Exception e) {
return "Error: " + e.getMessage();
}

return "(Could not read via clipboard)";
}

public static String lookupCustomerName(String userCode) {
        try {
            if (userCode == null || userCode.equals("N/A")) return null;
            String trimmed = userCode.replaceFirst("^0+", "");
            Connection conn = openMainDb();
            // Match by trimmed C_No (DB may store as 000007, card sends 000000000007)
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT C_Name FROM Customer WHERE CLNG(C_No)=" + Long.parseLong(trimmed));
            if (rs.next()) {
                String name = rs.getString("C_Name");
                conn.close();
                return name;
            }
            conn.close();
        } catch (Exception e) {
            return null;
        }
        return null;
    }

public static String lookupCustomerByMeter(String meterNo) {
try {
if (meterNo == null || meterNo.equals("N/A")) return null;
Connection conn = openMainDb();
ResultSet rs = conn.createStatement().executeQuery(
"SELECT C_Name FROM Customer WHERE C_MNo='" + meterNo + "'");
if (rs.next()) {
String name = rs.getString("C_Name");
conn.close();
return name;
}
conn.close();
} catch (Exception e) {
return null;
}
return null;
}


}

