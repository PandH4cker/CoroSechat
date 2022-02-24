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
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.CardTerminalException;

import opencard.core.service.CardChannel;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.CardServiceInabilityException;

import opencard.opt.service.CardServiceUnexpectedResponseException;


/**
 * A CardChannel to MFC smartcards.
 * Unlike the intended base class <tt>opencard.core.service.CardChannel</tt>,
 * this channel potentially modifies the APDUs before sending them to
 * the smartcard. Modifications include encoding of the channel ID
 * (for MFC cards that support logical channels) and cryptography,
 * for example MAC generation and decoding. The latter is done in
 * cooperation with an instance of <tt>CardAccessor</tt>.
 *
 * @version $Id: MFCCardChannel.java,v 1.19 1999/01/19 08:41:33 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see com.ibm.opencard.service.CardAccessor
 */
public class MFCCardChannel extends CardChannel
{
  /**
   * The logical channel ID.
   * The channel is responsible to encode it's ID in passing CommandAPDUs.
   * In order to do this, it needs to know that ID. Since a channel object
   * is associated to a single logical channel over it's lifetime, this
   * attribute is final. As logical channels are numbered, <tt>int</tt> is
   * an appropriate type for the ID.
   */
  protected /*final*/ int channel_ID;


  /**
   * The card's state, as seen through this channel.
   * This attribute allows cooperation of different card services.
   * For example, if a signature service has to change the current
   * directory, it does not have to use a file system service, but
   * it can change the directory itself and store the new one here.
   */
  protected MFCChannelState channel_state = null;


  /**
   * The accessor handling secure messaging and other access conditions.
   * Any security issues, including card holder verification, are taken
   * care of by the accessor. The accessor is a card service specific
   * object that will be set after allocation of the channel. It will
   * interpret the access conditions passed along with APDUs.
   *
   * @see #setAccessor
   */
  private CardAccessor card_accessor = null;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiate a MFC card channel.
   *
   * @param slotchan    the underlying slot channel
   * @param id          the channel identifier, 0 to 3 for ISO cards.
   *                    This parameter has to be 0, since there are
   *                    no multi channel MFC cards.
   * @param state       an object representing the smartcard state,
   *                    as it is seen through this channel. Since there
   *                    is only one logical channel, this is a global state.
   *
   * @exception InvalidCardChannelException
   *            if the channel ID is not 0
   */
  public MFCCardChannel(SlotChannel             slotchan,
                        int                     id,
                        MFCChannelState         state)
       throws InvalidCardChannelException
  {
    super(slotchan);

    channel_ID    = id;
    channel_state = state;

    if (id != 0)
      throw new InvalidCardChannelException("channel ID not 0 but " + id);
  }



  // access ///////////////////////////////////////////////////////////////////

  /**
   * Set the accessor for the next commands.
   * The accessor is responsible for interpreting the access conditions
   * passed along with commands. Every card service will use it's own
   * accessor. After allocating a channel, the accessor is set by invoking
   * this method. The accessor gets connected to this channel. Therefore,
   * an accessor can only be used with one channel at any time.
   * <br>
   * The accessor will be reset to <tt>null</tt> when a channel is
   * released. That way, no card service will be able to use this
   * service's accessor unapproved.
   *
   * @param accessor   the accessor to use from now on,
   *                   or <tt>null</tt> to reset
   */
  final public void setAccessor(CardAccessor accessor)
  {
    /*
     * - disconnect from the previous accessor, if there is one
     * - store the new accessor
     * - connect to the new accessor, if there is one
     */
    if (card_accessor != null)
      card_accessor.setChannel(null);

    card_accessor = accessor;

    if (card_accessor != null)
      card_accessor.setChannel(this);
  }


  /**
   * Get the card's state, as seen through this channel.
   *
   * @return  the state object associated with this channel
   */
  final public MFCChannelState getChannelState()
  {
    return channel_state;
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Exchange APDUs with the smartcard (top level).
   * This is the top level method typically used by card services.
   * The command APDU gets modified to reflect the channel ID and
   * any secure messaging restrictions imposed by the access conditions,
   * for example by calculating a MAC or encrypting the APDU.
   * The response APDU also gets modified as required by the access
   * conditions, for example by decrypting the APDU or checking it's MAC.
   * <br>
   * The access conditions are imposed by the smartcard for access to
   * a particular object stored there. The Java representation of any
   * smartcard object must provide methods to query the access information.
   * The card service will decide in which command class a particular
   * command falls, and pass the appropriate access conditions.
   * <br>
   * Passing <tt>null</tt> as access conditions will bypass the accessor.
   * This is equivalent to a mid level exchange of APDUs. Likewise if no
   * accessor has been set.
   * <br>
   * Unlike the mid and low level methods to exchange APDUs, this one
   * requires the APDU to be of class <tt>MFCCommandAPDU</tt>. This class
   * of APDUs provides additional information that is required to determine
   * whether secure messaging is needed for the command only, the response
   * only, or for both.
   *
   * @param command   the APDU to send to the smartcard
   * @param access    the access conditions imposed by the smartcard
   *
   * @return  the APDU received from the card, after some postprocessing
   *
   * @exception CardServiceInabilityException
   *            if the access conditions require secure messaging, but
   *            the accessor does not support it due to US export restrictions
   * @exception CardServiceUnexpectedResponseException
   *            if the access conditions require card communication,
   *            and the card's answer is inappropriate
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #executeCommand(opencard.core.terminal.CommandAPDU)
   * @see opencard.core.service.CardChannel#sendCommandAPDU
   */
  final public ResponseAPDU executeCommand(MFCCommandAPDU   command,
                                           AccessConditions access)
       throws CardServiceInabilityException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    ResponseAPDU response = null;

    encodeChannelID(command);

    if ((card_accessor == null) || (access == null))
      {
        /*
         * There is no accessor, or no access conditions.
         * Either way, there is no use in invoking the accessor,
         * so do a low-level call. Since the channel ID has been
         * set, this is equivalent to a low-level call.
         */
        response = sendCommandAPDU(command);
      }
    else
      {
        // Pass the call to the accessor.

        response = card_accessor.executeCommand(command, access);
      }

    return response;

  } // executeCommand (high level)


  /**
   * Exchange APDUs with the smartcard (top level).
   * This is the top level method typically used by card services.
   * The command APDU gets modified to reflect the channel ID and
   * any secure messaging restrictions imposed by the access conditions,
   * for example by calculating a MAC or encrypting the APDU.
   * The response APDU also gets modified as required by the access
   * conditions, for example by decrypting the APDU or checking it's MAC.
   * <br>
   * The access conditions are imposed by the smartcard for access to
   * a particular object stored there. The Java representation of any
   * smartcard object must provide methods to query the access information.
   * The card service will decide in which command class a particular
   * command falls, and pass that command class. The access conditions
   * will be queried from the channel state. If no access conditions can
   * be found, or if the command group passed is negative, the command
   * will be sent to the card directly.
   * <br>
   * Unlike the mid and low level methods to exchange APDUs, this one
   * requires the APDU to be of class <tt>MFCCommandAPDU</tt>. This class
   * of APDUs provides additional information that is required to determine
   * whether secure messaging is needed for the command only, the response
   * only, or for both.
   *
   * @param command   the APDU to send to the smartcard
   * @param group     the access group of the command to be executed
   *
   * @return  the APDU received from the card, after some postprocessing
   *
   * @exception CardServiceInabilityException
   *            if the access conditions require secure messaging, but
   *            the accessor does not support it due to US export restrictions
   * @exception CardServiceUnexpectedResponseException
   *            if the access conditions require card communication,
   *            and the card's answer is inappropriate
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #executeCommand(opencard.core.terminal.CommandAPDU)
   * @see opencard.core.service.CardChannel#sendCommandAPDU
   */
  final public ResponseAPDU executeCommand(MFCCommandAPDU command, int group)
       throws CardServiceInabilityException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    AccessConditions ac = null;

    if (group >= 0)
      {
        MFCCardObjectInfo coi = channel_state.getCurrentInfo();
        if (coi != null)
          ac = coi.getAccessConditions(group);
      }

    return executeCommand(command, ac);

  } // executeCommand (high level)


  /**
   * Exchange APDUs with the smartcard (mid level).
   * This is the mid level method typically used by the associated
   * instance of <tt>CardAccessor</tt>. It can also be used by card
   * services to send commands without access restrictions.
   * The command APDU gets modified only to reflect the channel ID.
   * The response APDU is returned as it was received from the smartcard.
   *
   * @param command   the APDU to send to the smartcard
   *
   * @return  the APDU received from the smartcard
   *
   * @exception CardTerminalException
   *            the terminal encountered a problem
   *
   * @see #executeCommand(com.ibm.opencard.service.MFCCommandAPDU, com.ibm.opencard.service.AccessConditions)
   * @see opencard.core.service.CardChannel#sendCommandAPDU
   */
  final public ResponseAPDU executeCommand(CommandAPDU command)
       throws CardTerminalException
  {
    // encode the channel ID and send the command

    ResponseAPDU response = null;

    encodeChannelID(command);
    response = sendCommandAPDU(command);

    return response;
  }


  /**
   * Encode the channel ID into a CommandAPU.
   * This method modifies the given command APDU to encode
   * the channel ID. It is assumed that the corresponding
   * bits in the APDU are set to 0.
   *
   * <p>
   * The current implementation is empty, since there are no
   * MFC cards supporting multiple channels. Consequently, the
   * only legal channel ID to be passed as a constructor argument
   * is 0.
   * <br>
   * The method could be made protected if this class is to be renamed
   * for re-use in a different context (ISOCardChannel or so).
   *
   * @param command  the APDU to modify for transmission via this channel
   */
  private void encodeChannelID(CommandAPDU command)
       throws InvalidCardChannelException
  {
    // no body
  }

} // class MFCCardChannel
