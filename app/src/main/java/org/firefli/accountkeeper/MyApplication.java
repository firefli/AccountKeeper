package org.firefli.accountkeeper;

import android.app.Application;

import org.firefli.accountkeeper.store.AccountStore;
import org.firefli.accountkeeper.store.EncryptionStore;
import org.firefli.accountkeeper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by firefli on 12/5/2015.
 */
public class MyApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MyApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
        if(Util.isRunningInEmulator()) {
            // Reset storage as there is a problem with encryption during emulation after restarting the app.
            logger.warn("Detected as on emulator! Reseting store!");
            EncryptionStore.deleteStore(this);
            AccountStore.deleteStore(this);
        }
    }
}
