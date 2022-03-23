package com.nihaocloud.nihao.account.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nihaocloud.nihao.R;
import com.nihaocloud.nihao.SeafConnection;
import com.nihaocloud.nihao.SeafException;
import com.nihaocloud.nihao.account.Account;
import com.nihaocloud.nihao.account.AccountInfo;
import com.nihaocloud.nihao.account.Authenticator;
import com.nihaocloud.nihao.data.DataManager;
import com.nihaocloud.nihao.ssl.CertsManager;
import com.nihaocloud.nihao.ui.EmailAutoCompleteTextView;
import com.nihaocloud.nihao.ui.activity.AccountsActivity;
import com.nihaocloud.nihao.ui.activity.BaseActivity;
import com.nihaocloud.nihao.ui.dialog.SslConfirmDialog;
import com.nihaocloud.nihao.util.ConcurrentAsyncTask;
import com.nihaocloud.nihao.util.Utils;

import org.json.JSONException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Objects;

public class AccountDetailActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String DEBUG_TAG = "AccountDetailActivity";
    public static final String TWO_FACTOR_AUTH = "two_factor_auth";
    public static final String BASIC_SIGN_ON_SERVER_URL = "AccountDetailActivity.BASIC_SIGN_ON_SERVER_URL";
    private TextView statusView;
    private Button loginButton;
    private ProgressDialog progressDialog;
    private EmailAutoCompleteTextView emailText;
    private EditText passwdText;
    private ImageView clearEmail, clearPasswd, ivEyeClick;
    private RelativeLayout rlEye;
    private TextInputLayout authTokenLayout;
    private EditText authTokenText;

    private boolean isPasswddVisible;
    private CheckBox cbRemDevice;
    private String mSessionKey;
    String serverURL;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail);
        final android.accounts.AccountManager mAccountManager = android.accounts.AccountManager.get(getBaseContext());
        statusView = findViewById(R.id.status_view);
        loginButton = findViewById(R.id.login_button);
        emailText = findViewById(R.id.email_address);
        passwdText = findViewById(R.id.password);

        clearEmail = findViewById(R.id.iv_delete_email);
        clearPasswd = findViewById(R.id.iv_delete_pwd);
        rlEye = findViewById(R.id.rl_layout_eye);
        ivEyeClick = findViewById(R.id.iv_eye_click);

        authTokenLayout = findViewById(R.id.auth_token_hint);
        authTokenText = findViewById(R.id.auth_token);
        authTokenLayout.setVisibility(View.GONE);

        cbRemDevice = findViewById(R.id.remember_device);
        cbRemDevice.setVisibility(View.GONE);
        Intent intent = getIntent();

        if (intent.getBooleanExtra("isEdited", false)) {
            String account_name = intent.getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_NAME);
            String account_type = intent.getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_TYPE);
            android.accounts.Account account = new android.accounts.Account(account_name, account_type);
            String email = mAccountManager.getUserData(account, Authenticator.KEY_EMAIL);
            mSessionKey = mAccountManager.getUserData(account, Authenticator.SESSION_KEY);
            emailText.setText(email);
            emailText.requestFocus();
        }
        serverURL = intent.getStringExtra(BASIC_SIGN_ON_SERVER_URL);
        if (serverURL == null) serverURL = getString(R.string.app_url);
        emailText.requestFocus();
        Toolbar toolbar = getActionBarToolbar();
        toolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);
        initListener();
    }

    private void initListener() {
        emailText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && emailText.getText().toString().trim().length() > 0) {
                clearEmail.setVisibility(View.VISIBLE);
            } else {
                clearEmail.setVisibility(View.INVISIBLE);
            }
        });

        passwdText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && passwdText.getText().toString().trim().length() > 0) {
                clearPasswd.setVisibility(View.VISIBLE);
            } else {
                clearPasswd.setVisibility(View.INVISIBLE);
            }
        });

        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailText.getText().toString().trim().length() > 0) {
                    clearEmail.setVisibility(View.VISIBLE);
                } else {
                    clearEmail.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        passwdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwdText.getText().toString().trim().length() > 0) {
                    clearPasswd.setVisibility(View.VISIBLE);
                } else {
                    clearPasswd.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearEmail.setOnClickListener(v -> emailText.setText(""));

        clearPasswd.setOnClickListener(v -> passwdText.setText(""));

        rlEye.setOnClickListener(v -> {
            if (!isPasswddVisible) {
                ivEyeClick.setImageResource(R.drawable.icon_eye_open);
                passwdText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ivEyeClick.setImageResource(R.drawable.icon_eye_close);
                passwdText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            isPasswddVisible = !isPasswddVisible;
            passwdText.postInvalidate();
            String input = passwdText.getText().toString().trim();
            if (!TextUtils.isEmpty(input)) {
                passwdText.setSelection(input.length());
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null)
            progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("email", emailText.getText().toString());
        savedInstanceState.putString("password", passwdText.getText().toString());
        savedInstanceState.putBoolean("rememberDevice", cbRemDevice.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emailText.setText((String) savedInstanceState.get("email"));
        passwdText.setText((String) savedInstanceState.get("password"));
        cbRemDevice.setChecked((boolean) savedInstanceState.get("rememberDevice"));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /* FYI {@link http://stackoverflow.com/questions/13293772/how-to-navigate-up-to-the-same-parent-state?rq=1} */
            Intent upIntent = new Intent(this, AccountsActivity.class);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                // NavUtils.navigateUpTo(this, upIntent);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(upIntent);
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the Login button
     */
    public void login(View view) {

        String email = emailText.getText().toString().trim();
        String passwd = passwdText.getText().toString();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            if (email.length() == 0) {
                emailText.setError(getResources().getString(R.string.err_email_empty));
                return;
            }

            if (passwd.length() == 0) {
                passwdText.setError(getResources().getString(R.string.err_passwd_empty));
                return;
            }

            String authToken = null;
            if (authTokenLayout.getVisibility() == View.VISIBLE) {
                authToken = authTokenText.getText().toString().trim();
                if (TextUtils.isEmpty(authToken)) {
                    authTokenText.setError(getResources().getString(R.string.two_factor_auth_token_empty));
                    return;
                }
            }

            boolean rememberDevice = false;
            if (cbRemDevice.getVisibility() == View.VISIBLE) {
                rememberDevice = cbRemDevice.isChecked();
            }
            try {
                serverURL = Utils.cleanServerURL(serverURL);
            } catch (MalformedURLException e) {
                statusView.setText(R.string.invalid_server_address);
                Log.d(DEBUG_TAG, "Invalid URL " + serverURL);
                return;
            }

            // force the keyboard to be hidden in all situations
            if (getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            loginButton.setEnabled(false);
            Account tmpAccount = new Account(null, serverURL, email, null, false, mSessionKey);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.settings_cuc_loading));
            progressDialog.setCancelable(false);
            ConcurrentAsyncTask.execute(new LoginTask(tmpAccount, passwd, authToken, rememberDevice));

        } else {
            statusView.setText(R.string.network_down);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoginTask extends AsyncTask<Void, Void, String> {
        Account loginAccount;
        SeafException err = null;
        String passwd;
        String authToken;
        boolean rememberDevice;

        public LoginTask(Account loginAccount, String passwd, String authToken, boolean rememberDevice) {
            this.loginAccount = loginAccount;
            this.passwd = passwd;
            this.authToken = authToken;
            this.rememberDevice = rememberDevice;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (params.length != 0)
                return "Error number of parameter";

            return doLogin();
        }

        private void resend() {
            ConcurrentAsyncTask.execute(new LoginTask(loginAccount, passwd, authToken, rememberDevice));
        }

        @Override
        protected void onPostExecute(final String result) {
            progressDialog.dismiss();
            if (err == SeafException.sslException) {
                authTokenLayout.setVisibility(View.GONE);
                cbRemDevice.setVisibility(View.GONE);
                SslConfirmDialog dialog = new SslConfirmDialog(loginAccount,
                        new SslConfirmDialog.Listener() {
                            @Override
                            public void onAccepted(boolean rememberChoice) {
                                CertsManager.instance().saveCertForAccount(loginAccount, rememberChoice);
                                resend();
                            }

                            @Override
                            public void onRejected() {
                                statusView.setText(result);
                                loginButton.setEnabled(true);
                            }
                        });
                dialog.show(getSupportFragmentManager(), SslConfirmDialog.FRAGMENT_TAG);
                return;
            } else if (err == SeafException.twoFactorAuthTokenMissing) {
                // show auth token input box
                authTokenLayout.setVisibility(View.VISIBLE);
                cbRemDevice.setVisibility(View.VISIBLE);
                cbRemDevice.setChecked(false);
                authTokenText.setError(getString(R.string.two_factor_auth_error));
            } else if (err == SeafException.twoFactorAuthTokenInvalid) {
                // show auth token input box
                authTokenLayout.setVisibility(View.VISIBLE);
                cbRemDevice.setVisibility(View.VISIBLE);
                cbRemDevice.setChecked(false);
                authTokenText.setError(getString(R.string.two_factor_auth_invalid));
            } else {
                authTokenLayout.setVisibility(View.GONE);
                cbRemDevice.setVisibility(View.GONE);
            }

            if (result != null && result.equals("Success")) {

                Intent retData = new Intent();
                retData.putExtras(getIntent());
                retData.putExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME, loginAccount.getSignature());
                retData.putExtra(android.accounts.AccountManager.KEY_AUTHTOKEN, loginAccount.getToken());
                retData.putExtra(android.accounts.AccountManager.KEY_ACCOUNT_TYPE, getIntent().getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_TYPE));
                retData.putExtra(SeafileAuthenticatorActivity.ARG_EMAIL, loginAccount.getEmail());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_NAME, loginAccount.getName());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_AUTH_SESSION_KEY, loginAccount.getSessionKey());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_SERVER_URI, loginAccount.getServer());
                retData.putExtra(TWO_FACTOR_AUTH, cbRemDevice.isChecked());
                setResult(RESULT_OK, retData);
                finish();
            } else {
                statusView.setText(result);
            }
            loginButton.setEnabled(true);
        }

        private String doLogin() {
            SeafConnection sc = new SeafConnection(loginAccount);

            try {
                // if successful, this will place the auth token into "loginAccount"
                if (!sc.doLogin(passwd, authToken, rememberDevice))
                    return getString(R.string.err_login_failed);

                // fetch email address from the server
                DataManager manager = new DataManager(loginAccount);
                AccountInfo accountInfo = manager.getAccountInfo();

                if (accountInfo == null)
                    return "Unknown error";

                // replace email address/username given by the user with the address known by the server.
//                loginAccount = new Account(loginAccount.server, accountInfo.getEmail(), loginAccount.token, false, loginAccount.sessionKey);
                loginAccount = new Account(accountInfo.getName(), loginAccount.server, accountInfo.getEmail(), loginAccount.token, false, loginAccount.sessionKey);

                return "Success";

            } catch (SeafException e) {
                err = e;
                if (e == SeafException.sslException) {
                    return getString(R.string.ssl_error);
                } else if (e == SeafException.twoFactorAuthTokenMissing) {
                    return getString(R.string.two_factor_auth_error);
                } else if (e == SeafException.twoFactorAuthTokenInvalid) {
                    return getString(R.string.two_factor_auth_invalid);
                }
                switch (e.getCode()) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        return getString(R.string.err_wrong_user_or_passwd);
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return getString(R.string.invalid_server_address);
                    default:
                        return e.getMessage();
                }
            } catch (JSONException e) {
                return e.getMessage();
            }
        }
    }
}
