package com.github.MrrRaph.corosechat.client.communications.card.codes;

public enum CommandCode {
    GENERATE_RSA_KEYPAIR((byte) 0xF6, "Generate RSA Key Pair"),
    RSA_ENCRYPT((byte) 0xA0, "RSA Encrypt"),
    RSA_DECRYPT((byte) 0XA2, "RSA Decrypt"),
    GET_PUBLIC_RSA_KEY((byte) 0xFE, "Get Public RSA Key"),
    PUT_PUBLIC_RSA_KEY((byte) 0xF4, "Put Public RSA Key"),
    DES_ENCRYPT((byte) 0xB0, "DES Encrypt"),
    DES_DECRYPT((byte) 0xB2, "DES Decrypt");

    private final byte code;
    private final String codeName;

    CommandCode(final byte code, final String codeName) {
        this.code = code;
        this.codeName = codeName;
    }

    public byte getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.codeName;
    }
}
