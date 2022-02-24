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

/**
 * A re-usable command APDU for MFC smartcards.
 * The secure messaging requires information about the direction of the
 * data transfer, to determine whether the command, the response, or both
 * have to be processed for PROtection or ENCryption. This class extends
 * the base class by attributes and methods to set and query this information.
 * <br>
 * In the JavaDoc comments below, <i>SM</i> is used as an abbreviation for
 * <i>Secure Messaging</i>. It refers to the cryptographic processing that
 * is required to satisfy access contions that involve PRO or ENC.
 *
 * @version $Id: MFCCommandAPDU.java,v 1.3 1998/09/14 12:00:09 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCCommandAPDU extends CommandAPDU
{
  /** Specifies whether the command contains data relevant for SM. */
  private boolean command_flag = false;

  /** Specifies whether the response will contain data relevant for SM. */
  private boolean response_flag = false;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new, re-usable command APDU for MFC smartcards.
   * The provider and requester flags are set to <tt>false</tt>.
   *
   * @param size   the maximum size of this command APDU
   *
   * @see #setProviderFlag
   * @see #setRequesterFlag
   */
  public MFCCommandAPDU(int size)
  {
    super(size);
  }

  /**
   * Instantiates a new, re-usable predefined command APDU for MFC smartcards.
   * The provider and requester flags are set to <tt>false</tt>.
   *
   * @param buffer   the buffer to use for holding the APDU
   * @param length   the length of the APDU currently in the buffer
   *
   * @exception IndexOutOfBoundsException
   *   <tt>length</tt> exceeds the size of the array <tt>buffer</tt>.
   *
   * @see #setProviderFlag
   * @see #setRequesterFlag
   */
  public MFCCommandAPDU(byte[] buffer, int length)
  {
    super(buffer, length);
  }

  // access ///////////////////////////////////////////////////////////////////

  /**
   * Sets the flag for SM relevant command data.
   * If the flag is set to <tt>true</tt>, the command data
   * will be subject to secure messaging operations.
   *
   * @param is  whether the command includes SM relevant data
   *
   * @see #isDataProvider
   */
  final public void setProviderFlag(boolean is)
  {
    command_flag = is;
  }

  /**
   * Sets the flag for SM relevant response data.
   * If the flag is set to <tt>true</tt>, the expected response
   * data will be subject to secure messaging operations.
   *
   * @param is  whether the response will include SM relevant data
   *
   * @see #isDataRequester
   */
  final public void setRequesterFlag(boolean is)
  {
    response_flag = is;
  }


  /**
   * Queries the flag for SM relevant command data.
   * If this flag is <tt>true</tt>, and the access conditions require
   * secure messaging, the command will be PROtected or ENCrypted.
   *
   * @return  whether the command includes SM relevant data
   *
   * @see #setProviderFlag
   */
  final public boolean isDataProvider()
  {
    return command_flag;
  }

  /**
   * Queries the flag for SM relevant response data.
   * If this flag is <tt>true</tt>, and the access conditions require
   * secure messaging, the response to this command will be expected
   * to be PROtected or ENCrypted.
   *
   * @return  whether the response will include SM relevant data
   *
   * @see #setRequesterFlag
   */
  final public boolean isDataRequester()
  {
    return response_flag;
  }


  // service //////////////////////////////////////////////////////////////////

  /**
   * Appends a data block to the APDU currently stored.
   * The data block consists of a length byte, deduced from the length
   * of the array passed as argument, and the data actually stored in
   * the array. To append a byte array without the length byte, use
   * <tt>append(byte[] data)</tt> in the base class.
   * <br>
   * If the buffer is not large enough to append the block, the buffer
   * contents may get corrupted. An <tt>ArrayIndexOutOfBoundsException</tt>
   * will be thrown in this case.
   *
   * @param data   the data block to append
   *
   * @exception java.lang.ArrayIndexOutOfBoundsException
   *   the buffer is too small to hold the composed APDU
   */
  final public void appendBlock(byte[] data)
       throws ArrayIndexOutOfBoundsException
  {
    append((byte)data.length);
    append(data);
  }


  /**
   * Appends part of an array as a data block to the APDU currently stored.
   * The data block consists of a length byte, followed by the actual data
   * stored in the array.
   * Appending a part of an array without a length byte is not supported.
   * To append the complete array, use <tt>appendBlock(byte[])</tt>.
   *
   * @param date     array holding the block to append
   * @param offset   index of the first byte of the block to append
   * @param length   length of the block to append
   *
   * @exception java.lang.ArrayIndexOutOfBoundsException
   *   the buffer is too small to hold the composed APDU
   *
   * @see #appendBlock(byte[])
   */
  final public void appendBlock(byte[] data, int offset, int length)
  {
    append((byte) length);
    System.arraycopy(data, offset, apdu_buffer, apdu_length, length);
    apdu_length += length;
  }

} // class MFCCommandAPDU
