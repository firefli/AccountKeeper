package org.firefli.accountkeeper;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.firefli.accountkeeper.model.Account;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity {

    private List<Account> mAccountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initObjects();
        initLayout();
    }

    private void initObjects() {
        mAccountList = new LinkedList<Account>();
        for(int i = 0; i < 10; i++) {
            Account currAcct = new Account();
            currAcct.setName("TEST_"+i);
            mAccountList.add(currAcct);
        }
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
                if(view == null) {
                    view = getLayoutInflater().inflate(R.layout.account_item, null, false);
                }
                Account currAcct = mAccountList.get(i);
                ((TextView)view.findViewById(R.id.textName)).setText(currAcct.getName());
                ((TextView)view.findViewById(R.id.textPass)).setText("*****");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
