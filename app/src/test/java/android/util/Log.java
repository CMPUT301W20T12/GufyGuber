/* Neat trick from: https://stackoverflow.com/a/46793567
   Author: Paglian
   Edited by: Robert MacGillivray
   Purpose: To effectively mock the log class for unit testing.
 */
package android.util;

public class Log {
    public static int d(String tag, String msg) {
        System.out.println("Mock debug log: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("Mock info log: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("Mock warning log: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("Mock error log: " + tag + ": " + msg);
        return 0;
    }
}
