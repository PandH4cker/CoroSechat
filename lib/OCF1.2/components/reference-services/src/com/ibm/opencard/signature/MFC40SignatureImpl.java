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

import opencard.core.util.Tracer;
import opencard.core.terminal.CommandAPDU;
import com.ibm.opencard.service.MFCCommandAPDU;
import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CardServiceInvalidParameterException;
import java.security.InvalidKeyException;
import opencard.core.terminal.ResponseAPDU;
import com.ibm.opencard.service.MFCResponseAPDUCodes;
import opencard.core.service.CardServiceInabilityException;
import com.ibm.opencard.service.MFCCardChannel;
import com.ibm.opencard.access.MFCAccessInformation;
import opencard.opt.signature.JCAStandardNames;


/**
 * Helper class for implementation of a signature card service for MFC 4.0
 * and compatible.
 * Send APDUs to the card. The methods that send APDUs are synchronized
 * simply to reuse the APDU buffers.
 *
 * @version $Id: MFC40SignatureImpl.java,v 1.8 1998/09/07 09:29:42 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 * @see opencard.opt.signature.SignatureCardService
 * @see MFCSignatureService
 */
public class MFC40SignatureImpl implements MFCSignatureImpl {
  /** Tracer for debugging output. */
  private static Tracer ctracer = new Tracer(MFC40SignatureImpl.class);

  /** The CLAss and INStruction bytes for commands. */
  /*final*/protected MFCSigCodes mfcCodes = null;

  // reuseable command APDUs //////////////////////////////////////////////////
  private MFCCommandAPDU calcHashAPDU = null;
  private MFCCommandAPDU genSigAPDU   = null;
  private CommandAPDU keyInfoAPDU  = null;
  private MFCCommandAPDU verSigAPDU   = null;

  /**
   * Instantiates a new signature card service implementation.
   *
   * @param codes   the command codes for the MFC card to support
   */
  public MFC40SignatureImpl(MFCSigCodes codes) {
    mfcCodes = codes;
  }

  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The MFC 4.0 card only supports "SHA-1/RSA".
   * @param alg The standard algorithm name.
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  public void assertSignDataAlgorithm(String alg) throws CardServiceInabilityException
  {
    if (! alg.equals(JCAStandardNames.SHA1_RSA))
      throw new CardServiceInabilityException
      (alg + " not supported");

  }

  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The MFC 4.0 card only supports "RSA".
   * @param alg The standard algorithm name.
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  public void assertSignHashAlgorithm(String alg) throws CardServiceInabilityException
  {
    if (! alg.equals(JCAStandardNames.RAW_RSA))
      throw new CardServiceInabilityException
      (alg + " not supported");

  }

  /**
   * Assert that a specific signature service implementation supports a padding algorithm.
   * @param alg The padding algorithm name.
   *         The MFC 4.0 supports
   *         ISO9796
   *         PKCS#1
   *         ZEROPADDING
   * @exception CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  public void assertSignPadAlgorithm(String alg) throws CardServiceInabilityException
  {
    if ((alg.equals(JCAStandardNames.ISO_PADDING))  || (alg.equals(JCAStandardNames.PKCS_PADDING)) ||
        (alg.equals(JCAStandardNames.ZERO_PADDING))) {
      return;
    }

    throw new CardServiceInabilityException("algorithm "  + alg + "not supported by CardOS");
  }

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
  public synchronized void calculateHash(MFCCardChannel channel, MFCKeyInfo ki, byte[] bytes)
  throws CardServiceException, CardTerminalException
  {
    int sliceSize = mfcCodes.getPrepareHashInputLength(ki.getHashAlgorithm());
    int dataLen = bytes.length;
    ctracer.debug("calculateHash", "hashing "+dataLen+" bytes, slicesize "+sliceSize+" bytes, key "+ki);

    // send all prepare hash slices
    int rest = dataLen;
    while (rest > sliceSize) {
      MFCCommandAPDU apdu = getHashAPDU(ki,bytes, dataLen - rest ,sliceSize,false);
      ResponseAPDU resp = channel.executeCommand(apdu,ki.getAccessConditions(MFCAccessInformation.CMD_CLS_USE_KEY));
      mfcCodes.analyseStatus(resp.sw(),mfcCodes.getCalculateHashByte(),ki);

      rest-=sliceSize;
    }
    // send finish hash slice
    MFCCommandAPDU apdu = getHashAPDU(ki,bytes, dataLen - rest ,rest,true);
    ResponseAPDU resp = channel.executeCommand(apdu,ki.getAccessConditions(MFCAccessInformation.CMD_CLS_USE_KEY));
    mfcCodes.analyseStatus(resp.sw(),mfcCodes.getCalculateHashByte(),ki);
  }

  /**
   * return command APDU for generate signature command
   * @return com.ibm.opencard.service.MFCCommandAPDU
   * @param ki key info of the key to be used
   * @param data byte[]
   * @exception CardServiceInvalidParameterException maximum input block length exceeded
   */
  protected MFCCommandAPDU getGenSigAPDU(MFCKeyInfo ki, byte[] data) 
  throws CardServiceInvalidParameterException {
    // parameter check
    int len=(data == null)?0:data.length;
    if (len > mfcCodes.getEffectiveDataLength()) throw new CardServiceInvalidParameterException
      ("maximum input block length is " + mfcCodes.getEffectiveDataLength());

    // reuse command APDU
    if (genSigAPDU == null) {
      genSigAPDU = new MFCCommandAPDU(6+mfcCodes.getEffectiveDataLength()); //@@@ check this

      // secure messaging is not supported for PKA command groups use key and import key in MFC 4.0
      // TBD BED what about 4.21 ?
      genSigAPDU.setProviderFlag(false);
      genSigAPDU.setRequesterFlag(false);

      genSigAPDU.append(mfcCodes.getISOClassByte());
      genSigAPDU.append(mfcCodes.getGenerateSignatureByte());
      genSigAPDU.append((byte)0); // P1
      genSigAPDU.append((byte)(ki.getKeyNr() & 0xFF)); // P2
    } else {
      genSigAPDU.setLength(4);
      genSigAPDU.setByte(3,ki.getKeyNr());
    }
    if (len > 0 ) {
      genSigAPDU.appendBlock(data); // length field and the data over which a signature is required
    }

    genSigAPDU.append((byte) (ki.getOutputBlockLength() & 0xFF)); // expected length of signature

    return genSigAPDU;
  }

  /**
   * get APDU for prepare or finish hash stage of calculate hash command
   * Note: the block length must be less then the maximum effective data length of the card
   * @return com.ibm.opencard.service.MFCCommandAPDU
   * @param ki key info of the key to be used
   * @param data the data to be hashed
   * @param offset offset into data for the current block
   * @param length length of current block
   * @param finish if set to false, return a prepare hash command APDU,
   *               if set to true, return a finish hash command APDU.
   */
  protected MFCCommandAPDU getHashAPDU(MFCKeyInfo ki, byte[] data, int offset, int length, boolean finish) {

    // reuse command APDU
    if (calcHashAPDU == null) {
      // allcate maximum effective data length to be on the safe side for all possible hash algorithms
      calcHashAPDU = new MFCCommandAPDU(6+mfcCodes.getEffectiveDataLength());

      // secure messaging is not supported for PKA command groups use key and import key in MFC 4.0
      // TBD BED what about 4.21 ?
      calcHashAPDU.setProviderFlag(false);
      calcHashAPDU.setRequesterFlag(false);

      calcHashAPDU.append(mfcCodes.getISOClassByte());
      calcHashAPDU.append(mfcCodes.getCalculateHashByte());
    } else {
      calcHashAPDU.setLength(2);
    }
    calcHashAPDU.append(finish?mfcCodes.getFinishByte():mfcCodes.getPrepareByte()); // P1
    calcHashAPDU.append(ki.getHashAlgorithmNr()); // P2
    calcHashAPDU.appendBlock(data,offset,length); // length and data over which a signature is required

    return calcHashAPDU;

  }

  /**
   * APDUs used by the service implementation are reused
   * @return opencard.core.terminal.CommandAPDU
   */
  protected CommandAPDU getKeyInfoAPDU(int keyNr, boolean isPrivate) {
    byte key = (byte)(keyNr & 0xFF);
    if (isPrivate) {
      key &= 0x7F; // switch off bit 7 for private keys
    } else {
      key |= 0x80; // switch on bit 7 for public keys
    }
    if (keyInfoAPDU == null) {
      keyInfoAPDU = new CommandAPDU(5);

      keyInfoAPDU.append(mfcCodes.getISOClassByte());
      keyInfoAPDU.append(mfcCodes.getReadKeyInfoByte());
      keyInfoAPDU.append((byte)0); // P1
      keyInfoAPDU.append(key); // P2
      keyInfoAPDU.append((byte)0); // maximum response length allowed
    } else {
      keyInfoAPDU.setByte(3,key);
    }

    return keyInfoAPDU;
  }

  /**
   * return command APDU for verify signature command
   * @return com.ibm.opencard.service.MFCCommandAPDU
   * @param ki key info of the key to be used
   * @param data byte[]
   * @exception CardServiceInvalidParameterException maximum input block length exceeded
   */
  protected MFCCommandAPDU getVerifyAPDU(MFCKeyInfo ki, byte[] signature, byte[] data) 
  throws CardServiceInvalidParameterException {
    // parameter check
    int len=(data == null)?0:data.length;
    if ((len + signature.length) > mfcCodes.getEffectiveDataLength()) throw new CardServiceInvalidParameterException
      ("input exceeds maximum effective data length");

    // reuse command APDU
    if (verSigAPDU == null) {
      verSigAPDU = new MFCCommandAPDU(6+ mfcCodes.getEffectiveDataLength());

      // secure messaging is not supported for PKA command groups use key and import key in MFC 4.0
      // TBD BED what about 4.21 ?
      verSigAPDU.setProviderFlag(false);
      verSigAPDU.setRequesterFlag(false);

      verSigAPDU.append(mfcCodes.getISOClassByte());
      verSigAPDU.append(mfcCodes.getVerifySignatureByte());
      verSigAPDU.append((byte)0); // P1
      verSigAPDU.append((byte)(ki.getKeyNr() & 0xFF)); // P2
    } else {
      verSigAPDU.setLength(4);
      verSigAPDU.setByte(3,ki.getKeyNr());
    }
    verSigAPDU.append((byte)((len + signature.length) & 0xFF)); // data length
    if (len > 0 ) {
      verSigAPDU.append(data); // the data over which a signature is required
    }

    verSigAPDU.append(signature); // the signature to be verified
    return verSigAPDU;
  }

  /**
   * execute command 'read key info' and return an object describing the key
   * @return MFCKeyInfo
   * @param channel MFCCardChannel
   * @param keyNr the key number
   * @param parser the key info response parser
   * @param privateKey info is requested for private key?
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public synchronized MFCKeyInfo readKeyInfo(MFCCardChannel channel, int keyNr, MFCKeyInfoRParser parser, boolean privateKey)
  throws CardServiceException, CardTerminalException
  {
    ctracer.debug("readKeyInfo",(privateKey?"private":"public")+" key nr "+(keyNr & 0x7F));
    ResponseAPDU resp = channel.executeCommand(getKeyInfoAPDU(keyNr,privateKey));
    mfcCodes.analyseStatus(resp.sw(),mfcCodes.getReadKeyInfoByte(),null);

    return parser.parseKeyInfoResponse(resp.data());
  }

  /**
   * send generate signature command
   * @return The signature.
   * @param channel com.ibm.opencard.service.MFCCardChannel
   * @param ki the key must not be a hash-only key
   * @param bytes byte[] the hash/message digest to be signed.
   *                     If this parameter is null, the previous method must have been calculateHash()
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public synchronized byte[] sign(MFCCardChannel channel, MFCKeyInfo ki, byte[] bytes)
  throws CardServiceException, CardTerminalException
  {
    ctracer.debug("sign",ki.toString());
    ResponseAPDU resp = channel.executeCommand(getGenSigAPDU(ki,bytes),ki.getAccessConditions(MFCAccessInformation.CMD_CLS_USE_KEY));
    mfcCodes.analyseStatus(resp.sw(),mfcCodes.getGenerateSignatureByte(),ki);
    return resp.data();
  }

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
  public synchronized boolean verify(MFCCardChannel channel, MFCKeyInfo ki, byte[] signature, byte[] data)
  throws CardServiceException, CardTerminalException
  {
    ctracer.debug("verify",ki.toString());
    ResponseAPDU resp = channel.executeCommand(getVerifyAPDU(ki, signature, data),ki.getAccessConditions(MFCAccessInformation.CMD_CLS_USE_KEY));
    if (mfcCodes.indicatesError(resp.sw())) {
      // handle special return code myself
      if (resp.sw() == MFCResponseAPDUCodes.RAPDU_VERIFICATION_ERROR) return false;
      // pass all unexpected return codes to analyseStatus
      mfcCodes.analyseStatus(resp.sw(),mfcCodes.getGenerateSignatureByte(),ki);
    }
    return true;
  }
}
