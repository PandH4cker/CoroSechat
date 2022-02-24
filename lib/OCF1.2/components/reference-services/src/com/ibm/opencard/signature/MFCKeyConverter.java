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

import java.security.InvalidKeyException;
import opencard.core.service.CardServiceException;
import java.security.PrivateKey;
import java.security.PublicKey;
/**
 * Create MFC  specific byte streams for key files.
 *
 * @version $Id: MFCKeyConverter.java,v 1.3 1998/08/19 12:39:50 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public interface MFCKeyConverter {
  /**
   * Check if the card can store a private Key of the provided type
   *
   * @exception InvalidKeyException The card does not support the kind of key provided.
   */
  void assertSupportsKey(PrivateKey key) throws InvalidKeyException;

  /**
   * Check if the card can store a public Key of the provided type
   *
   * @exception InvalidKeyException The card does not support the kind of key provided.
   */
  void assertSupportsKey(PublicKey key) throws InvalidKeyException;

  /** Returns the actual key in KeyData format specified for PKA files
   *
   * @param keyInfo
   *        Additional key info to be merged into actual key to
   *        make it a valid PKA file.
   *
   * @return Byte array in a form as specified for a full-fledged PKA file
   * @exception InvalidKeyException The card does not support the kind of key provided
   */
  public byte[] pkaFile(PrivateKey key, byte[] keyInfo) throws InvalidKeyException;

  /** Returns the actual key in KeyData format specified for PKA files
   *
   * @param keyInfo
   *        Additional key info to be merged into actual key to
   *        make it a valid PKA file.
   *
   * @return Byte array in a form as specified for a full-fledged PKA file
   * @exception InvalidKeyException The card does not support the kind of key provided
   */
  public byte[] pkaFile(PublicKey key, byte[] keyInfo) throws InvalidKeyException;

  /**
   * Construct a key from the key file content of an MFC key file
   * Creates a subclass of java.security.PublicKey.
   *
   * @exception CardServiceException The card service can not interpret the fileContent. 
   */
  PublicKey readPublicKey(MFCKeyInfo ki, byte[] fileContent) throws CardServiceException;
}