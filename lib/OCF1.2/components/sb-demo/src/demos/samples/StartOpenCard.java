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

import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.OpenCardPropertyLoadingException;


/** 
 * A sample that starts up OpenCard and shuts it down again. These operations are the 
 * most basic ones to be carried out with OCF as they set the whole machinery 
 * into operation and shut it down again in a controlled manner. 
 * <TT>SmartCard.start</TT> must therefore always be invoked before carrying out other
 * OpenCard functionality. <TT>SmartCard.shutdown</TT> is the corresponding method to
 * be invoked afterwards.
 *
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: StartOpenCard.java,v 1.8 1998/09/17 14:46:59 cvsusers Exp $
 **/

public class StartOpenCard { 

  private static final String NAME = "start OpenCard";


  public static void main (String [] args) {
    System.out.println ("------------------------------------------------------------");
    System.out.println ("start use case: " + NAME);
    System.out.println ("");

    try {
      SmartCard.start ();
      if (SmartCard.isStarted () )
        System.out.println ("configuration was done.");

      System.out.println ("OpenCard is " + (SmartCard.isStarted () ? "" : "NOT") + " started!");

      System.out.println ("try again anyway\n");
      SmartCard.start ();

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

} // StartOpenCard


// end of StartOpenCard.java -------------------------------------------------------
