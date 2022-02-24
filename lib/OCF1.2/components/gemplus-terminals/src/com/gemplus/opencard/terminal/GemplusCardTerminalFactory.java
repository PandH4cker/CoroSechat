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
 *  - In addition,  modification and redistribution of this  source
 *    code must retain the below original copyright notice.
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

/*
 * Copyright © 1997, 1998 IBM Corporation.
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
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE. ANY
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

package com.gemplus.opencard.terminal;

import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.TerminalInitException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.Tracer;


/**
 * <tt>CardTerminalFactory</tt> for Gemplus card terminals.
 * Supported terminals types are GemCore-based readers (GCR410,GCR700 and E-PAD)
 * and Oros-based readers (GCR400, GCI400 and GCR500).<p>
 * Terminal specific informations are stored in a String array.<p>
 * <ul>
 * <li> The first element is always the friendly name of the reader
 * <li> Second element declares the type of the reader hardware (for example GCR410)
 * <li> In case of GCR410 (GemCore) the third parameter declares the
 * device used for communication (e.g., COM1, COM2, SERIALA, SERIALB, ...)
 * </ul>
 *
 * @version $Id: GemplusCardTerminalFactory.java,v 3.0 1999/02/11 17:21:16 root Exp root $<br>
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)<br>
 * Patched by Gilles.Pauzie@gemplus.com (Fri, 22 Jan 1999)<br>
 * Patched by Gilles.Pauzie@gemplus.com (Tue, 9 Feb 1999) Bug fixed for
 *   cards only using T=1 protocol<br>
 *
 * @see opencard.core.terminal.CardTerminalFactory
 *
 */
public class GemplusCardTerminalFactory implements CardTerminalFactory {

  /**
   *  create a specific <tt>CardTerminal</tt> object that knows how to handle
   *  a specific reader and register it to the CardTerminalRegistry.
   *  @param     ctr
   *             the CardTerminalRegistry for registration-process
   *  @param     terminalInfo
   *             The String array with configuration parameters for the
   *             specific terminal.
   *  @see       CardTerminalFactory
   *  @see       GemCoreCardTerminal
   *  @see       OrosCardTerminal
   */
  public void createCardTerminals(CardTerminalRegistry ctr,
                                  String[] terminalInfo)
          throws CardTerminalException,
                 TerminalInitException {

    // check for minimal parameter requirements
    if (terminalInfo.length < 2)
      throw new TerminalInitException(
                    "at least 2 parameters necessary to identify the terminal");

    // is it a GemCore-based reader?
    if ( (terminalInfo[1].equals("GCR410")) ||
         (terminalInfo[1].equals("GCR700")) ||
         (terminalInfo[1].equals("E-PAD"))
         ){

      // GemCore reader needs one additional parameter for the serial port name
      if (terminalInfo.length != 3)
        throw new TerminalInitException("createCardTerminals: "
                      + "Factory needs 3 parameters for GemCore-based terminal");

      // creates the terminal instance
      // and registers to the CardTerminalRegistry
      ctr.add(new GemCoreCardTerminal(terminalInfo[TERMINAL_NAME_ENTRY],
                                     terminalInfo[TERMINAL_TYPE_ENTRY],
                                     terminalInfo[TERMINAL_ADDRESS_ENTRY]));
    // is it an Oros-based reader?
    } else if ( (terminalInfo[1].equals("GCR400")) ||
                (terminalInfo[1].equals("GCR500")) ||
                (terminalInfo[1].equals("GCI400"))
              ){

      // Oros reader needs one additional parameter for the serial port name
      if (terminalInfo.length != 3)
        throw new TerminalInitException("createCardTerminals: "
                      + "Factory needs 3 parameters for Oros-based terminal");

      // creates the terminal instance
      // and registers to the CardTerminalRegistry
      ctr.add(new OrosCardTerminal(terminalInfo[TERMINAL_NAME_ENTRY],
                                    terminalInfo[TERMINAL_TYPE_ENTRY],
                                    terminalInfo[TERMINAL_ADDRESS_ENTRY]));
    } else
      throw new TerminalInitException("Type unknown: "
                                      + terminalInfo[TERMINAL_NAME_ENTRY]);
  }


  /** initialize the factory */
  public void open() throws CardTerminalException {}


  /** deinitialize the factory */
  public void close() throws CardTerminalException {}
}
