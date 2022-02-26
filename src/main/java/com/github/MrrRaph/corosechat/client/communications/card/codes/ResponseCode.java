package com.github.MrrRaph.corosechat.client.communications.card.codes;

public enum ResponseCode {
    OK(0x9000, "Ok"),
    SW_NO_KEYS(0x6300, "No Keys Generated"),
    SW_DEBUG_POINT(0x63FF, "Debug Point Reached"),
    SW_EXCEEDED_MAX_SIZE(0x6F00, "Exceeded Max Size"),
    UNKNOWN(0, "Unknown Response");

    private final Integer responseCode;
    private final String response;

    ResponseCode(final Integer responseCode, final String response) {
        this.responseCode = responseCode;
        this.response = response;
    }

    public static ResponseCode fromSW(final int sw) {
        for (ResponseCode rc : ResponseCode.values())
            if (sw == rc.responseCode)
                return rc;
        return UNKNOWN;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }

    @Override
    public String toString() {
        return this.response;
    }
}
