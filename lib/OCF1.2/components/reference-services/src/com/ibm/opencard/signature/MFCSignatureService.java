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
import java.util.Hashtable;
import opencard.core.service.SmartCard;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.SmartCard;
import opencard.core.util.Tracer;
import opencard.opt.signature.SignatureCardService;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;
import opencard.opt.security.PrivateKeyFile;
import opencard.opt.security.PublicKeyFile;
import opencard.opt.signature.JCAStandardNames;

import com.ibm.opencard.service.MFCCardService;
import com.ibm.opencard.service.MFCCardChannel;



// DO OBSERVE:
// Use exclusive card access for generateSignature()/verifySignature() to
// guarantee uninterupted APDU sequence in case of a hashing key. If the
// key requires hashing, the GENERATE_SIGNATURE command has to follow
// immediately after the CALCULATE_HASH command - no other APDUs in between!

/*******************************************************************************
* <tt>MFCSignatureService</tt> extends the common base class
* <tt>MFCCardService</tt> of all MFC card services and implements the
* <tt>SignatureCardService</tt> interface defined by OCF.
* <tt>MFCSignatureService</tt> serves as the base class of all IBM
* MFC-based <tt>SignatureCardService</tt> implementations.<p>
*
* Instances of <tt>MFCSignatureService</tt> or one of its
* subclasses, which talk to a specific physical smart card, delegate
* execution of the functionality defined in <tt>SignatureCardService</tt>
* to a (singleton) driver for that card, which implements interface
* <tt>MFCSignatureImpl</tt> for a specific card.
*
* @version $Id: MFCSignatureService.java,v 1.10 1999/10/14 15:44:13 pbendel Exp $
* @author Peter Bendel (peter_bendel@de.ibm.com)
*
* @see com.ibm.opencard.service.MFCCardService
* @see opencard.opt.signature.SignatureCardService
*******************************************************************************/
public class MFCSignatureService extends MFCCardService
implements SignatureCardService {

  /** The tracer for MFCSignatureService: tracing on class name. */
  private Tracer itracer = new Tracer(this, MFCSignatureService.class);
  private static Tracer ctracer = new Tracer(MFCSignatureService.class);


  /** Instances of <tt>MFCSignatureService</tt>s share a single instance
   * of a card service implementation class implementing <tt>MFCSignatureImpl</tt>.
   */
  protected MFCSignatureImpl sigImpl = null;

  protected MFCKeyInfoRParser kiParser = null;


  /**
   * Creates a new signature service for MFC smartcards.
   * The service cannot be used until it has been initialized by invoking
   * <tt>initialize</tt>. If this service has to be initialized as part
   * of a derived service, <tt>initSignature</tt> has to be invoked instead.
   *
   * @see #initialize
   * @see #initSignature
   */
  public MFCSignatureService() {
    // no body
    
  }

  /**
   * Initializes this service.
   * This is an entry point for initializing the MFC signature service.
   * It invokes <tt>initSignature</tt> to perform the actual initialization.
   * Derived services must not invoke this method, but have to invoke
   * <tt>initSignature</tt> directly.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   which smartcard to contact
   * @param blocking    whether operation shall be blocking
   *
   * @exception CardServiceException initialization failed
   *
   * @see #initSignature
   * @see com.ibm.opencard.service.MFCCardService#initialize
   */
  protected void initialize(CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking )
  throws CardServiceException
  {
    initSignature(MFCSignatureFactory.newSigParam(scheduler,
                                                  smartcard,
                                                  blocking, card_type));
  }

  /**
   * Initializes this service from encapsulated parameters.
   * This method initializes the local attributes and invokes
   * <tt>initGeneric</tt> in the base class.
   *
   * @param parameter   an object encapsulating the parameters to this service
   *
   * @exception CardServiceException
   *            if the initialization failed.
   *            With the current implementation, this cannot happen.
   *
   * @see MFCSignatureParameter
   * @see com.ibm.opencard.service.MFCCardService#initGeneric
   */
  public final void initSignature(MFCSignatureParameter parameter)
  throws CardServiceException
  {
    ctracer.debug("initSignature", "initializing");

    super.initGeneric(parameter);

    sigImpl = parameter.sigImpl;
    kiParser = parameter.kiParser;
  }



  /**
   * satisfy access conditions.
   * Access conditions must be satisfied before calculating the hash.
   * beetween calculating the hash and generating the signature no other
   * command may be sent to the card.
   * The trick is to send a generateSignature command to the card using a hash key.
   * The MFCCardAccessor will retry the command until the access conditions are
   * satisfied and then a rc 69 85 (the key is a hash key) is expected.
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  protected void satisfyAccessConditions(MFCKeyInfo ki)
  throws CardServiceException,CardTerminalException
  {
    itracer.debug("satisfyAccessConditions",ki.toString());
    byte[] data = new byte[2];
    try {
      if (ki.isPrivate()) {
        // use an operation that establishes access conditions for private key
        sigImpl.sign(getMFCChannel(),ki, data);
      } else {
        // use any operation that establishes access conditions for public key
        byte[] dummysig = new byte[ki.getInputBlockLength()];
        sigImpl.verify(getMFCChannel(),ki,data,dummysig);
      }
    } catch (CardServiceInvalidParameterException e) {
      itracer.debug("satisfyAccessConditions", "access satisfied");
    }
  }

  /**
   * Generate a digital Signature.
   * First hash the data, then pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * SHA1withRSA: PKCS#1 padding.
   * SHA1withDSA: currently JCA does not specify the padding, thus the padding defined in the key is
   *              implicitly being used
   * <p>
   * The standard algorithm name must be specified as defined in the 
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param privateKey    a reference to the private key on card to be used for signing.
   *         MFC cards only support key references of type PrivateKeyFile.
   * @param signAlgorithm standard digital signature algorithm name
   * @param data          data to be signed
   *
   * @return signature
   *
   * @exception InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public byte[] signData  (PrivateKeyRef privateKey,
                           String signAlgorithm,
                           byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("signData", "implicit padding");
    try {
      allocateCardChannel();

      return signDataInternal(privateKey, signAlgorithm, null, data);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * Overloaded method to generate a digital Signature
   * that allows to specify the padding algorithm.
   *
   * @@param padAlgorithm
   *         Padding Algorithm name
   *         for example one of
   *         ISO9796
   *         PKCS#1
   *         ZEROPADDING
   *         ...
   *
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public byte[] signData  (PrivateKeyRef privateKey,
                           String signAlgorithm,
                           String padAlgorithm,
                           byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("signData", "padding " +padAlgorithm);
    try {
      allocateCardChannel();

      return signDataInternal(privateKey, signAlgorithm, padAlgorithm, data);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * calculate hash and generate signatre
   * @param padAlgorithm If specified, check whether the card and the key support the pad algorithm.
   *                     If not specified, use the key's pad algorithm
   *
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  protected byte[] signDataInternal (PrivateKeyRef privateKey,
                                     String signAlgorithm,
                                     String padAlgorithm,
                                     byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select key parent directory
     * - read key info
     * - check parameters
     * - authenticate (before next steps to avoid retry of calculate hash)
     * - calculate hash
     * - generate signature
     * - release channel
     */
    if (! (privateKey instanceof PrivateKeyFile) ) throw new CardServiceInvalidParameterException
      ("PrivateKeyRef must be a PrivateKeyFile");

    PrivateKeyFile kf = (PrivateKeyFile) privateKey;
    CardFilePath parentDirectory = kf.getDirectory();
    int keyNr = kf.getKeyNumber();

    if (parentDirectory == null)
      throw new CardServiceInvalidParameterException
      ("signData: parentDirectory = null");

    itracer.debug("signDataInternal", "parent directory is " + parentDirectory +
                  " key number is " + keyNr + " sign algorithm is " + signAlgorithm+
                  " pad algorithm is "+((padAlgorithm==null)?"null":padAlgorithm));

    // check if card os supports the hash/pka algorithm
    sigImpl.assertSignDataAlgorithm(signAlgorithm);

    // use padding defined in JCA if padding is not specified
    if (signAlgorithm.equals(JCAStandardNames.SHA1_RSA) && (padAlgorithm==null))
      padAlgorithm = JCAStandardNames.PKCS_PADDING;

    // check if card os supports the padding algorithm
    if (padAlgorithm!=null) sigImpl.assertSignPadAlgorithm(padAlgorithm);

    MFCCardChannel channel = getMFCChannel();

    selectObject(channel, parentDirectory, false);

    //read private key info
    MFCKeyInfo ki = sigImpl.readKeyInfo(channel, keyNr, kiParser, true);
    // check if key is hash key and supports the algorithms
    ki.assertSupportsSignAlgorithm(signAlgorithm,true);
    if (padAlgorithm !=null ) ki.assertSupportsPadAlgorithm(padAlgorithm);

    // Access condition must be satisfied here, before calculating the hash.
    // Beetween calculating the hash and generating the signature no other
    // command may be sent to the card.
    satisfyAccessConditions(ki);

    // calculate hash
    sigImpl.calculateHash(channel, ki, data);

    // sign the hash
    return sigImpl.sign(channel, ki, null);
  }

  /**
   * Generate a digital Signature on the provided hash.
   * Pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   *
   * @@param privateKey
   *         A reference to the private key on card to be used for signing.
   *         MFC cards only support key references of type PrivateKeyFile.
   * @@param signAlgorithm
   *         Standard Algorithm names as defined in the
   *         Java Cryptography Architecture API Specification & Reference
   *         e.g.
   *         DSA:  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to RawDSA is always 20 bytes long.
   *
   *         RSA: The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *              Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * @@param hash
   *         The hash/digest to be signed.
   * @@return Signed data.
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   *
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public byte[] signHash  (PrivateKeyRef privateKey,
                           String signAlgorithm,
                           byte[] hash)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("signData", "implicit padding");
    try {
      allocateCardChannel();

      return signHashInternal(privateKey, signAlgorithm, null, hash);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * Overloaded method to generate a digital Signature
   * that allows to specify the padding algorithm.
   *
   * @@param padAlgorithm
   *         Padding Algorithm name
   *         e.g. one of
   *         ISO9796
   *         PKCS#1
   *         ZEROPADDING
   *         ...
    *
    * @exception java.security.InvalidKeyException
    *         The key type is not supported by the specific card service.
    * @exception CardServiceException any subclass of CardServiceException
    * @exception CardTerminalException any subclass of CardTerminalException
   */
  public byte[] signHash  (PrivateKeyRef privateKey,
                           String signAlgorithm,
                           String padAlgorithm,
                           byte[] hash)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("signHash", "padding " +padAlgorithm);
    try {
      allocateCardChannel();

      return signHashInternal(privateKey, signAlgorithm, padAlgorithm, hash);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * generate signature on the provided hash
   * @param padAlgorithm If specified, check whether the card and the key support the pad algorithm.
   *                     If not specified, use the key's pad algorithm
    *
    * @exception java.security.InvalidKeyException
    *         The key type is not supported by the specific card service.
    * @exception CardServiceException any subclass of CardServiceException
    * @exception CardTerminalException any subclass of CardTerminalException
   */
  protected byte[] signHashInternal (PrivateKeyRef privateKey,
                                     String signAlgorithm,
                                     String padAlgorithm,
                                     byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select key parent directory
     * - read key info
     * - check parameters
     * - authenticate (before next steps to avoid retry of calculate hash)
     * - calculate hash
     * - generate signature
     * - release channel
     */
    if (! (privateKey instanceof PrivateKeyFile) ) throw new CardServiceInvalidParameterException
      ("PrivateKeyRef must be a PrivateKeyFile");

    PrivateKeyFile kf = (PrivateKeyFile) privateKey;
    CardFilePath parentDirectory = kf.getDirectory();
    int keyNr = kf.getKeyNumber();

    if (parentDirectory == null)
      throw new CardServiceInvalidParameterException
      ("signHash: parentDirectory = null");

    itracer.debug("signHashInternal", "parent directory is " + parentDirectory + " key number is " + keyNr + " sign algorithm is " + signAlgorithm+ " pad algorithm is "+((padAlgorithm==null)?"null":padAlgorithm));

    // check if card os supports the pka algorithm
    sigImpl.assertSignHashAlgorithm(signAlgorithm);

    // use padding defined in JCA if padding is not specified
    if (signAlgorithm.equals(JCAStandardNames.RAW_RSA) && (padAlgorithm==null))
      padAlgorithm = JCAStandardNames.PKCS_PADDING;

    // check if card os supports the padding algorithm
    if (padAlgorithm!=null) sigImpl.assertSignPadAlgorithm(padAlgorithm);

    MFCCardChannel channel = getMFCChannel();

    selectObject(channel, parentDirectory, false);

    //read private key info
    MFCKeyInfo ki = sigImpl.readKeyInfo(channel, keyNr, kiParser,true);
    // check if key is not a hash key and supports the algorithms
    ki.assertSupportsSignAlgorithm(signAlgorithm,false);
    if (padAlgorithm !=null ) ki.assertSupportsPadAlgorithm(padAlgorithm);

    // check input data length
    ki.assertSupportsInputLength(data.length);

    // sign the hash
    return sigImpl.sign(channel, ki, data);
  }


  /**
   * Verify a digital Signature including hashing.
   * First hash the data, then pad the hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * The standard algorithm name must be specified as defined in the 
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard digital signature algorithm name
   * @param data the data for which the signature should be verified
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedData(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  byte[] data,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("verifySignedData", "implicit padding");
    try {
      allocateCardChannel();

      return verifySignedDataInternal(publicKey, signAlgorithm, null, data, signature);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * Verify a digital Signature including hashing
   * (overload method that allows to specify the padding algorithm to be used).
   * First hash the data, then pad the hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The standard algorithm name must be specified as defined in the 
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard digital signature algorithm name
   * @param padAlgorithm padding algorithm name, for example one of 
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param data the data for which the signature should be verified
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedData(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  String padAlgorithm,
                                  byte[] data,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("verifySignedData", "padding "+padAlgorithm);
    try {
      allocateCardChannel();

      return verifySignedDataInternal(publicKey, signAlgorithm, padAlgorithm, data, signature);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * calculate hash and verify signature
   * @param padAlgorithm If specified, check whether the card and the key support the pad algorithm.
   *                     If not specified, use the key's pad algorithm
    *
    * @exception java.security.InvalidKeyException
    *         The key type is not supported by the specific card service.
    * @exception CardServiceException any subclass of CardServiceException
    * @exception CardTerminalException any subclass of CardTerminalException
   */
  protected boolean verifySignedDataInternal (PublicKeyRef publicKey,
                                              String signAlgorithm,
                                              String padAlgorithm,
                                              byte[] data,
                                              byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select key parent directory
     * - read key info
     * - check parameters
     * - authenticate (before next steps to avoid retry of calculate hash)
     * - calculate hash
     * - verify signature
     * - release channel
     */
    if (! (publicKey instanceof PublicKeyFile) ) throw new CardServiceInvalidParameterException
      ("PublicKeyRef must be a PublicKeyFile");

    PublicKeyFile kf = (PublicKeyFile) publicKey;
    CardFilePath parentDirectory = kf.getDirectory();
    int keyNr = kf.getKeyNumber();

    if (parentDirectory == null)
      throw new CardServiceInvalidParameterException
      ("verifySignedData: parentDirectory = null");

    itracer.debug("verifySignedDataInternal", "parent directory is " + parentDirectory + " key number is " + (keyNr & 0x7F) + " sign algorithm is " + signAlgorithm+ " pad algorithm is "+((padAlgorithm==null)?"null":padAlgorithm));

    // check if card os supports the hash/pka algorithm
    sigImpl.assertSignDataAlgorithm(signAlgorithm);

    // check if card os supports the padding algorithm
    if (padAlgorithm!=null) sigImpl.assertSignPadAlgorithm(padAlgorithm);

    MFCCardChannel channel = getMFCChannel();

    selectObject(channel, parentDirectory, false);

    //read public key info
    MFCKeyInfo ki = sigImpl.readKeyInfo(channel, keyNr, kiParser,false);
    // check if key is hash key and supports the algorithms
    ki.assertSupportsSignAlgorithm(signAlgorithm,true);
    if (padAlgorithm !=null ) ki.assertSupportsPadAlgorithm(padAlgorithm);
    // check if signature has correct length
    ki.assertSignatureLength(signature.length);

    // Access condition must be satisfied here, before calculating the hash.
    // Beetween calculating the hash and generating the signature no other
    // command may be sent to the card.
    satisfyAccessConditions(ki);

    // calculate hash
    sigImpl.calculateHash(channel, ki, data);

    // verify the hash
    return sigImpl.verify(channel, ki, signature, null);
  }

  /**
   * Verify a digital Signature.
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature verificationoperation on card.
   * Pad the provided hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference, 
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard key algorithm name
   * @param hash
   *         The hash for which the signature should be verified.
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedHash(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  byte[] hash,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("verifySignedHash", "implicit padding");
    try {
      allocateCardChannel();

      return verifySignedHashInternal(publicKey, signAlgorithm, null, hash, signature);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * Verify a digital Signature
   * (overloaded method that allows to specify the padding algorithm to be used).
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature verification operation on card.
   * Pad the provided hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference, 
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard key algorithm name
   * @param padAlgorithm padding algorithm name, for example one of 
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param hash
   *         The hash for which the signature should be verified.
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedHash(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  String padAlgorithm,
                                  byte[] hash,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    itracer.debug("verifySignedHash", "padding "+padAlgorithm);
    try {
      allocateCardChannel();

      return verifySignedHashInternal(publicKey, signAlgorithm, padAlgorithm, hash, signature);

    } finally {
      releaseCardChannel();
    }
  }

  /**
   * verify signature for given hash
   * @param padAlgorithm If specified, check whether the card and the key support the pad algorithm.
   *                     If not specified, use the key's pad algorithm
    *
    * @exception java.security.InvalidKeyException
    *         The key type is not supported by the specific card service.
    * @exception CardServiceException any subclass of CardServiceException
    * @exception CardTerminalException any subclass of CardTerminalException
   */
  protected boolean verifySignedHashInternal (PublicKeyRef publicKey,
                                              String signAlgorithm,
                                              String padAlgorithm,
                                              byte[] hash,
                                              byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException
  {
    /*
     * - check parameters
     * - get channel
     * - select key parent directory
     * - read key info
     * - check parameters
     * - authenticate (before next steps to avoid retry of calculate hash)
     * - calculate hash
     * - verify signature
     * - release channel
     */
    if (! (publicKey instanceof PublicKeyFile) ) throw new CardServiceInvalidParameterException
      ("PublicKeyRef must be a PublicKeyFile");

    PublicKeyFile kf = (PublicKeyFile) publicKey;
    CardFilePath parentDirectory = kf.getDirectory();
    int keyNr = kf.getKeyNumber();

    if (parentDirectory == null)
      throw new CardServiceInvalidParameterException
      ("verifySignedHash: parentDirectory = null");

    itracer.debug("verifySignedHashInternal", "parent directory is " + parentDirectory + " key number is " + keyNr + " sign algorithm is " + signAlgorithm+ " pad algorithm is "+((padAlgorithm==null)?"null":padAlgorithm));

    // check if card os supports the pka algorithm
    sigImpl.assertSignHashAlgorithm(signAlgorithm);

    // check if card os supports the padding algorithm
    if (padAlgorithm!=null) sigImpl.assertSignPadAlgorithm(padAlgorithm);

    MFCCardChannel channel = getMFCChannel();

    selectObject(channel, parentDirectory, false);

    //read public key info
    MFCKeyInfo ki = sigImpl.readKeyInfo(channel, keyNr, kiParser,false);
    // check if key is hash key and supports the algorithms
    ki.assertSupportsSignAlgorithm(signAlgorithm,false);
    if (padAlgorithm !=null ) ki.assertSupportsPadAlgorithm(padAlgorithm);
    // check if signature has correct length
    ki.assertSignatureLength(signature.length);

    // check input length
    ki.assertSupportsInputLength(hash.length);

    // verify the hash
    return sigImpl.verify(channel, ki, signature, hash);
  }
}
