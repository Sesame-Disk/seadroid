package com.nihaocloud.sesamedisk.monitor;

import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.database.table.SeafCachedFile;

import java.io.File;

interface CachedFileChangedListener {
    void onCachedBlocksChanged(Account account, SeafCachedFile cf, File file);

    void onCachedFileChanged(Account account, SeafCachedFile cf, File file);
}

