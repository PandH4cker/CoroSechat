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

import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.TerminalInitException;
import opencard.core.terminal.CardTerminalRegistry;

import opencard.core.util.Tracer;

/**
 * A factory for creating an ApduIOCardTerminal.
 * 
 * Configuration parameters must be in the String array passed
 * to the createTerminal method.
 * <br>The first element is the friendly name of the terminal.
 * <br>The second element is the type of terminal (currently only "Socket"
 * is supported. 
 * <br>The third element is the address and port to contact the simulator.
 *
 * @author Laurent Lagosanto (lago@research.gemplus.com)
 * @version $Id: ApduIOCardTerminalFactory.java,v 0.1 2001/07/05 08:35:01 root Exp root $
 */
public class ApduIOCardTerminalFactory 
    implements CardTerminalFactory
{
  /**
   * debug utility
   */
  static Tracer ctracer = new Tracer(ApduIOCardTerminal.class);
  
    /**
     *  Creates and configure an ApduIOCardTerminal.
     *
     *  @param ctr the CardTerminalRegistry for registration-process
     *  @param terminalInfo configuration parameters for the terminal
     *
     */
  public void createCardTerminals( CardTerminalRegistry ctr, String[] infos )
  	throws CardTerminalException, TerminalInitException
  {
    ctracer.info( "createCardTerminals", "with " + infos.length + " arguments");
    // check that it's an APDUIO
    if ( infos[TERMINAL_TYPE_ENTRY].equals( "Socket" ) )
    {
      // Socket terminal needs one adress parameter
      if (infos.length != 3)
      {
	      throw new TerminalInitException("createCardTerminals needs 3 parameters");
      }
      // creates the terminal and registers it
      ctr.add( new ApduIOCardTerminal(
      	infos[TERMINAL_NAME_ENTRY], 
        infos[TERMINAL_TYPE_ENTRY],
      	infos[TERMINAL_ADDRESS_ENTRY] ) ) ;
    } 
    else
    {
    	throw new TerminalInitException( "Type unknown: " + infos[TERMINAL_TYPE_ENTRY] ) ;
    }
  } /**/


	/**
   * Initializes the factory.
	 *
	 * @throw CardTerminalException 
   */
  public void open() 
  	throws CardTerminalException 
  {
		; 
  } /**/


  /**
   * Terminates the factory.
   *
   * @throw CardTerminalException 
   */
  public void close() 
  	throws CardTerminalException 
	{
		;
	} /**/

}
