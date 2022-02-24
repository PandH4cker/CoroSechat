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
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.terminal.CardTerminalException;

import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.CardFileFileID;
import opencard.opt.iso.fs.CardFileShortFileID;
import opencard.opt.iso.fs.FileSystemCardService;

import com.ibm.opencard.service.MFCCardChannel;


/**
 * A file system service for MFC smartcards.
 * The file system service extends the file service by creational operations,
 * for example to create or invalidate files.
 *
 * @version $Id: MFCFileSystem.java,v 1.15 1999/03/11 13:32:24 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCFileSystem extends MFCFileAccess
   implements FileSystemCardService
{
  /** The file system card service implementation to use. */
  MFCFileSystemImpl file_system_impl = null;


  // construction /////////////////////////////////////////////////////////////


  /**
   * Creates a new MFC file system card service.
   * Before the service can actually be used, it has to be initialized by
   * invoking <tt>initialize</tt>. If this service has to be initialized
   * as part of a derived service, initialization has to be done by invoking
   * <tt>initFileSystem</tt> instead.
   */
  public MFCFileSystem()
  {
    // no body
  }


  /**
   * Initializes this service.
   * This is an entry point for initializing the MFC file access card
   * service. It invokes <tt>initFileSystem</tt> to perform the actual
   * initialization. Derived services must not invoke this method, but
   * have to invoke <tt>initFileSystem</tt> directly.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   which smartcard to contact
   * @param blocking    whether operation shall be blocking
   *
   * @see #initFileSystem
   * @see com.ibm.opencard.service.MFCCardService#initialize
   */
  protected void initialize(CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking )
    throws CardServiceException
  {
    initFileSystem(MFCFileFactory.newFileSysParam(scheduler,
                                                  smartcard,
                                                  blocking,
                                                  card_type));
  }


  /**
   * Initializes this service from encapsulated parameters.
   * This method initializes the local attributes and invokes
   * <tt>initFileAccess</tt> in the base class.
   *
   * @param parameter   an object encapsulating the parameters to this service
   *
   * @exception CardServiceException
   *            if the initialization failed.
   *            With the current implementation, this cannot happen.
   *
   * @see MFCFileSysParameter
   * @see MFCFileAccess#initFileAccess
   */
  public final void initFileSystem(MFCFileSysParameter parameter)
       throws CardServiceException
  {
    super.initFileAccess(parameter);

    file_system_impl = parameter.fsys_impl;
  }


  // service //////////////////////////////////////////////////////////////////


  /**
   * Creates a file on the smartcard.
   * The file may be an elementary or dedicated file.
   * The information required to create the file is encoded in the
   * file header. The encoding is card-specific.
   *
   * @param parent   the path of the directory to create the file in
   * @param header   the header of the file to create
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void create(CardFilePath parent, byte[] header)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - allocate a card channel
     * - select the parent directory
     * - create the new file by calling implementation
     * - release the card channel
     */
    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, parent, true);
      file_system_impl.createFile(channel, header);

    } finally {
      releaseCardChannel();
    }

  } // create


  /*
   * Deletes a file on the smartcard.
   * The file may be an elementary or dedicated file.
   *
   * @param file   the file to invalidate
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void delete(CardFilePath file)
       throws CardServiceException, CardTerminalException
  {
    CardFilePath parent = new CardFilePath(file);
    boolean      ok     = parent.chompTail();   // determine parent

    if (!ok)
      throw new CardServiceInvalidParameterException
        ("cannot deduce parent DF of " + file.toString());

    CardFilePathComponent target = file.tail();

    short fileID;

    if      (target instanceof CardFileFileID)
      fileID = (short) (((CardFileFileID)target).toShort());
    else if (target instanceof CardFileShortFileID)
      fileID = (short) (((CardFileShortFileID)target).toByte() | 0xfd00);
    else
      throw new CardServiceInvalidParameterException
        ("unsupported path component: " + target.toString());

    /*
     * Now that the target is identified, do the rest:
     * - allocate channel
     * - select parent directory
     * - delete file by calling implementation
     * - remove file from cache
     * - release channel
     */
    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, parent, true);
      file_system_impl.deleteFile(channel, fileID);

      channel.getChannelState().removeObjectInfo(file);

    } finally {
      releaseCardChannel();
    }

  } // delete


  /**
   * Invalidates a file on the smartcard.
   * The file may be an elementary or dedicated file.
   *
   * @param file   the file to invalidate
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void invalidate(CardFilePath file)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - get channel
     * - select file
     * - invalidate file by calling implementation
     * - release channel
     */
    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      file_system_impl.invalidateFile(channel);

    } finally {
      releaseCardChannel();
    }

  } // invalidate


  /*
   * Rehabilitates a file on the smartcard.
   * The file may be an elementary or dedicated file.
   *
   * @param file   the file to rehabilitate
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void rehabilitate(CardFilePath file)
       throws CardServiceException, CardTerminalException
  {
    /*
     * - get channel
     * - select file
     * - rehabilitate file by calling implementation
     * - release channel
     */
    try {
      allocateCardChannel();

      MFCCardChannel channel = getMFCChannel();

      selectObject(channel, file, true);
      file_system_impl.rehabilitateFile(channel);

    } finally {
      releaseCardChannel();
    }

  } // rehabilitate


} // class MFCFileSystem
