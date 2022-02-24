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


import opencard.core.terminal.CardTerminalException;

import opencard.opt.service.CardServiceObjectNotAvailableException;
import opencard.opt.service.CardServiceUnexpectedResponseException;

import opencard.opt.iso.fs.CardFilePath;


/**
 * Interface for generic card service implementations.
 * A card service implementation is a stateless object that
 * builds the commands that have to be sent to the smartcard.
 * The generic card service offers <tt>select</tt> as the
 * only method that actually accesses the smartcard. Here is
 * the interface for an implementation.
 *
 * @version $Id: MFCCardServiceImpl.java,v 1.7 1998/09/08 15:01:07 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface MFCCardServiceImpl
{
  /**
   * Select a file or directory on the smartcard.
   * The select command is useful for file services as well as for signature
   * or other services, since it selects an application and CHV domain on the
   * smartcard. It can also be used to select a directory in which a complex
   * key, for example an RSA key, is defined.
   *
   * @param channel   where to contact the smartcard
   * @param path      where to select to
   * @param info      whether to return information, <tt>true</tt> if so
   * @param srparser  helper to parse the select response for the info to
   *                  return, may be <tt>null</tt> if no info is requested
   *
   * @return   information about the selected target, if requested
   *
   * @exception CardServiceObjectNotAvailableException
   *            if the target could not be selected
   * @exception CardServiceUnexpectedResponseException
   *            if the select response could not be interpreted
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public MFCCardObjectInfo selectObject(MFCCardChannel           channel,
                                        CardFilePath             path,
                                        boolean                  info,
                                        MFCSelectResponseParser  srparser)
       throws CardServiceObjectNotAvailableException,
              CardServiceUnexpectedResponseException,
              CardTerminalException;

} // interface MFCCardServiceImpl
