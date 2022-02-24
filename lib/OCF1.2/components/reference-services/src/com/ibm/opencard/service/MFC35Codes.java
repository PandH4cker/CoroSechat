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


import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceOperationFailedException;
import opencard.opt.service.CardServiceObjectNotAvailableException;


/**
 * CLAss and INStruction codes for MFC 3.5 and 4.0 smartcards.
 * This class implements exactly the interface <tt>MFCCodes</tt>.
 * Most methods are trivial and therefore not commented.
 *
 * @version $Id: MFC35Codes.java,v 1.16 1999/01/18 13:29:12 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCodes
 */
public class MFC35Codes implements MFCCodes
{
  /** The code for an unknown operation. */
  public final static byte OP_UNKNOWN = (byte) -1;

  /**
   * The command bytes for basic operations.
   * Basic operations start with <tt>OP_SELECT</tt> and end
   * before <tt>NOOP_GENERIC_END</tt>.
   * Make sure to update this table if the constants in
   * <tt>MFCCodes</tt> are changed.
   *
   * @see com.ibm.opencard.service.MFCCodes
   * @see com.ibm.opencard.service.MFCCodes#OP_SELECT
   * @see com.ibm.opencard.service.MFCCodes#NOOP_BASIC_END
   */
  private static byte[] basic_ops =
  {
    (byte) 0xa4, // select
    (byte) 0x84, // get random (get challenge, ask random)
    (byte) 0x86, // put random (give random)
    (byte) 0x20, // verify CHV
    (byte) 0x82, // external authenticate
    (byte) 0x88  // internal authenticate
  };


  /**
   * The command bytes for file operations.
   * File operations start with <tt>OP_READ_BINARY</tt> and
   * end before <tt>NOOP_FILE_END</tt>.
   * Make sure to update this table if the constants in
   * <tt>MFCCodes</tt> are changed.
   *
   * @see com.ibm.opencard.service.MFCCodes
   * @see com.ibm.opencard.service.MFCCodes#OP_READ_BINARY
   * @see com.ibm.opencard.service.MFCCodes#NOOP_FILE_END
   */
  private static byte[] file_ops =
  {
    (byte) 0xb0, // read binary
    (byte) 0xb2, // read record
    (byte) 0xd6, // update binary
    (byte) 0xdc, // update record
    (byte) 0xe2, // append record, rejected by MFC 4.0 and below
    (byte) 0xe0, // create file
    (byte) 0xe4, // delete file
    (byte) 0x04, // invalidate
    (byte) 0x44, // rehabilitate
    (byte) 0x32, // increase
    (byte) 0x30  // decrease
  };


  /**
   * The access groups for file operations.
   * See <tt>file_ops</tt> for the mapping.
   *
   * @see #file_ops
   */
  private static int[] file_ags =
  {
    1, 1, 0, 0, // read x2, update x2
    2, 2, 3,    // create x2, delete
    4, 5,       // invalidate, rehabilitate
    3, 0        // delete, update
  };


  // construction /////////////////////////////////////////////////////////////

  public MFC35Codes()
  {
    ;
  }

  public int  getBlockSize() { return 64; }

  public boolean needsZeroLc() { return true ; }
  public boolean needsZeroLe() { return false; }

  public boolean needsZeroLe(boolean info) { return needsZeroLe(); }


  public byte getINS(int operation)
  {
    byte ins = OP_UNKNOWN;

    if      ((operation >= OP_SELECT) && (operation < NOOP_BASIC_END))
      ins = basic_ops[operation-OP_SELECT];
    else if ((operation >= OP_READ_BINARY) && (operation < NOOP_FILE_END))
      ins = file_ops[operation-OP_READ_BINARY];

    return ins;
  }


  public int getAG(int operation)
  {
    int ag = -1;

    if      ((operation >= OP_SELECT) && (operation < NOOP_BASIC_END))
      ; // no access group (@@@ wrong for VERIFY CHV, may cause trouble!)
    else if ((operation >= OP_READ_BINARY) && (operation < NOOP_FILE_END))
      ag = file_ags[operation-OP_READ_BINARY];

    return ag;
  }


  public byte getISOClassByte() { return (byte) 0xa4; }
  public byte getClassByte()    { return (byte) 0xa4; }

  //@@@ for GeldKarte, until reworked:
  public byte getReadRecordByte() { return (byte) 0xb2; }
  public byte getAskRandomByte () { return (byte) 0x84; }


  /**
   * Analyses the status of a smartcard's response to a command.
   * If the command completed successfully, the method returns without
   * further action. If an error occurred, an exception is thrown.
   * This method uses <tt>indicatesError</tt> to determine whether an
   * exception has to be thrown.
   *
   * @param status   the status word returned by the smartcard
   * @param command  the INStruction sent to the smartcard
   * @param context  the context in which the command was issued
   *
   * @exception CardServiceException  iff the status indicates an error
   *
   * @see #indicatesError
   */
  public void analyseStatus(int status, byte command, Object context)
       throws CardServiceException
  {
    if (!indicatesError(status))
      return;

    StringBuffer sb = new StringBuffer();

    if (context != null)
      sb.append(context.toString()).append(": ");

    if ((command == basic_ops[0]) &&
        (status == MFCResponseAPDUCodes.RAPDU_FILE_NOT_FOUND))
      {
        sb.append("object not found");
        throw new CardServiceObjectNotAvailableException(sb.toString());
      }
    else
      {
        sb.append("INS 0x");
        sb.append(Integer.toHexString(command & 0xff));
        sb.append(" bad status 0x");
        sb.append(Integer.toHexString(status & 0xffff));
        throw new CardServiceOperationFailedException(sb.toString());
      }

  } // analyseStatus


  /**
   * Checks whether the response status indicates an error.
   * If so, <tt>true</tt> is returned.
   *
   * @param status   the status returned by the smartcard
   * @return    <tt>false</tt> if the command completed successfully,
   *            <tt>true</tt> otherwise
   */
  public boolean indicatesError(int status)
  {
    return (status != MFCResponseAPDUCodes.RAPDU_OK);
  }

} // class MFC35Codes
