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


import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import com.ibm.opencard.service.MFCCardChannel;


/**
 * An interface to a file system service implementation.
 * A file system service provides creational access to smartcard
 * files, for example CREATE, DELETE, or INVALIDATE. The implementation
 * of such a service is responsible for creating command APDUs and
 * evaluating the smartcard's response APDUs.
 *
 * @version $Id: MFCFileSystemImpl.java,v 1.3 1998/08/11 11:04:52 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface MFCFileSystemImpl
{

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
  public void createFile(MFCCardChannel channel,
                         byte[]         header )
       throws CardServiceException, CardTerminalException;


  /**
   * Deletes a file on the smartcard.
   * The parent directory of the file to delete has to be selected.
   * The file itself may be an elementary or dedicated file. The
   * smartcard's restrictions for deleting files apply.
   *
   * @param channel   the contact to the smartcard
   * @param file      the identifier of the file to delete
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void deleteFile(MFCCardChannel channel,
                         short          file   )
       throws CardServiceException, CardTerminalException;


  /**
   * Invalidates a file on the smartcard.
   * The file to be invalidated has to be selected.
   * The file itself may be a dedicated or elementary file.
   *
   * @param channel   the contact to the smartcard
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void invalidateFile(MFCCardChannel channel)
       throws CardServiceException, CardTerminalException;


  /**
   * Rehabilitates a file on the smartcard.
   * The file to be rehabilitated has to be selected.
   * The file itself may be a dedicated or elementary file.
   *
   * @param channel   the contact to the smartcard
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void rehabilitateFile(MFCCardChannel channel)
       throws CardServiceException, CardTerminalException;


} // interface MFCFileSystemImpl
