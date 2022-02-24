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


import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;

import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.OpenCardPropertyLoadingException;


/**
 * A sample providing information about the registered terminals. 
 * Please <B>Note</B> that the set of <i>registered</i> terminals 
 * may differ from the set of terminals that is <i>physically attached</i> 
 * to the system.
 *
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: GetTerminalInfos.java,v 1.8 1999/10/14 15:44:17 pbendel Exp $
 **/

public class GetTerminalInfos {

  private static final String NAME = "get information about registered terminals";
  private static final char ENDL   = '\n';


  public static void main (String [] args) {
    System.out.println ("------------------------------------------------------------");
    System.out.println ("start use case: " + NAME);
    System.out.println ("");

    try {
      SmartCard.start ();
      CardTerminalRegistry ctr = CardTerminalRegistry.getRegistry ();

      Enumeration terminals = ctr.getCardTerminals ();

      int counter = 0;
      while (terminals.hasMoreElements () ) {
        CardTerminal terminal = (CardTerminal) terminals.nextElement ();
        printTerminalInfos (terminal);
        counter++;
      }

      System.out.println ("there " +
        (counter > 1 ? "are" : "is") + " " +
        counter + " terminal" +
        (counter > 1 ? "s" : "") + " installed!");

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
   * Output infos about the slot object passed.
   */
  public static void printSlotInfos (CardTerminal terminal, int aSlotID) {
    try {
      System.out.println ("Slot info: ");
      System.out.println ("slot ID: " + aSlotID );
      System.out.println ("card present: " + (terminal.isCardPresent(aSlotID) ? "yes" : "no") );
    }
    catch(CardTerminalException ex){
      System.out.println ("CardTerminalException: ");
      System.out.println (ex.getMessage () );
    }
  } // printSlotInfos


  /**
   * Output infos about the terminal object passed. This will output information
   * about the slots of a terminal as well.
   */
  public static void printTerminalInfos (CardTerminal terminal) {
    System.out.println (
      "Address: " + terminal.getAddress () + ENDL +
      "Name:    " + terminal.getName () + ENDL +
      "Type:    " + terminal.getType () + ENDL);

    System.out.println ("terminal.features (): ");
    Properties termProps = terminal.features ();
    if (termProps != null)
      termProps.list (System.out);

    int slots = terminal.getSlots();

    System.out.println ("\nslot infos: ");
    System.out.println ("there " +
      (slots > 1 ? "are" : "is") + " " +
      slots + " slot" +
      (slots > 1 ? "s" : "") +
      " present in this terminal!");

    for (int j = 0; j < slots; j++) {
      System.out.println ("slot channel available: " +
        (terminal.isSlotChannelAvailable (j) ? "yes" : "no") );

      printSlotInfos (terminal,j);
    } // for

  } // printTerminalInfos


} // end of GetTerminalInfos.java -------------------------------------------------------
