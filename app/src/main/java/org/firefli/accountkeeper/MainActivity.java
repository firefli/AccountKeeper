package org.firefli.accountkeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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

import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener, EnterPasswordDialog.EnterPasswordDialogListener {

    private List<Account> mAccountList;
    private DefaultAccount defaultAccount;
    private EncryptionManager eManager;
    private Runnable onPwdReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initObjects();
        initLayout();
        //loadAccounts();
    }

    private void initObjects() {
        mAccountList = new LinkedList<Account>();
        eManager = new EncryptionManager();
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
                return 0;
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
                            if(defaultAccount.unlock(eManager)) {
                                lockMenuItem.setIcon(R.drawable.ic_lock_open_white_24dp);
                            } else {
                                showPwdDialog(this);
                            }
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

    private void showPwdDialog(Runnable onPwdRetun) {
        this.onPwdReturn = onPwdRetun;
        FragmentManager fm = getFragmentManager();
        EnterPasswordDialog pwdDialog = new EnterPasswordDialog();
        pwdDialog.show(fm, "fragment_enter_pwd");
    }

    private void showAlertMessage(String alertMsg) {
        new AlertDialog.Builder(this).setMessage(alertMsg).setTitle("Something is wrong!").create().show();
    }

    @Override
    public void onFinishPwdDialog(char[] inputText) {
        new SetKeyTask(onPwdReturn).execute(inputText);
        onPwdReturn = null;
    }

    private void loadAccounts() {
        try {
            mAccountList.addAll(new AccountStore(this, eManager).pull());
            for (Account acct : mAccountList) {
                if (acct instanceof DefaultAccount) {
                    defaultAccount = (DefaultAccount)acct;
                }
            }
            mAccountList.remove(defaultAccount);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            showAlertMessage("Encryption failure!");
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
            showPwdDialog(new Runnable() {
                public void run() {
                    loadAccounts();
                }
            });
        }
    }

    private void showAccountPwd(final Account acct) {
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

    private class SetKeyTask extends AsyncTask<char[], Void, Void> {
        private Runnable onPwdReturn;
        private ProgressDialog progressIndicator;
        public SetKeyTask(Runnable onPwdReturn) {
            super();
            this.onPwdReturn = onPwdReturn;
        }
        @Override
        protected void onPreExecute() {
            progressIndicator = ProgressDialog.show(MainActivity.this, null, "Please wait...", true, false);
        }
        @Override
        protected Void doInBackground(char[]... params) {
            try {
                eManager.setKey(params[0]);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            progressIndicator.dismiss();
            if(onPwdReturn != null)
                onPwdReturn.run();
        }
    }

}
