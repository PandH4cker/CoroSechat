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


import opencard.core.service.SmartCard;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.terminal.CardTerminalException;

import opencard.opt.service.CardServiceObjectNotAvailableException;
import opencard.opt.service.CardServiceUnexpectedResponseException;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFileInfo;

import com.ibm.opencard.service.MFCCardService;
import com.ibm.opencard.service.MFCCardChannel;


/**
 * A file access service for MFC smartcards.
 * It provides methods for reading and updating smartcard files.
 *
 * @version $Id: MFCFileAccess.java,v 1.5 1999/03/11 13:32:23 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCFileAccess extends MFCCardService
    implements FileAccessCardService
{
  /** The file system card service implementation to use. */
  protected MFCFileAccessImpl file_access_impl = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Creates a new MFC file access card service.
   * Before the service can actually be used, it has to be initialized
   * by invoking <tt>initialize</tt>. If this service has to be initialized
   * as part of a derived service, <tt>initFileAccess</tt> has to be
   * invoked instead.
   *
   * @see #initialize
   * @see #initFileAccess
   */
  public MFCFileAccess()
  {
    // no body
  }


  /**
   * Initializes this service.
   * This is an entry point for initializing the MFC file access card
   * service. It invokes <tt>initFileAccess</tt> to perform the actual
   * initialization. Derived services must not invoke this method, but
   * have to invoke <tt>initFileAccess</tt> directly.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   which smartcard to contact
   * @param blocking    whether operation shall be blocking
   *
   * @see #initFileAccess
   * @see com.ibm.opencard.service.MFCCardService#initialize
   */
  protected void initialize(CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking )
    throws CardServiceException
  {
    initFileAccess(MFCFileFactory.newFileParam(scheduler,
                                               smartcard,
                                               blocking,
                                               card_type));
  }


  /**
   * Initializes this service from encapsulated parameters.
   * This method initializes the local attributes and invokes
   * <tt>initGeneric</tt> in the base class.
   *
   * @param parameter   an object encapsulating the parameters to this service
   *
   * @exception CardServiceException
   *            if the initialization failed.
   *            With the current implementation, this cannot happen.
   *
   * @see MFCFileParameter
   * @see com.ibm.opencard.service.MFCCardService#initGeneric
   */
  public final void initFileAccess(MFCFileParameter parameter)
       throws CardServiceException
  {
    super.initGeneric(parameter);

    file_access_impl = parameter.fa_impl;
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Selects a file or directory on the smartcard.
   * The implementation of this method is provided by the base class,
   * <tt>MFCCardService</tt>. This file card service uses a file info
   * parser to interpret the card's response to the SELECT command, so
   * file or directory specific information will be available.
   *
   * @param file   the file or directory to select
   *
   * @return   information about the object that has been selected
   *
   * @exception CardServiceObjectNotAvailableException
   *            if the target could not be selected
   * @exception CardServiceUnexpectedResponseException
   *            if the select response could not be parsed, or
   *            an unexpected error status was reported
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see com.ibm.opencard.service.MFCCardService#selectObject
   * @see com.ibm.opencard.isofs.MFC35FileSRParser
   * @see com.ibm.opencard.isofs.MFCFileInfo
   */
  final public MFCFileInfo selectFile(CardFilePath file)
       throws CardServiceObjectNotAvailableException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    checkFileArg(file);

    MFCFileInfo result = null;

    try {
      allocateCardChannel();

      result = (MFCFileInfo) selectObject(getMFCChannel(), file, true);

    } finally {
      releaseCardChannel();
    }

    return result;
  }


  /**
   * Checks whether a file or directory exists on the smartcard.
   * The target will be selected, without requesting information.
   * If the select succeeds, the target exists and <tt>true</tt>
   * is returned.
   *
   * @param file   the file or directory to check for
   * @return       <tt>true</tt> if the file or directory exists,
   *               <tt>false</tt> if not
   *
   * @exception CardServiceUnexpectedResponseException
   *            if the card reported an unexpected error status
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  final public boolean exists(CardFilePath file)
       throws CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    checkFileArg(file);

    boolean success = true;

    try {
      allocateCardChannel();

      // Select and catch the 'FileNotFound' exception.
      // Any other exception will be thrown on.

      try {
        selectObject(getMFCChannel(), file, false);
      } catch (CardServiceObjectNotAvailableException e) {
        success = false;
      }

    } finally {
      releaseCardChannel();
    }

    return success;
  }


  /**
   * Returns information about a file on the smartcard.
   * The information can be used to determine whether the file is a
   * dedicated file, transparent, structured, and so on. Some other
   * information, for example the file size or an optional record
   * size, can be queried, too.
   *
   * @param file   the file or directory about which to return information
   * @return       information about the specified target
   *
   * @exception CardServiceObjectNotAvailableException
   *            if the target could not be selected
   * @exception CardServiceUnexpectedResponseException
   *            if the select response could not be parsed, or
   *            an unexpected error status was reported
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public CardFileInfo getFileInfo(CardFilePath file)
       throws CardServiceObjectNotAvailableException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    checkFileArg(file);

    CardFileInfo info = null;

    try {
      allocateCardChannel();
      info = (MFCFileInfo) selectObject(getMFCChannel(), file, true);
    } finally {
      releaseCardChannel();
    }

    return info;

  } // getFileInfo


  /**
   * Reads data from a transparent smartcard file.
   *
   * @param file    the file to read from
   * @param offset  index of first byte to read
   * @param length  number of bytes to read
   *
   * @return   the data read in a byte array
   *
   * @exception CardServiceException  if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public byte[] read(CardFilePath file, int offset, int length)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check magic number for length
     * - check parameters
     * - get channel
     * - select file
     * - read by calling implementation
     * - release channel
     */
    if (length == READ_SEVERAL)
      length = 0;

    checkFileArg(file);

    if ((offset < 0) || (length < 0))
      throw new CardServiceInvalidParameterException
        ("read: offset = " + offset + ", length = " + length);

    byte[] result = null;

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      result = file_access_impl.readBinary(channel, offset, length);

    } finally {
      releaseCardChannel();
    }

    return result;

  } // read


  /**
   * Reads data from a structured smartcard file.
   *
   * @param file        the file to read from
   * @param record      the record to read (0 for first)
   *
   * @return   the data read in a byte array
   *
   * @exception CardServiceException  if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public byte[] readRecord(CardFilePath file, int record)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select file
     * - read by calling implementation
     * - release channel
     */
    checkFileArg(file);

    if (record < 0)
      throw new CardServiceInvalidParameterException
        ("readRecord: record number " + record);

    byte[] result = null;

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      result = file_access_impl.readRecord(channel, record);

    } finally {
      releaseCardChannel();
    }

    return result;

  } // readRecord


  /**
   * Reads data from a structured, cyclic file.
   * This method reads the first records from a structured file.
   * It is especially intended for reading cyclic files.
   *
   * @param file    the file to read from
   * @param number  the number of records to read
   *
   * @exception CardServiceException   if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public byte[][] readRecords(CardFilePath file, int number)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check for magic number
     * - check parameters
     * - allocate channel
     * - select file
     * - read records using the implementation
     * - release channel
     */
    if (number == FileAccessCardService.READ_SEVERAL)
      number = 0;

    checkFileArg(file);

    if (number < 0)
      throw new CardServiceInvalidParameterException
        ("readRecords: number = " + number);

    byte[][] records = null;

    try {
      allocateCardChannel();
      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      records = file_access_impl.readRecords(channel, number);

    } finally {
      releaseCardChannel();
    }

    return records;
  }


  /**
   * Writes data to a transparent smartcard file.
   *
   * @param file     the file to write to
   * @param foffset  file index to start writing at
   * @param source   the data to write
   * @param soffset  array index of data to write
   * @param length   number of bytes to write
   *
   * @exception CardServiceException   if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void write(CardFilePath file,
                    int          foffset,
                    byte[]       source,
                    int          soffset,
                    int          length)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select file
     * - write by calling implementation
     * - release channel
     */
    checkFileArg(file);

    if ((soffset < 0) || (foffset < 0) || (length <= 0))
      throw new CardServiceInvalidParameterException
        ("write: soffset = " + soffset +
         ", foffset = " + foffset + ", length = " + length);

    if (source == null)
      throw new CardServiceInvalidParameterException
        ("write: source = null");

    if (source.length < soffset + length)
      throw new CardServiceInvalidParameterException
        ("write: source[" + source.length + "], soffset = " + soffset +
         ", length = " + length);

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      file_access_impl.writeBinary(channel, foffset, source, soffset, length);

    } finally {
      releaseCardChannel();
    }

  } // write


  /**
   * Writes a complete array to a transparent smartcard file.
   * This is a convenience method that requires less arguments,
   * but does not allow to write only parts of the source array.
   *
   * @param file     the file to write to
   * @param foffset  file index to start writing at
   * @param source   the data to write
   *
   * @see #write(opencard.opt.iso.fs.CardFilePath, int, byte[], int, int)
   */
  public void write(CardFilePath file,
                    int          foffset,
                    byte[]       source)
       throws CardServiceException, CardTerminalException
  {
    write(file, foffset, source, 0, source.length);
  }


  /**
   * Writes data to a structured smartcard file.
   * Structured smartcard files consist of records, usually with
   * a fixed record size. The byte array passed as data source to
   * this method must have exactly the size of the record to write.
   *
   * @param file        the file to write to
   * @param record      the number of the record to write (0 for first)
   * @param source      the data to write
   *
   * @exception CardServiceException  if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void writeRecord(CardFilePath file, int record, byte[] source)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select file
     * - write by calling implementation
     * - release channel
     */
    checkFileArg(file);

    if (record < 0)
      throw new CardServiceInvalidParameterException
        ("writeRecord: record number " + record);

    if (source == null)
      throw new CardServiceInvalidParameterException
        ("writeRecord: source = null");

    // we can't check source.length, since we don't know the record size here


    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      file_access_impl.writeRecord(channel, record, source);

    } finally {
      releaseCardChannel();
    }

  } // writeRecord


  /*
   * Appends data to a structured file.
   * For a discussion of structured file types, see <tt>readRecord</tt>.
   * For linear files with or variable record size, this method appends
   * a new record at the end of the file. Typically, the space for
   * appending a record must have been allocated at the time the file was
   * created. For cyclic files, this method overwrites the oldest record
   * in the ring buffer, which the becomes the newest. The size of the data
   * has to match the file's record size exactly. For files with linear
   * structure and fixed record size, appending is not supported.
   * <br>
   * MFC smartcards prior to 4.1 do not support the APPEND RECORD command.
   * See <tt>MFC35FileAccessImpl.appendRecord</tt> to learn how this situation
   * is dealt with.
   *
   * @param file        the path to the file to append to
   * @param data        the data to write to the new record
   *
   * @exception CardServiceException
   *            if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see MFC35FileAccessImpl#appendRecord
   */
  public void appendRecord(CardFilePath file, byte[] data)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - check parameters
     * - allocate channel
     * - select file
     * - append by calling implementation
     * - release channel
     */
    checkFileArg(file);

    if (data == null)
      throw new CardServiceInvalidParameterException
        ("appendRecord: data = null");

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      file_access_impl.appendRecord(channel, data);

    } finally {
      releaseCardChannel();
    }

  } // appendRecord


  /**
   * Increases the value stored in a record of a structured file.
   * This operation is typically used for purse applications, in conjunction
   * with cyclic record files. The record updated is always the first one.
   *
   * @param file   the file in which to increase a record's value
   * @param delta  the 3-byte amount by which to increase the value
   *
   * @return   the contents of the modified record after the operation
   *
   * @exception CardServiceException  if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public byte[] increase(CardFilePath file, int delta)
       throws CardServiceException, CardTerminalException
  {
    checkFileArg(file);

    if ((delta < 0) || (delta > 0x00ffffff))
      throw new CardServiceInvalidParameterException
        ("increase: delta " + delta);

    byte[] data = null;

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      data = file_access_impl.increaseValue(channel, delta);

    } finally {
      releaseCardChannel();
    }

    return data;
  }


  /**
   * Decreases the value stored in a record of a structured file.
   * This operation is typically used for purse applications, in conjunction
   * with cyclic record files. The record updated is always the first one.
   *
   * @param file   the file in which to decrease a record's value
   * @param delta  the 3-byte amount by which to decrease the value
   *
   * @return   the contents of the modified record after the operation
   *
   * @exception CardServiceException  if anything went wrong
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public byte[] decrease(CardFilePath file, int delta)
       throws CardServiceException, CardTerminalException
  {
    checkFileArg(file);

    if ((delta < 0) || (delta > 0x00ffffff))
      throw new CardServiceInvalidParameterException
        ("decrease: delta " + delta);

    byte[] data = null;

    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      data = file_access_impl.decreaseValue(channel, delta);

    } finally {
      releaseCardChannel();
    }

    return data;
  }


} // class MFCFileAccess
