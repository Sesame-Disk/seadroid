package com.nihaocloud.nihao.avatar;

import com.google.common.collect.Lists;
import com.nihaocloud.nihao.NihaoApplication;
import com.nihaocloud.nihao.account.Account;
import com.nihaocloud.nihao.account.AccountManager;
import com.nihaocloud.nihao.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * load, cache, update avatars
 */
public class AvatarManager {
    private static final String DEBUG_TAG = "AvatarManager";

    private final AvatarDBHelper dbHelper = AvatarDBHelper.getAvatarDbHelper();
    private List<Avatar> avatars;
    private AccountManager accountMgr;

    public AvatarManager() {
        this.avatars = Lists.newArrayList();
        this.accountMgr = new AccountManager(NihaoApplication.getAppContext());
    }

    /**
     * get accounts who don`t have avatars yet
     *
     * @return ArrayList<Account>
     */
    public ArrayList<Account> getAccountsWithoutAvatars() {
        List<Account> accounts = accountMgr.getAccountList();

        if (accounts == null) return null;

        ArrayList<Account> accountsWithoutAvatar = Lists.newArrayList();

        for (Account account : accounts) {
            if (!hasAvatar(account)) {
                accountsWithoutAvatar.add(account);
            }
        }

        return accountsWithoutAvatar;
    }

    private boolean hasAvatar(Account account) {
        return dbHelper.hasAvatar(account);
    }

    public boolean isNeedToLoadNewAvatars() {
        ArrayList<Account> accounts = getAccountsWithoutAvatars();
        if (accounts == null || accounts.size() == 0) return false;
        else
            return true;
    }

    public List<Avatar> getAvatarList() {
        return dbHelper.getAvatarList();
    }

    public void saveAvatarList(List<Avatar> avatars) {
        dbHelper.saveAvatars(avatars);
    }

    public Avatar parseAvatar(String json) {
        if (json == null) return null;

        JSONObject obj = Utils.parseJsonObject(json);
        if (obj == null)
            return null;
        Avatar avatar = Avatar.fromJson(obj);
        if (avatar == null)
            return null;

        return avatar;
    }
}
