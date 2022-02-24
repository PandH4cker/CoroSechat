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
package com.ibm.opencard.signature;

import com.ibm.opencard.service.MFCCardChannel;

/**
 * The interface to a key import card service implementation.
 * The implementation of such a service is
 * responsible for creating command APDUs and evaluating the
 * smartcard's response APDUs.
 *
 * @version $Id: MFCKeyImportImpl.java,v 1.4 1998/08/19 12:39:50 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public interface MFCKeyImportImpl {

  /**
   * Send the command APDU for import key.
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param ki old key info
   * @param data byte[] the key file data
   * @param isPrivate true if a private key should be imported
   * @exception opencard.core.service.CardServiceException any subclass of CardServiceException
   * @exception opencard.core.terminal.CardTerminalException any subclass of CardTerminalException
   */
  public void importKey(MFCCardChannel channel, MFCKeyInfo ki,
                        byte[] data, boolean isPrivate)
  throws opencard.core.service.CardServiceException,
    opencard.core.terminal.CardTerminalException;

  /**
   * Send the command APDU for validate key.
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param keyNr number of key to be verified
   * @param ki key info of key to be used for verification of signature
   * @param signature the signature to be verified
   * @param isPrivate true if a private key should be validated
   * @exception opencard.core.service.CardServiceException any subclass of CardServiceException
   * @exception opencard.core.terminal.CardTerminalException any subclass of CardTerminalException
   */
  boolean validateKey(MFCCardChannel channel,
                      int keyNr,
                      MFCKeyInfo ki,
                      byte[] signature,
                      boolean isPrivate)
  throws opencard.core.service.CardServiceException,
    opencard.core.terminal.CardTerminalException;
}