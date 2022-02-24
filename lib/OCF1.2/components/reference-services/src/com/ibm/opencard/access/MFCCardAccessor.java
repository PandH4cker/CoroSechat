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

package com.ibm.opencard.access;


import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;

import opencard.core.service.CHVDialog;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceInvalidCredentialException;
import opencard.opt.service.CardServiceUnexpectedResponseException;

import com.ibm.opencard.service.CardAccessor;
import com.ibm.opencard.service.AccessConditions;
import com.ibm.opencard.service.MFCCommandAPDU;
import com.ibm.opencard.service.MFCCardChannel;
import com.ibm.opencard.service.MFCResponseAPDUCodes;


/**
 * The card accessor is responsible for satisfying access conditions.
 * Command APDUs to be sent to the card will be passed to the accessor first,
 * along with access conditions. The accessor then uses the mid and low level
 * communication methods of the channel to satisfy the access conditions and
 * to execute the command. It relies on a helper class for CHV, while other
 * access conditions (except ALWays and NEVer) are not supported due to US
 * export regulations.
 *
 * @version $Id: MFCCardAccessor.java,v 1.22 1998/12/18 14:51:15 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCCardAccessor implements CardAccessor
{
  /** This channel will be used to exchange APDUs with the smartcard. */
  protected MFCCardChannel   card_channel = null;


  /** The set of credentials available to this accessor. */
  protected MFCCredentialSet credential_set = null;


  /** This helper will take care of card holder verification. */
  protected MFCCHVProvider   helper_CHV = null;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Create a new accessor.
   *
   * @param chverifier   the helper for Card Holder Verification
   */
  public MFCCardAccessor(MFCCHVProvider chverifier)
  {
    credential_set = new MFCCredentialSet();    // no credentials provided yet

    helper_CHV = chverifier;
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Gets the set of credentials.
   * An application provides the credentials to be used in a
   * <tt>CredentialBag</tt>, which contains card-specific
   * <tt>CredentialStore</tt> objects. The card service extracts the
   * credential stores for MFC smartcards and copies the credentials
   * to a <tt>MFCCredentialSet</tt>, which will be not be used at all,
   * since this accessor does not support access conditions involving
   * cryptography.
   * <br>
   * This method is used to get the credential set for this accessor.
   * After creation, that set is empty. Changes in the credential set
   * will take effect immediately, no explicit set has to be performed.
   * That is the reason why no set method is provided for the set of
   * credentials.
   *
   * @return  the set of credentials available to this accessor
   *
   * @see opencard.opt.security.CredentialBag
   * @see opencard.opt.security.CredentialStore
   * @see com.ibm.opencard.access.MFCCredentialSet
   * @see com.ibm.opencard.service.MFCCardService
   */
  public MFCCredentialSet getCredentials()
  {
    return credential_set;
  }


  /**
   * Set the channel to which this accessor is connected.
   * This method is called by a channel as soon as an accessor is passed
   * to it. The accessor needs to know the channel to exchange APDUs with
   * the smartcard via the mid and low level methods provided there. When
   * the accessor is disconnected, the channel will call this method again,
   * with argument <tt>null</tt>. Consequently, an accessor can only be
   * used with one channel at a time.
   * <br>
   * The channel could be passed as an argument to <tt>executeCommand</tt>
   * instead. This is not done to simplify that method. Besides, there
   * will usually be passed more than one command APDU for a channel
   * allocation, so the overhead should be smaller using an explicit
   * set prior to communication.
   *
   * @param channel  the card channel to use,
   *                 or <tt>null</tt>
   *
   * @see com.ibm.opencard.service.MFCCardChannel#setAccessor
   */
  final public void setChannel(MFCCardChannel channel)
  {
    card_channel = channel;
  }


  /**
   * Set the CHV dialog to be used if CHV is required.
   * This dialog is application dependent. Therefore, this
   * method is needed to supply an application-defined dialog.
   * The dialog will be passed on to the CHV helper.
   * <br>
   * The interface <tt>CHVDialog</tt> does not explicitly require
   * a screen dialog. A card terminal's IO capabilities could also
   * be used.
   *
   * @param dialog  the dialog to invoke if CHV is required
   *
   * @see MFCCHVProvider
   * @see opencard.core.service.CHVDialog
   */
  final public void setCHVDialog(CHVDialog dialog)
  {
    helper_CHV.setCHVDialog(dialog);
  }  



  // service //////////////////////////////////////////////////////////////////


  /**
   * Generic method to exchange APDUs with the smartcard.
   * This method checks and downcasts the arguments and then
   * invokes <tt>execCommand</tt> below. See there for details.
   */
  public ResponseAPDU executeCommand(CommandAPDU command,
                                     AccessConditions access)
       throws CardServiceInabilityException,
              CardServiceInvalidCredentialException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    if (!(access instanceof MFCAccessConditions))
      throw new CardServiceInvalidParameterException
        ("Unknown type of access conditions.");
    if (!(command instanceof MFCCommandAPDU))
      throw new CardServiceInvalidParameterException
        ("Wrong type of CommandAPDU class.");

    return execCommand((MFCCommandAPDU)command, (MFCAccessConditions)access);
  }


  /**
   * Exchange APDUs with the smartcard.
   * This method is invoked by the card channel. The channel ID is already
   * encoded in the command APDU, so it can be passed to the low-level send
   * method immediately. This accessor is responsible for satisfying the
   * access conditions involved with the command. In order to do so, it may
   * have to perform a card holder verification. Other access conditions
   * are not supported, due to US export regulations.
   * <p>
   * The current implementation uses an <I>optimistic</I> approach.
   * It assumes that CHV has been performed before. If access is denied,
   * the assumption was wrong and CHV is performed before the command is
   * retried. This optimistic approach costs one useless command for
   * each CHV to perform, but saves the trouble of keeping track of
   * CHVs and their invalidation due to select commands. Worse cases may
   * occur in conjunction with other access conditions, but these are
   * not considered here.
   *
   * @param command  the APDU to send to the smartcard
   * @param access   the access conditions for the command in the APDU
   *
   * @return  the response APDU obtained from the smartcard
   *
   * @exception CardServiceInabilityException
   *            The access conditions could not be satisfied.
   * @exception CardServiceInvalidCredentialException
   *            The credentials provided were incorrect.
   * @exception CardServiceUnexpectedResponseException
   *            A helper failed to interpret the smartcard's response.
   * @exception CardTerminalException
   *            The terminal encountered an error.
   */
  public ResponseAPDU execCommand(MFCCommandAPDU      command,
                                  MFCAccessConditions access )
       throws CardServiceInabilityException,
              CardServiceInvalidCredentialException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    // Just to be sure, check for access condition NEVer.
    // In this case, every action would be useless.
    if (access.isNeverAccessible())
      throw new CardServiceInabilityException("object NEVer accessible");

    // Send the command, assuming CHV has been performed before.
    ResponseAPDU response = card_channel.sendCommandAPDU(command);

    // Check the response. If access is denied, our optimistic approach
    // has been wrong and we need to do something about it. First, check
    // the access conditions. If they involve AUT, PRO, or ENC, there's
    // nothing that can be done about it, due to US export regulations.
    // Otherwise, perform CHV if required, then retry the command.

    if (response.sw() == MFCResponseAPDUCodes.RAPDU_NOT_AUTHENTICATED)
      {
        if (access.requiresAuthentication() ||
            access.requiresProtection()     ||
            access.requiresEncryption()     )
          throw new CardServiceInabilityException
            ("authentication and secure messaging not supported");

        if (access.requiresCHV())
          response = retryWithCHV(command, access);
      }

    return response;

  } // executeCommand            


  /**
   * Retry a command after performing CHV.
   * This method is invoked if a command failed due to access conditions
   * not satisfied, and the access conditions require CHV to be performed.
   * The CHV will be performed, and the command is issued again.
   * <br>
   * If the access conditions combine authentication and CHV, authentication
   * is done before this method is invoked. This way, the CHV is queried
   * from the user only if it is really required.
   *
   * @param command     the command to retry
   * @param access      the access conditions to satisfy
   *
   * @return  the smartcard's response to the command
   *
   * @exception CardServiceInvalidCredentialException
   *            if the password was incorrect or is blocked
   * @exception CardServiceInabilityException
   *            if no CHV provider is defined
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected ResponseAPDU retryWithCHV(MFCCommandAPDU      command,
                                      MFCAccessConditions access) 
       throws CardServiceInabilityException,
              CardServiceInvalidCredentialException,
              CardTerminalException
  {
    if (helper_CHV == null)
      throw new CardServiceInabilityException("no CHV helper defined");

    if (! helper_CHV.performCHV(card_channel, access.getCHVNumber()))
      throw new CardServiceInvalidCredentialException
        ("CHV incorrect or blocked");

    return card_channel.sendCommandAPDU(command);   
  }

} // class MFCCardAccessor
