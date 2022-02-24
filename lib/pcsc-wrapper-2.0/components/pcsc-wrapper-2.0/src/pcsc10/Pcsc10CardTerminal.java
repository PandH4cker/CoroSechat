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

package com.ibm.opencard.terminal.pcsc10;

import java.lang.*;
import java.io.*;
import java.util.*;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CommandAPDU;

//import opencard.core.util.Tracer;
//import opencard.core.util.HexString;
import opencard.core.util.*;

import opencard.opt.terminal.TerminalCommand;
import opencard.opt.terminal.PowerManagementInterface;

/** Implementation of an OpenCard <tt>CardTerminal</tt> for PCSC.
 *
 * @author Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: Pcsc10CardTerminal.java,v 2.0 2001/06/14 13:32:36 root Exp root $
 *
 * @see opencard.core.terminal.CardTerminal
 */
public class Pcsc10CardTerminal 
  extends CardTerminal

  implements TerminalCommand,
             Pollable,
	     PowerManagementInterface {

  private Tracer itracer = new Tracer(this, Pcsc10CardTerminal.class);

  /** The reference to the PCSC ResourceManager for this card terminal. */
  private OCFPCSC1 pcsc;

  /** The reference to the Pcsc10CardTerminalFactory */
  private Pcsc10CardTerminalFactory    PCSCfactory = null;

  /** The context to the PCSC ResourceManager */
  private int context = 0;

  /** The state of this card terminal. */
  private boolean closed;

  /** Is a card inserted currently? */
  private boolean cardInserted = false;

  /** The record of the card status */
  private boolean CachedCardStatus = false;

  /** Date of release */
  private static final String DATE = "October 31, 2000";

  /** Release version */
  private static final String VERSION = "Release v2.0";

  /** The cardHandle */
  private int cardHandle = 0;

  /* states returned by SCardGetStatusChange */
  private static final int SCARD_STATE_MUTE=0x200;
  private static final int SCARD_STATE_PRESENT=0x020;

  /** The <tt>CardID</tt> of the presently inserted card. */
  private CardID cid = null;

  /** The <tt>ATR</tt> of the presently inserted card. */
  private byte[] cachedATR;

  /** Specify the number of expected returned data in the ISO command. */
  private int le = 0;

  /** Specify the number of data in the ISO command. */
  private int lc = 0;

  /** Get the length of the Command APDU buffer. */
  private int lenbuf = 0;

  /** ISO case choice requested. */
  private int caseAPDU = 0;

  /** Transport layer uses the full ISO standard in mode T=0. */
  private boolean TPDU_Uses_ISO = true;

  /** ISO command used protocol */
  private int protocolUsed = 0;

  /** Communication port status for the reader */
  private int portMode = 0;

  /** Communication port for the reader */
  private int port = 0;

  /** Instantiate an <tt>Pcsc10Terminal</tt>.
   *
   * @param     name
   *            The user friendly name.
   * @param     type
   *            The terminal type (here "PCSC")
   * @param     address
   *            The communication port
   * @param     factory
   *            The card terminal reference
   * @param     port
   *            The communication port ID
   * @exception CardTerminalException
   *            Thrown when a problem occured.
   */
  protected Pcsc10CardTerminal(String name, String type, String address,
			       Pcsc10CardTerminalFactory factory,
			       int port)
    throws CardTerminalException {

      super(name, type, address);

      if (address.equals("SHARED")) {
         portMode = Pcsc10Constants.SCARD_SHARE_SHARED;
      } else {
         portMode = Pcsc10Constants.SCARD_SHARE_EXCLUSIVE;
      }

      try {
        itracer.debug("Pcsc10CardTerminal", "connect to PCSC 1.0 resource manager");
  
        // load native library
        OCFPCSC1.loadLib();
        pcsc = new OCFPCSC1();

      } catch (PcscException e) {
        throw translatePcscException(e);
      }

      PCSCfactory = factory;    // to refer card terminal factory
      this.port = port;         // to trace the communication port

      // add one slot
      addSlots(1);
   }

   /*
    * Open the card terminal: We register with the
    * <tt>CardTerminalRegistry</tt> as a <tt>Pollable</tt>
    * card terminal. Connect to the PCSC resource manager
    *
    * @throws CardTerminalException
    */
   public void open() throws CardTerminalException {

      try {
         itracer.debug("Pcsc10CardTerminal", 
                       "connect to PCSC 1.0 resource manager");

         // connect to the PCSC resource manager
         context = 
            pcsc.SCardEstablishContext(Pcsc10Constants.SCARD_SCOPE_USER);

         itracer.debug("Pcsc10CardTerminal", "Driver initialized");

      } catch (PcscException e) {
         throw translatePcscException(e);
      }

      // Get the TPDU ISO normatives from the opencard.properties file.
      try {

         // Suppose that the file contains all the information needed.
         String ISOKeyValue =
            SystemAccess.getSystemAccess().getProperty("Uses_Standard_ISO_TPDU");

         if (ISOKeyValue.equals("false")) {
            setISOTPDUMode(false);
         } else {
            setISOTPDUMode(true);
         }

         if (!TPDU_Uses_ISO) {
            System.out.println("TPDU Not Used\n");
         } else {
            System.out.println("Uses ISO TPDU\n");
         }
      } catch (Exception e) {
         setISOTPDUMode(true);
         System.out.println("Uses ISOTPDU\n");
      }

      closed = false;

      // register to cardterminal registry
      CardTerminalRegistry.getRegistry().addPollable((Pollable) this);
   }


  /** Close the connection to the card terminal by powerDown the Card.
   *  Could be used by unregister to free up the resources used by the
   *  terminal.
   *
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown if there are problems with closing the
   *            connection
   */
  public void close() 
    throws CardTerminalException {

    // to close the specific card terminal port
    PCSCfactory.close(this.port);

    if (!closed) {
      itracer.debug("close", "disable polling");
      
      CardTerminalRegistry.getRegistry().removePollable((Pollable)this);

      closed = true;

      // is card inserted and powered?
      if (cardInserted && cid != null) {
        itracer.debug("close", "card inserted - try to power down card");
  
        cid = null;    // invalidate cardID

        //cardDisconnect(Pcsc10Constants.SCARD_EJECT_CARD);
        powerDownCard(0, -1);

      } else
        itracer.debug("close", "no card inserted");

      try {
        itracer.debug("close", "release context");
        pcsc.SCardReleaseContext(context);

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
    if (CachedCardStatus) {
      itracer.debug("internalReset", "cardHandle exists - try reconnect");
      cid = null;    // invalidate CardID
    
      Integer returnedProtocol = new Integer(0);

      try {
	  if (cardHandle != 0) {

	      // If the card handle is available, retrieve the second ATR
	      pcsc.SCardReconnect(cardHandle, portMode, 
				  Pcsc10Constants.SCARD_PROTOCOL_T0 
				  | Pcsc10Constants.SCARD_PROTOCOL_T1, 
				  Pcsc10Constants.SCARD_RESET_CARD, 
				  returnedProtocol);
	  } else {

	      // If the card handle is not available, retrieve the first ATR
	      cardHandle = 
                  pcsc.SCardConnect(context, getName(), portMode, 
                                    Pcsc10Constants.SCARD_PROTOCOL_T0 
                                    | Pcsc10Constants.SCARD_PROTOCOL_T1,
				    returnedProtocol);
	  }

	  UpdateCardStatus(0);

	  if (cachedATR == null) {
	      throw new CardTerminalException("No ATR present. Card badly inserted? ", 
					      this);
	  }

	  cid = new CardID(this, 0, cachedATR);

      } catch (PcscException e) {
	  throw translatePcscException(e);
      }

      return getCardID(slot, ms);
      
    } else {
	itracer.debug("internalReset", 
		      "card reset failed - no card inserted");

	return null;
    }
  }


  /** Query the card terminal about its features.<p>
   *
   *  @return features
   *
   */
  protected Properties internalFeatures(Properties features) {

      features.put("Release Date: ", DATE);
      features.put("PCSC CardTerminal Version: ", VERSION);
      features.put("Reader Name: ", getName());
      features.put("Reader Type: ", getType());

      return features;
  }

  /** Check whether there is a smart card present.
   *
   * @param  slot
   *         Number of the slot to check (must be 0 for PCSC)
   * @return True if there is a smart card inserted in the card
   *         terminals slot.
   */
  public synchronized boolean isCardPresent(int slot) 
    throws CardTerminalException {

      return CachedCardStatus;

  }

   /**
    * Update the smart card status.
    * 
    * @param slot
    * @return True if there is a smart card inserted in the card terminals
    * slot.
    */
   protected boolean UpdateCardStatus(int slot)
       throws CardTerminalException {

      int       offset, i, k, l;
      boolean   protocolSet = false;


    // check if terminal is already closed...
    if (!closed) {

      // check for the right slot-number
      if (slot != 0) {
          throw new CardTerminalException("Invalid Slot number: " + slot);
      }
      /* fill in the data structure for the state request */
      PcscReaderState[] rState = new PcscReaderState[1];
      rState[0] = new PcscReaderState();
      rState[0].CurrentState = Pcsc10Constants.SCARD_STATE_UNAWARE;
      rState[0].Reader = getName();

      try {
        /* set the timeout to 1 second */
        pcsc.SCardGetStatusChange(context, 1, rState);

        // PTR 0219: check if a card is present but unresponsive
        if ( ((rState[0].EventState & SCARD_STATE_MUTE)!=0)
             &&
             ((rState[0].EventState & SCARD_STATE_PRESENT)!=0)) {

          throw new CardTerminalException("Card present but unresponsive in slot "+slot);
        }

      } catch (PcscException e) {
	  throw translatePcscException(e);
      }

      cachedATR = rState[0].ATR;

      // check the length of the returned ATR. if ATR is empty / null, no card is inserted
      if (cachedATR != null) {
	  if (cachedATR.length > 0) {

	      // Determine which card protocol is used by the card

	      // While a TDi character exist,
	      // places l on the next TDi
	      // protocol memorises the first found protocol.
	      // Today, we assumes that only T=0, T=1 or T=0/1 are supported.
	      // So we memorised the first founded protocol which must be
	      // T=0 for bi-protocol card according to ISO standard.
	      l = 1;        // T0 offset

	      while ((cachedATR[l] & (byte) 0x80) != (byte) 0x00) {
		  offset = 0;

		  for (k = (byte) 0x10, i = 0; i < 4; k <<= 1, i++) {
		      if ((cachedATR[l] & k) != (byte) 0x00) {
			  offset++;
		      }
		  }

		  l += offset;

		  if (!protocolSet) {
		      protocolSet = true;
		      protocolUsed = cachedATR[l] & (byte) 0x0F;

		      break;
		  }
	      }

	      // Card is present
	      CachedCardStatus = true;
	      return true;
	  } else {
	      // No Card is present
	      CachedCardStatus = false;
	      return false;
	  }
      } else {
	  // No Card is present
	  CachedCardStatus = false;
	  return false;
      }
    } else {
	return false; // return "no card inserted", because terminal is already closed
    }
   }

   /** @deprecated since OCF1.2
   */
  public CardID getCardID(int slot, int timeout) 
    throws CardTerminalException {

    return getCardID(slot);
  }


  /** Return the <tt>CardID</tt> of the presently inserted card. Will returned
   *  the cached card if slot's status has not changed; otherwise it will
   *  really retrieve the <tt>CardID</tt>.<p>
   *
   *  @param      slot
   *      slot number
   *  @return     A <tt>CardID</tt> object representing the inserted smart card.
   *  @exception  opencard.core.terminal.CardTerminalException
   *      thrown when problem occured getting the ATR of the card
   */
  public CardID getCardID(int slot)  
    throws CardTerminalException {

    // ... the PCSC card terminal has only one slot
    if (slot != 0) {
	throw new CardTerminalException("Invalid slot number: " + slot);
    }
    if (CachedCardStatus && (cid != null)) {
	return cid;
    } else {
	return null;
    }
  }

  /** 
   * The internal openSlotChannel method.
   *  <tt>internalOpenSlotChannel</tt> is executed at the beginning of openSlotChannel.
   *
   * @param     slotID
   *    The number of the slot for which a <tt>SlotChannel</tt> is requested.
   */
  protected synchronized void internalOpenSlotChannel(int slotID)
    throws CardTerminalException {
        
      // cardConnect();
      itracer.debug("internalOpenSlotChannel", "CONNECT CARD:");
      powerUpCard(0, -1);
  }

  /**
   * The internal closeSlotChannel method.
   * <tt>internalCloseSlotChannel</tt> is executed
   * at the end of closeSlotChannel.
   *
   * @param     sc
   *		The <tt>SlotChannel</tt> to close.
   * @exception CardTerminalException
   *            thrown in case of errors closing the card (e.g. error disconnecting the card).
   */
  protected void internalCloseSlotChannel(SlotChannel sc)
    throws CardTerminalException {
      super.internalCloseSlotChannel(sc);
      //cardDisconnect(Pcsc10Constants.SCARD_LEAVE_CARD);
      powerDownCard(0, -1);
  }

  
  /** returns true if card is connected */
  private boolean isCardConnected(int slot) throws CardTerminalException {
      boolean   cardstatus = false;

      try {
	  cardstatus = isCardPresent(slot);
	  
	  if (cardstatus & (cardHandle == 0)) {
	      cardConnect();
	  }
      } catch (CardTerminalException e) {
	  throw new CardTerminalException(e.getMessage());
      }
      
      return cardstatus;
  }
  
  /** get card handle */
  private int getCardHandle() {
    return cardHandle;
  }
  
  /** connect to the card 
      replaced "Pcsc10Constants.SCARD_SHARE_EXCLUSIVE" by "portMode"
  */
  private void cardConnect() throws CardTerminalException {
    
    // we use the EXCLUSIVE mode of PCSC, so we cannot connect
    // to the reader without a card inserted
    
    Integer returnedProtocol = new Integer(0);    
    try {
      itracer.debug("cardConnect", "connect to smartcard");
      cardHandle = pcsc.SCardConnect(context,
                                     getName(),
				     portMode,
				     Pcsc10Constants.SCARD_PROTOCOL_T0
				     | Pcsc10Constants.SCARD_PROTOCOL_T1,
                   returnedProtocol);

      itracer.debug("cardConnect", "got card handle: " + cardHandle);
      
      if (cardHandle == 0) {
	  throw new CardTerminalException("No cardHandle present.  ", this);
      }
      UpdateCardStatus(0);
      
    } catch(PcscException e) {
      throw translatePcscException(e);
    }    
  }
  
  /** encapsulates SCardDisconnect
   *
   * @param disposition
   */
  private void cardDisconnect(int disposition) throws CardTerminalException {
    if (cardHandle != 0) {
      try {
        itracer.debug("cardDisconnect", "disconnect smartcard - cardHandle=" + cardHandle);
        pcsc.SCardDisconnect(cardHandle, disposition);
      } catch (PcscException e) {
        throw translatePcscException(e);
      } finally {
        itracer.debug("cardDisconnect", "invalidate card handle");
        cardHandle = 0;
      }
    } else
      itracer.debug("cardDisconnect", "cardHandle already 0 - disconnect impossible");
  }


  /** Send control command to terminal.
   *
   * @param     cmd
   *            a byte array containing the command to be send to the card terminal
   * @return    Response from terminal.
   * @exception opencard.core.terminal.CardTerminalException
   *            Exception thrown by driver.
   * @see       opencard.opt.terminal.TerminalCommand
   */
  public byte[] sendTerminalCommand(byte[] cmd)
    throws CardTerminalException {

    byte[]    responseData = null;

    if (cardHandle == 0)
      throw new CardTerminalException("no card present", this);

    try {

	responseData = pcsc.SCardControl(cardHandle, 0, cmd);

	checkNonNullResponse(responseData);

	return responseData;

    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }

   /**
    * Send powerUpCard command to terminal.
    * 
    * @exception CardTerminalException
    * @param slotID integer containing the card terminal slotID
    * @param ms integer containing the timeout
    * @param empty not used
    * @return Response from terminal.
    * @see opencard.opt.terminal.TerminalCommand
    */
   public byte[] powerUpCard(int slotID, int ms, 
                             int empty) throws CardTerminalException {

       // check if card handle exists
       if (CachedCardStatus) {

	   itracer.debug("PowerUpCard", "cardHandle exists - try reconnect");

	   Integer        returnedProtocol = new Integer(0);

	   try {

	       if (cardHandle != 0) {
		   pcsc.SCardReconnect(cardHandle, portMode, 
				       Pcsc10Constants.SCARD_PROTOCOL_T0 
				       | Pcsc10Constants.SCARD_PROTOCOL_T1, 
				       Pcsc10Constants.SCARD_UNPOWER_CARD, 
				       returnedProtocol);
	       } else {
		   cardHandle = 
		       pcsc.SCardConnect(context, getName(), portMode, 
					 Pcsc10Constants.SCARD_PROTOCOL_T0 
					 | Pcsc10Constants.SCARD_PROTOCOL_T1, returnedProtocol);
	       }

	       UpdateCardStatus(0);

	       if (cachedATR == null) {
		   throw new CardTerminalException("No ATR present. Card badly inserted? ", 
						   this);
	       }
	       cid = new CardID(this, 0, cachedATR);

	   } catch (PcscException e) {
	       throw translatePcscException(e);
	   }

	   return cachedATR;

       } else {
	   itracer.debug("powerUpCard", "card reset failed - no card inserted");

	   return null;
       }
   }


   /**
    * Send powerUpCard command to terminal.
    * 
    * @exception CardTerminalException
    * @param slotID integer containing the card terminal slotID
    * @param ms integer containing the timeout
    * @return Response from terminal.
    * @see opencard.opt.terminal.TerminalCommand
    */
   public void powerUpCard(int slotID, int ms) throws CardTerminalException {

       try {
	   powerUpCard(slotID, ms, 0);
       } catch (Exception e) {
	   throw new CardTerminalException(e.getMessage());
       }

   }

   /**
    * Send powerDownCard command to terminal.
    * 
    * @exception CardTerminalException
    * @param slotID integer containing the card terminal slotID
    * @param ms integer containing the timeout
    * @return Response from terminal.
    * @see opencard.opt.terminal.TerminalCommand
    */
    public void powerDownCard(int slotID, int ms)
	throws CardTerminalException {

	cardDisconnect(Pcsc10Constants.SCARD_UNPOWER_CARD);

	return;

    }

  /** check that Response from card is not null */
  private void checkNonNullResponse(byte [] responseData)
      throws CardTerminalException {
      if (responseData == null) {
	  throw new CardTerminalException("No response from reader. ", this);
      }
  }

  /** The implementation of <tt>CardTerminal.internalSendAPDU()</tt>.
   *
   * @param slot
   *        logical slot number
   * @param capdu
   *        C-APDU to send to the card
   * @param ms
   *        not supported, ignored
   */
  protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms) 
    throws CardTerminalException {

    // ... the Pcsc card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot: " + slot);

    itracer.debug("internalSendAPDU", "sending " + capdu);

    byte [] responseData = null;

    // line "responseData = pcsc.SCardTransmit(cardHandle, capdu.getBytes());"
    // modified for managing all 4 cases with automatic APDU chaining
    // when necessary...
    
    lenbuf = (int) capdu.getLength();

    byte[]    apduCommand = new byte[lenbuf];

    System.arraycopy(capdu.getBuffer(), 0, apduCommand, 0, lenbuf);

    // Transfer the APDU command inside the bufferCommand.
    if (lenbuf > 4) {
	le = apduCommand[4];
	le = this.convertByte(le);
	
	if (le == 0) {
	    le = 256;
	}
    }
    
    // main transmition block
    try {
	
	// if T=1 or else T=0
	if (protocolUsed == 1) {
	    
	    // T=1 transmition
	    responseData = pcsc.SCardTransmit(cardHandle, apduCommand);
	    
	} else {
	    
	    // Determine which type of Exchange between the reader
	    if (lenbuf == 4) {
		// Case 1 short
		caseAPDU = 1;
		itracer.debug("internalSendAPDU", "Case 1");
	    } else if (lenbuf == 5) {
		// Case 2 short
		itracer.debug("internalSendAPDU", "Case 2");
		caseAPDU = 2;
		le = apduCommand[4];
	    } else if ((le + 5) == lenbuf) {    
		// Case 3 short
		itracer.debug("internalSendAPDU", "Case 3");
		caseAPDU = 3;
	    } else if ((le + 5 + 1) == lenbuf) {
		// Case 4 short
		itracer.debug("internalSendAPDU", "Case 4");
		caseAPDU = 4;
		le = convertByte(apduCommand[apduCommand.length - 1]);
		apduCommand = new byte[lenbuf - 1];
		System.arraycopy(capdu.getBuffer(),
				 0,
				 apduCommand,
				 0, 
				 lenbuf - 1);
	    } else {
		throw new CardTerminalException("Did not recognize the APDU command");
	    } // if (Determine which type of Exchange)

	    // T=0 transmition (first command)
	    itracer.debug("internalSendAPDU", "Send APDU Command");
	    responseData = pcsc.SCardTransmit(cardHandle, apduCommand);
	    itracer.debug("internalSendAPDU", HexString.hexify(responseData));
	    
	    checkNonNullResponse(responseData);

	    // main switch block for cases 2 and 4
	    switch (caseAPDU) {
		
	    case 2: {
		itracer.debug("internalSendAPDU", 
			      "case 2  status returned" 
			      + responseData[responseData.length - 2]);
		
		// if 6C
		if (responseData[responseData.length - 2] == (byte) 0x6c) {

		    // received 0x6C Need to reissue the same command
		    // with La found responseData.
		    // responseData   = sw1 sw2
		    int     la_length = 
			convertByte(responseData[responseData.length - 1]);
		    
		    if (la_length == 0) {
			la_length = 256;
		    }
		    
		    int     temp_le = this.convertByte(le);
		    
		    if (temp_le == 0) {
			temp_le = 256;
		    }
		    
		    apduCommand[4] = 
			(byte) responseData[responseData.length - 1]; // P3
		    
		    // T=0 transmition (command w/ La)
		    responseData = pcsc.SCardTransmit(cardHandle, 
						      apduCommand);
		    
		    itracer.debug("internalSendAPDU", 
				  HexString.hexify(responseData));
		    
		    checkNonNullResponse(responseData);
		    
		    itracer.debug("internalSendAPDU", 
				  "le "
				  + HexString.hexify(temp_le) 
				  + "la "
				  + HexString.hexify(la_length));

		    if (temp_le < la_length) {

			byte[]       buffer = new byte[temp_le + 2];
			
			System.arraycopy(responseData, 0, buffer, 0, 
					 temp_le);
			System.arraycopy(responseData, 
					 responseData.length - 2, buffer, 
					 temp_le, 2);
			
			responseData = new byte[buffer.length];
			
			// System.arraycopy(buffer, buffer.length, responseData, responseData.length - 2, buffer.length);
			System.arraycopy(buffer, 0, responseData, 0, 
					 buffer.length);
			
		    } // if (temp_le < la_length)

		} // if (6C)

		break;
	    } // case 2
	    
	    case 4: {
		itracer.debug("internalSendAPDU",
			      "Case 4  + ISO TPDU support :  "
			      + TPDU_Uses_ISO
			      + "  status returned by the card"
			      + responseData[responseData.length - 2]);
		
		/* Note: Some smartcard are not fully compatible
		   with ISO normatives in case short 4.
		   So when a card returns a 0x9000 to inform a good
		   transfer of the APDU command then the terminal
		   have to terminate the transaction and it shall
		   return the sw1 sw2 to the user. In the case of
		   fully ISO, the terminal sends a get response
		   to extract the "le" bytes requested inside
		   the APDU command. */
		
		// if 61 etc.
		if (responseData[responseData.length - 2] == (byte) 0x61
		    || (responseData[responseData.length - 2] == (byte) 0x90 
			&& TPDU_Uses_ISO)
		    || responseData[responseData.length - 2] == (byte) 0x9F) {
		    
		    byte[]  apduCommand1 = new byte[5];
		    
		    apduCommand1[0] = apduCommand[0];
		    apduCommand1[1] = (byte) 0xC0; // INS (Get Response)
		    apduCommand1[2] = (byte) 0x00; // P1
		    apduCommand1[3] = (byte) 0x00; // P2
		    
		    // init the p3 with the Le
		    int     temp_le = this.convertByte(le);
		    
		    if (le == 0) {
			temp_le = 256;
		    }
		    
		    // Default Case Le is requested in the get response
		    apduCommand1[4] = (byte) le;
		    
		    // verify if we have La < Le in the case of sw2 = 0x61
		    if (responseData[responseData.length - 2] != (byte) 0x90) {
			if (convertByte(responseData[responseData.length - 1])
			    < temp_le) {
			    // La is requested in the get response
			    apduCommand1[4] = 
				(byte) responseData[responseData.length - 1];
			}
		    }
		    
		    // T=0 transmition (command w/ Le)
		    responseData = pcsc.SCardTransmit(cardHandle, 
						      apduCommand1);
		    
		    checkNonNullResponse(responseData);
		    
		    // if 6C
		    if (responseData[responseData.length - 2] == (byte) 0x6c) {
			
			// receive 0x6C Need to reissue the same command
			// with La found responseData.
			// responseData   = sw1 sw2
			int  la_length = 
			    convertByte(responseData[responseData.length - 1]);
			
			temp_le = this.convertByte(le);
			
			if (temp_le == 0) {
			    temp_le = 256;
			}
			
			apduCommand1[4] = 
			    (byte) responseData[responseData.length - 1]; // P3
			
			// T=0 transmition (command w/ La)
			responseData = pcsc.SCardTransmit(cardHandle, 
							  apduCommand1);
			
			checkNonNullResponse(responseData);
			
			if (temp_le < la_length) {

			    byte[]    buffer = new byte[le + 2];
			    
			    System.arraycopy(responseData, 0, buffer, 0, 
					     temp_le);
			    System.arraycopy(responseData, 
					     responseData.length - 2, 
					     buffer, temp_le, 2);
			    
			    responseData = new byte[buffer.length];
			    
			    System.arraycopy(buffer, buffer.length, 
					     responseData, 
					     responseData.length - 2, 
					     buffer.length);

			} // if (temp_le < la_length)

		    } // if (6C)

		} // if (61 etc.)
		
		break;
	    } // case 4
	    
	    } // switch (main switch block for cases 2 and 4)

	} // if (if T=1 or else T=0)
	
    } catch (PcscException e) {
      
	// check for SemaphoreTimeout
	if (e.returnCode() == 0x79) {
	    // try a card reset (reconnect) with timeout 5 seconds
	    internalReset(0,5000);
	    throw new CardTerminalException("PC/SC Error: semaphore timeout - perhaps forbidden or wrong card command.");
	} else {
	    throw translatePcscException(e);
	}

    } // try (main transmition block)

    ResponseAPDU rAPDU = new ResponseAPDU(responseData);
    itracer.debug("internalSendAPDU", "receiving " + rAPDU);
    return rAPDU;
}


  /** Signal to observers that an inserted card was removed.<p>
   *
   * @param slot
   *        slot number
   */
  protected void cardRemoved(int slotID) {

    super.cardRemoved(slotID);

    try {
      cardDisconnect(Pcsc10Constants.SCARD_LEAVE_CARD);
    } catch (CardTerminalException cte) {
      // ignore this exception
    }
  }

  /** This method is normally used by the <tt>CardTerminalRegistry</tt> to
   *  generate the <tt>OpenCard</tt> events if the Slot implementation does
   *  not support events itself.
   */
  public void poll() 
    throws CardTerminalException {

    if (!closed) {
      UpdateCardStatus(0);
      try {
        boolean newStatus = isCardPresent(0);
        if (cardInserted != newStatus) {
          itracer.debug("poll", "status change");
          cardInserted = !cardInserted;
          // ... notify listeners
          if (cardInserted) {
            cid = new CardID(this, 0, cachedATR); // U.Steinmueller Infineon
            cardInserted(0);
          } else {
            cardRemoved(0);
	    cachedATR = null;        // invalidate ATR
	    cid = null;              // invalidate CardID
          }
        } else {
          // ... no change took place
          itracer.debug("poll", "no status change");
        }
      }
      catch (CardTerminalException cte) {
        itracer.debug("poll", cte);

        // make sure the CardTerminalException is 
        // propagated to listeners waiting for a card
        cardInserted(0);
      }
    }
  }


  /** translate the PcscException into CardTerminalException.<p>
    */
  protected CardTerminalException translatePcscException(PcscException e) {
    return new CardTerminalException("Pcsc10CardTerminal: " + e.getMessage(), this);
  }

    /**
     * convertByte
     */
    protected int convertByte(int byteToConvert) {
	int       e = byteToConvert & 0x7f;

	if ((byteToConvert & 0x80) == 0x80) {
	    e += 128;
	}

	return e;
    }

    /**
     * Returns TPDU ISO mode.
     *
     * @throws CardTerminalException
     */
    public boolean isISOTPDU() throws CardTerminalException {
	return TPDU_Uses_ISO;
    }

    /**
     * Set the TPDU to use ISO or not ISO mode.
     *
     * @throws CardTerminalException
     */
    public void setISOTPDUMode(boolean Uses_ISO_norm)
	throws CardTerminalException {

	TPDU_Uses_ISO = Uses_ISO_norm;

    }

}
