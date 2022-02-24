/*
 * Copyright © 2000 Gemplus
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

//-----------------------------------------------------------------------------
// PACKAGE DEFINITION
//-----------------------------------------------------------------------------
package com.gemplus.opencard.terminal.apduio;

//-----------------------------------------------------------------------------
// IMPORTS
//-----------------------------------------------------------------------------
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;
import opencard.core.util.HexString;

import com.sun.javacard.apduio.CadClient;
import com.sun.javacard.apduio.TLP224Exception;
import com.sun.javacard.apduio.Apdu;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Pure Java CardTerminal dedicated to send APDUs to the JavaCard Simulator
 * coming with the JavaCard Development Kit.
 * This CardTerminal is built on top of the Sun ApduIO classes
 *
 * @author Laurent Lagosanto (lago@research.gemplus.com)
 * @version $Id: ApduIOCardTerminal.java,v 0.1 2001/07/05 08:35:01 root Exp root $
 **/
public class ApduIOCardTerminal extends CardTerminal
    implements Pollable
{
  /**
   * debug utility
   */
  static Tracer ctracer = new Tracer(ApduIOCardTerminal.class);

  /**
   * used to send APDUs to the simulation VM.
   */
  protected CadClient cad;

  /**
   * cached CardID containing the answer to reset
   */
  protected CardID cardID;

  /**
   * true when the terminal is connected
   */
  protected boolean connected = false;

  /**
   * true when the terminal is opened
   */
  protected boolean opened = false;

  /**
   * name or adress of the machine running the simulator. (default = localhost)
   */
  protected String host = "localhost";

  /**
   * Listening port of the simulator.
   */
  protected int port = 9025;

	/**
	 * socket used to communicate with the simulator.
	 */
	protected Socket sock;

  /**
   * Instantiates a <tt>ApduIOCardTerminal</tt> object.
   * @param     name The friendly name of the terminal.
   * @param     type The terminal type
   * @param     address of the simulator host: "hostname:port"
   * @exception CardTerminalException when the instantiation fails for any reason.
   */
  protected ApduIOCardTerminal(String name, String type, String address)
    throws CardTerminalException
  {
    super(name, type, address);
    ctracer.info("<init>", "name|string|address = " + name + "|" + type + "|" + address);

    try
    {
      // decodes the adress:  <host>[:port]
      if (address != null && address.equals("") == false)
      {
        int pos = address.indexOf(":");
        if (pos > 0)
        {
          host = address.substring(0, pos);
          port = Integer.parseInt(address.substring(pos + 1));
        }
        else
        {
          // uses the default port
          host = new String(address);
        }
      }
      ctracer.debug("<init>", "created on " + host + ":" + port);
    }
    catch(Exception ex)
    {
      throw new CardTerminalException("Unable to instantiate ApduIOCardTerminal:\nName = "
                                       + name + "\nType = " + type
                                       + "\naddress = " + address
                                       + "\nException: " + ex
                                       + "\nwith message: " + ex.getMessage());
    }
    // only one slot in this terminal
    addSlots(1);
  }

  /**
   * open the terminal.
   * Does nothing except registering this terminal to the polling-list
   * @exception CardTerminalException never.
   */
  public synchronized void open()
  {
    ctracer.info("open", "open terminal: " + getName());
    ctracer.debug("open", "add terminal to polling-list");

    // add this terminal to polling-list
    CardTerminalRegistry.getRegistry().addPollable((Pollable)this);
    opened = true;
  }

  /**
   * disable the terminal
   * @exception CardTerminalException Thrown in case of errors during close process
   */
  public synchronized void close() throws CardTerminalException
  {
    ctracer.info( "close", "close terminal: " + getName() );
    ctracer.debug("close", "remove terminal from polling-list");
    // remove this terminal to polling-list
    CardTerminalRegistry.getRegistry().removePollable((Pollable)this);

		// power down the card, if inserted
    cardID = null;
    try
    {
      if(cad != null) // if inserted
      {
        cad.powerDown();
        sock.close();
        opened = false;
        ctracer.debug("close", "close cad");
      }
    }
    catch(IOException ioex)
    {
      ctracer.debug("close", "IOException in close:\nMessage: " + ioex.getMessage());
    }
    catch(TLP224Exception tex)
    {
    throw new CardTerminalException("TLP224Exception in close:\nMessage: " + tex.getMessage());
    }
  }

  /**
   * Check whether a smart card is present in a particular slot.
   * @param     slotID slot to check for a card.
   * @return    true if connected to a simulator
   * @exception IndexOutOfBoundsException when the slotID is different from 0
   */
  public synchronized boolean isCardPresent(int slotID) throws CardTerminalException
  {
  	ctracer.info("isCardPresent", "isCardPresent(" + slotID + " on " + getName() + "...");
  	if (slotID == 0)
    {
    	return connected;
    }
    else
    {
    	throw new IndexOutOfBoundsException("Wrong slotID: only 0 is allowed.");
    }
  }

  /**
   * Return the ATR of the card inserted in the specified slot.
   * @param     slotID slot id.
   * @return    The CardID containing the ATR.
   * @exception CardTerminalException in case of communication problems.
   * @exception IndexOutOfBoundsException when the slotID is different from 0
   */
  public synchronized CardID getCardID(int slotID) throws CardTerminalException
  {
    ctracer.info("getCardID", "getCardID(" + slotID + " on " + getName() + "...");
    if (isCardPresent(slotID))
    {
      try
      {
      	// powerUp the card
        byte[] atr = cad.powerUp();
        ctracer.debug("getCardID", "powered Up");
        // get the ATR
        cardID = new CardID(this,slotID,atr);
      }
      catch (TLP224Exception tex)
      {
      	throw new CardTerminalException("TLP224Exception in getCardID: \n"
                                        + "Message: " + tex.getMessage());
      }
      catch (IOException ioex)
      {
				throw new CardTerminalException("IOException in getCardID: " +
				                               "Message: " + ioex.getMessage());
      }
    }
    return cardID;
  }


  /**
   * Updates the card inserted/removed state.
   */
  public synchronized void poll()
  {
    ctracer.info("poll", "polling " + getName() + "...");
    if (opened == true && connected == false)
    {
      try
      {
        // debug information
        ctracer.debug("poll", "simulator socket connection");
        // try to open the socket
        sock = new Socket(host, port);
        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
        cad = new CadClient(in, out);
        ctracer.debug("poll", "CadClient created");
        connected = true;
        // connection succeded: notify listeners
        cardInserted(0);
      }
      catch(IOException e)
      {
        // debug information
        ctracer.debug("poll", "socket connection failed");
        sock = null;
        connected = false;
      }
    }
    else
    {
      // already connected
      try
      {
        // verify connection using powerUp
        // works only because cad.powerUp doesn't do a reset
        this.cardID = new CardID(cad.powerUp());
      }
      catch(TLP224Exception tex)
      {
        // debug information
        ctracer.debug("poll", "TLP224Exception " + tex.getMessage());
        connected = false;
        cardRemoved(0);
      }
      catch(IOException e)
      {
        // debug information
        ctracer.debug("poll", "socket connection failed");
        sock = null;
        connected = false;
        cardRemoved(0);
      }
    }
  }

  /**
   * re-powers up the card, and retreives the ATR.
   *
   * @param     slotID  the slot number of the slot used.
   * @param     ms A timeout in milliseconds. (ignored)
   * @return    The CardID containing the ATR.
   * @exception CardTerminalException if there is a problem during reset.
   */
  protected synchronized CardID internalReset(int slotID, int ms) throws CardTerminalException
  {
	  ctracer.info("internalReset", "internalReset(" + slotID + ") on " + getName());
	  if (isCardPresent(slotID) == false)
	  {
    	// to force the power up
    	this.cardID = null;
    	return this.getCardID(slotID);
	  }
    else
    {
	    throw new CardTerminalException("no card inserted");
    }
  }

  /**
   * sends APDU commands
   * @param     slotID The slot number of the slot used.
   * @param     capdu The <tt>CommandAPDU</tt> to send.
   * @param     ms A timeout in milliseconds. (ignored)
   * @return    the response to this APDU
   * @exception CardTerminalException if there is an error in apdu exchange
   */
  protected synchronized ResponseAPDU internalSendAPDU(int slotID, CommandAPDU capdu, int ms)
    throws CardTerminalException
  {
    ctracer.info("internalReset", "internalReset(" + slotID + ") on " + getName());

		if (this.isCardPresent(slotID))
    {
	    // map CommandAPDU into Apdu object
	    Apdu apdu = new Apdu();
	    ctracer.debug("internalSendAPDU", "capdu: "+ HexString.hexify(capdu.getBytes()));

	    // build the header
	    byte[] cmd = apdu.getCommand();
	    cmd[Apdu.CLA] = (byte) capdu.getByte(0);
	    cmd[Apdu.INS] = (byte) capdu.getByte(1);
	    cmd[Apdu.P1]  = (byte) capdu.getByte(2);
	    cmd[Apdu.P2]  = (byte) capdu.getByte(3);

	    int Lc = 0;
	    int Le = 0;

	    if (capdu.getLength() == 4)
	    {
	      // no Lc, no Le
	    }
	    else if (capdu.getLength() == 5)
	    {
	      // Le, no Lc
	      Le = capdu.getByte(4);
	    }
	    else
	    {
	      Lc = capdu.getByte(4);
	      if (capdu.getLength() == 5 + Lc)
	      {
	        // Lc, no Le
	      }
	      else
	      {
	        // Lc, Le
	        Le = capdu.getByte(capdu.getLength()-1);
	      }
	    }
	    apdu.Le = Le;
	    ctracer.debug("internalSendAPDU", "cmd : "+ HexString.hexify(cmd));

	    // data buffer
      if (Lc > 0)
      {
		    byte[] dataIn = new byte[Lc];
		    System.arraycopy(capdu.getBuffer(), 5, dataIn, 0, dataIn.length);
		    apdu.setDataIn(dataIn);
	      ctracer.debug("internalSendAPDU", "data: "+ HexString.hexify(dataIn));
      }

			// apdu exchange
	    try
	    {
	      this.cad.exchangeApdu(apdu);
	    }
      catch(IOException ioex)
	    {
	      throw new CardTerminalException("IOException in internalSendAPDU:\nMessage: " + ioex.getMessage());
	    }
      catch(TLP224Exception tex)
	    {
	      throw new CardTerminalException("TLP224Exception in internalSendAPDU:\nMessage: " + tex.getMessage());
	    }
	    // ResponseAPDU creation and setting
	    byte[] resp = new byte[apdu.getLe()+ 2];
	    System.arraycopy(apdu.getDataOut(),0,resp,0,apdu.getLe());
	    System.arraycopy(apdu.getSw1Sw2(),0,resp,apdu.getLe(),2);
	   // debug information
	    ctracer.debug("internalSendAPDU", "response: "+ HexString.hexify(resp));
	    // debug information
	    ctracer.debug("internalSendAPDU", "got response: " + resp);
	    return new ResponseAPDU(resp);
    }
	  else
	  {
	    throw new CardTerminalException("no card inserted");
	  }
  }
}
