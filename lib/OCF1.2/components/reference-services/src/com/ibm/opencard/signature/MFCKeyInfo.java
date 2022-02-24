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

import opencard.core.service.CardServiceInvalidParameterException;
import opencard.opt.iso.fs.CardFilePath;
import com.ibm.opencard.service.MFCCardObjectInfo;
import com.ibm.opencard.access.MFCAccessParser;
import java.security.InvalidKeyException;
import java.util.StringTokenizer;
import opencard.opt.signature.JCAStandardNames;


/**
 * Information about a smartcard PKA file
 *
 * @version $Id: MFCKeyInfo.java,v 1.16 1998/10/30 14:44:54 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public class MFCKeyInfo extends MFCCardObjectInfo {
  private boolean verified=false;
  private boolean completed=false;
  private boolean generated=false;
  private String cryptoAlgorithm = "unknown";
  private String hashAlgorithm = "unknown";
  private byte hashAlgorithmByte = (byte)0;
  private String padAlgorithm= "unknown";
  private boolean signatureKey=false;
  private boolean certificationKey=false;
  private boolean encryptionKey=false;
  private int inputBlockLength=0;
  private int outputBlockLength=0;
  private int keyLength=0;
  private byte[] keyInfo = null;
  private short fileID=0x00;
  private int fileSize;
  private CardFilePath path=null;

  /**
   * constructor
   */
  public MFCKeyInfo() {

  }

  /**
   * check if a key can be used for key validation or certification
   * @exception CardServiceInvalidParameterException the key is not a certification key
   */
  protected void assertIsCertificationKey() throws CardServiceInvalidParameterException {
    if (! isCertificationKey() ) throw new CardServiceInvalidParameterException
      ("validation key is no certification key");
    if (! isVerified() ) throw new CardServiceInvalidParameterException
      ("validation key is not verified");
  }

  /**
   * Check a given signature length  is compatible with the key's input length.
   * The signature is 'encrypted' with the public key and then compared to the provided hash.
   * @param length data input length
   * @exception CardServiceInvalidParameterException Padding Algorithm and data length are incompatible
   */
  protected void assertSignatureLength(int length) throws CardServiceInvalidParameterException {
    if (length > this.getInputBlockLength())
      throw new CardServiceInvalidParameterException
      ("signature length is: " + length + " key expects: " + getInputBlockLength());
  }

  /**
   * Check a given data input length is compatible with the key's input length and padding algorithm
   * @param length data input length
   * @exception CardServiceInvalidParameterException Padding Algorithm and data length are incompatible
   */
  public void assertSupportsInputLength(int length) throws CardServiceInvalidParameterException {
    // sanity check
    if ( (this.padAlgorithm.equals( JCAStandardNames.ZERO_PADDING ) && (length > getInputBlockLength())) ||
         (this.padAlgorithm.equals( JCAStandardNames.ISO_PADDING ) && (length > getInputBlockLength() / 2)) ||
         (this.padAlgorithm.equals( JCAStandardNames.PKCS_PADDING ) && (length > getInputBlockLength() - 11)))
      throw new CardServiceInvalidParameterException
      ("data length: " + length + " incompatible with padding algorithm: " + padAlgorithm +
       " and key input length: " + getInputBlockLength());
  }

  /**
   * Check if the key supports the specified padding algorithm
   * @param padAlgorithm standard name of padding algorithm
   * @exception java.security.InvalidKeyException Algorithm and key are incompatible.
   */
  protected void assertSupportsPadAlgorithm(String padAlgorithm) throws InvalidKeyException {
    if (! this.padAlgorithm.equals(padAlgorithm))
      throw new InvalidKeyException
      ("requested algorithm: " + padAlgorithm + " key algorithm: " + this.padAlgorithm);
  }

  /**
   * Check if the key supports the specified algorithm
   * @param signAlgorithm JCA standard algorithm name
   * @exception java.security.InvalidKeyException Algorithm and key are incompatible.
   */
  protected void assertSupportsSignAlgorithm(String signAlgorithm, boolean hashKey) throws InvalidKeyException {
    String signAlg=signAlgorithm;
    // key usage is signing ?
    if (!isSignatureKey()) throw new InvalidKeyException("key usage does not allow signing");
    // hashing algorithm
    if (hashKey) {
      if (!isHashKey()) throw new InvalidKeyException("key does not support hashing");
      StringTokenizer t= new StringTokenizer(signAlgorithm, "with");
      if (t.countTokens() !=2) throw new InvalidKeyException(signAlgorithm + " not supported");
      String hashAlg=t.nextToken();
      if (! hashAlg.equals(getHashAlgorithm())) throw new InvalidKeyException
        ("requested algorithm: " + hashAlg + " key algorithm: " + getHashAlgorithm());
      signAlg=t.nextToken();
    } else {
      if (isHashKey()) throw new InvalidKeyException("key requires hashing on card");
    }
    // PKA algorithm
    if (! signAlg.equals(getCryptoAlgorithm())) throw new InvalidKeyException
      ("requested algorithm: " + signAlgorithm + " key algorithm: " + getCryptoAlgorithm());
  }

  /**
   *  * JCA Standard name,for example "RSA"
   */
  public String getCryptoAlgorithm() {
    return cryptoAlgorithm;
  }

  /**
   * file size in bytes
   */
  public int getFileSize() {
    return fileSize;
  }

  /**
   * JCA standard names, for example "SHA-1"
   */
  public String getHashAlgorithm() {
    return hashAlgorithm;
  }

  /**
   * MFC specific encoding of hash algorithms
   */
  public byte getHashAlgorithmNr() {
    return hashAlgorithmByte;
  }

  /**
   * length of input block
   */
  public int getInputBlockLength() {
    return inputBlockLength;
  }

  /**
   * customer provided key info stored in the key file
   */
  public byte[] getKeyInfo() {
    return keyInfo;
  }

  /**
   * nominal key length in bits
   */
  public int getKeyLength() {
    return keyLength;
  }

  /**
   * Key number of the key. The key number is the lower byte of the file id.
   * @return int
   */
  public int getKeyNr() {
    return (fileID & 0xFF);
  }

  /**
   * output block length
   */
  public int getOutputBlockLength() {
    return outputBlockLength;
  }

  /**
   * padding algorithm name, one of
   * <ul>
   * <li> ZEROPADDING
   * <li> PKCS#1
   * <li> ISO9796
   * </ul>
   */
  public String getPadAlgorithm() {
    return padAlgorithm;
  }

  /**
   * path to the key file
   */
  public CardFilePath getPath() {
    return path;
  }

  /**
   * @return true if the key usage bit for certification is on
   */
  public boolean isCertificationKey() {
    return certificationKey;
  }

  /**
   * Is the key usable, or has the import step been interrupted?
   * @return boolean
   */
  public boolean isCompleted() {
    return completed;
  }

  /**
   * was the key generated on card?
   * @return boolean
   */
  public boolean isGenerated() {
    return generated;
  }

  /**
   * is the key a hash key (requires hashing on card)?
   */
  public boolean isHashKey() {
    return (hashAlgorithmByte!=(byte)0);
  }

  /**
   * @return true if the key usage bit for key encryption is on
   */
  public boolean isKeyEncryptionKey() {
    return encryptionKey;
  }

  /**
   * Is the key a private key
   * @return boolean
   */
  public boolean isPrivate() {
    return (0==(fileID & 0x80));
  }

  /**
   * @return true if the key usage bit for generating signatures is on
   */
  public boolean isSignatureKey() {
    return signatureKey;
  }

  /**
   * Has the key been authenticated by a "micro certificate"?
   * @return boolean
   */
  public boolean isVerified() {
    return verified;
  }

  /**
   * Is the key usable as a certification key
   * @param newValue boolean
   */
  protected void setCertificationKey(boolean newValue) {
    this.certificationKey = newValue;
  }

  /**
   * Is the key usable, or has the import step been interrupted?
   * @param newValue boolean
   */
  protected void setCompleted(boolean newValue) {
    this.completed = newValue;
  }

  /**
   * JCA Standard name,for example "RSA"
   */
  protected void setCryptoAlgorithm(String newValue) {
    this.cryptoAlgorithm = newValue;
  }

  /**
   * Is the key usable as an encryption key
   * @param newValue boolean
   */
  protected void setEncryptionKey(boolean newValue) {
    this.encryptionKey = newValue;
  }

  /**
   * set the file ID, ID of EF_PKA is 0xFExx
   */
  protected void setFileID(short id) {
    fileID = id;
  }

  /**
   * set the file size in bytes
   */
  protected void setFileSize(int size) {
    fileSize = size;
  }

  /**
   * was the key generated on card?
   * @param newValue boolean
   */
  protected void setGenerated(boolean newValue) {
    this.generated = newValue;
  }

  /**
   * JCA standard names, for example "SHA-1"
   */
  protected void setHashAlgorithm(String newValue) {
    this.hashAlgorithm = newValue;
  }

  /**
   * is the key a hash key (requires hashing on card)?
   */
  protected void setHashAlgorithmNr(byte alg) {
    this.hashAlgorithmByte = alg;
  }

  /**
   * length of input block
   */
  protected void setInputBlockLength(int newValue) {
    this.inputBlockLength = newValue;
  }

  /**
   * customer provided key info stored in the key file
   */
  protected void setKeyInfo(byte[] newValue) {
    this.keyInfo = newValue;
  }

  /**
   * nominal key length in bits
   */
  protected void setKeyLength(int newValue) {
    this.keyLength = newValue;
  }

  /**
   * output block length
   */
  protected void setOutputBlockLength(int newValue) {
    this.outputBlockLength = newValue;
  }

  /**
   * padding algorithm name, one of
   * <ul>
   * <li> ZEROPADDING
   * <li> PKCS#1
   * <li> ISO9796
   * </ul>
   */
  protected void setPadAlgorithm(String newValue) {
    this.padAlgorithm = newValue;
  }

  /**
   * path to the key file
   */
  public void setPath(CardFilePath newPath) {
    path=newPath;
  }

  /**
   * Is the key usable as a signature key
   * @param newValue boolean
   */
  protected void setSignatureKey(boolean newValue) {
    this.signatureKey = newValue;
  }

  /**
   * Has the key been authenticated by a "micro certificate"?
   * @param newValue boolean
   */
  protected void setVerified(boolean newValue) {
    this.verified = newValue;
  }

  /** Add information to the base class toString() method. */
  protected void toStringHook(StringBuffer sb) {
    // file id
    sb.append(";").append("EF_PKA 0x");sb.append(Integer.toHexString(fileID & 0xffff));
    // path
    sb.append(", ").append(path);
    // private or public
    sb.append(", ").append(isPrivate()?"private":"public");
    // file size
    sb.append(", ").append(getFileSize()).append(" bytes");
    // algorithm names
    sb.append(", ").append(getHashAlgorithm()).append("/").append(getPadAlgorithm()).append("/").append(getCryptoAlgorithm());
    // signkey?
    sb.append(", SIGNKEY=").append(isSignatureKey()?"Y":"N");
    // certification key?
    sb.append(", CERT=").append(isCertificationKey()?"Y":"N");
    // complete?
    sb.append(", COMP=").append(isCompleted()?"Y":"N");
    // lenghts
    sb.append(", LEN=").append(getKeyLength()).append("/").append(getInputBlockLength()).append("/").append(getOutputBlockLength());

  }
}
