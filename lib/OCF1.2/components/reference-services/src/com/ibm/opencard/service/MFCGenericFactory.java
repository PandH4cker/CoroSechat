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


import opencard.core.service.CardServiceScheduler; // for custom channels

import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.access.MFCAccessorFactory;


/**
 * A factory class for MFC generic service objects.
 * Here, the term factory refers to the design technique, not to
 * OCF card service factories.
 * The generic service, that is the base class of all MFC services,
 * is responsible for the SELECT command. This factory class allows
 * to create appropriate implementations of the generic card service,
 * along with some other helpers used.
 *
 * @version $Id: MFCGenericFactory.java,v 1.2 1998/08/14 06:32:44 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public final class MFCGenericFactory
{
  /** The standard command codes for MFC 3.5 and 4.0 smartcards. */
  private static MFCCodes mfc35_codes = null;

  /** The standard command codes for MFC 4.1 and 4.21 smartcards. */
  private static MFCCodes mfc41_codes = null;


  /** Generic card service implementation for MFC 3.5 and 4.0 */
  private static MFCCardServiceImpl mfc35_csi = null;

  /** Generic card service implementation for MFC 4.1 and above. */
  private static MFCCardServiceImpl mfc41_csi = null;


  /** Generic select response parser for all MFC cards. */
  private static MFCSelectResponseParser sr_parser = null;



  /** Disabled default constructor. */
  private MFCGenericFactory()
  {
    // no body
  }


  // command codes ////////////////////////////////////////////////////////////

  /**
   * Returns command codes for the given card OS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   command codes suitable for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCCodes getCodes(int cos)
  {
    MFCCodes codes = null;

    switch (cos)
      {
      case IBMMFCConstants.IBM_MFC_3_COS:
      case IBMMFCConstants.IBM_MFC_4_COS:
        codes = get35Codes();
        break;

      case IBMMFCConstants.IBM_MFC_4F_COS:
      case IBMMFCConstants.IBM_MFC_421_COS:
        codes = get41Codes();
        break;

      default: // will not happen
        break;
      }

    return codes;
  }


  /**
   * Returns command codes for MFC 3.5 and 4.0 smartcards.
   * The codes are created on the first invocation and re-used later.
   *
   * @return command codes for MFC 4.0 and below
   */
  public static MFCCodes get35Codes()
  {
    if (mfc35_codes == null)
      mfc35_codes = new MFC35Codes();
    return mfc35_codes;
  }


  /**
   * Returns command codes for MFC 4.1 and above smartcards.
   * The codes are created on the first invocation and re-used later.
   *
   * @return command codes for MFC 4.1 and 4.21
   */
  public static MFCCodes get41Codes()
  {
    if (mfc41_codes == null)
      mfc41_codes = new MFC41Codes();
    return mfc41_codes;
  }


  // card service implementation //////////////////////////////////////////////

  /**
   * Returns a generic card service implementation for the given CardOS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   a card service implementation suitable for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCCardServiceImpl getCSImpl(int cos)
  {
    MFCCardServiceImpl csi = null;

    switch (cos)
      {
      case IBMMFCConstants.IBM_MFC_3_COS:
      case IBMMFCConstants.IBM_MFC_4_COS:
        csi = get35CSImpl();
        break;

      case IBMMFCConstants.IBM_MFC_4F_COS:
      case IBMMFCConstants.IBM_MFC_421_COS:
        csi = get41CSImpl();
        break;

      default: // will not happen
        break;
      }

    return csi;
  }


  /**
   * Returns generic card service implementation for MFC 4.0 and below cards.
   * The implementation is created on the first invocation and re-used later.
   *
   * @return implementation of generic card service for MFC 4.0 and 3.5
   */
  public static MFCCardServiceImpl get35CSImpl()
  {
    if (mfc35_csi == null)
      mfc35_csi = new MFC35CardServiceImpl(get35Codes());
    return mfc35_csi;
  }


  /**
   * Returns generic card service implementation for MFC 4.1 and above cards.
   * The implementation is created on the first invocation and re-used later.
   *
   * @return implementation of generic card service for MFC 4.1 and 4.21
   */
  public static MFCCardServiceImpl get41CSImpl()
  {
    if (mfc41_csi == null)
      mfc41_csi = new MFC41CardServiceImpl(get41Codes());
    return mfc41_csi;
  }


  // select response parser ///////////////////////////////////////////////////

  /**
   * Returns a generic select response parser for the given CardOS.
   * Currently, there is only one select response parser for all MFC cards.
   * The argument is therefore ignored.
   * The parser is created on the first invocation and re-used later.
   * This method is typically not invoked, since more specific select
   * response parsers are required for most services.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   a generic select response parser for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCSelectResponseParser getSRParser(int cos)
  {
    if (sr_parser == null)
      sr_parser = new MFC35ObjectSRParser(MFCAccessorFactory.getACParser(cos));
    return sr_parser;
  }


  // service parameters ///////////////////////////////////////////////////////

  /**
   * Initializes a <tt>MFCCardServiceParameter</tt>.
   * The parameter gets initialized with a card accessor and a generic card
   * service implementation. The select response parser is not initialized,
   * since it would be replaced by a more specific one in most cases.
   * <br>
   * The scheduler stored in the parameter is checked. If it is not yet
   * customized, it will be customized to manage a <tt>MFCCardChannel</tt>.
   * If the scheduler has already been customized to manage another kind
   * of channel, the services created from the parameter will not be usable.
   *
   * @param param       the parameter to initialize
   * @param cos         the card OS identifier, see <tt>IBMMFCConstants</tt>
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static void initParameter(MFCCardServiceParameter param, int cos)
  {
    param.setAccessor(MFCAccessorFactory.newAccessor(cos));
    param.setCardServiceImpl(getCSImpl(cos));

    if (param.cs_scheduler != null)
      customizeScheduler(param.cs_scheduler);
  }


  // customized channels //////////////////////////////////////////////////////

  /**
   * Customizes a scheduler to manage a <tt>MFCCardChannel</tt>.
   * Customization is performed only if the scheduler is not yet customized.
   *
   * @param scheduler   the scheduler to customize
   *
   * @see opencard.core.service.CardServiceScheduler
   */
  public static void customizeScheduler(CardServiceScheduler scheduler)
  {
    synchronized (scheduler)
      {
        if (!scheduler.isCustomized())
          {
            MFCChannelState chanstate = new MFCChannelState();
            MFCCardChannel  channel   =
              new MFCCardChannel(scheduler.getSlotChannel(), 0, chanstate);
            scheduler.setCustomChannel(channel);
          }
      }
  }


} // class MFCGenericFactory
