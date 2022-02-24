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

package com.ibm.opencard.factory;


import java.util.Enumeration;

import opencard.core.service.CardService;
import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;
import opencard.core.util.Tracer;

import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.service.IBMCardServiceFactory;
import com.ibm.opencard.isofs.MFCFileAccess;
import com.ibm.opencard.isofs.MFCFileSystem;
import com.ibm.opencard.signature.MFCSignatureService;
import com.ibm.opencard.script.MFCScriptService;



/**
 * A factory for MFC card services (exportable).
 * This factory can only instantiate card services which are exportable
 * according to U.S. Export Regulations.
 * For non-exportable MFC card services use MFCCardServiceFactoryER.
 * This class is not available in exportable verions of OpenCard.
 * 
 * While the first OCF design assumed a standard constructor for card
 * services, the new card services for MFC smartcards require different
 * constructor arguments for each card service. The responsibility
 * of the card service factory extends to the creation of various
 * helper objects.
 *
 * @version $Id: MFCCardServiceFactory.java,v 1.8 1999/03/11 13:32:20 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @see MFCCardServiceFactoryER
 */
public class MFCCardServiceFactory extends IBMCardServiceFactory
{
  /** A tracer for debugging output. */
  private static Tracer ctracer = new Tracer(MFCCardServiceFactory.class);

  /** This array holds the classes that can be instantiated here. */
  private static Class[] sign_service =
  {
    MFCFileAccess.class,
    MFCFileSystem.class,
    MFCSignatureService.class,
    MFCScriptService.class
  };

  /** MFC 4.1 has no signature functions */
  private static Class[] file_service =
  {
    MFCFileAccess.class,
    MFCFileSystem.class,
    MFCScriptService.class
  };


  // construction /////////////////////////////////////////////////////////////

  /** Default constructor. */
  public MFCCardServiceFactory()
  {
    // no body
  }


  // access ///////////////////////////////////////////////////////////////////


  /** Indicate whether this <tt>CardServiceFactory</tt> "knows" the smart card OS
   * and/or installed card applications
   * and might be able to instantiate <tt>CardService</tt>s for it. 
   * <p>
   * This method replaces the former knows() method.
   * <p>
   * Should return a CardType that contains enough information to answer
   * the getClassFor() method.
   * <p>
   * The factory can inspect the card (communicate with the card) using
   * the provided CardServiceScheduler if the CardID information is insufficient
   * to classify the card.
   * 
   *
   * @param     cid
   *		A <tt>CardID</tt> received from a <tt>Slot</tt>.
   * @param scheduler
   *    A <tt>CardServiceScheduler</tt> that can be used to communicate with
   *    the card to determine its type.
   *
   * @return A valid CardType if the factory can instantiate services for this
   *         card.
   *         CardType.UNSUPPORTED if the factory does not know the card.
   *
   * @see ##getClassFor
   *
   */
  protected CardType getCardType(CardID cid, 
                     CardServiceScheduler scheduler) {
     int cos = determineCardOS(cid);

     if (cos==-1) {
       return CardType.UNSUPPORTED;
     }
     return new CardType(cos);
                                          }




  // service //////////////////////////////////////////////////////////////////


  /** Return an enumeration of known <tt>CardService</tt> classes.
   *
   * @param     type
   *            The <tt>CardType</tt> for which to enumerate.
   * @return    An <tt>Enumeration</tt> of class objects.
   */
  protected Enumeration getClasses(CardType type) {
    ctracer.debug("getClasses", "card type is " + type.getType());

    Class[] classes = null;

    switch (type.getType()) {
    case IBMMFCConstants.IBM_MFC_3_COS:
    case IBMMFCConstants.IBM_MFC_4F_COS:
      classes = file_service;
      break;
    case IBMMFCConstants.IBM_MFC_4_COS:
    case IBMMFCConstants.IBM_MFC_421_COS:
      classes = sign_service;
      break;

    default:
      ctracer.debug("cardServiceClasses", "no classes - strange!");
      break;
    }

    return enumerateClasses(classes);
  }
} // class MFCCardServiceFactory
