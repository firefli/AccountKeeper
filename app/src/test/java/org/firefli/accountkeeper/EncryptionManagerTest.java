package org.firefli.accountkeeper;

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
        manager.setKey(KEY);
        char[] decryptedText2 = manager.decrypt(encryptedData);
        assertThat(decryptedText).isEqualTo(decryptedText2);
    }

    @Test
    public void testRemoveKey() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        manager.removeKey();
        exception.expect(EncryptionManager.EncryptionManagerNeedsKeyException.class);
        manager.encrypt(plainText);
    }

    @Test
    public void testSavedSalt() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        Mockito.when(encStore.retrieveSalt()).thenReturn(new byte[]{5, 6, 7, 8, 9, 10});
        byte[] encryptedData = manager.encrypt(plainText);
        char[] decryptedText = manager.decrypt(encryptedData);
        assertThat(decryptedText).isEqualTo(plainText);
    }

}
