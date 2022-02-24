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

package com.gemplus.opencard.terminal;

import java.io.*;
import java.util.Properties;
import javax.comm.*;

import opencard.core.terminal.*;
import opencard.core.util.*;

import opencard.opt.terminal.protocol.*;
import opencard.opt.terminal.TerminalCommand;
import opencard.opt.terminal.PowerManagementInterface;
import opencard.opt.terminal.UserInteraction;

/**
 * PureJava-Implementation of an OpenCard <code>CardTerminal</code> for
 * GemPlus serial reader (Oros or GemCore OS) using the javax.comm package.
 *
 * IMPORTANT: with jdk1.1 javax.comm works only with signed applets  !!!
 *
 * @version $Id: GemplusSerialCardTerminal.java,v 3.1 1999/06/29 10:54:26 root Exp root $<br>
 * @author Stephan Breideneich (sbreiden@de.ibm.com)<br>
 * Patched by dave.durbin@jcp.co.uk (Mon, 30 Nov 1998) for GemCore compatibility<br>
 * Patched by Patrick.Biget@gemplus.com (Thu, 3 Dec 1998) -> com.gemplus zone<br>
 * Patched by Gilles.Pauzie@gemplus.com (Mon, 11 Jan 1999) New features, see<br>
 *  README file<br>
 * Patched by Gilles.Pauzie@gemplus.com (Mon, 18 Jan 1999)<br>
 *    <ul>
 *    <li> Renamed to GemplusSerialCardTerminal an abstract class for Gemplus
 *      serial readers (Oros and GemCore based readers).
 *    <li> Supports multi-slots for GemCore readers.
 *    <li> Implements TerminalCommand, PowerManagementInterface and
 *         UserInteraction interfaces.
 *    </ul><br>
 * Patched by Gilles.Pauzie@gemplus.com (Tue, 9 Feb 1999) Bug fixed for
 *   cards only using T=1 protocol<br>
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.core.terminal.CardTerminalRegistry
 * @see opencard.core.terminal.CardTerminalFactory
 * @see opencard.core.terminal.Slot
 * @see opencard.core.terminal.CardID
 * @see opencard.opt.terminal.TerminalCommand
 * @see opencard.opt.terminal.PowerManagementInterface
 * @see opencard.opt.terminal.UserInteraction
 */

abstract class GemplusSerialCardTerminal extends CardTerminal
    implements Pollable ,
               TerminalCommand,
               PowerManagementInterface,
               UserInteraction {


  private static Tracer ctracer = new Tracer(GemplusSerialCardTerminal.class);

  /** time in milliseconds to block waiting for port open */
  private final int PORT_TIMEOUT = 5;

  /** initial baudrate between host and reader */
  private final int PORT_BAUDRATE = 9600;

  /** defines block waiting time in milliseconds for receiving a T1 block */
  private final int DEFAULT_TERMINAL_TIMEOUT = 5000;

  /** defines block waiting time in milliseconds for exchange with risks */
  private final int LOW_TERMINAL_TIMEOUT = 500;

  /** T1 protocol: used address for host device */
  private final int HOST_ID = 2;

  /** T1 protocol: used address for reader device */
  private final int TERMINAL_ID = 4;

  /** if set in slot state */
  private final int STATE_CARD_INSERTED = 0x04;

  /** serial port for communication with terminal */
  private SerialPort serPort = null;

  /** data from terminal */
  private InputStream serIn = null;

  /** data to terminal */
  private OutputStream serOut = null;

  /** is a card currently inserted? */
  private boolean[] cardInserted = {false,false};

  /** is the terminal closed? */
  private boolean readerClosed = true;

  /** implementation of the protocol handler */
  private GemplusBlockProtocol protocol = null;

  /** cached ATR (no card inserted: cachedATR == null) */
  private byte[][] cachedATR = {null,null};

  /** cached state of the slot */
  private int[] cachedCardStatus = {0,0};

  /** protocol of the card */
  private int[] usedCardProtocol = {-1,-1};

  /** firmware version of used reader */
  private String firmwareVersion;

  /** current baud rate of the reader */
  private int baudRate;


  /** Public and protected methods:=============================================
  **/

  /**
   * Instantiates a <tt>GemplusSerialCardTerminal</tt> object.
   *
   * @param     name
   *            The user friendly name.
   * @param     type
   *            The terminal type.
   * @param     serialDevice
   *            The device name used for reader communication (e.g. COM1).
   * @exception CardTerminalException
   *            Thrown when the instantiation fails for any reason.
   */
  protected GemplusSerialCardTerminal(String name, String type, String serialDevice)
    throws CardTerminalException {

    super(name, type, serialDevice);

  }


  /**
   * open serial port and init transfer protocol
   *
   * @param     firmware
   *            The string OS firmware to check
   * @exception CardTerminalException
   *            Thrown in case of errors during init process.
   */
  public abstract void open() throws CardTerminalException;
  protected void open(String firmware) throws CardTerminalException {

    boolean bSuccess;

    CommPortIdentifier portId = null;

    try {

      ctracer.info("open", "open terminal: " + getName());

      // open serial port, application name is this terminalname
      ctracer.debug("open", "opening serial port " + getAddress());
      portId = CommPortIdentifier.getPortIdentifier(getAddress());
      serPort = (SerialPort)portId.open(getName(), PORT_TIMEOUT);

      // config port parameters
      baudRate = 9600;
      ctracer.debug("open", "set port to " + PORT_BAUDRATE + " baud with 8N1");
      serPort.setSerialPortParams(baudRate,
                                  SerialPort.DATABITS_8,
                                  SerialPort.STOPBITS_1,
                                  SerialPort.PARITY_NONE);

      serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      serPort.enableReceiveThreshold(1);
      serPort.enableReceiveTimeout(PORT_TIMEOUT);

      // setup streams
      serOut = serPort.getOutputStream();
      serIn = serPort.getInputStream();

      // create Protocol Class
      ctracer.debug("open", "Initiate protocol for reader communication"
                            + "\nhost   = " + HOST_ID
                            + "\nreader = " + TERMINAL_ID);

      protocol = new GemplusBlockProtocol(HOST_ID, TERMINAL_ID,
                                          LOW_TERMINAL_TIMEOUT,
                                          serIn, serOut);
      // try to communicate with the reader (try different baud rate)
      do {
        bSuccess = true;
        try {
          protocol.open();
        }catch (T1Exception e){
          bSuccess = false;
          if (baudRate == 9600) { baudRate = 19200;}
          else { baudRate = 38400;}
        }
        if (bSuccess == true){ break;}
        ctracer.debug("open", "new baud rate= " + baudRate);
        serPort.setSerialPortParams(baudRate,
                                  SerialPort.DATABITS_8,
                                  SerialPort.STOPBITS_1,
                                  SerialPort.PARITY_NONE);
      } while(baudRate < 38400);

      if (!bSuccess) {
        throw new CardTerminalException("No reader found!");
      }
      // get firmware version from the connected reader
      firmwareVersion = null;
      ctracer.debug("open", "get firmware version from reader");
      firmwareVersion = getFirmwareVersion();
      ctracer.debug("open", "reader firmware: " + firmwareVersion);
      ctracer.debug("open", "reader baud rate: " + baudRate);

      // check the firmware version
      if (! firmwareVersion.regionMatches(0,firmware,0,firmware.length())) {
        throw new CardTerminalException("Invalid reader!");
      }

      // set the default timeout value
      protocol.setBlockWaitingTime(DEFAULT_TERMINAL_TIMEOUT);

      // select ISO card driver in the reader
      ctracer.debug("open", "select ISO card driver");
      selectISOCard();

      // disable TLP mode (and ROS mode also)
      ctracer.debug("open", "disable TLP mode");
      disableTLPMode();

      // terminal is ready for use
      setReaderClosed(false);

      // is card already inserted? check...
      UpdateCardStatus(0);

      // add this terminal to polling-list
      ctracer.debug("open", "add terminal to polling-list");
      CardTerminalRegistry.getRegistry().addPollable((Pollable)this);

    // remap Exception to CardTerminalException
    } catch (Exception e) {
      throw new CardTerminalException(e.toString());
    }
  } // open


  /**
   * close serial port and disable the terminal
   *
   * @exception CardTerminalException
   *            Thrown in case of errors during close process
   */
  public void close() throws CardTerminalException {
  int index = 0;

    ctracer.info("close", "close " + getName());

    // check if already closed...
    if (!getReaderClosed()) {

      ctracer.debug("close", "remove terminal from polling list");
      try {

        for (index=0;index < slots.size();index++) {
          // power down the card
          ctracer.debug("close", "powerDownCard");
          powerDownCard(index);
        }
        CardTerminalRegistry.getRegistry().removePollable((Pollable)this);

        // set closed flag
        setReaderClosed(true);

        // close protocol
        ctracer.debug("close", "close communication protocol");
        protocol.close();

        // close the streams
        ctracer.debug("close", "close serial in- and output streams");
        serIn.close();
        serOut.close();

        // close the serial port
        ctracer.debug("close", "close serial port");
        serPort.close();

      // remap IOException to CardTerminalException
      } catch (IOException e) {

        throw new CardTerminalException(e.toString());
      }
    }
  } // close


  /** Check whether there is a smart card present in a particular slot.
   *
   * @param     slotID
   *            slot to check for a card.
   * @return    True if there is a smart card inserted in the slot.
   */
  public boolean isCardPresent(int slotID) throws CardTerminalException {
    return ((getCardStatus(slotID) & STATE_CARD_INSERTED)
             == STATE_CARD_INSERTED)
           ? true
           : false;
  }


  /** Return the answer-to-reset (ATR) response of the card inserted in slot
   *  <tt>slotID</tt> as a <tt>CardID</tt>.
   *
   * @param     slotID
   *            slot id.
   * @return    The ATR response in form of a <tt>CardID</tt> object.
   * @exception CardTerminalException
   *            Thrown in case of problems in the card terminal.
   */
  public CardID getCardID(int slotID) throws CardTerminalException {
    return getCardID(slotID, DEFAULT_TERMINAL_TIMEOUT);
  }


  /** Return the answer-to-reset (ATR) response of the card inserted in slot
   *  <tt>slotID</tt> as a <tt>CardID</tt>.
   *
   * @param     slotID
   *            slot id.
   * @param     ms
   *            A timeout in milliseconds.
   * @return    The ATR response in form of a <tt>CardID</tt> object.
   * @exception CardTerminalException
   *            Thrown in case of problems in the card terminal.
   */
  public CardID getCardID(int slotID, int ms) throws CardTerminalException {

    CardID cardID = null;
    byte[] cardStatus;

    ctracer.info("getCardID", "get the CardID");

    // check if card is inserted
    if ((getCardStatus(slotID) & STATE_CARD_INSERTED)
            == STATE_CARD_INSERTED) {

      // check if card is powered (==> cachedATR != null)
      if (cachedATR[slotID] == null) {

        ctracer.debug("getCardID",
                      "Cached CardID not available - request ATR from reader.");

        ctracer.debug("getCardID", "power up card and get ATR");

        cardID = new CardID(getSlot(slotID), powerUpCard(slotID));

        ctracer.debug("getCardID", "return CardID from new ATR: " + cardID);

      } else {
        ctracer.debug("getCardID",
                      "return CardID from cached ATR: " + cardID);

        cardID = new CardID(getSlot(slotID), cachedATR[slotID]);
      }

    } else {
      ctracer.debug("getCardID", "no card inserted - invalidate cached ATR.");
      cachedATR[slotID] = null;
    }

    return cardID;

  } // getCardID

  /**
   * power up card (with reset): Abstract must be defined in the specific
   *                             reader classe
   *
   * @param     slotID
   *            slot number with the inserted card
   * @exception CardTerminalException
   *            Thrown when power up method failed.
   */
  protected abstract byte[] powerUpCard(int slotID) throws CardTerminalException;
  /**
   * power up card
   *
   * @param     slotID
   *            slot number with the inserted card
   * @param     timeout
   *            The time to wait before returning (in seconds);
   *            -1 signals an indefinite timeout (i.e., wait forever).
   * @exception CardTerminalException
   *            Thrown when there is no card present or a timeout occurs.
   */
  public void powerUpCard(int slotID,int timeout)
                  throws CardTerminalException {
    protocol.setBlockWaitingTime(timeout);
    powerUpCard(slotID);
    protocol.setBlockWaitingTime(DEFAULT_TERMINAL_TIMEOUT);
  }

  /**
   * power up card (with reset)
   *
   * @param     slotID
   *            slot number with the inserted card
   * @param     powerUpRequest
   *            the command to send to the reader
   * @exception CardTerminalException
   *            Thrown when power up method failed.
   */
  protected byte[] powerUpCard(int slotID,byte[] powerUpRequest)
          throws CardTerminalException {

    // power down the card to avoid a Warm reset
    ctracer.debug("powerUpCard", "powerDownCard");
    powerDownCard(slotID);

    byte[] rcvData  = null;
    byte[] ATR      = null;
    int    lenATR   = 0;

    ctracer.debug("powerUpCard", "power-up card");

    // invalidate cached ATR
    cachedATR[slotID] = null;

    /*
     * - send power up command sequence to the reader
     * - catch and remap T1Exception to CardTerminalException
     */
    try {
      rcvData = protocol.transmit(HOST_ID, TERMINAL_ID, powerUpRequest);
    } catch (T1Exception t1e) {
      System.out.println("Exception sending POWER_UP_CARD instruction");
      throw new CardTerminalException(t1e.toString());
    }
    ctracer.debug("powerUpCard", "rcData length = " + rcvData.length);

    UpdateCardStatus(slotID);

    if (rcvData != null) {

      if (rcvData[0] == 0) {             // no errorcode from reader

          ctracer.debug("powerUpCard", HexString.hexify(rcvData));
          // calc whole length of ATR
          lenATR = rcvData.length - 1;

          // copy the ATR
          ATR = new byte[lenATR];
          System.arraycopy(rcvData, 1, ATR, 0, lenATR);
      } else {
        throw new CardTerminalException("got errorcode "
                  + HexString.hexify(rcvData[0]) + "h from card reader");
      }   
    } else {
        throw new CardTerminalException("no response from card reader");
    }

    cachedATR[slotID] = ATR;
    UpdateCardProtocol(slotID);

    return ATR;
  } // powerUpCard


  /**
   * power down card: Abstract must be defined in the specific
   *                             reader class
   *
   * @param     slotID
   *            slot number with the inserted card
   * @exception CardTerminalException
   *            Thrown when power down method failed.
   */
  protected abstract void powerDownCard(int slotID)throws CardTerminalException;
  /**
   * power down card
   *
   * @param     slotID
   *            slot number with the inserted card
   * @param     timeout
   *            The time to wait before returning (in seconds);
   *            -1 signals an indefinite timeout (i.e., wait forever).
   * @exception CardTerminalException
   *            Thrown when power down method failed.
   */
  public void powerDownCard(int slotID,int timeout)
                  throws CardTerminalException {

    protocol.setBlockWaitingTime(timeout);
    powerDownCard(slotID);
    protocol.setBlockWaitingTime(DEFAULT_TERMINAL_TIMEOUT);
  }
  /**
   * power down card
   *
   * @param     slotID
   *            slot number with the inserted card
   * @exception CardTerminalException
   *            Thrown when there is no card present or a timeout occurs.
   */
  protected void powerDownCard(int slotID,byte[] cmdPowerDown)
    throws CardTerminalException {


    byte[] rcvData  = null;

    ctracer.debug("powerDownCard", "power-down card");
    /*
     * - send power down command sequence to the reader
     * - catch and remap T1Exception to CardTerminalException
     */
    try {
      rcvData = protocol.transmit(HOST_ID, TERMINAL_ID, cmdPowerDown);
    }catch (T1Exception t1e) {
      System.out.println("Exception sending POWER_DOWN_CARD instruction");
      throw new CardTerminalException(t1e.toString());
    }
  }



  /**
   * Checks the status of this reader.
   *
   * @exception CardTerminalException
   *            Thrown whenn error occurred within poll mechanism.
   */
  public void poll() throws CardTerminalException {
  int index = 0;

    if (!getReaderClosed()) {

      ctracer.info("poll", "poll " + getName() + "...");

      // update card status
      ctracer.debug("poll", "Update internal card status information.");
      for (index=0;index < slots.size();index++) {
        UpdateCardStatus(index);

        if (!cardInserted[index]) {

          /*
           * internal state: no card inserted
           */

          if (isCardPresent(index)) {
            cardInserted[index] = true;

            ctracer.debug("poll", "Card found in slot " + index);

            // notify listeners
            ctracer.debug("poll", "notify CTListeners about inserted card");
            cardInserted(index);
          }

        } else {

          /*
           * internal state: card inserted
           */

          if (!isCardPresent(index)) {

            ctracer.debug("poll",
                          "Card removed - invalidate cached informations.");

            // invalidate cached informations
            cardInserted[index] = false;
            cachedATR[index] = null;

            // notify listeners
            ctracer.debug("poll", "notify CTListeners about removed card");
            cardRemoved(index);
          }

        }
      }
    }
  } // poll


  /** The <tt>CardTerminal</tt> internal <tt>features()</tt> method
   * to be provided by this implementation.
   *
   * @param     features
   *            A <tt>Properties</tt> object that needs to be enhanced
   *            with the card terminal specific features.
   * @return    features
   */
  public Properties internalFeatures(Properties features) {
    return features;
  }


  /** The internal reset method to be provided by the concrete implementation.
   *
   * @param     slot
   *            The slot number of the slot to be resetted.
   * @param     ms
   *            A timeout in milliseconds.
   * @return    The <tt>CardID</tt> of the card.
   *
   * @exception CardTerminalException
   *            Thrown when error occurred during reset.
   */
  public CardID internalReset(int slotID, int ms)
          throws CardTerminalException {

    ctracer.debug("internalReset", "not yet implemented");

    return null;
  }


  /** The <tt>internalSendAPDU</tt> : an abstract method. Must be defined in
   *                                  the specific reader class
   *
   * @param     slot
   *            The slot number of the slot used.
   * @param     capdu
   *            The <tt>CommandAPDU</tt> to send.
   * @param     ms
   *            A timeout in milliseconds.
   * @return    A <tt>ResponseAPDU</tt>.
   */
  public abstract ResponseAPDU internalSendAPDU(int slotID,CommandAPDU capdu,int ms)
          throws CardTerminalException;
  /** The <tt>internalSendAPDU</tt> : implementation of T=0 and T=1 APDU command
   *                                 (according ISO/IEC 7816-4 1995)
   *
   * @param     slot
   *            The slot number of the slot used.
   * @param     capdu
   *            The <tt>CommandAPDU</tt> to send.
   * @param     ms
   *            A timeout in milliseconds.
   * @param     cmdIsoIn
   *            cmd reader for an Iso In (T=0) command.
   * @param     cmdIsoOut
   *            cmd reader for an Iso Out (T=0) command.
   * @param     cmdApdu
   *            cmd reader for an Apdu (T=1) command.
   * @return    A <tt>ResponseAPDU</tt>.
   */
  protected ResponseAPDU internalSendAPDU(int slotID,CommandAPDU capdu,int ms,
          byte cmdIsoIn, byte cmdIsoOut, byte cmdApdu)
          throws CardTerminalException {

    ResponseAPDU rAPDU = null;
    int timeout = 0;

    byte[] sendAPDU = null;
    byte[] sendCmd = null;
    byte[] tmpReceiveBuf = null;
    byte[] receiveBuf = null;
    byte finalLength,originalLength;

    // translate maximum timeout (-1) to DEFAULT_TERMINAL_TIMEOUT
    timeout = (ms == -1) ? DEFAULT_TERMINAL_TIMEOUT : ms;

    // set the timeout value
    protocol.setBlockWaitingTime(timeout);

    ctracer.debug("internalSendAPDU", "sending " + capdu);

    try {

      // assemble exchange command for reader
      sendAPDU = capdu.getBytes();

      // check the used card protocol and setup the right exchange command
      if (getUsedCardProtocol(slotID) == 1) {// T1 used...
        ctracer.debug("internalSendAPDU", "T=1 protocol");
        sendCmd = new byte[sendAPDU.length + 1];
        sendCmd[0] = cmdApdu; // reader command for an Apdu command
        // add the APDU command after the reader command
        System.arraycopy(sendAPDU, 0, sendCmd, 1, sendAPDU.length);
        // send the command
        tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
        if (tmpReceiveBuf == null)
          throw new CardTerminalException("no response from reader");
        if (tmpReceiveBuf.length - 1 < 2)
          throw new CardTerminalException("no response from smartcard");

        // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
        receiveBuf = new byte[tmpReceiveBuf.length - 1];
        System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);

      } else {                   // T0 used...
        ctracer.debug("internalSendAPDU", "T=0 protocol");
        // case 1 ==============================================================
        if (sendAPDU.length == 4) {
          ctracer.debug("internalSendAPDU", "T=0 case 1 ");
          sendCmd = new byte[sendAPDU.length + 2];
          sendCmd[0] = cmdIsoIn; // reader command for an Iso In command
          sendCmd[5] = (byte)0x00; // P3 = 0x00

          // add the APDU command after the reader command
          System.arraycopy(sendAPDU, 0, sendCmd, 1, sendAPDU.length);
          // send the command
          tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
          if (tmpReceiveBuf == null)
            throw new CardTerminalException("no response from reader");
          if (tmpReceiveBuf.length - 1 < 2)
            throw new CardTerminalException("no response from smartcard");

          // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
          receiveBuf = new byte[tmpReceiveBuf.length - 1];
          System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);

        // case 2 ==============================================================
        }else if (sendAPDU.length == 5) {
          ctracer.debug("internalSendAPDU", "T=0 case 2 ");
          sendCmd = new byte[sendAPDU.length + 1];
          sendCmd[0] = cmdIsoOut; // reader command for an Iso out command
          // add the APDU command after the reader command
          System.arraycopy(sendAPDU, 0, sendCmd, 1, sendAPDU.length);
          // send the command
          tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
          if (tmpReceiveBuf == null)
            throw new CardTerminalException("no response from reader");
          if (tmpReceiveBuf.length - 1 < 2)
            throw new CardTerminalException("no response from smartcard");

          // test SW1
          switch (tmpReceiveBuf[tmpReceiveBuf.length - 2]) {
          case 0x6C: // case 2S.3 (Le rejected, La indicated in SW2)
            ctracer.debug("internalSendAPDU", "T=0 response case 2S.3 (Le rejected, La indicated in SW2)");
            // Le is updated with La
            originalLength = sendCmd[5];
            sendCmd[5] = tmpReceiveBuf[tmpReceiveBuf.length - 1];
            // send the new command
            tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
            if (tmpReceiveBuf == null)
              throw new CardTerminalException("no response from reader");
            if (tmpReceiveBuf.length - 1 < 2)
              throw new CardTerminalException("no response from smartcard");
            if (tmpReceiveBuf.length - 3 < originalLength ) {
              // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
              receiveBuf = new byte[tmpReceiveBuf.length - 1];
              System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
            } else {
              // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
              receiveBuf = new byte[originalLength + 2];
              System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, originalLength);
              // copy SW1 and SW2
              System.arraycopy(tmpReceiveBuf, tmpReceiveBuf.length - 3, receiveBuf, originalLength, 2);
            }
            break;

          case 0x67: // case 2S.2 (Le rejected definitely)
            ctracer.debug("internalSendAPDU", "T=0 response case 2S.2 (Le rejected definitely)");
          default:   // case 2S.1 (Le accepted) or other cases
            ctracer.debug("internalSendAPDU", "T=0 response other cases ");
            // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
            receiveBuf = new byte[tmpReceiveBuf.length - 1];
            System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
            break;

          }

        } else {
          // cases 3 and 4 =====================================================
          if ((sendAPDU[4] + 5) == sendAPDU.length)
            ctracer.debug("internalSendAPDU", "T=0 case 3");
          else
            ctracer.debug("internalSendAPDU", "T=0 case 4");
          sendCmd = new byte[sendAPDU.length + 1];
          sendCmd[0] = cmdIsoIn; // reader command for an Iso In command
          // add the APDU command after the reader command
          System.arraycopy(sendAPDU, 0, sendCmd, 1, sendAPDU.length);
          // send the command
          tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
          if (tmpReceiveBuf == null)
            throw new CardTerminalException("no response from reader");
          if (tmpReceiveBuf.length - 1 < 2)
            throw new CardTerminalException("no response from smartcard");

          if ((sendAPDU[4] + 5) == sendAPDU.length) { // case 3
            // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
            receiveBuf = new byte[tmpReceiveBuf.length - 1];
            System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
          } else { // case 4
            // test SW1 and SW2
            if ( ((tmpReceiveBuf[tmpReceiveBuf.length - 2] == 0x90) &&
                  (tmpReceiveBuf[tmpReceiveBuf.length - 1] == 0x00)) ||
                 (tmpReceiveBuf[tmpReceiveBuf.length - 2] == 0x61)
               )
            {
              ctracer.debug("internalSendAPDU", "T=0 response cases 4S.2 or 4S.3");
              // cases 4S.2 or 4S.3
              sendCmd = new byte[6];
              sendCmd[0] = cmdIsoOut; // reader command for an Iso Out command
              sendCmd[1] = sendAPDU[0]; // Class
              sendCmd[2] = (byte) 0xC0; // INS (Get Response)
              sendCmd[3] = (byte) 0x00; // P1
              sendCmd[4] = (byte) 0x00; // P2
              if (tmpReceiveBuf[tmpReceiveBuf.length - 2] == 0x61)
                sendCmd[5] =
                  sendAPDU[sendAPDU.length - 1] < tmpReceiveBuf[tmpReceiveBuf.length - 2] ? sendAPDU[sendAPDU.length - 1] : tmpReceiveBuf[tmpReceiveBuf.length - 2]; // Lex or La (the lower)
              else
                sendCmd[5] = sendAPDU[sendAPDU.length - 1]; // Lex
              // send the command
              tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
              if (tmpReceiveBuf == null)
                throw new CardTerminalException("no response from reader");
              if (tmpReceiveBuf.length - 1 < 2)
                throw new CardTerminalException("no response from smartcard");

              // test SW1
              switch (tmpReceiveBuf[tmpReceiveBuf.length - 2]) {
              case 0x6C: // case 2S.3 (Le rejected, La indicated in SW2)
                ctracer.debug("internalSendAPDU", "T=0 response case 2S.3 (Le rejected, La indicated in SW2)");
                // Le is updated with La
                originalLength = sendCmd[5];
                sendCmd[5] = tmpReceiveBuf[tmpReceiveBuf.length - 1];
                // send the new command
                tmpReceiveBuf = protocol.transmit(HOST_ID, TERMINAL_ID, sendCmd);
                if (tmpReceiveBuf == null)
                  throw new CardTerminalException("no response from reader");
                if (tmpReceiveBuf.length - 1 < 2)
                  throw new CardTerminalException("no response from smartcard");
                if (tmpReceiveBuf.length - 3 < originalLength ) {
                  // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
                  receiveBuf = new byte[tmpReceiveBuf.length - 1];
                  System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
                } else {
                  // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
                  receiveBuf = new byte[originalLength + 2];
                  System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, originalLength);
                  // copy SW1 and SW2
                  System.arraycopy(tmpReceiveBuf, tmpReceiveBuf.length - 3, receiveBuf, originalLength, 2);
                }
                break;

              case 0x67: // case 2S.2 (Le rejected definitely)
                ctracer.debug("internalSendAPDU", "T=0 response case 2S.2 (Le rejected definitely)");
              default:   // case 2S.1 (Le accepted) or other cases
                ctracer.debug("internalSendAPDU", "T=0 response case 2other cases ");
                // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
                receiveBuf = new byte[tmpReceiveBuf.length - 1];
                System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
                break;

              }

            } else {
              ctracer.debug("internalSendAPDU", "T=0 response case 4 others cases");
              // others cases
              // copy tmpReceiveBuf to receiveBuf (without first status code from reader)
              receiveBuf = new byte[tmpReceiveBuf.length - 1];
              System.arraycopy(tmpReceiveBuf, 1, receiveBuf, 0, receiveBuf.length);
            }

          }
        }
      }
      if (receiveBuf != null) {
        if (receiveBuf.length > 0)
          rAPDU = new ResponseAPDU(receiveBuf);
        else
          throw new CardTerminalException("no response from smartcard");
      }

    // remap T1Exception to CardTerminalException
    } catch (T1Exception e) {
      ctracer.debug("internalSendAPDU",
                    "T1Exception remapped to CardTerminalException");
      throw new CardTerminalException(e.toString());
    }

    ctracer.debug("internalSendAPDU", "got response: " + rAPDU);

    return rAPDU;
  } // internalSendAPDU


  /** gets the firmware version from the reader: must be defined by the reader
      classe
   */
  protected abstract String getFirmwareVersion()throws CardTerminalException;




  /**
   * gets string with informations about terminal configuration and firmware version.
   */
  public String toString() {
    return super.toString() + "\nfirmware version: " + firmwareVersion;
  }


  /** The <tt>commandTerminal</tt>
   * Send a command to the terminal.
   *
   * @param     cmd
   *            A byte[] containing the command to be send to the card terminal.
   *
   * @return    A byte[] with the result (null if no result is available).
   *
   * @exception CardTerminalException
   *            Thrown when the method failed.
  **/
  public byte[] sendTerminalCommand(byte[] cmd) throws CardTerminalException {

    try {
      return protocol.transmit(HOST_ID, TERMINAL_ID, cmd);
    // remap Exception to CardTerminalException
    } catch (Exception e) {
      throw new CardTerminalException(e.toString());
    }

  }

  /** The <tt>clearDisplay</tt>
   * Clear the terminal display's.
   *
  **/
  public void clearDisplay(){
    byte[] clearCmd = {(byte) 0x2A};
    byte[] response;

    try {
      response = protocol.transmit(HOST_ID, TERMINAL_ID, clearCmd);
    } catch (Exception e) {
      //Nothing to do
    }
  }

  /** The <tt>display</tt>
   * Display a message.
   *
   * @param     message
   *            the string to display
   *
  **/
  public void display(String message){
    byte[] messByte;
    byte[] response, displayCmd;
    int index;

    messByte = message.getBytes();

    try {
      displayCmd = new byte[2 + (message.length()<=16 ? message.length():16)];
      displayCmd[0] = (byte) 0x2B;
      displayCmd[1] = (byte) 0x80;
      for (index=0;
           index < (message.length()<=16 ? message.length():16);
           index++) {
        displayCmd[2+index] = messByte[index];
      }
      response = protocol.transmit(HOST_ID, TERMINAL_ID, displayCmd);
      if (message.length()>16) {
        displayCmd = new byte[2 + (message.length() - 16)];
        displayCmd[0] = (byte) 0x2B;
        displayCmd[1] = (byte) 0xC0;
        for (index=0;
             index + 16 < (message.length()<=32 ? message.length():32);
             index++) {
          displayCmd[2+index] = messByte[index + 16];
        }
        response = protocol.transmit(HOST_ID, TERMINAL_ID, displayCmd);
      }
    } catch (Exception e) {
      //Nothing to do
    }
  }
  public String keyboardInput(CardTerminalIOControl ioControl) {
    // Not implemented
    return("");
  }
  public String promptUser(String prompt,CardTerminalIOControl ioControl) {
    // Not implemented
    return("");
  }

  /** Protected and Private methods ============================================
  **/

  /** Select an auxiliary Icc (only for GemCore readers)
   *
   * @param     slotID
   *            slot number with the inserted card
   * @exception CardTerminalException
   *            Thrown when select method failed.
   */
  protected void selectAuxiliaryIcc(int slotID) throws CardTerminalException {
    byte[] response = null;
    byte[] selectRequest = new byte[] {(byte) 0x1F, (byte) 0x02, (byte) slotID};

    try {
      response = protocol.transmit(HOST_ID, TERMINAL_ID, selectRequest);
    // remap Exception to CardTerminalException
    } catch (Exception e) {
      throw new CardTerminalException(e.toString());
    }
  }

  /** disable the TLP mode in the reader
   */
  private void disableTLPMode() throws CardTerminalException {
    byte[] ModeRequest = new byte[] { (byte)0x01,(byte)0x00,(byte)0x00};
    byte[] response = null;

    try {

      response = protocol.transmit(HOST_ID, TERMINAL_ID, ModeRequest);

      if (response == null) {
        ctracer.error("disableTLPMode", "no response from reader");

        throw new CardTerminalException("couldn't disable TLP mode");
      }

      // WARNING: Do not test the reader status. If we have previously disabled
      // the TLP mode, so this command will not recognized by the reader

    // remap Exception to CardTerminalException
    } catch (Exception e) {
      throw new CardTerminalException(e.toString());
    }
  }
  /** select ISO-7816 card driver
   */
  private void selectISOCard() throws CardTerminalException {

    byte[] SelectIsoRequest = new byte[] { (byte)0x17,(byte)0x02};
    byte[] response = null;

    try {

      response = protocol.transmit(HOST_ID, TERMINAL_ID, SelectIsoRequest);

      if (response == null) {
        ctracer.error("selectISOCard", "no response from reader");

        throw new CardTerminalException("couldn't select ISO card");
      }

      if (response[0] != 0)
        throw new CardTerminalException("got errorcode " + response[0]
                      + " from reader on Select request");

    // remap Exception to CardTerminalException
    } catch (Exception e) {
      throw new CardTerminalException(e.toString());
    }
  }

  /**
   * updates the card protocol from a slot.
   *
   * @param     slotID
   *            The slot number of the slot requesting the status.
   * @exception CardTerminalException
   *            Thrown when error occurred getting status from reader.
   */
  private void UpdateCardProtocol(int slotID) throws CardTerminalException {
  int offset,i,k,l;
  boolean protocolSet = false;

    setUsedCardProtocol(slotID,-1);
    if (!cardInserted[slotID])
      throw new CardNotPresentException("no card inserted", this, getSlot(slotID));
    if (cachedATR[slotID] == null)
      throw new CardNotPresentException("no ATR available", this, getSlot(slotID));
      // While a TDi character exist,
      //   places l on the next TDi
      //   protocol memorises the first found protocol.
      //      Today, we assumes that only T=0, T=1 or T=0/1 are supported. So we
      //      memorised the first founded protocol which must be T=0 for
      //      bi-protocol card according to ISO standard.
      l = 1; // T0 offset
      while ((cachedATR[slotID][l] & (byte) 0x80) != (byte) 0x00){
         offset = 0;
         for(k = (byte) 0x10,i = 0; i<4 ;  k <<=1, i++){
            if ((cachedATR[slotID][l] & k) != (byte) 0x00){
               offset++;
            }
         }
         l += offset;
         if (!protocolSet){
            protocolSet = true;
            setUsedCardProtocol(slotID,cachedATR[slotID][l] & (byte) 0x0F);
            break;
         }
      }
      if (!protocolSet)
	  // set to default protocol (T=0)
	  setUsedCardProtocol(slotID,0);
  }

  /**
   * updates the card status from a slot.
   *
   * @param     slotID
   *            The slot number of the slot requesting the status.
   * @exception CardTerminalException
   *            Thrown when error occurred getting status from reader.
   */
  protected abstract void UpdateCardStatus(int slotID) throws CardTerminalException;


  /**
   * returns the cached card status (notice: don't forget to call UpdateCardStatus)
   *
   * @return cached status from slot
   * @see    #UpdateCardStatus
   */
  private int getCardStatus(int slotID) {
    return cachedCardStatus[slotID];
  }


  /**
   *  Gets the used card protocol of the inserted card.
   *
   *  @return    returns 0 for T0, 1 for T1
   *  @exception CardTerminalException
   *             thrown when error occurred getting card status
   *  @exception CardNotPresentException
   *             thrown when no card inserted
   */
  private int getUsedCardProtocol(int slotID) {
    return usedCardProtocol[slotID];
  }
  private void setUsedCardProtocol(int slotID,int protocol) {
    usedCardProtocol[slotID] = protocol;
  }
  /**
   * get and set for readerClosed variable
   */
  protected boolean getReaderClosed() {
    return readerClosed;
  }
  protected void setReaderClosed(boolean value) {
    readerClosed = value;
  }
  /**
   * get and set for cachedCardStatus variable
   */
  protected int getCachedCardStatus(int slotID) {
    return cachedCardStatus[slotID];
  }
  protected void setCachedCardStatus(int slotID,int value) {
    cachedCardStatus[slotID] = value;
  }

  protected void tracerDebug(String function,String message) {
    ctracer.debug(function, message);
  }
  protected void tracerError(String function,String message) {
    ctracer.error(function, message);
  }
  protected byte[] protocolTransmit(byte[] command) throws T1Exception {
      return protocol.transmit(HOST_ID, TERMINAL_ID, command);
  }

}
