package org.firefli.accountkeeper.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by firefli on 11/29/14.
 */
public class EncryptionManager {

    //TODO: Major issue when re-entering password...

    public interface EncryptionManagerStorage {
        public void storeSalt(byte[] salt);
        public byte[] retrieveSalt();
    }

    private static int KEY_LENGTH = 256;
    private static int ITERATIONS = 10000;

    private EncryptionManagerStorage encryptionStore;
    private SecretKey mKey;
    private byte[] mSalt;

    public EncryptionManager(EncryptionManagerStorage store) {
        encryptionStore = store;
    }

    /**
     * Set the key to be used by the encryption manager. Intensive.
     * @param key
     */
    public void setKey(char[] key) throws GeneralSecurityException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(key, getSalt(), ITERATIONS, KEY_LENGTH);
        mKey = secretKeyFactory.generateSecret(spec);

        Arrays.fill(key, ' ');

//        try {
//            Assert.assertEquals(new String(decrypt(encrypt("test234".toCharArray()))), "test234");
//        } catch (EncryptionManagerNeedsKeyException e) {
//            e.printStackTrace();
//        }
    }

    private SecretKeySpec getKey() throws EncryptionManagerNeedsKeyException {
        if(!hasKey())
            throw new EncryptionManagerNeedsKeyException();

        return new SecretKeySpec(mKey.getEncoded(), "AES");
    }

    /**
     * Decrypt the provided data.
     * @param encryptedData
     * @return the decrypted data
     */
    public char[] decrypt(byte[] encryptedData) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        CharBuffer cbuf = Charset.defaultCharset().decode(ByteBuffer.wrap(decryptedBytes));
        char[] decrypted = new char[cbuf.limit()];
        cbuf.get(decrypted);
        //TODO: Clear the character buffer.
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
        //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // TODO: Make CBC/PKCS5Padding work.
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        int length = cipher.doFinal(inBuffer, outBuffer);
        byte[] encryptedBytes = new byte[length];
        outBuffer.flip();
        outBuffer.get(encryptedBytes, 0, length);
        inBuffer.clear();  //TODO: Clear the in and out buffers.
        outBuffer.clear();  //TODO: Clear the in and out buffers.
        return encryptedBytes;
    }

    private byte[] getSalt() {
        if(mSalt == null && (mSalt = encryptionStore.retrieveSalt()) == null) {
            mSalt = genNewSalt();
            encryptionStore.storeSalt(mSalt);
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

    //
    //  Exceptions
    //

    public static class EncryptionManagerNeedsKeyException extends Exception {}

}
