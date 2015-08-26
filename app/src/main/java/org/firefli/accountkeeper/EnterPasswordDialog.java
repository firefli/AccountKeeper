package org.firefli.accountkeeper;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by fireli on 8/23/2015.
 */
public class EnterPasswordDialog extends DialogFragment implements TextView.OnEditorActionListener {

    public interface EnterPasswordDialogListener {
        public void onFinishPwdDialog(char[] inputText);
    }

    private EditText mEditText;

    public EnterPasswordDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_password, container);
        mEditText = (EditText) view.findViewById(R.id.txt_pwd);
        getDialog().setTitle("Enter your password");

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
            // Return input text to activity
            EnterPasswordDialogListener activity = (EnterPasswordDialogListener) getActivity();
            Editable pwdEditable = mEditText.getText();
            char[] pwdChars = new char[pwdEditable.length()];
            pwdEditable.getChars(0, pwdChars.length, pwdChars, 0);
            pwdEditable.clear();
            activity.onFinishPwdDialog(pwdChars);
            dismiss();
            return true;
        }
        return false;
    }
}
