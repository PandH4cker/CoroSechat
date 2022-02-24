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

package com.ibm.opencard.terminal.pcscmig;

import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.Tracer;

/** <tt>CardTerminalFactory</tt> for PCSC card terminals.
  *
  * @author  Stephan Breideneich (sbreiden@de.ibm.com)
  * @version $Id: PcscMigCardTerminalFactory.java,v 1.10 1999/04/01 12:21:44 pbendel Exp $
  *
  * @see opencard.core.terminal.CardTerminalFactory
  */
public class PcscMigCardTerminalFactory implements CardTerminalFactory {
  private Tracer itracer = new Tracer(this, PcscMigCardTerminalFactory.class);

  /** The reference to the PCSC Migration for this card terminal. */
  private OCFPCSCM pcsc;

  /** Instantiate an <tt>PcscMigTerminalFactory</tt>.
   *
   * @exception CardTerminalException
   *            Thrown when a problem occured.
   */
  public PcscMigCardTerminalFactory()
    throws CardTerminalException {

    super();
    open();
  }

  /** create a specific <tt>CardTerminal</tt> object that knows how to handle
   *  a specific card terminal and register it to the CardTerminalRegistry.
   *  @param     ctr
   *		 the CardTerminalRegistry for registration-process
   *  @param     terminalInfo
   *             null - not needed for this factory
   *  @see       opencard.core.terminal.CardTerminalFactory
   */
  public void createCardTerminals(CardTerminalRegistry ctr, String[] terminalInfo)
    throws CardTerminalException {

    // add the terminals found in the PCSC ResourceManager
    String[] terminals = ListReaders();
    for (int i=0;i<terminals.length;i++) {
      ctr.add(new PcscMigCardTerminal(terminals[i], "PCSCMIG", ""));
    }
  }

  /** initialize the factory (setup the PC/SC-driver) */
  public void open() 
    throws CardTerminalException {

    // factory already opened?
    if (pcsc == null) {
      try {
        itracer.debug("PcscMigCardTerminalFactory", "connect to PCSC Migration");
        OCFPCSCM.loadLib();
        pcsc = new OCFPCSCM();
        itracer.debug("PcscMigCardTerminalFactory", "Driver initialized");
      } catch (PcscException e) {
        throw new CardTerminalException("PcscMigCardTerminalFactory: " + e.getMessage());
      }
    }
  }

  /** deinitialize the PC/SC-driver */
  public void close() 
    throws CardTerminalException {

    pcsc = null;
  }

  /** get the actual PC/SC reader list
   *
   *  @exception opencard.core.terminal.CardTerminalException
   *             thrown when error occured
   */
  private String[] ListReaders() throws CardTerminalException
  {
    itracer.debug("PcscMigCardTerminalFactory", "get reader list from PC/SC Migration");
    String[] terminals = null;
    try {
      terminals = pcsc.SCardListReaders("IBM_SCT$AllReaders");
    } catch (PcscException e) {
      throw new CardTerminalException("PcscMigCardTerminalFactory: " + e.getMessage());
    }
    return terminals;
  }

}
