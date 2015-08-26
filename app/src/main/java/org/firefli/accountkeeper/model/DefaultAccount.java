package org.firefli.accountkeeper.model;

import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;

/**
 * Created by Kyle on 8/24/2015.
 */
public class DefaultAccount extends Account {

    public DefaultAccount() {}

    public DefaultAccount(EncryptionManager eManager) throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        setName("default");
        setPassword(eManager, "default".toCharArray());
    }

    public boolean unlock(EncryptionManager eManager) {
        try {
            return getPassword(eManager).equals("default");
        } catch (GeneralSecurityException e) {
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {}
        return false;
    }

    public static boolean isDefaultAccount(String acctName) {
        return acctName.equals("default");
    }
}
