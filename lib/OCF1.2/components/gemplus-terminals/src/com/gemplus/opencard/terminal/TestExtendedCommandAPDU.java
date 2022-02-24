/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */

package com.gemplus.opencard.terminal;

// Java imports
import java.lang.*;
import java.io.*;
import java.util.*;

// OCF imports
import opencard.core.service.*;
import opencard.core.terminal.*;
import opencard.opt.util.*;
import opencard.core.util.*;
import opencard.opt.terminal.UserInteraction;
import com.gemplus.opencard.terminal.*;

// Test Class

public class TestExtendedCommandAPDU
{
    static PassThruCardService myCardService = null;
    
    // Basic tests (with Gemplus badge)
    public static void basicTests() throws Exception
    {
        // Test Case 2
        byte[] inputData=new byte[]{(byte)0x01,(byte)0x00};
        ExtendedCommandAPDU c2=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0xA4,
                                                       (byte)0x01,
                                                       (byte)0x00,
                                                       inputData); // Select File 01 00
        System.out.println("***** Test Case 2 *****");
        System.out.println("Command: "+c2.toString());
        ResponseAPDU r2=myCardService.sendCommandAPDU(c2);
        System.out.println("Response: "+r2.toString());
        System.out.println("");

        // Test Case 3
        ExtendedCommandAPDU c3=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0xC0,
                                                       (byte)0x00,
                                                       (byte)0x00,
                                                       (byte)0x1A); // Get Response
        System.out.println("***** Test Case 3 *****");
        System.out.println("Command: "+c3.toString());
        ResponseAPDU r3=myCardService.sendCommandAPDU(c3);
        System.out.println("Response: "+r3.toString());
        System.out.println("");

        // Test Case 1
        ExtendedCommandAPDU c1=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0xA4,
                                                       (byte)0x03,
                                                       (byte)0x00); // Select Parent DF            
        System.out.println("***** Test Case 1 *****");
        System.out.println("Command: "+c1.toString());
        ResponseAPDU r1=myCardService.sendCommandAPDU(c1);
        System.out.println("Response: "+r1.toString());
        System.out.println("");

        // Test Case 4
        ExtendedCommandAPDU c4=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0xA4,
                                                       (byte)0x01,
                                                       (byte)0x00,
                                                       inputData,
                                                       (byte)0x1A); // Select File 01 00 + Get Resposne
        System.out.println("***** Test Case 4 *****");
        System.out.println("Command: "+c4.toString());
        ResponseAPDU r4=myCardService.sendCommandAPDU(c4);
        System.out.println("Response: "+r4.toString());
        System.out.println("");
    }

    // APDU Streams Tests (with a special GemXpresso card applet)
    public static void selectTestApplet() throws Exception
    {
        byte[] selectData=new byte[]{(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x00,
                                     (byte)0x18,(byte)0xFF,(byte)0x00,(byte)0x00,
                                     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                     (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
        ExtendedCommandAPDU selectCommand=new ExtendedCommandAPDU((byte)0xA8,
                                                                  (byte)0xA4,
                                                                  (byte)0x04,
                                                                  (byte)0x00,
                                                                   selectData); // Select Applet AID=00 01

        System.out.println("***** Select Applet *****");
        System.out.println("Command: "+selectCommand.toString());
        ResponseAPDU selectResponse=myCardService.sendCommandAPDU(selectCommand);
        System.out.println("Response: "+selectResponse.toString());
        System.out.println("");
    }

    public static void apduStreamTest1() throws Exception
    {
        // Test #1 (primitive types)
/*        APDUDataOutputStream in1=new APDUDataOutputStream();
        //in1.setBigEndian(false);
        in1.writeBoolean(true); in1.writeBoolean(false);
        in1.writeByte((byte)0xAA); in1.writeByte((byte)0x55);
        in1.writeShort((short)0xA55A); in1.writeShort((short)0xA55A);
        in1.writeInt(0x12345678); in1.writeInt(0xAABBCCDD);
        
        ExtendedCommandAPDU c1=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0x01,
                                                       (byte)0x02,
                                                       (byte)0x03,
                                                       in1.toByteArray(),
                                                       (byte)in1.size()); // Return Input Data

        System.out.println("***** Test #1 (primitive types) *****");
        System.out.println("Command: "+c1.toString());
        ResponseAPDU r1=myCardService.sendCommandAPDU(c1);
        System.out.println("Response: "+r1.toString());
        System.out.println("");
        
        APDUDataInputStream out1=new APDUDataInputStream(r1);
        //out1.setBigEndian(false);
        boolean b1=out1.readBoolean();
        boolean b2=out1.readBoolean();
        System.out.println("Booleans: true ---> "+b1+" / false ---> "+b2);
        byte by1=out1.readByte();
        byte by2=out1.readByte();
        System.out.println("Bytes: AA ---> "+HexString.hexify((int)by1)+" / 55 ---> "+HexString.hexify((int)by2));
        short s1=out1.readShort();
        short s2=out1.readShort();
        System.out.println("Shorts: A55A ---> "+HexString.hexifyShort((int)s1)+" / A55A ---> "+HexString.hexifyShort((int)s2));
        int i1=out1.readInt();
        int i2=out1.readInt();
        System.out.println("Ints: 12345678 ---> "+HexString.hexifyShort(i1>>16)+HexString.hexifyShort(i1)
                            + " / AABBCCDD ---> "+HexString.hexifyShort(i2>>16)+HexString.hexifyShort(i2));
  */  }

    public static void apduStreamTest2() throws Exception
    {
        // Test #2 (arrays)
    /*    APDUDataOutputStream in2=new APDUDataOutputStream();
        //in2.setBigEndian(false);
        boolean[] ba=new boolean[]{true,false,true,false,true};
        in2.writeBooleanArray(ba); in2.writeBooleanArray(ba,1,3);
        byte[] bya=new byte[]{(byte)12,(byte)34,(byte)56,(byte)78,(byte)90};
        in2.writeByteArray(bya); in2.writeByteArray(bya,2,3);
        short[] sa=new short[]{(short)1122,(short)3344,(short)5566};
        in2.writeShortArray(sa); in2.writeShortArray(sa,1,1);
        int[] ia=new int[]{12345678,11223344};
        in2.writeIntArray(ia); in2.writeIntArray(ia,0,2);

        ExtendedCommandAPDU c2=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0x01,
                                                       (byte)0x02,
                                                       (byte)0x03,
                                                       in2.toByteArray(),
                                                       (byte)in2.size()); // Return Input Data

        System.out.println("\n\n***** Test #2 (arrays) *****");
        System.out.println("Command: "+c2.toString());
        ResponseAPDU r2=myCardService.sendCommandAPDU(c2);
        System.out.println("Response: "+r2.toString());
        System.out.println("");

        APDUDataInputStream out2=new APDUDataInputStream(r2);
        //out2.setBigEndian(false);
        System.out.println("Booleans:");
        boolean[] ba1=out2.readBooleanArray();
        System.out.print("   [Booleans array] ---> ");
        for (int i=0; i<ba1.length;i++) System.out.print(ba1[i]+" "); System.out.println("");
        boolean[] ba2=out2.readBooleanArray(2);
        System.out.print("   [true,false] ---> ");
        for (int i=0; i<ba2.length;i++) System.out.print(ba2[i]+" "); System.out.println("");
        boolean[] ba3=out2.readBooleanArray(6);
        System.out.print("   [true,false,true,false,true,false] ---> ");
        for (int i=0; i<ba3.length;i++) System.out.print(ba3[i]+" "); System.out.println("");
        
        System.out.println("Bytes:");
        byte[] bya1=out2.readByteArray();
        System.out.print("   [Bytes array] ---> ");
        for (int i=0; i<bya1.length;i++) System.out.print(bya1[i]+" "); System.out.println("");
        byte[] bya2=out2.readByteArray(4);
        System.out.print("   [12,34,56,78] ---> ");
        for (int i=0; i<bya2.length;i++) System.out.print(bya2[i]+" "); System.out.println("");
        byte[] bya3=out2.readByteArray(4);
        System.out.print("   [90,56,78,90] ---> ");
        for (int i=0; i<bya3.length;i++) System.out.print(bya3[i]+" "); System.out.println("");
        
        System.out.println("Shorts:");
        short[] sa1=out2.readShortArray();
        System.out.print("   [Shorts array] ---> ");
        for (int i=0; i<sa1.length;i++) System.out.print(sa1[i]+" "); System.out.println("");
        short[] sa2=out2.readShortArray(1);
        System.out.print("   [1122] ---> ");
        for (int i=0; i<sa2.length;i++) System.out.print(sa2[i]+" "); System.out.println("");
        short[] sa3=out2.readShortArray(3);
        System.out.print("   [3344,5566,3344] ---> ");
        for (int i=0; i<sa3.length;i++) System.out.print(sa3[i]+" "); System.out.println("");
        
        System.out.println("Ints:");
        int[] ia1=out2.readIntArray();
        System.out.print("   [Ints array] ---> ");
        for (int i=0; i<ia1.length;i++) System.out.print(ia1[i]+" "); System.out.println("");
        int[] ia2=out2.readIntArray(4);
        System.out.print("   [12345678,11223344,12345678,11223344] ---> ");
        for (int i=0; i<ia2.length;i++) System.out.print(ia2[i]+" "); System.out.println("");
*/    }

    public static void apduStreamTest3() throws Exception
    {
        // Test #3 (strings)
  /*      APDUDataOutputStream in3=new APDUDataOutputStream();
        in3.writeString("Coucou ");
        in3.writeString("me voila!\n");
        String cMoi=new String("C'est moi ");
        in3.writeString(cMoi);
        in3.writeString(cMoi,6,3);
        
        ExtendedCommandAPDU c3=new ExtendedCommandAPDU((byte)0x00,
                                                       (byte)0x01,
                                                       (byte)0x02,
                                                       (byte)0x03,
                                                       in3.toByteArray(),
                                                       (byte)in3.size()); // Return Input Data

        System.out.println("\n\n***** Test #3 (strings) *****");
        System.out.println("Command: "+c3.toString());
        ResponseAPDU r3=myCardService.sendCommandAPDU(c3);
        System.out.println("Response: "+r3.toString());
        System.out.println("");
        
        APDUDataInputStream out3=new APDUDataInputStream(r3);
        
        System.out.println(out3.readString());
    */}

    public static void main(String[] args)
    {
     byte[] beepCmd  = {(byte) 0x33, (byte) 0x80}; // Beep
     Slot slot;
     CardTerminal cardTerminal;

        try
        {
            // Init OCF
            SmartCard.start();
            SmartCard myCard=SmartCard.waitForCard(new CardRequest(CardRequest.ANYCARD));


            // print the ATR
            if (myCard != null) {
              CardID cardID = myCard.getCardID ();

              printCardID (cardID);
            }
            else
              System.out.println ("did not get a SmartCard object!");

            myCardService=(PassThruCardService)myCard.getCardService(PassThruCardService.class, true);

            // Basic Tests: Terminal commands
            slot = (myCard.getCardID()).getSlot();
            cardTerminal = slot.getCardTerminal();
            if ( cardTerminal.getType().equals("GCR700") ||
                 cardTerminal.getType().equals("GCR500")
                ) {
              ((GemplusSerialCardTerminal)cardTerminal).sendTerminalCommand(beepCmd);
              ((UserInteraction)cardTerminal).clearDisplay();
              ((UserInteraction)cardTerminal).display("Hello World. Are you ready?");
            }
            // Basic Tests: Card APDU commands
            basicTests();

            // APDU Streams Tests
            //selectTestApplet();
            //apduStreamTest1();
            //apduStreamTest2();
            //apduStreamTest3();
            SmartCard.shutdown();

        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
  /**
   * Prints out the information of the <TT>CardID</TT> object passed.
   */
  public static void printCardID (CardID cardID) {
    StringBuffer sb = new StringBuffer("Obtained the following CardID:\n\n");

    byte [] atr = cardID.getATR ();
    sb.append (HexString.hexify (atr) ).append ('\n');

    appendHistoricals (sb, cardID);

    System.out.println(sb);
  } // printCardID


  private static void appendHistoricals(StringBuffer sb, CardID cardID) {
    byte[] hist = cardID.getHistoricals();

    sb.append("Historicals: ");
    if (hist == null) {
      sb.append("none\n");
    }
    else {
      sb.append(HexString.hexify(hist)).append('\n');
      sb.append("as a string: ");
      for(int i=0; i<hist.length; i++)
        sb.append((hist[i]<32)? // signed byte extension!
                    ' ' : (char)hist[i]);
      sb.append('\n');
    }
  }


  private static void appendTS(StringBuffer sb, byte ts) {
    sb.append("TS = ").append(HexString.hexify(ts)).append("    ");
    sb.append((ts==(byte)0x3b) ? "direct" : "inverse").append(" convention\n");
  }


  private static void appendT0(StringBuffer sb, byte t0) {
    sb.append("T0 = ").append(HexString.hexify(t0)).append("    ");
    binify(sb, t0);
    sb.append('\n');
  }


  private static void appendClockrate(StringBuffer sb, byte cr) {
    // why is the output always "???" ?
    double[] mhz  = { -1.0, 5.0, 6.0, 8.0, 12.0, 16.0, 20.0, -1.0,
                      5.0, 7.5, 10.0, 15.0, 20.0, -1.0, -1.0, -1.0 };
    int[] factors = { -2, 372, 558, 744, 1116, 1488, 1860, -1,
                      512, 768, 1024, 1536, 2048, -1, -1, -1 };

    int fi = (cr >> 4) & 0xf;

    double speed =   mhz  [fi];
    int   factor = factors[fi];

    sb.append("Clock speed ");
    if (speed < 0)
      sb.append("???");
    else
      sb.append(speed);

    sb.append(" MHz, divided by ");
    if (factor < 0)
      sb.append("???");
    else
      sb.append(factor);
    sb.append('\n');
  }


  private static void appendBitAdjust(StringBuffer sb, byte b) {
    // why is the output always "???" ?
    double[] bra = { -1.0, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, -1.0,
                     12.0, 20.0, 1.0/2, 1.0/4, 1.0/8, 1.0/16, 1.0/32, 1.0/64 };
    int di = b & 0xf;

    sb.append("bit rate adjustment ");
    if (bra[di] < 0)
      sb.append("???");
    else
      sb.append(bra[di]);
    sb.append('\n');
  }


  private static void appendProgCurr(StringBuffer sb, byte b) {
    // why is the output always "???" ?
    int[] mpg = { 25, 50, 100, -1 };
    int ii = (b >> 5) & 3;

    sb.append("max prog current ");
    if (b < 0)
      sb.append("???");
    else
      sb.append(mpg[ii]).append(" mA");
    sb.append('\n');
  }


  private static void binify(StringBuffer sb, byte b) {
    for(int i=0; i<8; i++) {
      sb.append((b<0) ? '1' : '0');
      b <<= 1;
    }
  }



}
