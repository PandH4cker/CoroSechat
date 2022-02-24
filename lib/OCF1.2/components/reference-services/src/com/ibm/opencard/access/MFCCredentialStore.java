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

package com.ibm.opencard.access;


import java.util.Enumeration;

import opencard.opt.security.CredentialStore;
import opencard.opt.security.SignCredential;
import opencard.core.terminal.CardID;
import opencard.core.util.Tracer;

import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.service.IBMCardServiceFactory;


/**
 * A credential store for MFC smartcards.
 * A credential is a combination of a cryptographic key and the algorithm
 * it is intended for, for example DES. In the following explanation, replace
 * <i>key</i> by <i>credential</i> to understand what is meant.
 * <br>
 * Key stores are used to hold an application's credentials, in form of
 * cryptographic keys, for a specific smartcard. The same application may
 * support different kinds of smartcards, which may require different keys
 * for different cryptographic algorithms. For every smartcard supported,
 * the application will initialize a card-specific key store appropriately,
 * and present them to the card service in a key bag. The service will then
 * pick the appropriate key store for the actual smartcard.
 * <br>
 * MFC smartcards support different, nested key domains. Within a domain,
 * a key is identified by a unique number. The MFC key store is intended
 * to hold the keys for exactly one key domain. It therefore provides methods
 * to store and retrieve keys using integer numbers as key identifier.
 * If an application needs to define keys for different key domains, for
 * example a global domain and an application domain, there has to be one
 * key store for each domain.
 *
 * @version $Id: MFCCredentialStore.java,v 1.6 1998/08/13 16:53:12 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see opencard.opt.security.CredentialStore
 * @see opencard.opt.security.CredentialBag
 * @see opencard.opt.security.SecureService#provideCredentials
 */
public class MFCCredentialStore extends CredentialStore
{
  //private static Tracer ctracer = new Tracer(MFCCredentialStore.class);


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new, empty credential store for MFC smartcards.
   */
  public MFCCredentialStore()
  {
    ;
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Gets the identifiers of all credentials stored.
   * This method can be used if the credentials have to be organized
   * in a different data structure.
   *
   * @return  an <tt>Enumeration</tt> of all identifiers in this store
   *
   * @see java.util.Enumeration
   */
  final public Enumeration enumerateIDs()
  {
    return getCredentialIDs();
  }



  // service //////////////////////////////////////////////////////////////////

  /**
   * Checks whether these credentials are suitable for a particular smartcard.
   *
   * @param cardID   ATR of the smartcard
   * @return <tt>true</tt> if this store is suitable for the smartcard,
   *         <tt>false</tt> if the smartcard is not recognized
   */
  final public boolean supports(CardID cardID)
  {
    int     cos   = IBMCardServiceFactory.determineCardOS(cardID);
    boolean known = false;

    switch(cos)
      {
      case IBMMFCConstants.IBM_MFC_2_COS:
      case IBMMFCConstants.IBM_ZKA_2_COS:
      case IBMMFCConstants.IBM_MFC_3_COS:
      case IBMMFCConstants.IBM_MFC_4_COS:
      case IBMMFCConstants.IBM_MFC_4F_COS:
      case IBMMFCConstants.IBM_MFC_421_COS:
        known = true;
        break;

      default: // not known
        break;
      }

    return known;
  }


  /**
   * Store a new credential in this store.
   * If the store already holds a credential with the given identifier,
   * the new credential replaces the old one.
   *
   * @param num    the identifier of the credential in it's domain
   * @param cred   the credential to add
   */
  final public void storeCredential(int num, SignCredential cred)
  {
    Integer id = new Integer(num);
    storeCredential(id, cred);
  }


  /**
   * Retrieve a credential from this store.
   * If several credentials with the same number have to be looked up,
   * or if the number is already available as an <tt>Integer</tt>, the
   * method <tt>retrieveCredential(Integer)</tt> should be used instead.
   *
   * @param num   the identifier of the credential in it's domain
   * @return      the credential with the given identifier,
   *              or <tt>null</tt> if not found
   *
   * @see #retrieveCredential(java.lang.Integer)
   */
  final public SignCredential retrieveCredential(int num)
  {
    Integer id = new Integer(num);
    return (SignCredential) fetchCredential(id);
  }


  /**
   * Retrieve a credential from this store, with <tt>Integer</tt> identifier.
   * This method expects the identifier as an object rather than as an
   * elementary data item. If several stores may have to be searched for
   * a particular credential, this avoids the overhead of creating a new
   * object for every lookup operation. It is also useful if the credentials
   * are retrieved using the identifiers obtained from <tt>enumerateIDs</tt>.
   *
   * @param id   the identifier of the credential in it's domain
   * @return     the credential with the given identifier,
   *             or <tt>null</tt> if not found
   *
   * @see #retrieveCredential(int)
   * @see #enumerateIDs
   */
  final public SignCredential retrieveCredential(Integer id)
  {
    return (SignCredential) fetchCredential(id);
  }

} // class MFCCredentialStore
