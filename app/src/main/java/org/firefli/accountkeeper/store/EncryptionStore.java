package org.firefli.accountkeeper.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.firefli.accountkeeper.security.EncryptionManager;

/**
 * Created by firefli on 8/30/15.
 */
public class EncryptionStore implements EncryptionManager.EncryptionManagerStorage {

    private static final String PREFS_NAME = "EncStore";
    private static final String KEY_SALT = "Salt";
    private static final int BASE64_SETTINGS = Base64.NO_WRAP | Base64.NO_CLOSE | Base64.NO_PADDING;

    private Context mCtx;

    public EncryptionStore(Context ctx) {
        mCtx = ctx.getApplicationContext();
    }

    @Override
    public void storeSalt(byte[] salt) {
        SharedPreferences prefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putString(KEY_SALT, Base64.encodeToString(salt, BASE64_SETTINGS)).commit();
    }

    @Override
    public byte[] retrieveSalt() {
        byte[] salt = null;
        SharedPreferences prefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
        String saltStr = prefs.getString(KEY_SALT, null);
        if(saltStr != null) {
            salt = Base64.decode(saltStr, BASE64_SETTINGS);
        }
        return salt;
    }

    public static void deleteStore(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().clear().commit();
    }
}
