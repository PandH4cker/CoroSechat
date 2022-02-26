package com.github.MrrRaph.corosechat.client.utils.card;

import com.github.MrrRaph.corosechat.client.communications.card.codes.ResponseCode;

import java.util.Arrays;

public final class ResponseAPDUtils {
    /*public static ResponseCode byteArrayToResponseCode(byte[] arr) {
        return ResponseCode.fromSW(
                HexString.hexify(Arrays.copyOfRange(arr, arr.length - 2, arr.length))
        );
    }*/

    public static byte[] getDataFromResponseCodeByteArray(byte[] resp) {
        return Arrays.copyOfRange(resp, 0, resp.length - 2);
    }

    public static void printError(ResponseCode responseCode) {
        System.out.println("[!] ERROR: " + responseCode);
    }
}
