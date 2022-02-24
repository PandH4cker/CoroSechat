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

import java.util.Properties;
import java.util.Vector;
import java.util.Date;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CommandAPDU;

import opencard.core.util.Tracer;

import opencard.opt.terminal.PowerManagementInterface;
import opencard.opt.terminal.TerminalCommand;

import com.ibm.opencard.terminal.pcsc.PcscError;

/** Implementation of an OpenCard <tt>CardTerminal</tt> for PCSC.
  *
  * @author Stephan Breideneich (sbreiden@de.ibm.com)
  * @version $Id: PcscMigCardTerminal.java,v 1.19 1999/10/14 15:44:16 pbendel Exp $
  *
  * @see opencard.core.terminal.CardTerminal
  */
public class PcscMigCardTerminal extends CardTerminal

  implements  PcscMigConstants,
              PowerManagementInterface,
              TerminalCommand,
              Pollable {

  private Tracer itracer = new Tracer(this, PcscMigCardTerminal.class);

  /** The reference to the PCSC ResourceManager for this card terminal. */
  private OCFPCSCM pcsc;

  /** The context to the PCSC ResourceManager */
  private int context = 0;

  /** The state of this card terminal. */
  private boolean closed = true;

  /** Is a card inserted currently? */
  private boolean cardInserted;

  /** The cardHandle */
  private int cardHandle = 0;

  /** The maximal possible timeout in seconds */
  private static final int MAX_TIMEOUT = 60;

  /** The <tt>CardID</tt> of the presently inserted card. */
  private CardID cid = null;

  /** Instantiate an <tt>PcscMigTerminal</tt>.
   *
   *  @param      name
   *              The user friendly name.
   *  @param      type
   *              The terminal type (here "PCSC")
   *  @param      address
   *              not used
   *  @exception  CardTerminalException
   *              Thrown when a problem occured.
   */
  protected PcscMigCardTerminal(String name, String type,String address)
    throws CardTerminalException {
    super(name, type, address);

    try {
      itracer.debug("PcscMigCardTerminal", "connect to PCSC Migration");

      // load native library
      OCFPCSCM.loadLib();
      pcsc = new OCFPCSCM();

      /* connect to the PCSC resource manager */
      context = pcsc.SCardEstablishContext(SCARD_SCOPE_USER);

      itracer.debug("PcscMigCardTerminal", "Driver initialized");

    } catch (PcscException e) {
      throw translatePcscException(e);
    }

    /* add one slot */
    addSlots(1);
  }

  /** Open the card terminal: We register with the <tt>CardTerminalRegistry</tt>
   * as a <tt>Pollable</tt> card terminal.
   */
  public void open() {
    CardTerminalRegistry.getRegistry().addPollable((Pollable)this);
    closed = false;
  }

  /** Close the connection to the card terminal.
    * Could be used by unregister to free up the resources used by the
    * terminal.
    *
    * @exception  opencard.core.terminal.CardTerminalException
    *                     Thrown if there are problems with closing the
    *                     connection
    */
  public void close() throws CardTerminalException {
    if (!closed) {
      // is card inserted and powered?
      if (cardInserted && cid != null) {
        itracer.debug("close", "card inserted - try to power down card");
        powerDownCard(0, MAX_TIMEOUT);
      } else
        itracer.debug("close", "no card inserted");

      try {
        // remove from registry pollingList
        itracer.debug("close", "disable polling");
        CardTerminalRegistry.getRegistry().removePollable((Pollable)this);
        closed = true;

        itracer.debug("close", "release context");
        pcsc.SCardReleaseContext(context);
        context = 0;
      } catch (PcscException e) {
        throw translatePcscException(e);
      }
    } else {
      itracer.debug("close", "Terminal already closed!");
      throw new CardTerminalException("Pcsc10CardTerminal: already closed");
    }
  }

  /** Implementation of <tt>CardTerminal.internalReset()</tt>. */
  protected CardID internalReset(int slot, int ms) 
    throws CardTerminalException {

    // check if cardHandle exists
    if (cardHandle != 0) {
      itracer.debug("internalReset", "cardHandle exists - try reconnect");
      cid = null;                // invalidate CardID
    
      Integer returnedProtocol = new Integer(0);
      try {
        pcsc.SCardReconnect(cardHandle,
                            SCARD_SHARE_EXCLUSIVE,
                            SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1,
                            SCARD_UNPOWER_CARD,
                            returnedProtocol);

        cid = new CardID(this,0, getATR());
      } catch(PcscException e) {
        throw translatePcscException(e);
      }

      return getCardID(slot, ms);
    } else {
      itracer.debug("internalReset", "card reset failed - no card inserted");
      return null;
    }
  }


  /** Check whether there is a smart card present.
    *
    * @param  slot
    *           Number of the slot to check (must be 0 for PCSC)
    * @return True if there is a smart card inserted in the card
    *           terminals slot.
    */
  public boolean isCardPresent(int slot) throws CardTerminalException {

    if (!closed) {
      if (slot != 0)
        throw new CardTerminalException("Invalid Slot number: " + slot);
  
      try {
        int result = getTerminalStatus();
  
        if ((result & SCARD_STATE_PRESENT) == SCARD_STATE_PRESENT)
          return true;
        else {
          cid = null;                                 /* kill the cardID object*/
          return false;
        }
      } catch (CardTerminalException e) {
        cid = null;
        return false;
      }
    } else
      return false;
  }

  /** Request status of terminal.
    *
    * @return    Status information in some non-standard format.
    * @exception opencard.core.terminal.CardTerminalException
    *            Exceptions thrown by driver.
    */
  private int getTerminalStatus() throws CardTerminalException {
    int returnCode = 0;

    try {
      /* fill in the data structure for the state request */
      PcscReaderState[] rState = new PcscReaderState[1];
      rState[0] = new PcscReaderState();
      rState[0].CurrentState = SCARD_STATE_UNAWARE;
      rState[0].Reader = getName();

      /* set the timeout to 1 second */
      pcsc.SCardGetStatusChange(context, 1, rState);
      returnCode = rState[0].EventState;

    } catch (PcscException e) {
      throw translatePcscException(e);
    }

    return returnCode;
  }

  /** Return the <tt>CardID</tt> of the presently inserted card. Will returned
   * the cached card if slot's status has not changed; otherwise it will
   * really retrieve the <tt>CardID</tt>.<p>
   *
   * @param     slot
   *            slot number
   * @return    A <tt>CardID</tt> object representing the inserted smart card.
   * @exception opencard.core.terminal.CardTerminalException
   *            thrown when problem occured getting the ATR of the card
   */
  public CardID getCardID(int slot) throws CardTerminalException {
    return getCardID(slot, MAX_TIMEOUT);
  }

  /** Return the <tt>CardID</tt> of the presently inserted card. Will returned
   * the cached card if slot's status has not changed; otherwise it will
   * really retrieve the <tt>CardID</tt>.<p>
   *
   * @param     slot
   *            slot number
   * @param     timeout
   *            timeout in seconds
   * @return    A <tt>CardID</tt> object representing the inserted smart card.
   * @exception opencard.core.terminal.CardTerminalException
   *            thrown when problem occured getting the ATR of the card
   */
  public synchronized CardID getCardID(int slot, int timeout) throws CardTerminalException {

    // ... the PCSC card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot number: " + slot);

    // card present and powered up
    if (cardInserted && (cid != null)) {
      return cid;
    }
    else
      powerUpCard(slot, timeout);

    return cid;
  }

  /** Power up a card. If the card was already powered up it is resetted.
    *
    * @param      timeout
    *             The time to wait before returning (in s); <tt>-1</tt> signals
    *             an indefinite timeout (i.e., wait forever).
    * @exception  opencard.core.terminal.CardTerminalException
    *             Thrown when no card is present.
    * @see        opencard.opt.terminal.PowerManagementInterface
    */
  public void powerUpCard(int slot, int timeout) throws CardTerminalException {

    // ... the Pcsc card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot number: " + slot);

    if (timeout == 0)
      timeout = 1;              // cannot use 0 here... see docu

    if (timeout == -1) {        // emulate infinite timeout...
      while (!requestCard(MAX_TIMEOUT))
              ;
    } else {                    // emulate timeout longer than 60 s
      if (timeout > MAX_TIMEOUT) {
        for ( ; timeout > MAX_TIMEOUT; timeout -= MAX_TIMEOUT)
          if (requestCard(MAX_TIMEOUT))
            return;
      }
      if (!requestCard(timeout))
        throw new CardTerminalException("operation timed out", this);
    }
  }

  /** Power power down a card.
    *
    * @param      slot
    *             The slot-number of the terminal
    * @param      timeout
    *             The time to wait before returning (in seconds);
    *             <tt>-1</tt> signals an indefinite timeout (i.e., wait
    *             forever).
    * @exception  opencard.core.terminal.CardTerminalException
    *             Thrown when no card is present.
    * @see        opencard.opt.terminal.PowerManagementInterface
    */
  public void powerDownCard(int slot, int timeout) throws CardTerminalException {

    if (!isCardPresent(slot))   // checks slot number anyway
      throw new CardTerminalException("no card present", this);

    cid = null;          // invalidate cardID

    try {
      pcsc.SCardDisconnect(cardHandle, SCARD_EJECT_CARD);
      cardHandle = 0;
    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }

  /** Get ATR from card.
    *
    * @return the raw ATR data
    * @exception opencard.core.terminal.CardTerminalException
    *            Card did not respond properly
    */
  private byte[] getATR() throws CardTerminalException {

    byte[] ATR = null;
    try {
      ATR = pcsc.SCardGetAttrib(cardHandle, SCARD_ATTR_ATR_STRING);
    } catch(PcscException e) {
      throw translatePcscException(e);
    }
    return ATR;
  }


  /** Wait for card insertion, power up the card, and perform reset.
    *
    * @param      timeout
    *             Timeout in seconds (1..60).
    * @return     false if no card has been found within the timeout.
    * @exception  opencard.core.terminal.CardTerminalException
    *             Card did not reset properly.
    */
  private boolean requestCard(int timeout) throws CardTerminalException {

    if (timeout < 1 || timeout > MAX_TIMEOUT)
      throw new CardTerminalException("Invalid timeout: " + timeout, this);

    cid = null;                 // invalidate old card id

    // if actual time > endTime, the timeout is reached...
    long endTime = (new Date()).getTime() + (timeout * 1000);
    boolean cardPresent = false;
    while ((new Date()).getTime() < endTime) {

      // is card inserted?
      if (cardPresent = isCardPresent(0))
        break;

      // sleep a little bit and ignore any exception
      try { Thread.sleep(250); } catch(Exception e) {}
    }

    // try to connect the card
    if (cardPresent) {

      // we use the EXCLUSIVE mode of PCSC, so we cannot connect to the reader without a card inserted
      Integer returnedProtocol = new Integer(0);
      try {
        cardHandle = pcsc.SCardConnect(context,
                                       getName(),
                                       SCARD_SHARE_EXCLUSIVE,
                                       SCARD_PROTOCOL_T1, // Migration supports only T1 at the moment */
                                       returnedProtocol);

        cid = new CardID(this,0, getATR());

      } catch(PcscException e) {
        throw translatePcscException(e);
      }
    }

    return cardPresent;
  }


  /** Send control command to terminal.
   *
   * @param     cmd
   *                    a byte array containing the command to be send to the card terminal
   * @return    Response from terminal.
   * @exception opencard.core.terminal.CardTerminalException
   *                    Exception thrown by driver.
   * @see       opencard.opt.terminal.TerminalCommand
   */
  public byte[] sendTerminalCommand(byte[] cmd)
    throws CardTerminalException {

    if (cardHandle == 0)
      throw new CardTerminalException("no card present", this);

    try {
      byte[] responseData = pcsc.SCardControl(cardHandle, 0, cmd);
      return responseData;

    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }

  /** The implementation of <tt>CardTerminal.internalSendAPDU()</tt>.
   *
   * @param slot
   *          logical slot number
   * @param capdu
   *          C-APDU to send to the card
   * @param ms
   *          timeout in ms (not supported, ignored)
   */
  protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms) 
    throws CardTerminalException {

    // ... the Pcsc card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot: " + slot);

    itracer.debug("internalSendAPDU", "sending " + capdu);

    byte [] responseData = null;
    try {
      responseData = pcsc.SCardTransmit(cardHandle, capdu.getBytes());
    } catch (PcscException e) {
      throw translatePcscException(e);
    }
    ResponseAPDU rAPDU = new ResponseAPDU(responseData);
    itracer.debug("internalSendAPDU", "receiving " + rAPDU);
    return rAPDU;
  }

  /** Signal to observers that an inserted card was removed.<p>
   * @param slot
   *          slot number
   */
  protected void cardRemoved(int slotID) 
  {

    super.cardRemoved(slotID);
    itracer.debug("cardRemoved", "disconnect from PC/SC");
  }

  /** This method is normally used by the <tt>CardTerminalRegistry</tt> to
   *  generate the <tt>OpenCard</tt> events if the Slot implementation does
   *  not support events itself.
   */
  public void poll() 
    throws CardTerminalException {

    boolean newStatus = isCardPresent(0);

    if (cardInserted != newStatus) {

      itracer.debug("poll", "status change");
      cardInserted = !cardInserted;
      // ... notify listeners
      if (cardInserted) {
        cardInserted(0);
      } else {
        cardRemoved(0);
        if (cardHandle != 0) {
          try {
            pcsc.SCardDisconnect(cardHandle, SCARD_EJECT_CARD);
            itracer.debug("cardRemoved", "successfully disconnected from PC/SC");
          } catch (PcscException e) {
            throw translatePcscException(e);
          }
        }
      }
    } else {
      // ... no change took place
      itracer.debug("poll", "no status change");
    }
  }

  /** Release driver resources when garbage collected.
    */
  protected void finalize() {
    try {
      if (!closed)
        this.close();
    } catch (CardTerminalException e) {
      // ignore
    }
  }

  /** translate the PcscException into CardTerminalException.<p>
    */
  protected CardTerminalException translatePcscException(PcscException e) {

    // error description available? if not, description = null
    String description = PcscError.getMessage(e.returnCode());

    return new CardTerminalException(
                 e.getMessage()
                 + ((description == null) ? "" 
                                          : "description = " + description + "\n"),
                 this);

  }

}
