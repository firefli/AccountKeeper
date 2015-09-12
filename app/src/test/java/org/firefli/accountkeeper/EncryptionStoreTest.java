package org.firefli.accountkeeper;

import junit.framework.TestCase;

import org.firefli.accountkeeper.security.EncryptionManager;
import org.firefli.accountkeeper.store.EncryptionStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by firefli on 11/30/2014.
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class EncryptionStoreTest extends TestCase {

    private EncryptionManager.EncryptionManagerStorage encStore;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws GeneralSecurityException {
        encStore = new EncryptionStore(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testStorage() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        encStore.storeSalt(salt);
        byte[] pulledSalt = encStore.retrieveSalt();
        assertThat(pulledSalt).isEqualTo(salt);
    }

}
