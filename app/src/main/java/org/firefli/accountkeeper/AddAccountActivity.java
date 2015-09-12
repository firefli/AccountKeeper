package org.firefli.accountkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.firefli.accountkeeper.model.Account;
import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;

public class AddAccountActivity extends BaseActivity implements View.OnClickListener{

    public static final String EXTRA_NEW_ACCOUNT = "newAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        initView();
    }

    private void initView() {
        ((Button)findViewById(R.id.btn_enter)).setOnClickListener(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_add_account, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onClick(View v) {
        if(v == findViewById(R.id.btn_enter)) {
            returnNewAccount();
        }
    }

    private void returnNewAccount() {
        try {
            Account account = new Account();
            account.setName(((EditText) findViewById(R.id.input_acct_name)).getText().toString());
            Editable pwdSequence = ((EditText) findViewById(R.id.input_pass)).getText();
            account.setPassword(eManager, pwdSequence);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_NEW_ACCOUNT, account);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (EncryptionManager.EncryptionManagerNeedsKeyException e) {
            showPwdDialog(new Runnable() {
                @Override
                public void run() {
                    returnNewAccount();
                }
            });
        }
    }
}
