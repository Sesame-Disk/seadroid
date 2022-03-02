package com.nihaoconsult.app.play;

/**
 * get video link state listener
 */
public interface VideoLinkStateListener {
    void onSuccess(String fileLink);

    void onError(String errMsg);
}
