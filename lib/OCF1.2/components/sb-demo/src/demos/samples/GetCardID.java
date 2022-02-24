/*
 * Copyright © 1997 - 1999 IBM Corporation.
 * 
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 * 
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */

package demos.samples;


import opencard.core.service.CardRequest;
import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.HexString;
import opencard.core.util.OpenCardPropertyLoadingException;


/**
 * A sample that demonstrates the access to a smard card and 
 * obtains a <TT>CardID</TT> object which represents an ATR.
 *
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: GetCardID.java,v 1.9 1999/10/14 15:44:17 pbendel Exp $
 **/

public class GetCardID {

  private static final String NAME = "get a card ID";


  public static void main (String [] args) {
    System.out.println ("------------------------------------------------------------");
    System.out.println ("start use case: " + NAME);
    System.out.println ("");

    try {
      SmartCard.start ();

      CardRequest cr = new CardRequest (CardRequest.ANYCARD,null,null);
      SmartCard sm = SmartCard.waitForCard (cr);

      if (sm != null) {
        CardID cardID = sm.getCardID ();

        printCardID (cardID);
      }
      else
        System.out.println ("did not get a SmartCard object!");

      SmartCard.shutdown ();
    }
    catch (OpenCardPropertyLoadingException plfe) {
      System.out.println ("OpenCardPropertyLoadingException: ");
      System.out.println (plfe.getMessage () );
    }
    catch (ClassNotFoundException cnfe) {
      System.out.println ("ClassNotFoundException: ");
      System.out.println (cnfe.getMessage () );
    }
    catch (CardServiceException cse) {
      System.out.println ("CardServiceException: ");
      System.out.println (cse.getMessage () );
    }
    catch (CardTerminalException cte) {
      System.out.println ("CardTerminalException: ");
      System.out.println (cte.getMessage () );
    }

    System.out.println ("");
    System.out.println ("finished use case: " + NAME);
    System.out.println ("------------------------------------------------------------");
  } // main


  /**
   * Prints out the information of the <TT>CardID</TT> object passed.
   */
  public static void printCardID (CardID cardID) {
    StringBuffer sb = new StringBuffer("Obtained the following CardID:\n\n");

    byte [] atr = cardID.getATR ();
    sb.append (HexString.hexify (atr) ).append ('\n');

    appendHistoricals (sb, cardID);

    /* This should be completed someday...
     * the output seems to have problems with default values,
     * that's the main reason why it's disabled
    appendTS (sb, atr[0]);
    appendT0 (sb, atr[1]);

    int i = 2;

    if ((atr[0]&0x10) != 0) {
      appendClockrate(sb, atr[i]);
      appendBitAdjust(sb, atr[i]);
      i++;
    }

    if ((atr[0]&0x20) != 0) {
      appendProgCurr(sb, atr[i]);
    }
    * end of disabled output
    */

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
    sb.append("TS = ").append(HexString.hexify(t0)).append("    ");
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


} // GetCardID


// end of GetCardID.java -------------------------------------------------------
