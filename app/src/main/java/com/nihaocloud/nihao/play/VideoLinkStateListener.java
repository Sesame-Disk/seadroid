package com.nihaocloud.nihao.play;

/**
 * get video link state listener
 */
public interface VideoLinkStateListener {
    void onSuccess(String fileLink);

    void onError(String errMsg);
}
