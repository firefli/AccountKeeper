package org.firefli.accountkeeper.model;

import org.firefli.accountkeeper.security.EncryptionManager;

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

    public byte[] getRawPassword() {
        return ePass;
    }

    public char[] getPassword(EncryptionManager eManager) { return eManager.decrypt(ePass); }

    public void setPassword(byte[] password) {
        this.ePass = password;
    }

}
