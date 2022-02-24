package com.github.MrrRaph.corosechat.client.card.codes;

public enum ResponseCode {
    OK("90 00", "Ok"),
    SW_NO_KEYS("63 00", "No Keys Generated"),
    SW_DEBUG_POINT("63 FF", "Debug Point Reached"),
    SW_EXCEEDED_MAX_SIZE("6F 00", "Exceeded Max Size"),
    UNKNOWN("", "Unknown Response");

    private final String responseCode;
    private final String response;

    ResponseCode(final String responseCode, final String response) {
        this.responseCode = responseCode;
        this.response = response;
    }

    public static ResponseCode fromString(final String responseCode) {
        for (ResponseCode rc : ResponseCode.values())
            if (responseCode.equals(rc.responseCode))
                return rc;
        return UNKNOWN;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    @Override
    public String toString() {
        return this.response;
    }
}
