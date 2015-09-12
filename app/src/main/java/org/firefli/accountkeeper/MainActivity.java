package org.firefli.accountkeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.firefli.accountkeeper.model.Account;
import org.firefli.accountkeeper.model.DefaultAccount;
import org.firefli.accountkeeper.security.EncryptionManager;
import org.firefli.accountkeeper.store.AccountStore;
import org.firefli.accountkeeper.util.Logger;

import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String LOG_TAG = "MainActivity";
    private static final int ADD_ACCOUNT_REQ_CODE = 0;

    private AccountStore mAccountStore;
    private List<Account> mAccountList;
    private DefaultAccount defaultAccount;

    private MenuItem mLockMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initObjects();
        initLayout();
        loadAccounts();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADD_ACCOUNT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            Account newAccount = data.getParcelableExtra(AddAccountActivity.EXTRA_NEW_ACCOUNT);
            addNewAccount(newAccount);
        }
    }

    private void initObjects() {
        mAccountList = new LinkedList<Account>();
        mAccountStore = new AccountStore(this, eManager);
    }

    private void initLayout() {
        ListView accountListView = (ListView)findViewById(R.id.accountList);
        accountListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mAccountList.size();
            }

            @Override
            public Object getItem(int i) {
                return mAccountList.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.account_item, null, false);
                }
                Account currAcct = mAccountList.get(i);
                ((TextView) view.findViewById(R.id.textName)).setText(currAcct.getName());
                ((TextView) view.findViewById(R.id.textPass)).setText("*****");
                ((Button) view.findViewById(R.id.buttonShow)).setOnClickListener(MainActivity.this);
                ((Button) view.findViewById(R.id.buttonShow)).setTag(currAcct);
                return view;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mLockMenuItem = menu.findItem(R.id.action_lock);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add:
                displayAddNewAccount();
                break;
            case R.id.action_delete:
                break;
            case R.id.action_lock:
                final MenuItem lockMenuItem = item;
                if(eManager.hasKey()) {
                    eManager.removeKey();
                    lockMenuItem.setIcon(R.drawable.ic_lock_outline_white_24dp);
                } else {
                    showPwdDialog(new Runnable() {
                        public void run() {
                            lockMenuItem.setIcon(R.drawable.ic_lock_open_white_24dp);
                        }
                    });
                }
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getTag() instanceof Account) {
           showAccountPwd((Account)view.getTag());
        }
    }

    private boolean loadAccounts() {
        Logger.t();
        boolean hasLoaded = false;
        try {
            mAccountList.addAll(new AccountStore(this, eManager).pull());
            for (Account acct : mAccountList) {
                if (acct instanceof DefaultAccount) {
                    defaultAccount = (DefaultAccount)acct;
                }
            }
            mAccountList.remove(defaultAccount);
            ((BaseAdapter)((ListView) findViewById(R.id.accountList)).getAdapter()).notifyDataSetChanged();
            hasLoaded = true;
        } catch (GeneralSecurityException e) {
            Logger.d(e.getMessage());
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
            showPwdDialog(new Runnable() {
                public void run() {
                    loadAccounts();
                }
            });
        }
        return hasLoaded;
    }

    private void showAccountPwd(final Account acct) {
        Logger.t();
        try {
            acct.getPassword(eManager);
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
            showPwdDialog(new Runnable() {
                public void run() {
                    showAccountPwd(acct);
                }
            });
        } catch (GeneralSecurityException e) {
            showAlertMessage("Encryption failure.");
        }
    }

    private void displayAddNewAccount() {
        Intent intent = new Intent(this, AddAccountActivity.class);
        startActivityForResult(intent, ADD_ACCOUNT_REQ_CODE);
    }

    private void addNewAccount(final Account acct) {
        try {
            mAccountList.add(acct);
            mAccountStore.store(mAccountList);
            ((BaseAdapter)((ListView) findViewById(R.id.accountList)).getAdapter()).notifyDataSetChanged();
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
            showPwdDialog(new Runnable() {
                public void run() {
                    addNewAccount(acct);
                }
            });
        } catch (GeneralSecurityException e) {
            showAlertMessage("Encryption failure.");
        }
    }

}
