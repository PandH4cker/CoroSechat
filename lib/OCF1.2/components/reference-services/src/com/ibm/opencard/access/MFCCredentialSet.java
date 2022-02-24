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


import java.util.Hashtable;
import java.util.Enumeration;

import opencard.opt.iso.fs.CardFilePath;
import opencard.core.util.Tracer;
import opencard.opt.security.SignCredential;

/**
 * A credential collection for MFC smartcards.
 * MFC smartcards identify a key that has to be used by a key domain,
 * which is specified by a path, and a key number. A credential is a
 * tuple of a key and the associated algorithm, which is DES, RSA or DSA for MFC
 * smartcards. This credential collection allows storing and retrieving
 * credentials using a path and an integer as arguments.
 * <br>
 * When retrieving a key, the path provided may be that of a subdirectory
 * within the key domain. In order to find the key, a backtracking algorithm
 * has to be used. This means, if the key is not found with the path given,
 * it is searched again with the path to the parent directory, and so on.
 * This is the same backtracking algorithm that is used by the MFC smartcards
 * within the file system for keys with identifiers 0 to 3.
 *
 * @version $Id: MFCCredentialSet.java,v 1.2 1998/08/13 16:53:12 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCCredentialSet
{
  //private static Tracer ctracer = new Tracer(MFCCredentialSet.class);

  /** The underlying data structure. */
  private Hashtable cred_table = null;


  // inner class //////////////////////////////////////////////////////////////

  /**
   * A composition of a path and a number, to be used as credential identifier.
   * Since instances of this class will be put into a hashtable, equals()
   * and hashCode() get overridden here. Both methods depend on attributes
   * that may change dynamically. When using these objects in the hashtable,
   * it has to be made sure that they are not modified after insertion.
   */
  private class MFCCredentialID extends Object
  {
    CardFilePath credential_domain;
    int          credential_number;

    MFCCredentialID()
    {
    }

    MFCCredentialID(CardFilePath domain, int number)
    {
      credential_domain = domain;
      credential_number = number;
    }

    public int hashCode()
    {
      /*
       * By adding the credential number to the domain's hashcode, it is made
       * (basically) sure that credentials with different identifiers for the
       * same domain will be kept in different buckets in the hashtable.
       */
      return credential_domain.hashCode() + credential_number;
    }

    public boolean equals(Object o)
    {
      MFCCredentialID id   = (MFCCredentialID) o;
      boolean         same = false;

      /*
       * The key number is compared first. It is much faster than comparing
       * the domains, and if the numbers match, we have a good chance that
       * the identifiers match, assuming that keys with the same number but
       * different domain will be kept in a different bucket.
       */
      if ((id.credential_number == credential_number) &&
          (id.credential_domain.equals(credential_domain))
           )
        same = true;

      return same;
    }

    boolean isSameDomain(CardFilePath path)
    {
      // Return true iff this identifier has the domain specified by path.
      return credential_domain.equals(path);
    }

    boolean cdDotDot() // cd ..
    {
      /*
       * Chomp off the last part of the path, to change to parent directory.
       * If the path is already the root directory, false is returned.
       */
      return credential_domain.chompTail();
    }
  }


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new credential collection for MFC smartcards.
   */
  public MFCCredentialSet()
  {
    cred_table = new Hashtable();
  }


  // access ///////////////////////////////////////////////////////////////////



  // service //////////////////////////////////////////////////////////////////


  /**
   * Store a credential.
   * The path becomes part of the identifier for the credential, but it
   * will not be cloned. If there is a chance that the path object may
   * be changed, a copy of that object must be passed. Only absolute
   * paths may be used to represent a key domain. This is not checked.
   *
   * @param cred    the credential to store
   * @param path    the key domain for which the credential is valid
   * @param number  the key's number in it's domain
   *
   * @see #retrieveCredential
   */
  public void storeCredential(SignCredential cred,
                              CardFilePath  path,
                              int           number)
  {
    MFCCredentialID id = new MFCCredentialID(path, number);
    cred_table.put(id, cred);
  }


  /**
   * Retrieve a credential.
   * If no matching credential is found for the given path, backtracking
   * to parent directories is applied. The path object provided as argument
   * will not be changed.
   *
   * @param path   the key domain or a subdirectory within the domain
   * @param number the key's number within it's domain
   *
   * @return  the credential previously stored,
   *          or <tt>null</tt> if not found
   *
   * @see #storeCredential
   */
  public SignCredential retrieveCredential(CardFilePath path, int number)
  {
    CardFilePath    pathclone = new CardFilePath(path);
    MFCCredentialID id        = new MFCCredentialID(pathclone, number);
    Object          cred      = null;
    boolean         backtrack = true;

    do {
      cred = cred_table.get(id);     // search credential
      if (cred != null)              // if found
        backtrack = false;           // done
      else                           // if not found
        backtrack = id.cdDotDot();   // change to parent
    } while(backtrack);              // while parent exists

    return (SignCredential) cred;
  }


  /**
   * Delete all credentials of a given domain.
   * This method must be invoked if the credentials for a domain have to
   * be replaced. After the invocation, the new credentials can be stored.
   *
   * @param path    the key domain for which to delete all credentials
   *
   * @see #storeCredential
   */
  public void deleteCredentials(CardFilePath path)
  {
    Enumeration ids = cred_table.keys(); // the table's keys are our ids
    while (ids.hasMoreElements())
      {
        MFCCredentialID id = (MFCCredentialID) ids.nextElement();

        // Removing a credential while iterating on the hashtable means
        // that the iterator, that is the Enumeration returned by the
        // hashtable, has to be stable. The JavaDoc comments do not
        // specify whether this is the case. Let's pray it is...
        if (id.isSameDomain(path))
          cred_table.remove(id);
      }
  }

} // class MFCCredentialSet
