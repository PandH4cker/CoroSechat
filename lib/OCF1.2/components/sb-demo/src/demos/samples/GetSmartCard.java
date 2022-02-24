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
 * A sample demonstrating how to obtain a <TT>SmartCard</TT> object 
 * when using the event-driven paradigm instead of the procedural 
 * <TT>SmartCard.waitForCard</TT>.
 * Please <B>Note</B> that this sample actually waits for a card to be 
 * inserted so if there is one inserted already at the start it has 
 * to be removed first and inserted again.
 * 
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version 1.0
 **/

public class GetSmartCard implements CTListener { 

  private static final String NAME = "get a SmartCard object";
  private static Object monitor    = "synchronization monitor";


  /**
   * Starts the execution of this program.
   */
  public static void main (String [] args) {
    System.out.println ("------------------------------------------------------------");
    System.out.println ("start use case: " + NAME);
    System.out.println ("");

    try {
      // get OpenCard up and running
      SmartCard.start ();

      // we want to receive events when a card is inserted or removed so we 
      // install this object as a CardTerminalListener
      EventGenerator.getGenerator().addCTListener (new GetSmartCard () );

      // stop execution of the current thread here to wait for a card to be inserted
      synchronized (monitor) {
        try {
          monitor.wait ();
        }
        catch (InterruptedException ie) {
          System.out.println ("waiting was interrupted for some reason");
        }
      }

      // al right, we have done all we wanted so let's cleanup OpenCard
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
   * Is invoked if a card is inserted.
   *
   * @param ctEvent  the event indicating the terminal and slot
   */
  public void cardInserted (CardTerminalEvent ctEvent) {
    System.out.println ("card inserted");
            
    try{
      SmartCard card = SmartCard.getSmartCard (ctEvent);
      if (card != null)          
        System.out.println (" got a SmartCard " + card + card.getCardID () );

      // hm, we have a problem
      else
        System.out.println (" did NOT get a SmartCard");

      // notify monitor when we have looped as often as we want
      // so that we can finish now
      synchronized (monitor) {
        monitor.notify ();
      }

    }
    catch(CardTerminalException ex){
      ex.printStackTrace(System.out);
    }
  } // cardInserted


  /**
   * Is invoked if a card is removed.
   *
   * @param ctEvent  the event indicating the terminal and slot
   */
  public void cardRemoved (CardTerminalEvent ctEvent) {
    System.out.println ("card removed");
  } // cardRemoved


} // GetSmartCard


// end of GetSmartCard.java -------------------------------------------------------
