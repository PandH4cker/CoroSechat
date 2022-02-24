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

import com.ibm.opencard.access.BadHeaderException;
import com.ibm.opencard.access.MFCAccessParser;
import opencard.core.util.Tracer;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.signature.JCAStandardNames;

/**
 * Interpreter for smartcard responses to read key info commands.
 *
 * @version $Id: MFC40KeyInfoRParser.java,v 1.6 1998/10/27 13:31:05 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @see MFCKeyInfo
 */
public class MFC40KeyInfoRParser implements MFCKeyInfoRParser {

  private static Tracer ctracer = new Tracer(MFC40KeyInfoRParser.class);

  /** The standard ASN.1 tag for a MFC file header. */
  protected final static byte FILE_HEADER_TAG = (byte) 0x63;

  /** The underlying parser for access conditions. */
  protected MFCAccessParser accessParser = null;

  /**
   * Creates a new key info  response parser for read key info.
   *
   * @param accpar   a parser for access conditions, which are part
   *                 of the read key info response
   */
  public MFC40KeyInfoRParser(MFCAccessParser accpar) {
    accessParser=accpar;
  }

  /**
    * return the offset of the PKA specific header extension
    */
  protected int getExtHdrOffset() {
    return 14;
  }

  /**
   * parse the read key info response
   * @return MFCKeyInfo describing the key
   * @param response the byte array returned as response from the read key info command
   * @exception BadHeaderException Invalid response format
   */
  public MFCKeyInfo parseKeyInfoResponse(byte[] data) throws BadHeaderException
  {
    MFCKeyInfo ki = new MFCKeyInfo();
    if (data[0] != FILE_HEADER_TAG)
      throw new BadHeaderException("bad header tag 0x" +
                                   Integer.toHexString(data[0]));

    // access conditions
    ki.setAccessInfo(accessParser.parseAccessInformation(data, 6));
    // not a directory
    ki.setContainer(false);

    // file size and ID
    ki.setFileSize(((data[2]&0xff) << 8) | (data[3]&0xff));
    ki.setFileID((short)(((data[4]&0xff) << 8) | (data[5]&0xff)));

    int l1 = data[1] & 0xff;  // length of ETSI-header
    // int fileStatus = data[11] & 0xff;

    int l2 = data[12] & 0xff;  // length of extended header data
    // (including the following 7 bytes)

    // algorithms
    int publicKeyAlgorithm = data[getExtHdrOffset()] & 0xff;
    int hashAlgorithm = data[getExtHdrOffset()+1] & 0xff;
    ki.setHashAlgorithmNr(data[getExtHdrOffset()+1]);
    int padAlgorithm = data[getExtHdrOffset()+2] & 0xff;
    switch (publicKeyAlgorithm) {
      case 0x41: // MFC 4.0 PKCS#11 extensions
        ki.setCryptoAlgorithm(JCAStandardNames.DES_CIPHER);
        break;
      case 1:
        ki.setCryptoAlgorithm(JCAStandardNames.RAW_RSA);
        break;
      case 2:
        ki.setCryptoAlgorithm(JCAStandardNames.RAW_DSA);
        break;
      default:
        throw new BadHeaderException ("bad PKA algorithm 0x" +
                                      Integer.toHexString(publicKeyAlgorithm));
    }
    switch (hashAlgorithm) {
      case 0:
        break;
      case 3:
        ki.setHashAlgorithm(JCAStandardNames.MD5);
        break;
      case 4:
        ki.setHashAlgorithm(JCAStandardNames.SHA1);
        break;
      default:
        throw new BadHeaderException("bad hash algorithm 0x" +
                                     Integer.toHexString(hashAlgorithm));
    }
    switch (padAlgorithm) {
      case 0:
        ki.setPadAlgorithm(JCAStandardNames.ZERO_PADDING);
        break;
      case 3:
        ki.setPadAlgorithm(JCAStandardNames.ISO_PADDING);
        break;
      case 4:
        ki.setPadAlgorithm(JCAStandardNames.PKCS_PADDING);
        break;
      default:
        throw new BadHeaderException("bad padding algorithm 0x" +
                                     Integer.toHexString(padAlgorithm));
    }
    // key usage
    //int keyUsage = ((data[17]&0xff) << 8) | (data[18]&0xff);
    if (0!=(data[getExtHdrOffset()+3] & 0x01)) ki.setSignatureKey(true);
    if (0!=(data[getExtHdrOffset()+3] & 0x20)) ki.setCertificationKey(true);
    if (0!=(data[getExtHdrOffset()+3] & 0x08)) ki.setEncryptionKey(true);


    // key status
    int keyStatus = data[getExtHdrOffset()+5] & 0xff;
    if (0!=(keyStatus & 0x01)) ki.setVerified(true);
    if (0!=(keyStatus & 0x80)) ki.setCompleted(true);
    if (0!=(keyStatus & 0x02)) ki.setGenerated(true);

    // optional extension data
    //if(l2-7 > 0) {
    //  extensionData = new byte[l2-7];
    //  System.arraycopy(data, 20, extensionData, 0, l2-7);
    //}

    // the following data section starts at l1+2 (2 + header length)
    // (this conflicts with the spec, but seems to be o.k.)
    int l3 = data[l1+2] & 0xff;  // path-length
    byte[] pathArray = new byte[l3];
    System.arraycopy(data, l1+3, pathArray, 0, l3);
    ki.setPath(new CardFilePath(pathArray));

    ki.setKeyLength(((data[l1+l3+3]&0xff) << 8) | (data[l1+l3+4]&0xff));
    ki.setInputBlockLength(data[l1+l3+5] & 0xff);
    ki.setOutputBlockLength(data[l1+l3+6] & 0xff);

    // key info
    // key info length
    int l4 = data[l1+l3+7] & 0xff;
    // Keys can have key-info fields up to 0xFF bytes but due to limitations
    // of the output buffer only fewer bytes (about 216) can be returned.
    if (l4 > data.length-(l1+l3+8)) {
      l4 = data.length - (l1+l3+8);
      ctracer.warning("parseKeyInfoResponse", "Key-info length truncated to " + l4 + " bytes.");
    }
    byte[] info = new byte[l4];
    System.arraycopy(data, l1+l3+8, info, 0, l4);
    ki.setKeyInfo(info);

    ctracer.debug("parseKeyInfoResponse", ki.toString());

    return ki;
  }
}