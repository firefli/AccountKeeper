package org.firefli.accountkeeper.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.firefli.accountkeeper.model.Account;
import org.firefli.accountkeeper.model.DefaultAccount;
import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by firefli on 8/23/2015.
 */
public class AccountStore {

    private static final String PREFS_NAME = "Store";
    private static final int BASE64_SETTINGS = Base64.NO_WRAP | Base64.NO_CLOSE | Base64.NO_PADDING;

    public Context mCtx;
    public EncryptionManager eManager;


    public AccountStore(Context ctx, EncryptionManager manager) {
        mCtx = ctx.getApplicationContext();
        eManager = manager;
    }

    public void store(List<Account> accounts) throws EncryptionManager.EncryptionManagerNeedsKeyException, GeneralSecurityException {
        SharedPreferences prefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        Set<String> eNames = new HashSet<String>();

        for(Account account : accounts) {
            String eName = eManager.base64Encrypt(account.getName().toCharArray(), BASE64_SETTINGS);
            eNames.add(eName);
            String b64pwd = Base64.encodeToString(account.getRawPwd(), BASE64_SETTINGS);
            if(!prefs.contains(eName) || !prefs.getString(eName, "").equals(b64pwd)) {
                prefsEdit.putString(eName, b64pwd);
            }
        }

        for(String key : prefs.getAll().keySet()) {
            if(!eNames.contains(key) ) {
                char[] acctName = eManager.base64Decrypt(key, BASE64_SETTINGS);
                if(!DefaultAccount.isDefaultAccount(acctName)) {
                    prefsEdit.remove(key);
                }
            }
        }

        prefsEdit.commit();
    }

    public DefaultAccount pullDefaultAccount() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        DefaultAccount defAccount = null;
        SharedPreferences prefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
        Map<String, String> savedAccounts = (Map<String, String>) prefs.getAll();
        if(savedAccounts.isEmpty()) {
            defAccount = createDefaultAccount();
        } else {
            for(String eAcctName : savedAccounts.keySet()) {
                if (DefaultAccount.isDefaultAccount(eManager.base64Decrypt(eAcctName, BASE64_SETTINGS))) {
                    defAccount = new DefaultAccount();
                    defAccount.setRawPwd(Base64.decode(savedAccounts.get(eAcctName), BASE64_SETTINGS));
                }
            }
        }
        return defAccount;
    }

    public List<Account> pull() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        SharedPreferences prefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
        Map<String, String> savedAccounts = (Map<String,String>)prefs.getAll();
        List<Account> accounts = new ArrayList<Account>(savedAccounts.size());

        for(String eAcctName : savedAccounts.keySet()) {
            char[] name = eManager.base64Decrypt(eAcctName, BASE64_SETTINGS);
            Account nextAcct = DefaultAccount.isDefaultAccount(name)? new DefaultAccount() : new Account();
            nextAcct.setName(new String(name));
            nextAcct.setRawPwd(Base64.decode(savedAccounts.get(eAcctName), BASE64_SETTINGS));
            accounts.add(nextAcct);
        }

        if(accounts.isEmpty()) {
            accounts.add(createDefaultAccount());
        }

        return accounts;
    }

    private DefaultAccount createDefaultAccount() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        DefaultAccount defaultAcct = new DefaultAccount(eManager);
        store(Arrays.asList(new Account[]{defaultAcct}));
        return defaultAcct;
    }

    public static boolean hasStore(Context ctx) {
        return !ctx.getSharedPreferences(PREFS_NAME, 0).getAll().isEmpty();
    }

    public static void deleteStore(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().clear().commit();
    }

}
