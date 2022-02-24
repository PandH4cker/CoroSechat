package com.github.MrrRaph.corosechat.client.utils.bytes;

public final class ByteUtil {
    public static String printBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (byte b : bytes)
            sb.append(String.format("0x%02X ", b));
        sb.append("]");
        return sb.toString();
    }
}
