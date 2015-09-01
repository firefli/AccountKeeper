package org.firefli.accountkeeper.model;

import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;

/**
 * Created by firefli on 8/24/2015.
 */
public class DefaultAccount extends Account {

    private static final String DEFAULT_ACCT_NAME = "default";
    private static final String DEFAULT_ACCT_PWD = "default";

    public DefaultAccount() {
        setName(DEFAULT_ACCT_NAME);
    }

    public DefaultAccount(EncryptionManager eManager) throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        setName(DEFAULT_ACCT_NAME);
        setPassword(eManager, DEFAULT_ACCT_PWD.toCharArray());
    }

    public boolean unlock(EncryptionManager eManager) {
        try {
            return new String(getPassword(eManager)).equals("default");
        } catch (GeneralSecurityException e) {
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {}
        return false;
    }

    public static byte[] encryptedDefaultName(EncryptionManager eManager) throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        return eManager.encrypt(DEFAULT_ACCT_NAME.toCharArray());
    }

    public static boolean isDefaultAccount(String acctName) {
        return acctName.equals("default");
    }
}
