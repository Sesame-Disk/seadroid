package com.nihaocloud.sesamedisk.account;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.common.base.Objects;
import com.nihaocloud.sesamedisk.BuildConfig;
import com.nihaocloud.sesamedisk.util.Utils;

public class Account implements Comparable<Account>, Parcelable {
    private static final String DEBUG_TAG = "Account";
    public final static String ACCOUNT_TYPE = BuildConfig.ACCOUNT_TYPE;
    public final String server;
    public final String name;
    public final String email;
    public final Boolean is_shib;
    public String token;
    public String sessionKey;
    public String avatarUrl;

    public Account(String name, String server, String email, String token, Boolean is_shib, String sessionKey, String avatarUrl) {
        this.server = server;
        this.name = name;
        this.email = email;
        this.token = token;
        this.sessionKey = sessionKey;
        this.is_shib = is_shib;
        this.avatarUrl = avatarUrl;
    }

    public int getAccountId() {
        return Objects.hashCode(server, email);
    }

    public String getServerHost() {
        String s = server.substring(server.indexOf("://") + 3);
        return s.substring(0, s.indexOf('/'));
    }

    public String getServerDomainName() {
        String dn = getServerHost();
        // strip port, like :8000 in 192.168.1.116:8000
        if (dn.contains(":"))
            dn = dn.substring(0, dn.indexOf(':'));
        return dn;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getServerNoProtocol() {
        String result = server.substring(server.indexOf("://") + 3);
        if (result.endsWith("/"))
            result = result.substring(0, result.length() - 1);
        return result;
    }

    public String getToken() {
        return token;
    }

    public boolean isHttps() {
        return server.startsWith("https");
    }

    public boolean isShib() {
        return is_shib;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(server, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || (obj.getClass() != this.getClass()))
            return false;

        Account a = (Account) obj;
        if (a.server == null || a.email == null || a.token == null)
            return false;

        return a.server.equals(this.server) && a.email.equals(this.email);
    }

    public String getSignature() {
        return String.format("%s (%s)", getServerNoProtocol(), email);
    }

    public String getDisplayName() {
        String server = Utils.stripSlashes(getServerHost());
        return Utils.assembleUserName(name, email, server);
    }

    public android.accounts.Account getAndroidAccount() {
        return new android.accounts.Account(getSignature(), ACCOUNT_TYPE);
    }

    public boolean hasValidToken() {
        return !TextUtils.isEmpty(token);
    }


    @Override
    public int compareTo(Account other) {
        return this.toString().compareTo(other.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.server);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeValue(this.is_shib);
        dest.writeString(this.token);
        dest.writeString(this.sessionKey);
        dest.writeString(this.avatarUrl);
    }

    protected Account(Parcel in) {
        this.server = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.is_shib = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.token = in.readString();
        this.sessionKey = in.readString();
        this.avatarUrl = in.readString();
    }

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };


}
