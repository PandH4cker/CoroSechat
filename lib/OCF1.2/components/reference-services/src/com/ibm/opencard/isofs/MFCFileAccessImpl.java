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

package com.ibm.opencard.isofs;


import opencard.core.terminal.CardTerminalException;
import opencard.core.service.*;
import opencard.opt.iso.fs.*;

import com.ibm.opencard.service.MFCCardChannel;


/**
 * The interface to a file access card service implementation.
 * A file access service provides read and update access to
 * smartcard files. The implementation of such a service is
 * responsible for creating command APDUs and evaluating the
 * smartcard's response APDUs.
 *
 * @version $Id: MFCFileAccessImpl.java,v 1.2 1998/09/14 11:51:32 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface MFCFileAccessImpl
{

  /**
   * Reads some bytes from a transparent file.
   * The transparent file has to be selected.
   *
   * @param channel   where to contact the smartcard
   * @param offset    index of first byte to read
   * @param length    number of bytes to read, 0 for unspecified
   *
   * @return   array holding the bytes read
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public byte[] readBinary(MFCCardChannel channel,
                           int            offset,
                           int            length)
       throws CardServiceException, CardTerminalException;


  /**
   * Reads a record from a structured file.
   * The structured file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param record   index of the record to read (0 for first record)
   *
   * @return   byte array holding the data read
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public byte[] readRecord(MFCCardChannel channel,
                           int            record)
       throws CardServiceException, CardTerminalException;


  /**
   * Reads the first records from a structured file.
   * The structured file has to be selected.
   *
   * @param channel   how to contact the smartcard
   * @param number    number of records to read, 0 for all
   *
   * @return an array of arrays holding the data read
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public byte[][] readRecords(MFCCardChannel channel,
                              int            number)
       throws CardServiceException, CardTerminalException;


  /**
   * Writes data to a transparent file.
   * The transparent file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param foffset  file index of first byte to write to
   * @param source   the data to write
   * @param soffset  array index of first byte to write
   * @param length   number of bytes to write
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public void writeBinary(MFCCardChannel channel,
                          int            foffset,
                          byte[]         source,
                          int            soffset,
                          int            length)
       throws CardServiceException, CardTerminalException;


  /**
   * Writes data to a structured file.
   * The structured file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param record   number of the record to write (0 for first record)
   * @param source   the data to write in the record
   *
   * @exception CardServiceException   if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public void writeRecord(MFCCardChannel   channel,
                          int              record,
                          byte[]           source)
       throws CardServiceException, CardTerminalException;


  /**
   * Appends data to a structured file.
   * The structured file has to be selected.
   * Appending is supported for cyclic and linear variable files.
   * <br>
   * MFC smartcards prior to 4.1 do not support the APPEND RECORD command.
   * See <tt>MFC35FileAccessImpl.appendRecord</tt> to learn how this situation
   * is dealt with.
   *
   * @param channel  how to contact the smartcard
   * @param data     the data to append
   *
   * @exception CardServiceException   if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   *
   * @see MFC35FileAccessImpl#appendRecord
   */
  public void appendRecord(MFCCardChannel channel,
                           byte[]         data   )
       throws CardServiceException, CardTerminalException;


  /**
   * Increases the value of a record in a structured file.
   * The file has to be selected. The first record within
   * the selected file will be modified. Typically, this
   * operation is used with cyclic files.
   *
   * @param channel   the contact to the smartcard
   * @param delta     the 3-byte unsigned amount by which to increase
   *
   * @return    the record after the operation.
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public byte[] increaseValue(MFCCardChannel channel, int delta)
       throws CardServiceException, CardTerminalException;


  /**
   * Decreases the value of a record in a structured file.
   * The file has to be selected. The first record within
   * the selected file will be modified. Typically, this
   * operation is used with cyclic files.
   *
   * @param channel   the contact to the smartcard
   * @param delta     the 3-byte unsigned amount by which to decrease
   *
   * @return    the record after the operation
   *
   * @exception CardServiceException  if the service encountered an error
   * @exception CardTerminalException if the terminal encountered an error
   */
  public byte[] decreaseValue(MFCCardChannel channel, int delta)
       throws CardServiceException, CardTerminalException;


} // interface MFCFileAccessImpl
