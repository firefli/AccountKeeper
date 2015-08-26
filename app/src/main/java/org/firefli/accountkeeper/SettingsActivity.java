package org.firefli.accountkeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.firefli.accountkeeper.store.AccountStore;

/**
 * Created by firefli on 8/25/15.
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        public static final String KEY_PREF_RESET = "pref_key_reset";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final Preference myPref = (Preference) findPreference(KEY_PREF_RESET);

            if(!AccountStore.hasStore(getActivity())) {
                myPref.setEnabled(false);
            }

            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == DialogInterface.BUTTON_POSITIVE) {
                                AccountStore.deleteStore(getActivity());
                                myPref.setEnabled(false);
                            }
                        }
                    };
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Are you sure you want to reset all data?")
                            .setPositiveButton("Yes", onClickListener)
                            .setNegativeButton("No", onClickListener)
                            .show();
                    return true;
                }
            });
        }
    }
}
