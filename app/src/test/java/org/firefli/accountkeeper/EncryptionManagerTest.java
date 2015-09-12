package org.firefli.accountkeeper;

import android.util.Base64;

import junit.framework.TestCase;

import org.firefli.accountkeeper.security.EncryptionManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.security.GeneralSecurityException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by firefli on 11/30/2014.
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class EncryptionManagerTest extends TestCase {

    private static final char[] KEY = new char[]{'t','e','s','t'};
    private EncryptionManager manager;
    private char[] plainText;
    private EncryptionManager.EncryptionManagerStorage encStore;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws GeneralSecurityException {
        plainText = "I need to be encrypted".toCharArray();
        encStore = Mockito.mock(EncryptionManager.EncryptionManagerStorage.class);
        manager = new EncryptionManager(encStore);
        manager.setKey(KEY);
    }

    @Test
    public void testEncrypt() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        byte[] encryptedData = manager.encrypt(plainText);
        assertThat(encryptedData).isNotNull();
        assertThat(encryptedData).isNotEmpty();
        char[] decryptedText = manager.decrypt(encryptedData);
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    public void testResetKey() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        byte[] encryptedData = manager.encrypt(plainText);
        char[] decryptedText = manager.decrypt(encryptedData);
        manager.setKey(new char[]{'t','s','e','t'});
        exception.expect(GeneralSecurityException.class);
        char[] decryptedText2 = manager.decrypt(encryptedData);
        exception.reportMissingExceptionWithMessage("Expected GeneralSecurityException");
        manager.setKey(new char[]{'t', 'e', 's', 't'});
        assertThat(decryptedText).isEqualTo(decryptedText2);
    }

    @Test
    public void testNewManager() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        encStore = new EncryptionManager.EncryptionManagerStorage() {
            byte[] salt = null;
            @Override
            public void storeSalt(byte[] salt) {
                this.salt = salt;
            }
            @Override
            public byte[] retrieveSalt() {
                return salt;
            }
        };
        manager = new EncryptionManager(encStore);
        manager.setKey(KEY);
        byte[] encryptedData = manager.encrypt(plainText);
        manager = new EncryptionManager(encStore);
        manager.setKey(KEY);
        assertThat(manager.decrypt(encryptedData)).isEqualTo(plainText);
    }

    @Test
    public void testRemoveKey() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        manager.removeKey();
        exception.expect(EncryptionManager.EncryptionManagerNeedsKeyException.class);
        manager.encrypt(plainText);
        exception.reportMissingExceptionWithMessage("Expected EncryptionManager.EncryptionManagerNeedsKeyException");
    }

    @Test
    public void testSavedSalt() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        Mockito.when(encStore.retrieveSalt()).thenReturn(new byte[]{5, 6, 7, 8, 9, 10});
        byte[] encryptedData = manager.encrypt(plainText);
        char[] decryptedText = manager.decrypt(encryptedData);
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    public void testBase64Encrypt() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        int base64settings = Base64.NO_CLOSE | Base64.NO_PADDING | Base64.NO_WRAP;
        char[] input = "TEST1234".toCharArray();
        assertThat(manager.base64Decrypt(manager.base64Encrypt(input, base64settings), base64settings)).isEqualTo(input);
    }

}
