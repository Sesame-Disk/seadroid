package com.nihaocloud.sesamedisk.account.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.account.Authenticator;
import com.nihaocloud.sesamedisk.cameraupload.CameraUploadManager;
import com.nihaocloud.sesamedisk.ui.BaseAuthenticatorActivity;
import com.nihaocloud.sesamedisk.ui.adapter.ItemArrayAdapter;

import java.util.Objects;

/**
 * The Authenticator activity.
 * <p>
 * Called by the Authenticator and in charge of identifing the user.
 * <p>
 * It sends back to the Authenticator the result.
 */
public class SeafileAuthenticatorActivity extends BaseAuthenticatorActivity {
    private final String DEBUG_TAG = this.getClass().getSimpleName();
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_SERVER_URI = "SERVER_URI";
    public final static String ARG_EDIT_OLD_ACCOUNT_NAME = "EDIT_OLD_ACCOUNT";
    public final static String ARG_EMAIL = "EMAIL";
    public final static String ARG_NAME = "NAME";
    public final static String ARG_SHIB = "SHIB";
    public final static String ARG_AUTH_SESSION_KEY = "TWO_FACTOR_AUTH";
    public final static String ARG_IS_EDITING = "isEdited";
    private static final int REQ_SIGNUP = 1;
    private AccountManager mAccountManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seafile_authenticator);

        final String[] array = getResources().getStringArray(R.array.choose_server_array);
        final ItemArrayAdapter<String> listAdapter = new ItemArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, array, data -> Uri.parse(data).getHost());
        final ListView listView = findViewById(R.id.account_create_list);

        @SuppressLint("InflateParams")
        View footerView = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.server_list_footer, null, false);

        listView.addFooterView(footerView, null, false);
        listView.setFooterDividersEnabled(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setAdapter(listAdapter);
        if (array.length > 0) listView.setItemChecked(0, true);

        final Button singleSignOnButton = footerView.findViewById(R.id.single_sign_on_button);
        singleSignOnButton.setOnClickListener(v -> {
            final int position = listView.getCheckedItemPosition();
            final String url = listAdapter.getItem(position);
            final Intent intent = new Intent(SeafileAuthenticatorActivity.this, SingleSignOnAuthorizeActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(SingleSignOnAuthorizeActivity.SINGLE_SIGN_ON_SERVER_URL, url);
            startActivityForResult(intent, REQ_SIGNUP);
        });

        final Button basicAuthenticationButton = footerView.findViewById(R.id.basic_authentication_button);
        basicAuthenticationButton.setOnClickListener(v -> {
            final int position = listView.getCheckedItemPosition();
            final String url = listAdapter.getItem(position);
            final Intent intent = new Intent(SeafileAuthenticatorActivity.this, AccountDetailActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(AccountDetailActivity.BASIC_SIGN_ON_SERVER_URL, url);
            startActivityForResult(intent, REQ_SIGNUP);
        });

        mAccountManager = AccountManager.get(getBaseContext());

        if (getIntent().getBooleanExtra(ARG_SHIB, false)) {
            final Intent intent = new Intent(this, SingleSignOnAuthorizeActivity.class);
            final android.accounts.Account account = new android.accounts.Account(getIntent().getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_NAME), com.nihaocloud.sesamedisk.account.Account.ACCOUNT_TYPE);
            final String url = mAccountManager.getUserData(account, Authenticator.KEY_SERVER_URI);
            intent.putExtra(SingleSignOnAuthorizeActivity.SINGLE_SIGN_ON_SERVER_URL, url);
            intent.putExtras(getIntent().getExtras());
            startActivityForResult(intent, REQ_SIGNUP);
        } else if (getIntent().getBooleanExtra(ARG_IS_EDITING, false)) {
            final Intent intent = new Intent(this, AccountDetailActivity.class);
            final android.accounts.Account account = new android.accounts.Account(getIntent().getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_NAME), com.nihaocloud.sesamedisk.account.Account.ACCOUNT_TYPE);
            final String url = mAccountManager.getUserData(account, Authenticator.KEY_SERVER_URI);
            intent.putExtra(AccountDetailActivity.BASIC_SIGN_ON_SERVER_URL, url);
            intent.putExtras(getIntent().getExtras());
            startActivityForResult(intent, REQ_SIGNUP);
        }

        Toolbar toolbar = getActionBarToolbar();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.choose_server);
        toolbar.setNavigationOnClickListener(view ->
                navigateUpOrBack(SeafileAuthenticatorActivity.this, null));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(DEBUG_TAG, "onActivityResult");
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else {
            finish();
        }
    }

    private void finishLogin(Intent intent) {
        Log.d(DEBUG_TAG, "finishLogin");
        String newAccountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        final Account newAccount = new Account(newAccountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        String serveruri = intent.getStringExtra(ARG_SERVER_URI);
        String email = intent.getStringExtra(ARG_EMAIL);
        String name = intent.getStringExtra(ARG_NAME);
        String sessionKey = intent.getStringExtra(ARG_AUTH_SESSION_KEY);
        boolean shib = intent.getBooleanExtra(ARG_SHIB, false);
        int cameraIsSyncable = 0;
        boolean cameraSyncAutomatically = true;

        if (intent.getBooleanExtra(ARG_IS_EDITING, false)) {
            String oldAccountName = intent.getStringExtra(ARG_EDIT_OLD_ACCOUNT_NAME);
            final Account oldAccount = new Account(oldAccountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
            // serverUri and mail stay the same. so just update the token and exit
            if (oldAccount.equals(newAccount)) {
                mAccountManager.setAuthToken(newAccount, Authenticator.AUTHTOKEN_TYPE, authtoken);
                mAccountManager.setUserData(newAccount, Authenticator.SESSION_KEY, sessionKey);
                mAccountManager.setUserData(newAccount, Authenticator.KEY_NAME, name);
                Bundle result = new Bundle();
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, newAccountName);
                setAccountAuthenticatorResult(result);
                setResult(RESULT_OK, intent);
                finish();
                return;
            }

            Log.d(DEBUG_TAG, "removing old account " + oldAccountName);
            cameraIsSyncable = ContentResolver.getIsSyncable(oldAccount, CameraUploadManager.AUTHORITY);
            cameraSyncAutomatically = ContentResolver.getSyncAutomatically(oldAccount, CameraUploadManager.AUTHORITY);
            mAccountManager.removeAccount(oldAccount, null, null);
        }

        Log.d(DEBUG_TAG, "adding new account " + newAccountName);
        mAccountManager.addAccountExplicitly(newAccount, null, null);
        mAccountManager.setAuthToken(newAccount, Authenticator.AUTHTOKEN_TYPE, authtoken);
        mAccountManager.setUserData(newAccount, Authenticator.KEY_SERVER_URI, serveruri);
        mAccountManager.setUserData(newAccount, Authenticator.KEY_EMAIL, email);
        mAccountManager.setUserData(newAccount, Authenticator.KEY_NAME, name);
        mAccountManager.setUserData(newAccount, Authenticator.SESSION_KEY, sessionKey);
        if (shib) {
            mAccountManager.setUserData(newAccount, Authenticator.KEY_SHIB, "shib");
        }
        // set sync settings
        ContentResolver.setIsSyncable(newAccount, CameraUploadManager.AUTHORITY, cameraIsSyncable);
        ContentResolver.setSyncAutomatically(newAccount, CameraUploadManager.AUTHORITY, cameraSyncAutomatically);
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        result.putString(AccountManager.KEY_ACCOUNT_NAME, newAccountName);
        setAccountAuthenticatorResult(result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
