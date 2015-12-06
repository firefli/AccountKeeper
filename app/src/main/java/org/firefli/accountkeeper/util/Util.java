package org.firefli.accountkeeper.util;

import android.os.Build;

/**
 * Created by firefli on 12/5/2015.
 */
public class Util {

    public static boolean isRunningInEmulator() {
        return  Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK");
    }

}
