package org.firefli.accountkeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import org.firefli.accountkeeper.model.DefaultAccount;
import org.firefli.accountkeeper.security.EncryptionManager;
import org.firefli.accountkeeper.store.AccountStore;
import org.firefli.accountkeeper.store.EncryptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

/**
 * BaseActivity
 *
 * Allows any extending activity to query for the password.
 *
 * Created by firefli on 8/29/15.
 */
public class BaseActivity extends Activity implements EnterPasswordDialog.EnterPasswordDialogListener {

    private static final Logger logger = LoggerFactory.getLogger(BaseActivity.class);

    // Shared encryption manager across all BaseActivity.
    protected static EncryptionManager eManager;

    private Runnable onPwdReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initObjects();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == TRIM_MEMORY_UI_HIDDEN) {
            eManager.removeKey();
        }
    }

    private void initObjects() {
        if(eManager == null) {
            eManager = new EncryptionManager(new EncryptionStore(this));
            if(BuildConfig.DEBUG)
                eManager.setOpenLockLength(10 * 1000);
        }
    }

    protected void showPwdDialog(Runnable onPwdRetun) {
        logger.trace("");
        this.onPwdReturn = onPwdRetun;
        FragmentManager fm = getFragmentManager();
        EnterPasswordDialog pwdDialog = new EnterPasswordDialog();
        pwdDialog.show(fm, "fragment_enter_pwd");
    }

    @Override
    public void onFinishPwdDialog(char[] inputText) {
        new SetKeyTask(onPwdReturn).execute(inputText);
        onPwdReturn = null;
    }

    protected void showAlertMessage(String alertMsg) {
        new AlertDialog.Builder(this).setMessage(alertMsg).setTitle("Something is wrong!").create().show();
    }

    protected class SetKeyTask extends AsyncTask<char[], Void, Boolean> {
        private Runnable onPwdReturn;
        private ProgressDialog progressIndicator;
        public SetKeyTask(Runnable onPwdReturn) {
            super();
            this.onPwdReturn = onPwdReturn;
        }
        @Override
        protected void onPreExecute() {
            progressIndicator = ProgressDialog.show(BaseActivity.this, null, "Please wait...", true, false);
        }
        @Override
        protected Boolean doInBackground(char[]... params) {
            boolean success = false;
            try {
                eManager.setKey(params[0]);
                DefaultAccount defAccount = new AccountStore(BaseActivity.this, eManager).pullDefaultAccount();
                if(defAccount != null)
                    success = defAccount.unlock(eManager);
            } catch (GeneralSecurityException e) {
                logger.debug(e.getMessage());
            } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
                // Impossible as this is the test subsequent to setting a key.
                throw new RuntimeException(e);
            }
            if(!success) eManager.removeKey();
            return success;
        }
        @Override
        protected void onPostExecute(Boolean success) {
            progressIndicator.dismiss();
            if(success) {
                //mLockMenuItem.setIcon(R.drawable.ic_lock_open_white_24dp); // TODO: Create callback methods from EncryptionManager to trigger icon change.s
                if (onPwdReturn != null)
                    onPwdReturn.run();
            } else
                showAlertMessage("Failed to unlock.");
        }
    }

}
