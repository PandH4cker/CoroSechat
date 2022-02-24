/*
 * (C)Copyright IBM Corporation 1997 - 1999
 * All Rights Reserved.
 */

package com.ibm.opencard.service;

import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;

import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.CardFileAppID;


/**
 * Implementation of generic card services for MFC 4.1 and above.
 * This class extends the generic card services for MFC 3.5 and
 * above by the capability of selection by application name.
 *
 * @version $Id: MFC41CardServiceImpl.java,v 1.7 1998/10/07 06:55:20 cvsusers Exp $
 *
 * @author Roland Weber
 */
public class MFC41CardServiceImpl extends MFC35CardServiceImpl
{
  /**
   * The P1 byte for select by application.
   * The default value can be considered 'final' for MFC smartcards.
   * For other cards, like the GeldKarte, a different value may be
   * assigned in the constructor of a derived class.
   */
  protected byte p1_application_id = (byte) 0x04;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Creates a new generic card service implementation for MFC 4.1 and above.
   *
   * @param codes   the CLAss and INStruction codes for the smartcard
   */
  public MFC41CardServiceImpl(MFCCodes codes)
  {
    super(codes);
  }


  // service //////////////////////////////////////////////////////////////////


  // select support
  /*
   * In the base class, you will find some more attributes at the same place.
   * The comment found there applies here, too. In short: The statelessness
   * means that no data is stored across invocations. While a public method
   * is executed, the attributes may hold temporary data.
   */
  protected byte[]  application_id = null;


  /**
   * Add a path component to the current select operation.
   * If necessary, a command with the previously added components will
   * be sent to the smartcard.
   * This method checks only for application identifiers. Any other
   * components will be passed to the base class implementation.
   * <br>
   * Application identifiers are solitaire path components. They require
   * a select command of their own. If an application ID has been stored
   * by a previous invocation, the command will be executed now. If the
   * component just passed is an application ID, and the path composed
   * from previous invocations is not empty, a select command will be
   * executed before the application ID is stored.
   *
   * @param chan   the contact to the smartcard
   * @param comp   the path component to add
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #execSelect
   */
  protected void nextSelect(MFCCardChannel        chan,
                            CardFilePathComponent comp)
       throws CardTerminalException
  {
    if (application_id != null)
      execSelect(chan, false);          // will reset path_length

    if (comp instanceof CardFileAppID)
      {
        if (path_length != 0)
          execSelect(chan, false);      // will reset path length

        application_id = ((CardFileAppID)comp).toByteArray();
        path_length    = 1;
      }
    else
      {
        super.nextSelect(chan, comp);
      }
  }


  /**
   * Select the path that is stored by now.
   * This method checks whether the path currently stored consists of
   * an application ID. If so, it issues a corresponding select command,
   * and the response is stored in <tt>last_response</tt>.
   * Otherwise, the base class implementation is invoked. In any case,
   * the currently stored path is empty after this method returns.
   *
   * @param chan  the channel to the smartcard
   * @param info  <tt>true</tt> if the file info is needed
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see MFC35CardServiceImpl#last_response
   */
  protected void execSelect(MFCCardChannel chan,
                            boolean        info)
       throws CardTerminalException
  {
    if (application_id != null) // application ID is sole component
      {
        if (!path_error) try // <<< for cleanup
          {
            select_apdu.append(p1_application_id);
            select_apdu.append(info ? p2_select_info : p2_select_quiet);
            select_apdu.append((byte)application_id.length);
            select_apdu.append(application_id);

            sendSelectAPDU(chan, info);
          }
        finally {
          application_id = null;
          path_length    = 0;
        }
      }
    else
      {
        super.execSelect(chan, info);
      }
  } // execSelect

} // class MFC41CardServiceImpl
