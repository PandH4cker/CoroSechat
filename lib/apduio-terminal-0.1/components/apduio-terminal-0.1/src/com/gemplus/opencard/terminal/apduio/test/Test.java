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
package com.gemplus.opencard.terminal.apduio.test;

//-----------------------------------------------------------------------------
// IMPORTS
//-----------------------------------------------------------------------------
import opencard.core.service.SmartCard;
import opencard.core.service.CardRequest;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.CTListener;
import opencard.core.event.EventGenerator;

import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;

import opencard.opt.util.PassThruCardService;

import opencard.core.util.HexString;

/**
 * Test class for the ApduIOCardTerminal
 *
 * @author Laurent Lagosanto (lago@research.gemplus.com)
 * @version $Id: Test.java,v 0.1 2001/07/05 08:35:01 root Exp root $
 */
public class Test implements CTListener
{
  private PassThruCardService ptcs;

  public Test()
  {
  }

  public static void main(String[] args)
  {
    try
    {
      Test test = new Test();
      SmartCard.start();
      EventGenerator.getGenerator().addCTListener(test);
      Thread.currentThread().join();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void cardInserted(CardTerminalEvent ctEvent) throws CardTerminalException
  {
    System.out.println("Card Inserted");
    SmartCard card = SmartCard.getSmartCard(ctEvent,
      new CardRequest( CardRequest.ANYCARD, ctEvent.getCardTerminal(), null));

    if (card != null)
    {
      try
      {
        ptcs = (PassThruCardService)card.getCardService(PassThruCardService.class, true);

        select();
        System.out.println("Balance = " + getBalance());
        verifyPIN(new byte[] {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05} );
        debit((byte)16);
        System.out.println("Balance = " + getBalance());
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public void cardRemoved(CardTerminalEvent ctEvent) throws CardTerminalException
  {
    System.out.println("Card Removed");
    System.exit(0);
  }

  /**
   * select the applet
   */
  public void select() throws CardTerminalException
  {
    sendAndCheck(new byte[] { (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0a, (byte)0xa0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x62, (byte)0x3, (byte)0x1, (byte)0xc, (byte)0x6, (byte)0x1, (byte)0x7F} );
  }

  /**
   * query the balance from the applet
   */
  public String getBalance() throws CardTerminalException
  {
    byte[] balance = sendAndCheck( new byte[] { (byte)0xB0, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02} );
    return "0x" + HexString.hexifyShort(balance[0], balance[1]);
  }

  /**
   * present the PIN to the applet
   */
  public void verifyPIN(byte[] pin) throws CardTerminalException
  {
    byte[] cmd = new byte[] { (byte)0xB0, (byte)0x20, (byte)0x00, (byte)0x00, (byte)pin.length, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7F};
    System.arraycopy(pin, 0, cmd, 5, pin.length);
    sendAndCheck(cmd);
  }

  /**
   * send a debit cmd
   */
  public void debit(byte amount) throws CardTerminalException
  {
    // debit $16
    sendAndCheck(new byte[] { (byte)0xB0, (byte)0x40, (byte)0x00, (byte)0x00, (byte)0x01, amount, (byte)0x7F} );
  }

  /**
   * send a cmd APDU to the applet
   */
	private byte[] sendAndCheck(byte[] cmd) throws CardTerminalException
	{
	  CommandAPDU apdu;
	  ResponseAPDU res;
	  apdu = new CommandAPDU(cmd);
	  res = ptcs.sendCommandAPDU(apdu);
    if (res.sw() != 0x9000)
    {
      throw new CardTerminalException("SW = 0x" + Integer.toHexString(res.sw()));
    }
    return res.data();
	}
}
