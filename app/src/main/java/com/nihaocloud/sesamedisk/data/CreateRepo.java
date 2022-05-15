package com.nihaocloud.sesamedisk.data;

import com.nihaocloud.sesamedisk.SettingsManager;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateRepo {
    private long repoSize;
    private String repoId;
    private boolean encrypted;
    private long mtime;
    private String email;
    private String repoName;

    public CreateRepo(long repoSize, String repoId, boolean encrypted,
                      long mtime, String email, String repoName) {
        this.repoSize = repoSize;
        this.repoId = repoId;
        this.encrypted = encrypted;
        this.mtime = mtime;
        this.email = email;
        this.repoName = repoName;
    }

    public static CreateRepo fromJson(JSONObject obj) throws JSONException {
        String repoName = obj.getString("repo_name");
        String repoId = obj.getString("repo_id");
        long repoSize = obj.optLong("repo_size", 0);
        boolean encrypted = obj.optString("encrypted", "false").contains("true");
        long mtime = obj.optLong("mtime", 0);
        String email = obj.optString("email", "");
        return new CreateRepo(repoSize, repoId, encrypted, mtime, email, repoName);
    }

    public boolean canLocalDecrypt() {
        return encrypted && SettingsManager.instance().isEncryptEnabled();
    }

    public long getRepoSize() {
        return repoSize;
    }

    public void setRepoSize(Integer repoSize) {
        this.repoSize = repoSize;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(Integer mtime) {
        this.mtime = mtime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }


}
