/*
 * (C)Copyright IBM Corporation 1997 - 1999
 * All Rights Reserved.
 */
package com.ibm.opencard.signature;

import opencard.core.service.CardServiceInabilityException;
import opencard.opt.signature.JCAStandardNames;

/**
 * Helper class for implementation of a signature card service for MFC 4.21
 * and compatible.
 * Send APDUs to the card. The methods that send APDUs are synchronized
 * simply to reuse the APDU buffers.
 *
 * @version $Id: MFC421SignatureImpl.java,v 1.4 1998/10/07 06:56:47 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 * @see opencard.opt.signature.SignatureCardService
 * @see MFCSignatureService
 * @see MFC40SignatureImpl
 */
public class MFC421SignatureImpl extends MFC40SignatureImpl {

  /**
   * Instantiates a new signature card service implementation.
   *
   * @param codes   the command codes for the MFC card to support
   */
  public MFC421SignatureImpl(MFCSigCodes codes)
  {
    super(codes);
  }

  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The MFC 4.21 card only supports "SHA-1/RSA" and "DSA" (which combines SHA-1 and RawDSA)
   * @param alg The standard algorithm name.
   * @exception opencard.core.service.CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  public void assertSignDataAlgorithm(String alg) throws opencard.core.service.CardServiceInabilityException
  {
    if (! alg.equals(JCAStandardNames.SHA1_DSA))
      super.assertSignDataAlgorithm(alg);
  }

  /**
   * Assert that a specific signature service implementation supports a signature algorithm.
   * The MFC 4.21 card only supports "RSA" and "RawDSA"
   * @param alg The standard algorithm name.
   * @exception opencard.core.service.CardServiceInabilityException
   *            Thrown if the algorithm is not supported.
   */
  public void assertSignHashAlgorithm(String alg) throws opencard.core.service.CardServiceInabilityException
  {
    if (! alg.equals(JCAStandardNames.RAW_DSA))
      super.assertSignHashAlgorithm(alg);
  }
}
