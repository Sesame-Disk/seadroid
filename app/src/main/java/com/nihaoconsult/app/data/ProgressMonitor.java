package com.nihaoconsult.app.data;

public interface ProgressMonitor {
    void onProgressNotify(long total, boolean updateTotal);

    boolean isCancelled();
}
