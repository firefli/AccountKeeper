package org.firefli.accountkeeper.model;

import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;

/**
 * Created by firefli on 11/29/14.
 */
public class Account {

    private String name;
    private byte[] ePass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPassword() {
        return ePass != null;
    }

    public char[] getPassword(EncryptionManager eManager) throws EncryptionManager.EncryptionManagerNeedsKeyException, GeneralSecurityException {
        return eManager.decrypt(ePass);
    }

    public void setPassword(EncryptionManager eManager, char[] password) throws EncryptionManager.EncryptionManagerNeedsKeyException, GeneralSecurityException {
        this.ePass = eManager.encrypt(password);
    }

    public byte[] getRawPwd() {
        return ePass;
    }

    public void setRawPwd(byte[] rawPwd) {
        ePass = rawPwd;
    }
}
