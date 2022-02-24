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

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CardServiceInabilityException;
import com.ibm.opencard.service.MFCCardChannel;


/**
 * The interface to a signature card service implementation.
 * The implementation of such a service is
 * responsible for creating command APDUs and evaluating the
 * smartcard's response APDUs.
 *
 * @version $Id: MFCSignatureImpl.java,v 1.4 1998/08/19 12:39:50 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public interface MFCSignatureImpl {
  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The algorithm specified must be a combination of hash/pka algorithm, for example "SHA-1/RSA".
   * @param alg The standard algorithm name.
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  void assertSignDataAlgorithm(String alg) throws CardServiceInabilityException;

  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The algorithm specified must be a pka algorithm only, for example "RSA".
   * @param alg The standard algorithm name.
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  void assertSignHashAlgorithm(String alg) throws CardServiceInabilityException;

  /**
   * Assert that a specific signature service implementation supports a padding algorithm.
   * The algorithm specified must be one of
   * @param alg The padding algorithm name.
   *         for example one of
   *         ISO9796
   *         PKCS#1
   *         ZEROPADDING
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  void assertSignPadAlgorithm(String alg) throws CardServiceInabilityException;

  /**
   * send calculate hash commmand.
   * The hash is not returned from the card but used in the next step to generate a signature on the hash.
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param ki com.ibm.opencard.signature.MFCKeyInfo
   * @param bytes the data for which the hash is to be calculated
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  void calculateHash(MFCCardChannel channel, MFCKeyInfo ki, byte[] bytes)
  throws CardServiceException, CardTerminalException;

  /**
   * execute coammnd read key info and return an object describing the key
   * @return MFCKeyInfo
   * @param channel MFCCardChannel
   * @param keyNr the key number
   * @param parser the key info response parser
   * @param privateKey info is requested for private key?
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  MFCKeyInfo readKeyInfo(MFCCardChannel channel, int keyNr, MFCKeyInfoRParser parser, boolean privateKey)
  throws CardServiceException, CardTerminalException;

  /**
   * send generate signature command
   * @return The signature.
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param ki the key used for the algorithm
   * @param bytes byte[] the hash/message digest to be signed.
   *      If this parameter is null, the previous method must have been calculateHash()
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  byte[] sign(MFCCardChannel channel, MFCKeyInfo ki, byte[] bytes)
  throws CardServiceException, CardTerminalException;

  /**
   * send verify signature command
   * @return true if the verification succeeded
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param ki the key used for the algorithm
   * @param signature the signature to be verified
   * @param bytes byte[] the hash/message digest to be signed.
   *                     If this parameter is null, the previous method must have been calculateHash()
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  boolean verify(MFCCardChannel channel, MFCKeyInfo ki, byte[] signature, byte[] data)
  throws CardServiceException,opencard.core.terminal.CardTerminalException;
}