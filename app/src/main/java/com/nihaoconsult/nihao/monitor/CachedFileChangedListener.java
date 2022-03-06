package com.nihaoconsult.nihao.monitor;

import com.nihaoconsult.nihao.account.Account;
import com.nihaoconsult.nihao.data.SeafCachedFile;

import java.io.File;

interface CachedFileChangedListener {
    void onCachedBlocksChanged(Account account, SeafCachedFile cf, File file);

    void onCachedFileChanged(Account account, SeafCachedFile cf, File file);
}

