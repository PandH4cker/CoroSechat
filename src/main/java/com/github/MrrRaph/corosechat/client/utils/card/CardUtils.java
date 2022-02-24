package com.github.MrrRaph.corosechat.client.utils.card;

import opencard.core.service.SmartCard;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.opt.util.PassThruCardService;

public final class CardUtils {
    public static final byte CLA            = (byte) 0x90;
    public static final byte GET_MODULUS    = (byte) 0x00;
    public static final byte GET_EXPONENT   = (byte) 0x01;

    private static boolean selectApplet(PassThruCardService servClient) {
        boolean cardOk = false;
        try {
            CommandAPDU cmd = new CommandAPDU(new byte[]{
                    (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0A,
                    (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62,
                    (byte)0x03, (byte)0x01, (byte)0x0C, (byte)0x06, (byte)0x01
            });
            ResponseAPDU resp = sendAPDU(cmd, servClient);
            if(APDUtils.apdu2string(resp).equals("90 00"))
                cardOk = true;
        } catch(Exception e) {
            System.out.println("Exception caught in selectApplet: " + e.getMessage());
            System.exit(-1);
        }
        return cardOk;
    }


    public static PassThruCardService initNewCard(SmartCard card) {
        if(card != null)
            System.out.println("Smartcard inserted\n");
        else {
            System.out.println("Did not get a smartcard");
            System.exit(-1);
        }

        System.out.println("ATR: " + HexString.hexify(card.getCardID().getATR()) + "\n");

        PassThruCardService servClient = null;
        try {
            servClient = (PassThruCardService) card.getCardService(PassThruCardService.class, true);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Applet selecting...");
        if(!selectApplet(servClient)) {
            System.out.println("Wrong card, no applet to select!\n");
            System.exit(1);
        } else
            System.out.println("Applet selected");

        return servClient;
    }

    public static ResponseAPDU sendAPDU(CommandAPDU cmd, PassThruCardService servClient) {
        return sendAPDU(cmd, true, servClient);
    }

    public static ResponseAPDU sendAPDU(CommandAPDU cmd, boolean display, PassThruCardService servClient) {
        ResponseAPDU result = null;
        try {
            result = servClient.sendCommandAPDU(cmd);
            if(display)
                APDUtils.displayAPDU(cmd, result);
        } catch(Exception e) {
            System.out.println("Exception caught in sendAPDU: " + e.getMessage());
            System.exit(-1);
        }
        return result;
    }
}
