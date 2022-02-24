package com.github.MrrRaph.corosechat.client.utils.padding;

import java.util.Arrays;
import java.io.*;

public final class PKCS5 {
    public static byte[] addPKCS5Padding(byte[] clearContent, short blockSize) {
        byte padding = (byte) (blockSize - clearContent.length % blockSize);

        byte[] padText = new byte[(short) (padding & 0xFF)];
        Arrays.fill(padText, padding);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(clearContent);
            byteArrayOutputStream.write(padText);
        } catch (Exception ignored) {}

        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] trimPKCS5Padding(byte[] decrypted) {
        byte padding = decrypted[decrypted.length - 1];
        return Arrays.copyOfRange(
                decrypted,
                0,
                decrypted.length - (short) (padding & 0xFF)
        );
    }
}