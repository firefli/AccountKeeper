package org.firefli.accountkeeper;

import android.app.Application;

import org.firefli.accountkeeper.store.AccountStore;
import org.firefli.accountkeeper.store.EncryptionStore;
import org.firefli.accountkeeper.util.Logger;
import org.firefli.accountkeeper.util.Util;

/**
 * Created by Kyle on 12/5/2015.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(Util.isRunningInEmulator()) {
            // Reset storage as there is a problem with encryption during emulation after restarting the app.
            Logger.d("Detected as on emulator! Reseting store!");
            EncryptionStore.deleteStore(this);
            AccountStore.deleteStore(this);
        }
    }

}
