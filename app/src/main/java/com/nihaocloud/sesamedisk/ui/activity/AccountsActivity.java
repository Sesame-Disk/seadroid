package com.nihaocloud.sesamedisk.ui.activity;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.SettingsManager;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.account.AccountManager;
import com.nihaocloud.sesamedisk.account.Authenticator;
import com.nihaocloud.sesamedisk.monitor.FileMonitorService;
import com.nihaocloud.sesamedisk.ui.adapter.AccountAdapter;
import com.nihaocloud.sesamedisk.ui.adapter.NihaoAccountAdapter;
import com.nihaocloud.sesamedisk.ui.dialog.PolicyDialog;

import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class AccountsActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private final String DEBUG_TAG = "AccountsActivity";
    public final int DETAIL_ACTIVITY_REQUEST = 1;
    private android.accounts.AccountManager mAccountManager;
    private AccountManager accountManager;
    private AccountAdapter adapter;
    private List<Account> accounts;
    private FileMonitorService mMonitorService;
    private Account currentDefaultAccount;
    private final OnAccountsUpdateListener accountsUpdateListener = accounts -> refreshView();
    private final ServiceConnection mMonitorConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            FileMonitorService.MonitorBinder monitorBinder = (FileMonitorService.MonitorBinder) binder;
            mMonitorService = monitorBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mMonitorService = null;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.accounts);

        ListView accountsView = findViewById(R.id.account_list_view);
        @SuppressLint("InflateParams")
        View footerView = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.account_list_footer, null, false);

        Button addAccount = footerView.findViewById(R.id.account_footer_btn);
        registerForContextMenu(accountsView);

        mAccountManager = android.accounts.AccountManager.get(this);
        accountManager = new AccountManager(this);
        currentDefaultAccount = accountManager.getCurrentAccount();

        // updates toolbar back button
        boolean showHomeAsUp = currentDefaultAccount != null && currentDefaultAccount.hasValidToken();
        getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);

        addAccount.setOnClickListener(btn -> mAccountManager.addAccount(Account.ACCOUNT_TYPE,
                Authenticator.AUTHTOKEN_TYPE, null, null,
                AccountsActivity.this, accountCallback, null));

        accountsView.addFooterView(footerView, null, true);
        accountsView.setFooterDividersEnabled(false);
        adapter = new NihaoAccountAdapter(this);
        accountsView.setAdapter(adapter);
        accountsView.setOnItemClickListener((parent, view, position, id) -> {
            Account account = accounts.get(position);
            if (!account.hasValidToken()) {
                // user already signed out, input password first
                startEditAccountActivity(account);
            } else {
                // update current Account info from SharedPreference
                accountManager.saveCurrentAccount(account.getSignature());
                startFilesActivity();
            }
        });

        mAccountManager.addOnAccountsUpdatedListener(accountsUpdateListener, null, false);
        accounts = accountManager.getAccountList();

        String country = Locale.getDefault().getCountry();
        String language = Locale.getDefault().getLanguage();
        int privacyPolicyConfirmed = SettingsManager.instance().getPrivacyPolicyConfirmed();
        if (country.equals("CN") && language.equals("zh") && (privacyPolicyConfirmed == 0)) {
            showDialog();
        }
    }

    @Override
    public void onStart() {
        Log.d(DEBUG_TAG, "onStart");
        super.onStart();
        Intent bIntent = new Intent(this, FileMonitorService.class);
        bindService(bIntent, mMonitorConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        Log.d(DEBUG_TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        if (mMonitorService != null) {
            unbindService(mMonitorConnection);
            mMonitorService = null;
        }
        mAccountManager.removeOnAccountsUpdatedListener(accountsUpdateListener);
    }

    // Always reload accounts on resume, so that when user add a new account,
    // it will be shown.
    @Override
    public void onResume() {
        Log.d(DEBUG_TAG, "onResume");
        super.onResume();
        refreshView();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// if the current account sign out and no account was to logged in,
            // then always goes to AccountsActivity
            if (accountManager.getCurrentAccount() == null) {
                Intent intent = new Intent(this, BrowserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshView() {
        Log.d(DEBUG_TAG, "refreshView");
        accounts = accountManager.getAccountList();
        adapter.clear();
        adapter.setItems(accounts);

        // if the user switched default account while we were in background,
        // switch to BrowserActivity
        Account newCurrentAccount = accountManager.getCurrentAccount();
        if (newCurrentAccount != null && !newCurrentAccount.equals(currentDefaultAccount)) {
            startFilesActivity();
        }

        // updates toolbar back button
        if (newCurrentAccount == null || !newCurrentAccount.hasValidToken()) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }

        adapter.notifyChanged();
    }

    private void startFilesActivity() {
        Intent intent = new Intent(this, BrowserActivity.class);

        // first finish this activity, so the BrowserActivity is again "on top"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    AccountManagerCallback<Bundle> accountCallback = new AccountManagerCallback<Bundle>() {

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            if (future.isCancelled())
                return;

            try {
                Bundle b = future.getResult();

                if (b.getBoolean(android.accounts.AccountManager.KEY_BOOLEAN_RESULT)) {
                    String accountName = b.getString(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(DEBUG_TAG, "switching to account " + accountName);
                    accountManager.saveCurrentAccount(accountName);
                    startFilesActivity();
                }
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "unexpected error: " + e);
            }
        }
    };

    private void startEditAccountActivity(Account account) {
        mAccountManager.updateCredentials(account.getAndroidAccount(),
                Authenticator.AUTHTOKEN_TYPE, null, this, accountCallback, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                startFilesActivity();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Account account;
        switch (item.getItemId()) {
            case R.id.edit:
                account = adapter.getItem((int) info.id);
                startEditAccountActivity(account);
                return true;
            case R.id.delete:
                account = adapter.getItem((int) info.id);
                Log.d(DEBUG_TAG, "removing account " + account);
                mAccountManager.removeAccount(account.getAndroidAccount(), null, null);
                if (mMonitorService != null) {
                    mMonitorService.removeAccount(account);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Account account = accountManager.getCurrentAccount();
        if (account != null) {
            // force exit when current account was deleted
            Intent i = new Intent(this, BrowserActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else
            super.onBackPressed();
    }

    private void showDialog() {
        PolicyDialog mDialog = new PolicyDialog(AccountsActivity.this, R.style.PolicyDialog,
                confirm -> {
                    if (confirm) {
                        // TODO:
                        SettingsManager.instance().savePrivacyPolicyConfirmed(1);
                    } else {
                        // TODO:
                        System.exit(0);
                    }
                });
        mDialog.show();
        mDialog.setCancelable(false);
    }
}
