package com.nihaocloud.sesamedisk.data;

public class CameraSyncEvent {
    private String logInfo;

    public CameraSyncEvent(String logInfo) {
        this.logInfo = logInfo;
    }


    public String getLogInfo() {
        return logInfo;
    }

}
