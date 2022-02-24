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


import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;
import opencard.core.service.*;
import opencard.core.util.Tracer;


import com.ibm.opencard.service.MFCCommandAPDU;
import com.ibm.opencard.service.MFCCardChannel;
import com.ibm.opencard.service.MFCCodes;
import com.ibm.opencard.service.MFCCardObjectInfo;
import com.ibm.opencard.service.MFCResponseAPDUCodes;


/**
 * Implementation of a file access card service for MFC 3.5 and some others.
 * File access refers to read and update operations. It does not include
 * create, invalidate or other creational operations.
 *
 * @version $Id: MFC35FileAccessImpl.java,v 1.5 1999/01/19 08:43:20 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35FileAccessImpl implements MFCFileAccessImpl
{
  /** Tracer for debugging output. */
  private static Tracer ctracer = new Tracer(MFC35FileAccessImpl.class);


  /** The CLAss and INStruction bytes for commands. */
  /*final*/ protected MFCCodes mfc_codes = null;

  /** The CLAss byte for ISO commands gets cached here. */
  /*final*/ protected byte iso_class = 0;


  /** A re-usable command APDU for reading binary data. */
  private MFCCommandAPDU read_binary_apdu = null;

  /** A re-usable command APDU for updating binary data. */
  private MFCCommandAPDU update_binary_apdu = null;

  /** A re-usable command APDU for reading record data. */
  private MFCCommandAPDU read_record_apdu = null;

  /** A re-usable command APDU for updating record data. */
  private MFCCommandAPDU update_record_apdu = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new file access card service implementation.
   *
   * @param codes   the command codes for the MFC card to support
   */
  public MFC35FileAccessImpl(MFCCodes codes)
  {
    mfc_codes = codes;
    iso_class = codes.getISOClassByte();
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Reads bytes from a transparent file using a single command.
   * The file has to be selected. The length of the data to read
   * must be small enough to fit into a single response APDU.
   *
   * @param channel   where to contact the smartcard
   * @param accgroup  access group for READ commands
   * @param target    array to fill in the bytes, or <tt>null</tt>
   * @param toffset   array index to start filling. If <tt>target</tt>
   *                  is <tt>null</tt>, this value is ignored.
   * @param foffset   file index to start reading
   * @param length    number of bytes to read and fill in
   *
   * @return array holding the data read.
   *     If <tt>target</tt> is not <tt>null</tt>, it's value is returned.
   *     Otherwise, a new array will be allocated and returned.
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected byte[] readBinaryBytes(MFCCardChannel   channel,
                                   int              accgroup,
                                   byte[]           target,
                                   int              toffset,
                                   int              foffset,
                                   int              length)
       throws CardServiceException, CardTerminalException
  {
    MFCCommandAPDU command = getReadBinaryAPDU();

    command.append((byte)(foffset>>8));
    command.append((byte)(foffset   ));
    command.append((byte)(length    ));

    ResponseAPDU  response = channel.executeCommand(command, accgroup);
    mfc_codes.analyseStatus(response.sw(),
                            (byte) command.getByte(1),
                            "readBinary");

    if (target == null)
      target = response.data();         // common case: sole read command
    else
      System.arraycopy(response.data(), 0, target, toffset, length);

    return target;
  }


  /**
   * Writes bytes to a transparent file using a single command.
   * The file has to be selected. The length of the data to write
   * must be small enough to fit into a single command APDU.
   *
   * @param channel   where to contact the smartcard
   * @param accgroup  access group for UPDATE commands
   * @param foffset   file index to start writing at
   * @param source    array holding the bytes to write
   * @param soffset   first byte to write
   * @param length    number of bytes to write
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected void writeBinaryBytes(MFCCardChannel   channel,
                                  int              accgroup,
                                  int              foffset,
                                  byte[]           source,
                                  int              soffset,
                                  int              length)
       throws CardServiceException, CardTerminalException
  {
    // build the command APDU:
    //  - get pre-initialized APDU
    //  - append P1 and P2, specifying the file offset
    //  - append Lc and the data to write
    // execute and check response

    MFCCommandAPDU command = getUpdateBinaryAPDU();
    command.append((byte) (foffset >> 8));
    command.append((byte) foffset);
    command.appendBlock(source, soffset, length);

    ResponseAPDU  response = channel.executeCommand(command, accgroup);
    mfc_codes.analyseStatus(response.sw(),
                            (byte) command.getByte(1),
                            "writeBinary");

  } // writeBinaryBytes


  /**
   * Reads the first records from a cyclic file with MFC 3.5 or 4.0.
   * MFC cards up to and including 4.0 implement an obscure scheme for
   * addressing cyclic files. The most recently written record has to
   * be selected using absolute addressing and the magic number for the
   * <i>current</i> record. The following records have to be read using
   * relative addressing and the <i>previous</i> mode.
   * <br>
   * This method is invoked only for MFC cards up to and including MFC 4.0,
   * and for cyclic files. Since MFC 4.1, reading cyclic files can be done
   * using absolute addresses, so there is no need for a special method.
   *
   * @param channel   how to contact the smartcard
   * @param command   the APDU to send, pre-initialized
   * @param accgroup  the access group for reading
   * @param number    number of records to read
   *
   * @return an array of arrays holding the data read
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected byte[][] readCyclicRecords(MFCCardChannel   channel,
                                       MFCCommandAPDU   command,
                                       int              accgroup,
                                       int              number)
       throws CardServiceException, CardTerminalException
  {
    byte[][] data = new byte[number][];

    command.setByte(2, 0); // "current" record
    command.setByte(3, 4); // absolute addressing

    for(int i=0; i<number; i++)
      {
        ResponseAPDU response = channel.executeCommand(command, accgroup);
        mfc_codes.analyseStatus(response.sw(),
                                (byte) command.getByte(1),
                                "readCyclicRecords");
        data[i] = response.data();

        command.setByte(3, 3); // "previous" record
      }

    return data;

  } // readCyclicRecords


  /**
   * Writes a record in a cyclic file with MFC 3.5 or 4.0 smartcards.
   * MFC cards prior to 4.1 do not support the APPEND RECORD command.
   * Since they do not support linear variable files either, this does
   * not matter except for cyclic files, in which case a similiar
   * effect can be achieved using the UPDATE BINARY command.
   * <br>
   * This method is invoked only for MFC cards up to and including MFC 4.0,
   * and for cyclic files. Since MFC 4.1, APPEND RECORD is suppported, so
   * there is no need for a special method. The difference lies with the
   * access conditions: APPEND RECORD has command class <i>Create</i>,
   * while UPDATE RECORD has command class <i>Update</i>.
   *
   * @param channel   how to contact the smartcard
   * @param source    the data to write to the file
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected void writeCyclicRecord(MFCCardChannel       channel,
                                   byte[]               source)
       throws CardServiceException, CardTerminalException
  {
    MFCCommandAPDU command = getUpdateRecordAPDU();
    command.append((byte) 0x00);   // ignored
    command.append((byte) 0x02);   // "next" record
    command.appendBlock(source);

    ResponseAPDU response = channel.executeCommand
      (command,  mfc_codes.getAG(MFCCodes.OP_UPDATE_RECORD));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) command.getByte(1),
                            "writeCyclicRecord");
  } // writeCyclicRecord


  /**
   * Returns a command APDU for reading binary data.
   * The APDU is allocated at the first invocation and re-used later.
   * The CLAss and INStruction byte are set appropriately, the rest
   * of the APDU has to be appended by the caller.
   *
   * @return a pre-initialized APDU for the READ BINARY command
   *
   * @see #read_binary_apdu
   */
  private MFCCommandAPDU getReadBinaryAPDU()
  {
    if (read_binary_apdu == null)
      {
        read_binary_apdu = new MFCCommandAPDU(5);
        read_binary_apdu.append(iso_class);
        read_binary_apdu.append(mfc_codes.getINS(MFCCodes.OP_READ_BINARY));

        read_binary_apdu.setRequesterFlag(true); // receives data
      }
    else
      {
        read_binary_apdu.setLength(2);
        // The CLAss byte has to be reset.
        read_binary_apdu.setByte(0, iso_class);
      }

    return read_binary_apdu;
  }


  /**
   * Returns a command APDU for updating binary data.
   * The APDU is allocated at the first invocation and re-used later.
   * The CLAss and INStruction byte are set appropriately, the rest
   * of the APDU has to be appended by the caller.
   *
   * @return a pre-initialized APDU for the UPDATE BINARY command
   *
   * @see #update_binary_apdu
   */
  private MFCCommandAPDU getUpdateBinaryAPDU()
  {
    if (update_binary_apdu == null)
      {
        int maxlen = mfc_codes.getBlockSize() + 6;

        update_binary_apdu = new MFCCommandAPDU(maxlen);
        update_binary_apdu.append(iso_class);
        update_binary_apdu.append(mfc_codes.getINS(MFCCodes.OP_UPDATE_BINARY));

        update_binary_apdu.setProviderFlag(true); // sends data
      }
    else
      {
        update_binary_apdu.setLength(2);
        // The CLAss byte has to be reset.
        update_binary_apdu.setByte(0, iso_class);
      }

    return update_binary_apdu;
  }


  /**
   * Returns a command APDU for reading record data.
   * The APDU is allocated at the first invocation and re-used later.
   * The CLAss and INStruction byte are set appropriately, the rest
   * of the APDU has to be appended by the caller.
   *
   * @return a pre-initialized APDU for the READ RECORD command
   *
   * @see #read_record_apdu
   */
  private MFCCommandAPDU getReadRecordAPDU()
  {
    if (read_record_apdu == null)
      {
        read_record_apdu = new MFCCommandAPDU(5);
        read_record_apdu.append(iso_class);
        read_record_apdu.append(mfc_codes.getINS(MFCCodes.OP_READ_RECORD));

        read_record_apdu.setRequesterFlag(true); // receives data
      }
    else
      {
        read_record_apdu.setLength(2);
        // The CLAss byte has to be reset.
        read_record_apdu.setByte(0, iso_class);
      }

    return read_record_apdu;
  }


  /**
   * Returns a command APDU for updating record data.
   * The APDU is allocated at the first invocation and re-used later.
   * The CLAss and INStruction byte are set appropriately, the rest
   * of the APDU has to be appended by the caller.
   *
   * @return a pre-initialized APDU for the UPDATE RECORD command
   *
   * @see #update_record_apdu
   */
  private MFCCommandAPDU getUpdateRecordAPDU()
  {
    if (update_record_apdu == null)
      {
        int maxlen = mfc_codes.getBlockSize() + 6;

        update_record_apdu = new MFCCommandAPDU(maxlen);
        update_record_apdu.append(iso_class);
        update_record_apdu.append(mfc_codes.getINS(MFCCodes.OP_UPDATE_RECORD));

        update_record_apdu.setProviderFlag(true); // sends data
      }
    else
      {
        update_record_apdu.setLength(2);
        // The CLAss byte has to be reset.
        update_record_apdu.setByte(0, iso_class);
      }

    return update_record_apdu;
  }



  /////////////////////////////////////////////////////////////////////////////
  // file access services //////// MUST BE SYNCHRONIZED !!! ///////////////////
  /////////////////////////////////////////////////////////////////////////////
  // actually, synchronization is needed only if command APDUs are re-used


  /**
   * Reads data from a transparent file.
   * The file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param offset   index of first byte to read
   * @param length   number of bytes to read
   *
   * @return   byte array holding the data read
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized byte[] readBinary(MFCCardChannel  channel,
                                        int             offset,
                                        int             length)
       throws CardServiceException, CardTerminalException
  {
    byte[]   data = null;
    int blocksize = mfc_codes.getBlockSize();
    int accgroup  = mfc_codes.getAG(MFCCodes.OP_READ_BINARY);

    // Check for an unspecified length. Specify if so.
    // In case of length 0, return null
    if (length == 0)
      {
        MFCFileInfo info =
          (MFCFileInfo) channel.getChannelState().getCurrentInfo();
        int maxlen = info.file_size - offset; // remaining bytes in file

        if (maxlen < blocksize)
          length = maxlen;      // read remainder of file
        else
          length = blocksize;   // read with a single command
      }
    if (length == 0)
      return null;

    // The common case that the full data can be read with a single
    // command is checked here, since one arraycopy can be saved.
    if (length <= blocksize)
      {
        data = readBinaryBytes(channel, accgroup, null, 0, offset, length);
      }
    else
      {
        data = new byte [length];
        for(int done=0; done < length; done += blocksize)
          {
            /*
             * - determine request size
             * - read that many
             * - update target index
             */
            int len = blocksize;
            if ((length - done) < blocksize)
              len = length - done;

            readBinaryBytes(channel, accgroup, data, done, offset, len);

            offset += len;
          }
      }

    return data;

  } // readBinary()


  /**
   * Reads a record from a structured file.
   * The file has to be selected, and the file info
   * must have been set in the channel state.
   *
   * @param channel  how to contact the smartcard
   * @param record   number of the record to read (0 for first record)
   *
   * @return   byte array holding the data read
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized byte[] readRecord(MFCCardChannel  channel,
                                        int             record)
       throws CardServiceException, CardTerminalException
  {
    int size = 0;

    if (!mfc_codes.needsZeroLe())
      {
        // give the precise length of the response data
        MFCFileInfo mfi = (MFCFileInfo)
          channel.getChannelState().getCurrentInfo();
        size = mfi.record_size;
      }

    MFCCommandAPDU command = getReadRecordAPDU();
    command.append((byte)(record+1));   // record number
    command.append((byte) 0x04     );   // absolute addressing
    command.append((byte) size     );

    ResponseAPDU response = channel.executeCommand
      (command, mfc_codes.getAG(MFCCodes.OP_READ_RECORD));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) command.getByte(1),
                            "readRecord");

    // Linear variable files may contain empty records.
    // In this case, we return an empty array instead of null.
    byte[] data = response.data();
    if (data == null)
      data = new byte[0];

    return data;

  } // readRecord()


  /**
   * Reads the first records from a structured file.
   * The structured file has to be selected.
   *
   * @param channel   how to contact the smartcard
   * @param number    number of records to read, 0 for all
   *
   * @return an array of arrays holding the data read
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized byte[][] readRecords(MFCCardChannel channel,
                                           int            number)
       throws CardServiceException, CardTerminalException
  {
    MFCFileInfo mfi = (MFCFileInfo) channel.getChannelState().getCurrentInfo();
    int accgroup = mfc_codes.getAG(MFCCodes.OP_READ_RECORD);

    boolean readall = false; // set only for variable files

    // check parameter 'number'
    if (!mfi.isVariable())
      {
        int records = mfi.file_size / mfi.record_size;

        if (number == 0)                // read all?
          number = records;             // compute number
        else if (number > records)
          throw new CardServiceInvalidParameterException
            ("readRecords: beyond EOF");
      }
    else if (number == 0) // variable file, read all records
      {
        readall = true;                 // read until an error occurs
        number  = mfi.file_size/2;      // ...if all records were empty
        if (number > 254)
          number = 254;                 // ...maximum for records in file
      }

    byte     size = (byte) (mfc_codes.needsZeroLe() ? 0 : mfi.record_size);
    byte[][] data = null;

    MFCCommandAPDU command = getReadRecordAPDU();
    command.append((byte)0);   // "current" record
    command.append((byte)4);   // absolute addressing
    command.append((byte)size);

    if (mfi.is_cyclic && !mfc_codes.needsZeroLe())
      {
        // encountered the special case of a cyclic file on a MFC 4.0 or below
        // P1 and P2 of the command will be changed appropriately
        data = readCyclicRecords(channel, command, accgroup, number);
      }
    else // use absolute addressing
      {
        data = new byte [number][];
        for(int i=0; i < number; i++)
          {
            command.setByte(2, i+1); // absolute record address

            ResponseAPDU response = channel.executeCommand(command, accgroup);
            int status = response.sw();
            if (!mfc_codes.indicatesError(status)) // no error
              {
                // store the record in the data array
                // empty records are stored as arrays with length 0

                byte[] record = response.data();
                if (record == null)
                  record = new byte[0];
                data[i] = record;
              }
            else if (readall) // expected error
              {
                // We have been reading until the first error.
                // The array for the records has been allocated
                // with maximum (worst case) size. Shrink it to
                // what's needed, then terminate the loop.

                byte[][] records = new byte[i][];
                for(int j=0; j<i; j++)
                  records[j] = data[j];
                data = records;

                i = number; // ignore the error and terminate
              }
            else // unexpected error
              {
                // Check the status and throw an exception.
                mfc_codes.analyseStatus(response.sw(),
                                        (byte) command.getByte(1),
                                        "readRecords");
              }
          } // for i
      }

    return data;

  } // readRecords


  /**
   * Writes data to a transparent file.
   * The file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param foffset  file index of first byte to write to
   * @param source   the data to write
   * @param soffset  array index of first byte to write
   * @param length   number of bytes to write
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void writeBinary(MFCCardChannel channel,
                                       int            foffset,
                                       byte[]         source,
                                       int            soffset,
                                       int            length)
       throws CardServiceException, CardTerminalException
  {
    int blocksize = mfc_codes.getBlockSize();
    int accgroup  = mfc_codes.getAG(MFCCodes.OP_UPDATE_BINARY);

    for(int done=0; done < length; done += blocksize)
      {
        /*
         * - determine request size
         * - write that many
         * - update file index
         */
        int len = blocksize;
        if ((length - done) < blocksize)
          len = length - done;

        writeBinaryBytes(channel, accgroup,
                         foffset, source, soffset+done, len);

        foffset += len;
      }

  } // writeBinary()


  /**
   * Writes data to a structured file.
   * The file has to be selected.
   *
   * @param channel  how to contact the smartcard
   * @param source   the data to write in the record
   * @param record   number of the record to write (0 for first record)
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void writeRecord(MFCCardChannel   channel,
                                       int              record,
                                       byte[]           source)
       throws CardServiceException, CardTerminalException
  {
    MFCCommandAPDU command = getUpdateRecordAPDU();
    command.append((byte) (record+1));   // record number
    command.append((byte) 0x04);   // absolute addressing
    command.appendBlock(source);

    ResponseAPDU response = channel.executeCommand
      (command,  mfc_codes.getAG(MFCCodes.OP_UPDATE_RECORD));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) command.getByte(1),
                            "writeRecord");

  } // writeRecord


  /**
   * Appends data to a structured file.
   * The structured file has to be selected.
   * Appending is supported for cyclic and linear variable files.
   * <br>
   * MFC smartcards prior to 4.1 do not support the APPEND RECORD command.
   * Invocations of this method are mapped onto the UPDATE RECORD command
   * in this case. When designing a card layout, it has to be taken into
   * account that the access condition is taken from the <i>Update</i> group,
   * not the <i>Create</i> group. Additionally, the UPDATE RECORD command
   * is mutually exclusive with the INCREASE command. Implementing a purse
   * that supports appending records as well as increasing the value is
   * just not possible with MFC smartcards prior to 4.1.
   * See also <tt>writeCyclicRecord</tt>, which is invoked in this case.
   *
   * @param channel  how to contact the smartcard
   * @param data     the data to append
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #writeCyclicRecord
   */
  public synchronized void appendRecord(MFCCardChannel channel,
                                        byte[]         data   )
       throws CardServiceException, CardTerminalException
  {
    MFCFileInfo mfi = (MFCFileInfo) channel.getChannelState().getCurrentInfo();

    if (mfi.is_cyclic && !mfc_codes.needsZeroLe())
      {
        // encountered the special case of a cyclic file on a MFC 4.0 or below
        writeCyclicRecord(channel, data);
      }
    else // use the APPEND RECORD command
      {
        // this command is rare, the APDU is not re-used
        MFCCommandAPDU command = new MFCCommandAPDU(5 + data.length);
        command.append(mfc_codes.getISOClassByte());          // CLA
        command.append(mfc_codes.getINS(MFCCodes.OP_APPEND_RECORD));
        command.append((byte)0);                              // P1
        command.append((byte)0);                              // P2
        command.appendBlock(data);                            // Lc, data

        ResponseAPDU response = channel.executeCommand
          (command,  mfc_codes.getAG(MFCCodes.OP_APPEND_RECORD));
        mfc_codes.analyseStatus(response.sw(),
                                (byte) command.getByte(1),
                                "appendRecord");
      }

  } // appendRecord


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
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized byte[] increaseValue(MFCCardChannel channel, int delta)
       throws CardServiceException, CardTerminalException
  {
    byte instruction = mfc_codes.getINS(MFCCodes.OP_INCREASE);

    // this command is rare, so the APDU is not re-used.
    MFCCommandAPDU command = new MFCCommandAPDU(9);
    command.append(mfc_codes.getClassByte());         // CLA
    command.append(instruction);                      // INS
    command.append((byte)0);                          // P1
    command.append((byte)0);                          // P2
    command.append((byte)3);                          // Lc
    command.append((byte)(delta >>> 16));             // amount, high byte
    command.append((byte)(delta >>>  8));             // amount, mid byte
    command.append((byte) delta        );             // amount, low byte

    if (mfc_codes.needsZeroLe())
      command.append((byte)0);

    command.setProviderFlag(true);
    command.setRequesterFlag(true);

    ResponseAPDU response = channel.executeCommand
      (command, mfc_codes.getAG(MFCCodes.OP_INCREASE));
    mfc_codes.analyseStatus(response.sw(), instruction, "increase");

    return response.data();

  } // increaseValue


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
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized byte[] decreaseValue(MFCCardChannel channel, int delta)
       throws CardServiceException, CardTerminalException
  {
    byte instruction = mfc_codes.getINS(MFCCodes.OP_DECREASE);

    // this command is rare, so the APDU is not re-used.
    MFCCommandAPDU command = new MFCCommandAPDU(9);
    command.append(mfc_codes.getClassByte());         // CLA
    command.append(instruction);                      // INS
    command.append((byte)0);                          // P1
    command.append((byte)0);                          // P2
    command.append((byte)3);                          // Lc
    command.append((byte)(delta >>> 16));             // amount, high byte
    command.append((byte)(delta >>>  8));             // amount, mid byte
    command.append((byte) delta        );             // amount, low byte

    if (mfc_codes.needsZeroLe())
      command.append((byte)0);

    command.setProviderFlag(true);
    command.setRequesterFlag(true);

    ResponseAPDU response = channel.executeCommand
      (command, mfc_codes.getAG(MFCCodes.OP_DECREASE));
    mfc_codes.analyseStatus(response.sw(), instruction, "decrease");

    return response.data();

  } // decreaseValue


} // class MFC35FileAccessImpl
