package org.firefli.accountkeeper.security;

/**
 * Created by firefli on 11/29/14.
 */
public class EncryptionManager {

    private byte[] mPass;

    /**
     * Set the password to be used by the encryption manager.
     * @param password
     */
    public void setPassword(char[] password) {

    }

    /**
     * Decrypt the provided data.
     * @param encryptedData
     * @return the decrypted data
     */
    public char[] decrypt(byte[] encryptedData) {
        return new char[]{'t','e','s','t'};
    }

    /**
     * Encrypt the provided data.
     * @param plainTextData
     * @return the encrypted data
     */
    public byte[] encrypt(char[] plainTextData) {
        return new byte[]{0,0,0,0};
    }

}
