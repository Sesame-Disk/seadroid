package com.nihaoconsult.app.monitor;

import com.nihaoconsult.app.account.Account;
import com.nihaoconsult.app.data.SeafCachedFile;

import java.io.File;

interface CachedFileChangedListener {
    void onCachedBlocksChanged(Account account, SeafCachedFile cf, File file);

    void onCachedFileChanged(Account account, SeafCachedFile cf, File file);
}

