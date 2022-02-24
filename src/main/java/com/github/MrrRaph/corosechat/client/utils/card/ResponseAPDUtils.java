package com.github.MrrRaph.corosechat.client.utils.card;

import com.github.MrrRaph.corosechat.client.card.codes.ResponseCode;
import opencard.core.util.HexString;

import java.util.Arrays;

public final class ResponseAPDUtils {
    public static ResponseCode byteArrayToResponseCode(byte[] arr) {
        return ResponseCode.fromString(
                HexString.hexify(Arrays.copyOfRange(arr, arr.length - 2, arr.length))
        );
    }

    public static byte[] getDataFromResponseCodeByteArray(byte[] resp) {
        return Arrays.copyOfRange(resp, 0, resp.length - 2);
    }

    public static void printError(ResponseCode responseCode) {
        System.out.println("[!] ERROR: " + responseCode);
    }
}
