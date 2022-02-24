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
import opencard.core.service.CardServiceException;
//import opencard.core.util.Tracer;

import com.ibm.opencard.service.MFCCodes;
import com.ibm.opencard.service.MFCCardChannel;
import com.ibm.opencard.service.MFCCardObjectInfo;
import com.ibm.opencard.service.MFCCommandAPDU;
import com.ibm.opencard.service.MFCResponseAPDUCodes;


/**
 * Implementation of a file system card service for MFC 3.5 and some other.
 * The file system service provides creational operations, like creating,
 * deleting or invalidating files on the smartcard.
 *
 * @version $Id: MFC35FileSystemImpl.java,v 1.14 1999/01/19 08:43:20 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35FileSystemImpl implements MFCFileSystemImpl
{
//private final static Tracer ctracer = new Tracer(MFC35FileSystemImpl.class);


  /** The CLAss and INStruction bytes for commands. */
  /*final*/ protected MFCCodes mfc_codes = null;


  /** A re-usable command APDU for deleting a file. */
  private MFCCommandAPDU delete_apdu = null;

  /** A re-usable command APDU for invalidating a file. */
  private MFCCommandAPDU invalidate_apdu = null;

  /** A re-usable command APDU for rehabilitating a file. */
  private MFCCommandAPDU rehabilitate_apdu = null;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new file system service for MFC 3.5 and above smartcards.
   *
   * @param codes   the command codes for the MFC card to support
   */
  public MFC35FileSystemImpl(MFCCodes codes)
  {
    mfc_codes = codes;
  }



  // service //////////////////////////////////////////////////////////////////


  /////////////////////////////////////////////////////////////////////////////
  // file system service /////// MUST BE SYNCHRONIZED !!! /////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // actually, synchronization is needed only if APDUs are re-used


  /**
   * Creates a file on the smartcard.
   * The directory in which the file should be created has to be
   * selected. The file itself may be an elementary or dedicated
   * file. All information required to create the file is encoded
   * in that header. The encoding is card-specific.
   *
   * @param channel    the contact to the smartcard
   * @param header     the header of the file to create
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void createFile(MFCCardChannel channel,
                                      byte[]         header)
       throws CardServiceException, CardTerminalException
  {
    byte ins = mfc_codes.getINS(MFCCodes.OP_CREATE_FILE);

    // The size of the APDU is basically unlimited, and the command is rare.
    // Therefore, this APDU does not get re-used.
    MFCCommandAPDU create_apdu = new MFCCommandAPDU(header.length + 5);

    create_apdu.append(mfc_codes.getClassByte());         // CLA
    create_apdu.append(ins);                              // INS
    create_apdu.append((byte) 0);                         // P1
    create_apdu.append((byte)0);                          // P2
    create_apdu.appendBlock(header);                      // Lc, data
    create_apdu.setProviderFlag(true);

    ResponseAPDU  response = channel.executeCommand
      (create_apdu, mfc_codes.getAG(MFCCodes.OP_CREATE_FILE));
    mfc_codes.analyseStatus(response.sw(), ins, "createFile");

    // The CREATE command implicitly selects the created file. The
    // current selection will be invalidated to maintain consistency
    // of the channel state. This enforces a select if the file is
    // subsequently accessed, but is much simpler than duplicating
    // some of the code in the implementation of the select command.

    channel.getChannelState().setCurrentPath(null);

  } // createFile


  /**
   * Deletes a file on the smartcard.
   * The parent directory of the file has to be selected.
   *
   * @param channel   how to contact the smartcard
   * @param file      the ID of the file to delete
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void deleteFile(MFCCardChannel channel,
                                      short          file   )
       throws CardServiceException, CardTerminalException
  {
    if (delete_apdu == null)
      {
        delete_apdu = new MFCCommandAPDU(7);
        delete_apdu.append(mfc_codes.getClassByte());
        delete_apdu.append(mfc_codes.getINS(MFCCodes.OP_DELETE_FILE));
        delete_apdu.append((byte)0);                       // P1
        delete_apdu.append((byte)0);                       // P2
        delete_apdu.append((byte)2);                       // Lc (file ID)

        delete_apdu.setProviderFlag(true);
      }
    else
      {
        delete_apdu.setLength(5);
        // ISO secure messaging may have changed the CLAss byte
        delete_apdu.setByte(0, mfc_codes.getClassByte());
      }

    delete_apdu.append((byte)(file >> 8));
    delete_apdu.append((byte) file      );

    ResponseAPDU  response = channel.executeCommand
      (delete_apdu, mfc_codes.getAG(MFCCodes.OP_DELETE_FILE));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) delete_apdu.getByte(1),
                            "deleteFile");

  } // deleteFile


  /**
   * Invalidates a file on the smartcard.
   * The file has to be selected. It has to be valid.
   *
   * @param channel   how to contact the smartcard
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void invalidateFile(MFCCardChannel channel)
       throws CardServiceException, CardTerminalException
  {
    if (invalidate_apdu == null)
      {
        invalidate_apdu = new MFCCommandAPDU(5);
        invalidate_apdu.append(mfc_codes.getClassByte());
        invalidate_apdu.append(mfc_codes.getINS(MFCCodes.OP_INVALIDATE));
        invalidate_apdu.append((byte)0); // P1
        invalidate_apdu.append((byte)0); // P2

        if (mfc_codes.needsZeroLc())
          invalidate_apdu.append((byte)0);

        // The invalidate command does not really provide data.
        // However, if secure messaging is required, it is the
        // command that has to be protected, therefore the flag.
        invalidate_apdu.setProviderFlag(true);
      }
    else // ISO secure messaging may have changed the CLAss byte
      invalidate_apdu.setByte(0, mfc_codes.getClassByte());

    ResponseAPDU  response = channel.executeCommand
      (invalidate_apdu, mfc_codes.getAG(MFCCodes.OP_INVALIDATE));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) invalidate_apdu.getByte(1),
                            "invalidate");

  } // invalidateFile


  /**
   * Rehabilitates a file on the smartcard.
   * The file has to be selected. It has to be invalid.
   *
   * @param channel   how to contact the smartcard
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public synchronized void rehabilitateFile(MFCCardChannel channel)
       throws CardServiceException, CardTerminalException
  {
    if (rehabilitate_apdu == null)
      {
        rehabilitate_apdu = new MFCCommandAPDU(5);
        rehabilitate_apdu.append(mfc_codes.getClassByte());
        rehabilitate_apdu.append(mfc_codes.getINS(MFCCodes.OP_REHABILITATE));
        rehabilitate_apdu.append((byte)0);
        rehabilitate_apdu.append((byte)0); // P1, P2

        if (mfc_codes.needsZeroLc())
          rehabilitate_apdu.append((byte)0);

        // The rehabilitate command does not really provide data.
        // However, if secure messaging is required, it is the
        // command that has to be protected, therefore the flag.
        rehabilitate_apdu.setProviderFlag(true);
      }
    else // ISO secure messaging may have changed the CLAss byte
      rehabilitate_apdu.setByte(0, mfc_codes.getClassByte());

    ResponseAPDU  response = channel.executeCommand
      (rehabilitate_apdu, mfc_codes.getAG(MFCCodes.OP_REHABILITATE));
    mfc_codes.analyseStatus(response.sw(),
                            (byte) rehabilitate_apdu.getByte(1),
                            "rehabilitate");

  } // rehabilitateFile

} // class MFC35FileSystemImpl
