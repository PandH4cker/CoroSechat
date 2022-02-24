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

import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.access.MFCAccessorFactory;
import com.ibm.opencard.service.MFCGenericFactory;
import com.ibm.opencard.service.IBMCardServiceFactory;


/**
 * A factory class for MFC signature service objects.
 * Here, the term factory refers to the design technique, not to
 * OCF card service factories.
 * The signature services implement the interfaces ...
 * This class allows to create appropriate implementations of these services,
 * along with some other helper objects.
 *
 * @version $Id: MFCSignatureFactory.java,v 1.5 1999/03/11 13:32:27 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public final class MFCSignatureFactory {

  /** Command codes for MFC 4.0 signature commands. */
  private static MFCSigCodes mfc40_sig_codes = null;

  /** Command codes for MFC 4.21 signature commands. */
  private static MFCSigCodes mfc421_sig_codes = null;


  /** Signature service implementation for MFC 4.0 */
  private static MFCSignatureImpl ssi_40 = null;

  /** Signature service implementation for MFC 4.21 */
  private static MFCSignatureImpl ssi_421 = null;


  /** Key info parser for MFC 4.0 and 4.21 */
  private static MFCKeyInfoRParser ki40_parser = null;
  private static MFCKeyInfoRParser ki421_parser = null;




  /** Disabled default constructor. */
  private MFCSignatureFactory() {
    // no body
  }

  /** Returns command codes for MFC 4.0 signature commands. */
  static MFCSigCodes get40SigCodes() {
    if (mfc40_sig_codes == null)
      mfc40_sig_codes = new MFC40SigCodes();
    return mfc40_sig_codes;
  }

  /** Returns a signature service implementation for MFC 4.0 cards. */
  private static MFCSignatureImpl get40SigImpl() {
    if (ssi_40 == null)
      ssi_40 = new MFC40SignatureImpl(get40SigCodes());
    return ssi_40;
  }

  /** Returns command codes for MFC 4.21 signature commands. */
  static MFCSigCodes get421SigCodes() {
    // currently no specific codes for 4.21
    return get40SigCodes();
  }

  /** Returns a signature service implementation for MFC 4.21 cards. */
  private static MFCSignatureImpl get421SigImpl() {
    if (ssi_421 == null)
      ssi_421 = new MFC421SignatureImpl(get421SigCodes());
    return ssi_421;
  }

  /**
   * Returns a key info parser for the 4.0 card
   * The parser is created on the first invocation and re-used later.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       a key info parser for the 4.0 card
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCKeyInfoRParser get40KIParser(int cos) {
    if (ki40_parser == null)
      ki40_parser = new MFC40KeyInfoRParser(MFCAccessorFactory.getACParser(cos));
    return ki40_parser;
  }

  /**
   * Returns a key info parser for the 4.21 card
   * The parser is created on the first invocation and re-used later.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       a key info parser for the 4.21 card
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCKeyInfoRParser get421KIParser(int cos) {
    if (ki421_parser == null)
      ki421_parser = new MFC421KeyInfoRParser(MFCAccessorFactory.getACParser(cos));
    return ki421_parser;
  }


  /**
   * Returns a key info parser for the given CardOS.
   * The parser is created on the first invocation and re-used later.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       a key info parser for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCKeyInfoRParser getKIParser(int cos) {
    MFCKeyInfoRParser parser = null;

    switch (cos) {
      case IBMMFCConstants.IBM_MFC_4_COS:
        parser = get40KIParser(cos);
        break;
      case IBMMFCConstants.IBM_MFC_421_COS:
        parser = get421KIParser(cos);
        break;
      default:
        break;
    }
    return parser;
  }



  /**
   * Returns signature command codes for the given CardOS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       signature command codes for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCSigCodes getSigCodes(int cos) {
    MFCSigCodes codes = null;

    switch (cos) {
      case IBMMFCConstants.IBM_MFC_4_COS:
        codes = get40SigCodes();
        break;
      case IBMMFCConstants.IBM_MFC_421_COS:
        codes = get421SigCodes();
        break;
      default:
        break;
    }
    return codes;
  }

  /**
   * Returns a signature service implementation for the given CardOS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       a signature service implementation for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCSignatureImpl getSigImpl(int cos) {
    MFCSignatureImpl ssi = null;

    switch (cos) {
      case IBMMFCConstants.IBM_MFC_4_COS:
        ssi = get40SigImpl();
        break;
      case IBMMFCConstants.IBM_MFC_421_COS:
        ssi = get421SigImpl();
        break;
      default:
        break;
    }

    return ssi;
  }

  /**
   * Initializes a <tt>MFCSignatureParameter</tt>.
   * The parameter gets initialized with a signature service implementation
   * and a key info parser. The generic parts are initialized by invoking
   * <tt>MFCGenericFactory.initParameter</tt>.
   *
   * @param param       the parameter object to initialize
   * @param cos         the card OS to support, see <tt>IBMMFCConstants</tt>
   *
   * @see com.ibm.opencard.service.MFCGenericFactory#initParameter
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static void initSigParam(MFCSignatureParameter param, int cos) {
    MFCGenericFactory.initParameter(param, cos);

    param.setSignatureImpl(getSigImpl(cos));
    param.setKiParser    (getKIParser(cos));
  }

  /**
   * Returns a new, initialized parameter for creating a signature service.
   * The parameter is initialized by invoking <tt>initSigParam</tt>.
   *
   * @param scheduler   where to allocate channels,
   *                    gets stored in the parameter
   * @param smartcard   the smartcard to support,
   *                    gets stored in the parameter
   * @param blocking    whether to operate blocking,
   *                    gets stored in the parameter
   * @param cos         card os
   *
   * @return a parameter for instantiating <tt>MFCSignatureService</tt>
   *
   * @see #initSigParam
   * @see MFCSignatureService#MFCSignatureService(com.ibm.opencard.signature.MFCSignatureParameter)
   */
  public static
  MFCSignatureParameter newSigParam(CardServiceScheduler scheduler,
                                    SmartCard            smartcard,
                                    boolean              blocking,
                                    int cos) {
    MFCSignatureParameter p = new MFCSignatureParameter(scheduler, smartcard,blocking);
    initSigParam(p, cos);
    return p;
  }
}