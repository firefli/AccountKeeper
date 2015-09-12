package org.firefli.accountkeeper;

import android.os.Parcel;

import junit.framework.TestCase;

import org.firefli.accountkeeper.model.Account;
import org.firefli.accountkeeper.security.EncryptionManager;
import org.firefli.accountkeeper.store.AccountStore;
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
 * Test cases for AccountStore.
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AccountModelTest extends TestCase {

    private static final char[] KEY = new char[]{'t','e','s','t'};
    private EncryptionManager eManager;
    private EncryptionManager.EncryptionManagerStorage encStore;
    private AccountStore acctStore;
    private Account acct;
    private static final String ACCT_NAME = "test";
    private static final char[] ACCT_PWD = "pass".toCharArray();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        encStore = Mockito.mock(EncryptionManager.EncryptionManagerStorage.class);
        eManager = new EncryptionManager(encStore);
        eManager.setKey(KEY);
        acct = new Account(ACCT_NAME, ACCT_PWD, eManager);
    }

    @Test
    public void testEncryptedPass() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        assertThat(acct.getPassword(eManager)).isEqualTo(ACCT_PWD);
    }

    @Test
    public void testParcelling() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        Parcel parcel = Parcel.obtain();
        acct.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Account parcelAcct = Account.CREATOR.createFromParcel(parcel);
        assertThat(parcelAcct).isNotSameAs(acct);
        assertThat(parcelAcct).isEqualTo(acct);
    }

}
