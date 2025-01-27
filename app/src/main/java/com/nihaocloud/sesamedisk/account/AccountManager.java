package com.nihaocloud.sesamedisk.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.common.collect.Lists;
import com.nihaocloud.sesamedisk.BuildConfig;
import com.nihaocloud.sesamedisk.cameraupload.CameraUploadManager;
import com.nihaocloud.sesamedisk.data.ServerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Account Manager.<br>
 * note the differences between {@link Account} and {@link AccountInfo}<br>
 */

@SuppressLint("MissingPermission")
public class AccountManager {
    @SuppressWarnings("unused")
    private final String DEBUG_TAG = "AccountManager";
    public static final String SHARED_PREF_NAME = BuildConfig.APPLICATION_ID + "latest_account";
    public static final String SHARED_PREF_ACCOUNT_NAME = BuildConfig.APPLICATION_ID + ".account_name";

    /**
     * used to manage multi Accounts when user switch between different Accounts
     */
    private final SharedPreferences actMangeSharedPref;
    private final SharedPreferences.Editor editor;
    private final android.accounts.AccountManager accountManager;
    private final Context ctx;

    public AccountManager(Context context) {
        this.ctx = context.getApplicationContext();
        accountManager = android.accounts.AccountManager.get(context);
        // used to manage multi Accounts when user switch between different Accounts
        actMangeSharedPref = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = actMangeSharedPref.edit();
        // migrate old accounts
        AccountDBHelper.migrateAccounts(context);
    }

    public List<Account> getAccountList() {
        List<Account> list = new ArrayList<>();
        android.accounts.Account[] availableAccounts = accountManager.getAccountsByType(Account.ACCOUNT_TYPE);
        for (android.accounts.Account availableAccount : availableAccounts) {
            Account a = getSeafileAccount(availableAccount);
            list.add(a);
        }
        return list;
    }

    public List<Account> getSignedInAccountList() {
        List<Account> list = new ArrayList<>();
        android.accounts.Account[] availableAccounts = accountManager.getAccountsByType(Account.ACCOUNT_TYPE);
        for (android.accounts.Account availableAccount : availableAccounts) {
            Account a = getSeafileAccount(availableAccount);
            if (a.hasValidToken())
                list.add(a);
        }
        return list;
    }

    public Account getCurrentAccount() {
        String name = actMangeSharedPref.getString(SHARED_PREF_ACCOUNT_NAME, null);

        if (name != null) {
            List<Account> list = getAccountList();
            for (Account a : list) {
                if (a.hasValidToken() && a.getSignature().equals(name)) {
                    return a;
                }
            }
        }

        return null;
    }

    public Account getSeafileAccount(android.accounts.Account androidAccount) {
        String server = accountManager.getUserData(androidAccount, Authenticator.KEY_SERVER_URI);
        String email = accountManager.getUserData(androidAccount, Authenticator.KEY_EMAIL);
        String name = accountManager.getUserData(androidAccount, Authenticator.KEY_NAME);
        boolean is_shib = accountManager.getUserData(androidAccount, Authenticator.KEY_SHIB) != null;
        String token = accountManager.peekAuthToken(androidAccount, Authenticator.AUTHTOKEN_TYPE);
        String session_key = accountManager.getUserData(androidAccount, Authenticator.SESSION_KEY);
        return new Account(name, server, email, token, is_shib, session_key);
    }

    public void setServerInfo(Account account, ServerInfo serverInfo) {
        accountManager.setUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_URI, serverInfo.getUrl());
        accountManager.setUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_VERSION, serverInfo.getVersion());
        accountManager.setUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_FEATURES, serverInfo.getFeatures());
    }

    /**
     * Return cached ServerInfo
     *
     * @param account Account
     * @return ServerInfo. Will never be null.
     */
    public ServerInfo getServerInfo(Account account) {
        String server = accountManager.getUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_URI);
        String version = accountManager.getUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_VERSION);
        String features = accountManager.getUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_FEATURES);
        return new ServerInfo(server, version, features);
    }

    /**
     * save current Account info to SharedPreference<br>
     * <strong>current</strong> means the Account is now in using at the foreground if has multiple accounts
     *
     * @param accountName name
     */
    public void saveCurrentAccount(String accountName) {

        editor.putString(SHARED_PREF_ACCOUNT_NAME, accountName);
        editor.commit();
    }

    /**
     * when user sign out, delete authorized information of the current Account instance.<br>
     * If Camera Upload Service is running under the Account, stop the service.
     */
    public void signOutAccount(Account account) {
        if (account == null || TextUtils.isEmpty(account.getToken())) {
            return;
        }

        CameraUploadManager cameraManager = new CameraUploadManager(ctx);

        accountManager.invalidateAuthToken(Account.ACCOUNT_TYPE, account.getToken());

        // disable camera upload if on this account
        Account camAccount = cameraManager.getCameraAccount();
        if (camAccount != null && camAccount.equals(account)) {
            cameraManager.disableCameraUpload();
        }
    }

    /**
     * get all email texts from database in order to auto complete email address
     *
     * @return ArrayList<String>
     */
    public ArrayList<String> getAccountAutoCompleteTexts() {
        ArrayList<String> autoCompleteTexts = Lists.newArrayList();
        List<Account> accounts = getAccountList();
        if (accounts == null) return null;
        for (Account act : accounts) {
            if (!autoCompleteTexts.contains(act.getEmail()))
                autoCompleteTexts.add(act.getEmail());
        }
        return autoCompleteTexts;
    }
}