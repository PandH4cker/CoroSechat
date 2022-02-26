package com.github.MrrRaph.corosechat.client.utils.card.rsa;

import com.github.MrrRaph.corosechat.client.communications.card.codes.ResponseCode;
import com.github.MrrRaph.corosechat.client.exceptions.NoKeysGeneratedException;
import com.github.MrrRaph.corosechat.client.utils.card.ResponseAPDUtils;
import com.github.MrrRaph.corosechat.client.utils.multiples.Pair;
import opencard.core.terminal.ResponseAPDU;
import opencard.opt.util.PassThruCardService;

import static com.github.MrrRaph.corosechat.client.communications.card.codes.CommandCode.GENERATE_RSA_KEYPAIR;
import static com.github.MrrRaph.corosechat.client.communications.card.codes.CommandCode.GET_PUBLIC_RSA_KEY;
import static com.github.MrrRaph.corosechat.client.utils.bytes.ByteUtil.printBytes;
import static com.github.MrrRaph.corosechat.client.utils.card.CardUtils.*;

public final class RSAUtils {
    public static byte[] getPublicRSAExponent(PassThruCardService cardService) throws NoKeysGeneratedException {
        ResponseAPDU resp = sendCommand(GET_PUBLIC_RSA_KEY, (byte) 0x00, GET_EXPONENT, (byte) 0x00, cardService);
        ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
        if (responseCode == ResponseCode.OK) {
            byte[] data = resp.data();
            byte[] publicExponent = new byte[data[0] & 0xFF];

            System.arraycopy(data, 1, publicExponent, 0, data[0] & 0xFF);
            System.out.println("[+] Retrieved Public Exponent: " + printBytes(publicExponent));

            return publicExponent;
        } else {
            ResponseAPDUtils.printError(responseCode);
            throw new NoKeysGeneratedException("Exponent not found in the card.");
        }
    }

    public static byte[] getPublicRSAModulus(PassThruCardService cardService) throws NoKeysGeneratedException {
        ResponseAPDU resp = sendCommand(GET_PUBLIC_RSA_KEY, (byte) 0x00, GET_MODULUS, (byte) 0x00, cardService);
        ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
        if (responseCode == ResponseCode.OK) {
            byte[] data = resp.data();
            byte[] modulus = new byte[data[0] & 0xFF];

            System.arraycopy(data, 1, modulus, 0, data[0] & 0xFF);
            System.out.println("[+] Retrieved Modulus: " + printBytes(modulus));
            return modulus;
        } else {
            ResponseAPDUtils.printError(responseCode);
            throw new NoKeysGeneratedException("Modulus not found in the card.");
        }
    }

    public static Pair<byte[], byte[]> getPublicRSAKey(PassThruCardService cardService) {
        try {
            return new Pair<>(getPublicRSAModulus(cardService), getPublicRSAExponent(cardService));
        } catch (NoKeysGeneratedException e) {
            return generateRSAKeyPair(cardService);
        }
    }

    public static Pair<byte[], byte[]> generateRSAKeyPair(PassThruCardService cardService) {
        System.out.println("Generating RSA Key Pair..");
        ResponseAPDU resp = sendCommand(GENERATE_RSA_KEYPAIR, (byte) 0x00, (byte) 0x00, (byte) 0x00, cardService);
        ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
        if (responseCode == ResponseCode.OK) {
            System.out.println("[+] Successfully Generated RSA Key Pair !");
            return getPublicRSAKey(cardService);
        } else {
            ResponseAPDUtils.printError(responseCode);
            System.exit(1);
        }
        // Unreachable
        return null;
    }
}
