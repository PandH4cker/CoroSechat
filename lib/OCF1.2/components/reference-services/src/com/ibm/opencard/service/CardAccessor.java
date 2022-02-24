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

package com.ibm.opencard.service;


import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;

import opencard.core.service.CardChannel;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidCredentialException;

import opencard.opt.service.CardServiceMissingCredentialsException;
import opencard.opt.service.CardServiceUnexpectedResponseException;


/**
 * An interface for an access condition handler.
 * A card accessor is intended to execute smartcard commands while
 * satisfying access conditions transparently. For example, it can
 * get a challenge from the smartcard and encrypt the command before
 * sending it.
 *
 * @version $Id: CardAccessor.java,v 1.1 1998/12/18 14:48:14 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface CardAccessor
{
  /**
   * Connects or disconnects this accessor to a channel.
   * The channel is required to send commands to the smartcard.
   * To connect, pass a <tt>CardChannel</tt> as the argument.
   * To disconnect, pass <tt>null</tt>.
   *
   * @param channel     the channel for contacting the smartcard,
   *                    or <tt>null</tt> to disconnect
   */
  public void setChannel(MFCCardChannel channel);


  /**
   * Sends a command to the smartcard and satisfies required access conditions.
   * It is implementation dependent which access conditions are supported.
   * If this accessor is incapable to satisfy a required access condition,
   * an exception is thrown. The same happens if the data supplied to the
   * accessor is incomplete or wrong. For example, a cryptographic credential
   * could be missing, or a wrong password may have been entered.
   *
   * @param command     the command to send to the smartcard
   * @param access      the conditions that have to be satisfied in order
   *                    to execute the command. The type of these conditions
   *                    is implementation dependent. Typically, an instance
   *                    of this interface will only be able to deal with one
   *                    particular type of access conditions.
   * @return    the response obtained from the smartcard on executing
   *            the command
   *
   * @exception CardServiceInabilityException
   *            if a required access condition is not supported. For example,
   *            exportable accessors may be unable to deal with cryptographic
   *            access conditions like secure messaging.
   * @exception CardServiceUnexpectedResponseException
   *            if a transparent smartcard access failed. For example,
   *            the accessor may have to get a random challenge from
   *            the smartcard. If the smartcard returns an error code,
   *            this exception would be thrown.
   * @exception CardServiceMissingCredentialsException
   *            if a credential that is required cannot be found. For
   *            example, the accessor may have to perform an external
   *            authentication with the smartcard, but there is no
   *            matching key for that purpose.
   * @exception CardServiceInvalidCredentialException
   *            if a credential is rejected by the smartcard. This may
   *            happen if wrong keys are stored in the accessor, or if
   *            a wrong password is entered by the user.
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public ResponseAPDU executeCommand(CommandAPDU      command,
                                     AccessConditions access )
       throws CardServiceInabilityException,
              CardServiceUnexpectedResponseException,
              CardServiceMissingCredentialsException,
              CardServiceInvalidCredentialException,
              CardTerminalException
  ;

} // interface CardAccessor
