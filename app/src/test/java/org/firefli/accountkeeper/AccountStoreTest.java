package org.firefli.accountkeeper;

import junit.framework.TestCase;

import org.firefli.accountkeeper.model.Account;
import org.firefli.accountkeeper.model.DefaultAccount;
import org.firefli.accountkeeper.security.EncryptionManager;
import org.firefli.accountkeeper.store.AccountStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.security.GeneralSecurityException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test cases for AccountStore.
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AccountStoreTest extends TestCase {

    private static final char[] KEY = new char[]{'t','e','s','t'};
    private EncryptionManager eManager;
    private EncryptionManager.EncryptionManagerStorage encStore;
    private AccountStore acctStore;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws GeneralSecurityException {
        encStore = Mockito.mock(EncryptionManager.EncryptionManagerStorage.class);
        Mockito.when(encStore.retrieveSalt()).thenReturn(new byte[]{5, 6, 7, 8, 9, 10});
        eManager = new EncryptionManager(encStore);
        eManager.setKey(KEY);
        acctStore = new AccountStore(RuntimeEnvironment.application.getApplicationContext(), eManager);
    }

    @Test
    public void testFirstTimeSetup() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        eManager.removeKey();
        exception.expect(EncryptionManager.EncryptionManagerNeedsKeyException.class);
        acctStore.pull();
        exception.reportMissingExceptionWithMessage("Expected EncryptionManager.EncryptionManagerNeedsKeyException");
        eManager.setKey(KEY);
        List<Account> accountList = acctStore.pull();
        assertThat(accountList.size()).isGreaterThanOrEqualTo(1);
        boolean isDefaultFound = false;
        for(Account acct : accountList) {
            isDefaultFound = DefaultAccount.isDefaultAccount(acct);
            if(isDefaultFound) break;
        }
        assertThat(isDefaultFound).isTrue();
    }

    @Test
    public void testStoreAccounts() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        List<Account> accountList = acctStore.pull();
        // Assert that this is a fresh test preference store and test case.
        assertThat(accountList.size()).isEqualTo(1);
        // Remove default account.
        Account defAccount = accountList.remove(0);
        // Add two new accounts.
        accountList.add(new Account("test1", "test1pwd".toCharArray(), eManager));
        accountList.add(new Account("test2", "test2pwd".toCharArray(), eManager));
        // Store new accounts.
        acctStore.store(accountList);
        // Add default account back into expected accounts.
        accountList.add(defAccount);
        List<Account> storedAccountList = acctStore.pull();
        assertThat(storedAccountList).containsOnly(accountList.toArray(new Account[accountList.size()]));
        assertThat(acctStore.pullDefaultAccount().unlock(eManager)).isTrue();
    }

    @Test
    public void testDefaultPull() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        // First time sets up the account.
        DefaultAccount defAccount = acctStore.pullDefaultAccount();
        assertThat(defAccount).isNotNull();
        assertThat(defAccount.unlock(eManager)).isTrue();
        // Second time pulls the previously set up account.
        DefaultAccount defAccount2 = acctStore.pullDefaultAccount();
        assertThat(defAccount2).isNotNull();
        assertThat(defAccount2.unlock(eManager)).isTrue();
        assertThat(defAccount2.getRawPwd()).isEqualTo(defAccount.getRawPwd());
    }

}
