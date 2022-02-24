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


import java.util.Enumeration;

import opencard.core.service.SmartCard;
import opencard.core.service.CardType;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceFactory;
import opencard.core.terminal.CardID;

import com.ibm.opencard.IBMMFCConstants;


/**
 * The base class for IBM's card service factories.
 * The standard card service factory gets extended by a method that
 * interprets the ATR of IBM smartcards. There is also a method that
 * builds an enumeration from an array of classes.
 * <br>
 * This class is abstract since it cannot instantiate any card service
 * for any smartcard. This functionality must be added in derived classes.
 *
 *
 * @version $Id: IBMCardServiceFactory.java,v 1.12 1999/03/11 13:32:25 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCardService
 */
public abstract class IBMCardServiceFactory extends CardServiceFactory
{
  // some ROM ATRs
  private static String mfc35_historicals  = "MFC IBM 40H9601"; // MFC 3.5.1
  private static String mfc40_historicals  = "IBM MFC4PKA 4.0";
  private static String mfc421_historicals = "IBM MFC93128140";

  // constants to decode SmartCard Toolkit ATRs
  private static final int SCT_1_TAG = 0x7e;
  private static final int SCT_2_TAG = 0x7f;
  private static final int SCT_3_COS = 0x03; // MFC 3.5
  private static final int SCT_4_COS = 0x04; // MFC 4.0
  private static final int SCT_5_COS = 0x05; // MFC 4.1
  private static final int SCT_9_COS = 0x09; // MFC 4.21


  // construction /////////////////////////////////////////////////////////////

  /** Default constructor. */
  protected IBMCardServiceFactory()
  {
    ;
  }

  // access ///////////////////////////////////////////////////////////////////


  /**
   * Determine a normalized card operating system value.
   * This method gets mapped onto the static method <tt>determineCardOS</tt>.
   * <I>It may be removed in the future.</I>
   *
   * @param     cardID The <tt>CardID</tt> object to examine.
   * @return    The normalized COS value, or -1 if not recognized.
   *
   * @see #determineCardOS
   */
  protected int normalizedCOSValue(CardID cardID)
  {
    return determineCardOS(cardID);
  }


  /**
   * Analyzes a smartcard's ATR in order to determine the CardOS.
   * For OCF compliant cards, a CardOS identifier is contained in the ATR
   * (<i>Answer-To-Reset</i>). For non-OCF compliant cards, the historical
   * bytes within the ATR will be further investigated.
   * <br>
   * This method currently recognizes MFC 3.5, 4.0, and 4.1 cards.
   * <br>
   * <I>The JavaCard detection is still missing. Thomas, could you add it?</I>
   *
   * @param atr   the ATR of the smartcard
   * @return      the identifier of the CardOS, or -1 if not recognized
   */
  public static int determineCardOS(CardID atr)
  {
    int    cos         = -1;
    byte[] historicals = atr.getHistoricals();

    if (historicals != null)
      {
        if ((historicals[0] == SCT_1_TAG) || (historicals[0] == SCT_2_TAG))
          {
            // SCT proposed structure for historical bytes
            // IBM's issuing conventions
            switch (historicals[1]) {
            case SCT_3_COS:
              cos = IBMMFCConstants.IBM_MFC_3_COS;
              break;

            case SCT_4_COS:
              cos = IBMMFCConstants.IBM_MFC_4_COS;
              break;

            case SCT_5_COS:
              cos = IBMMFCConstants.IBM_MFC_4F_COS;
              break;

            case SCT_9_COS:
              cos = IBMMFCConstants.IBM_MFC_421_COS;
              break;

            default:
              break;
            }
          }
        else // check for ROM ATR
          {
            String hist = new String(historicals);

            if        (hist.equals(mfc35_historicals)) {
              cos = IBMMFCConstants.IBM_MFC_3_COS;
            } else if (hist.equals(mfc40_historicals)) {
              cos = IBMMFCConstants.IBM_MFC_4_COS;
            } else if (is41ATR(hist)) {
              cos = IBMMFCConstants.IBM_MFC_4F_COS;
            } else if (hist.equals(mfc421_historicals)) {
              cos = IBMMFCConstants.IBM_MFC_421_COS;
            }

          } // ROM ATR check
      } // historicals != null

    return cos;

  } // determineCardOS()


  /**
   * Check whether the historical bytes are a MFC 4.1 ROM ATR.
   * The MFC 4.1 ROM ATR is dependent on the EEPROM size, so the simple
   * check that can be done for the other MFCs would not work.
   *
   * @param historicals   the historical bytes from the ATR
   * @return    <tt>true</tt> if the bytes are a MFC 4.1 ROM ATR,
   *            <tt>false</tt> otherwise
   */
  private static boolean is41ATR(String historicals)
  {
    // The ATR is "IBM MFC400x0831", where  x  is one of  0, 1, 2, 3.
    // The check is split into: length, prefix, suffix, x

    boolean maybe = (historicals.length() == 15);

    if (maybe)
      maybe = historicals.startsWith("IBM MFC400");
    if (maybe)
      maybe = historicals.endsWith("0831");
    if (maybe)
      maybe = ("0123".indexOf(historicals.charAt(10)) >= 0);

    return maybe; // if it still may be, then it is
  }

  // service //////////////////////////////////////////////////////////////////


  /**
   * Create an enumeration from an array of classes.
   *
   * @param classes   the array to enumerate
   * @return   an enumeration of the argument array
   */
  protected Enumeration enumerateClasses(Class[] classes)
  {
    // inner classes can access final local variables only
    final Class[] fca = classes;

    return new Enumeration() {
      int ctr = 0;

      public boolean hasMoreElements() {
	return ((fca != null) && (ctr < fca.length));
      }

      public Object nextElement() {
	return fca[ctr++];
      }
    };
  } // enumerateClasses

  /** Utility method to instantiate a <tt>CardService</tt>.
   * In addition to the functionality provided in the parent
   * class stores the card type in the service.
   *
   * @param     clazz
   *		The class of the <tt>CardService</tt> to instantiate.
   * @param     scheduler
   *		The controlling scheduler.
   * @param     card
   *		The owning <tt>SmartCard</tt> object.
   * @param     blocking
   *		Whether to run the new <tt>CardService</tt> in blocking mode.
   * @return    The instantiated <tt>CardService</tt> object or <tt>null</tt> if
   *		the requested class could not be instantiated.
   *
   * @exception CardServiceException
   *    if the service could be instantiated using the default constructor,
   *    but encountered an error when <tt>initialize</tt> was invoked
   *
   * @see CardService#CardService()
   * @see CardService#initialize
   */
  protected CardService newCardServiceInstance(Class clazz, 
                                               CardType type,
                                               CardServiceScheduler scheduler,
					                                     SmartCard card, boolean blocking)
    throws CardServiceException {

    MFCCardService instance = null;

    try {
      instance = (MFCCardService) clazz.newInstance();
    } catch (NoSuchMethodError nsme) {
      // instantiation failed, return null
    } catch (IllegalAccessException iax) {
      // instantiation failed, return null
    } catch (InstantiationException ix) {
      // instantiation failed, return null
    }

    if (instance != null)
      instance.setCardType(type);
      instance.initialize(scheduler, card, blocking);

    return instance;
  }


} // class IBMCardServiceFactory
