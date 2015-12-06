package org.firefli.accountkeeper.security;

import android.app.Activity;
import android.util.Base64;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by firefli on 11/29/14.
 */
public class EncryptionManager {

    public interface EncryptionManagerStorage {
        public void storeSalt(byte[] salt);
        public byte[] retrieveSalt();
    }

    public interface EncryptionManagerListener {
        public Activity getActivity();
        public void onEncryptionManagerUnlocked();
        public void onEncryptionManagerLocked();
    }

    private static int KEY_LENGTH = 256;
    private static int ITERATIONS = 10000;
    private static int DEFAULT_UNLOCK_TIME = 5 * 60 * 1000;  // 5 minutes
    private static final int BLOCK_SIZE = 16;
    private static final int IV_LENGTH = BLOCK_SIZE;

    private EncryptionManagerStorage encryptionStore;
    private SecretKey mKey;
    private byte[] mSalt;
    private WeakReference<EncryptionManagerListener> mEncryptionManagerListener;
    private long unlockLength;
    private Timer lockTimer;

    public EncryptionManager(EncryptionManagerStorage store) {
        encryptionStore = store;
        unlockLength = DEFAULT_UNLOCK_TIME;
    }

    /**
     * Set the key to be used by the encryption manager. Intensive.
     * @param key
     */
    public void setKey(char[] key) throws GeneralSecurityException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(key, getSalt(), ITERATIONS, KEY_LENGTH);
        mKey = secretKeyFactory.generateSecret(spec);
        startUnlockTimer();
        Arrays.fill(key, ' ');
        notifyUnlocked();
    }

    public void removeKey() {
        mKey = null;
        lockTimer.cancel();
        notifyLocked();
    }

    public boolean hasKey() {
        return mKey != null;
    }

    public void setOpenLockLength(long time) {
        this.unlockLength = time;
    }

    private SecretKeySpec getKey() throws EncryptionManagerNeedsKeyException {
        if(!hasKey()) throw new EncryptionManagerNeedsKeyException();
        return new SecretKeySpec(mKey.getEncoded(), "AES");
    }

    public void setListener(EncryptionManagerListener listener) {
        mEncryptionManagerListener = new WeakReference<EncryptionManagerListener>(listener);
    }

    private void notifyLocked() {
        final EncryptionManagerListener listener;
        if(mEncryptionManagerListener != null && (listener = mEncryptionManagerListener.get()) != null) {
            listener.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onEncryptionManagerLocked();
                }
            });
        }
    }

    private void notifyUnlocked() {
        final EncryptionManagerListener listener;
        if (mEncryptionManagerListener != null && (listener = mEncryptionManagerListener.get()) != null) {
            listener.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onEncryptionManagerUnlocked();
                }
            });
        }
    }

    /**
     * Decrypt the provided data.
     * @param encryptedData
     * @return the decrypted data
     */
    public char[] decrypt(byte[] encryptedData) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getKey(), getIv(encryptedData));
        byte[] decryptedBytes = cipher.doFinal(encryptedData, IV_LENGTH, encryptedData.length - IV_LENGTH);
        char[] decrypted = null;
        if(decryptedBytes == null) {
            decrypted = new char[0];
        } else {
            CharBuffer cbuf = Charset.defaultCharset().decode(ByteBuffer.wrap(decryptedBytes));
            decrypted = new char[cbuf.limit()];
            cbuf.get(decrypted);
        }
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
        ByteBuffer outBuffer = ByteBuffer.allocate(inBuffer.capacity() + BLOCK_SIZE);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = getRandIv();
        cipher.init(Cipher.ENCRYPT_MODE, getKey(), ivSpec);
        int length = cipher.doFinal(inBuffer, outBuffer);
        byte[] encryptedBytes = new byte[length + IV_LENGTH];
        System.arraycopy(ivSpec.getIV(), 0, encryptedBytes, 0, IV_LENGTH);
        outBuffer.flip();
        outBuffer.get(encryptedBytes, IV_LENGTH, length);
        inBuffer.clear();  //TODO: Clear the in and out buffers.
        outBuffer.clear();  //TODO: Clear the in and out buffers.
        return encryptedBytes;
    }

    public String base64Encrypt(char[] plainTextData, int base64Options) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        return new String(Base64.encodeToString(encrypt(plainTextData), base64Options));
    }

    public char[] base64Decrypt(String base64EncodedEncryptedString, int base64Options) throws GeneralSecurityException, EncryptionManagerNeedsKeyException {
        return decrypt(Base64.decode(base64EncodedEncryptedString, base64Options));
    }

    private static IvParameterSpec getIv(byte[] ivSrc){
        return new IvParameterSpec(ivSrc, 0, IV_LENGTH);
    }

    private static IvParameterSpec getRandIv(){
        byte[] s = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(s);
        return new IvParameterSpec(s);
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

    public void startUnlockTimer() {
        lockTimer = new Timer();
        lockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeKey();
            }
        }, new Date(System.currentTimeMillis() + unlockLength));
    }

    //
    //  Exceptions
    //

    public static class EncryptionManagerNeedsKeyException extends Exception {}

}
