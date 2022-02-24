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


import opencard.core.event.CardTerminalEvent;
import opencard.core.event.CTListener;
import opencard.core.event.EventGenerator;
import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.OpenCardPropertyLoadingException;


/**
 * A sample demonstrating the notification mechanism when a card is
 * being removed. If there is no card inserted when this application
 * is started one has to inserted first before it can be removed again.
 *
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: RemoveCard.java,v 1.7 1999/10/14 15:44:18 pbendel Exp $
 **/

public class RemoveCard implements CTListener {

  private static Object monitor    = "synchronization monitor";
  private static final String NAME = "notification after card removal";


  public static void main (String [] args) throws InterruptedException {
    System.out.println ("------------------------------------------------------------");
    System.out.println ("start use case: " + NAME);
    System.out.println ("");

    try {
      SmartCard.start ();

      // add this object as a listener
      EventGenerator.getGenerator().addCTListener (new RemoveCard () );

      synchronized (monitor) {
        System.out.println ("please remove a card now!\n");
        monitor.wait();
      }

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
   * Gets invoked if a card is inserted.
   */
  public void cardInserted (CardTerminalEvent ctEvent) {}


  /**
   * Gets invoked if a card is removed.
   */
  public void cardRemoved (CardTerminalEvent ctEvent) {
    System.out.println ("card was removed\n");

    synchronized (monitor) {
      monitor.notifyAll();
    }
  } // cardRemoved


} // RemoveCard


// end of RemoveCard.java -------------------------------------------------------
