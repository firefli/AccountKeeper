package org.firefli.accountkeeper.security;

import android.accounts.NetworkErrorException;
import android.database.CharArrayBuffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by firefli on 11/29/14.
 */
public class EncryptionManager {

    private static int KEY_LENGTH = 256;
    private static int ITERATIONS = 1000;

    private SecretKey mKey;
    private byte[] mSalt;

    /**
     * Set the key to be used by the encryption manager.
     * @param key
     */
    public void setKey(char[] key) throws GeneralSecurityException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(key, getSalt(false), ITERATIONS, KEY_LENGTH);
        mKey = secretKeyFactory.generateSecret(spec);
    }

    private SecretKeySpec getKey() throws EncryptionManagerNeedsKeyException {
        if(!hasKey())
            throw new EncryptionManagerNeedsKeyException();

        SecretKeySpec skeySpec = new SecretKeySpec(mKey.getEncoded(), "AES");

        return skeySpec;
    }

    /**
     * Decrypt the provided data.
     * @param encryptedData
     * @return the decrypted data
     */
    public char[] decrypt(byte[] encryptedData) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        CharBuffer buffer = ByteBuffer.wrap(decryptedBytes).asCharBuffer();
        char[] decrypted = new char[buffer.length()];
        buffer.get(decrypted.length);
        buffer.clear();
        return decrypted;
    }

    /**
     * Encrypt the provided data.
     * @param plainTextData
     * @return the encrypted data
     */
    public byte[] encrypt(char[] plainTextData) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        ByteBuffer inBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(plainTextData));
        ByteBuffer outBuffer = ByteBuffer.allocate(inBuffer.capacity() * 10);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        cipher.doFinal(inBuffer, outBuffer);
        byte[] encryptedBytes = outBuffer.array();
        inBuffer.clear();
        outBuffer.clear();
        return encryptedBytes;
    }

    public void setSalt(byte[] salt) {
        mSalt = Arrays.copyOf(salt, salt.length);
    }

    private byte[] getSalt(boolean newSalt) {
        if(mSalt == null || newSalt) {
            mSalt = genNewSalt();
        }
        return mSalt;
    }

    private byte[] genNewSalt() {
        byte[] salt = new byte[KEY_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public void removeKey() {
        mKey = null;
    }

    public boolean hasKey() {
        return mKey != null;
    }

    public static class EncryptionManagerNeedsKeyException extends Exception {}

}
