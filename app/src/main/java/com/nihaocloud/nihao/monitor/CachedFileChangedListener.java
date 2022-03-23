package com.nihaocloud.nihao.monitor;

import com.nihaocloud.nihao.account.Account;
import com.nihaocloud.nihao.data.SeafCachedFile;

import java.io.File;

interface CachedFileChangedListener {
    void onCachedBlocksChanged(Account account, SeafCachedFile cf, File file);

    void onCachedFileChanged(Account account, SeafCachedFile cf, File file);
}

