package org.firefli.accountkeeper.util;

import android.util.Log;

/**
 * Created by firefli on 8/26/15.
 */
public class Logger {

    public static void d(String msg) {
        Log.d("Account Keeper", msg);
    }

    public static void t() {
        StackTraceElement callingElem = Thread.currentThread().getStackTrace()[1];
        Log.v("Account Keeper", callingElem.getClassName() + "/" + callingElem.getMethodName() + ":" + callingElem.getLineNumber());
    }

}
