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


import com.ibm.opencard.service.AccessConditions;


/**
 * Represents the access conditions for a command class.
 * Access conditions can require CHV, AUThentication, ENCryption or
 * PROtection, or combinations thereof. Additionally, a CHV number
 * and key number may be needed. Instances of this class are used by
 * a <tt>MFCCardAccessor</tt> to determine the actions required to
 * execute a command.
 * <br>
 * The access information for all command classes is coded in some
 * status bytes that are returned on object selection. These bytes get
 * parsed by a <tt>MFCAccessParser</tt> in order to create instances
 * of this class. The access conditions for all of the command classes
 * is composed by the class <tt>MFCAccessInformation</tt>.
 * <p>
 * This class supports MFC 4.1 cards and below. It may find some use
 * with MFC 4.2 cards and above, but that is not yet sure.
 *
 * @version $Id: MFCAccessConditions.java,v 1.8 1998/12/18 14:51:14 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCAccessConditions implements AccessConditions
{

  /*
   * These flags are set to true if the respective access condition
   * has to be satisfied. Note that more than one flag may be true,
   * since MFC cards allow combined conditions like CHV+ENC.
   */
  private boolean requires_CHV = false;
  private boolean requires_PRO = false;
  private boolean requires_AUT = false;
  private boolean requires_ENC = false;

  /*
   * The access condition NEVer does not require any of the
   * access types mentioned above, since there is no way at all
   * to access the object. This attribute is used to distinguish
   * between access conditions NEVer and ALWays. It is set to
   * true for NEVer.
   */
  private boolean never_accessible = false;

  /*
   * These attributes store the CHV and key number to be used
   * to satisfy the respective access conditions.
   */
  private int number_of_CHV = 1;    // 1 or 2
  private int number_of_key = 0;    // 0 to 3


  // construction ////////////////////////////////////////////////////////////

  /**
   * Creates a new access condition ALWays.
   * This access condition does not require any secure messaging,
   * authentication or card holder verification. Several methods
   * are offered to restrict this access condition.
   * <br>
   * The constructor has default visibility package since it is expected
   * that there is a parser here in the package which interprets the
   * status information returned by the smartcard and creates appropriate
   * access conditions. Access conditions are not meant to be changed
   * anywhere else.
   *
   * @see #setCHV
   * @see #setProtection
   * @see #setAuthentication
   * @see #setEncryption
   *
   * @see #setCHVNumber
   * @see #setKeyNumber
   */
  MFCAccessConditions()
  {
  }


  // access //////////////////////////////////////////////////////////////////

  /**
   * Returns whether Card Holder Verification is required.
   * The CHV number can be determined using <tt>getCHVNumber()</tt>.
   *
   * @return <tt>true</tt> if CHV is required,
   *         <tt>false</tt> otherwise
   *
   * @see #getCHVNumber
   */
  final public boolean requiresCHV()
  {
    return requires_CHV;
  }


  /**
   * Returns whether PROtection is required.
   * If protection is required, the command and/or the response
   * have to be protected by a MAC. The key needed for the MAC
   * computation can be determined using <tt>getKeyNumber()</tt>.
   *
   * @return <tt>true</tt> if protection is required,
   *         <tt>false</tt> otherwise
   *
   * @see #getKeyNumber
   */
  final public boolean requiresProtection()
  {
    return requires_PRO;
  }


  /**
   * Returns whether AUThentication is required.
   * The key needed for the authentication can be determined
   * using <tt>getKeyNumber()</tt>.
   *
   * @return <tt>true</tt> if authentication is required,
   *         <tt>false</tt> otherwise
   *
   * @see #getKeyNumber
   */
  final public boolean requiresAuthentication()
  {
    return requires_AUT;
  }


  /**
   * Returns whether ENCryption is required.
   * If encryption is required, part of the command and/or the
   * response have to be encrypted. The key needed for the encryption
   * can be determined using <tt>getKeyNumber()</tt>.
   *
   * @return <tt>true</tt> if CHV is required,
   *         <tt>false</tt> otherwise
   *
   * @see #getKeyNumber
   */
  final public boolean requiresEncryption()
  {
    return requires_ENC;
  }


  /**
   * Returns whether an access is possible at all.
   * MFC cards support the access conditions ALWays and NEVer.
   * Neither of them requires any of the conditions mentioned
   * above, but they still have to be distinguished. This method
   * returns <tt>true</tt> iff the access condition is NEVer.
   *
   * @return <tt>true</tt> if the access condition is NEVer,
   *         <tt>false</tt> otherwise
   */
  final public boolean isNeverAccessible()
  {
    return never_accessible;
  }

  /**
   * Returns the number of the CHV to perform.
   * MFC cards support two Card Holder Verifications which are
   * distinguished by their number, 1 or 2.
   *
   * @return 1 for CHV1, 2 for CHV2
   */
  public int getCHVNumber()
  {
    return number_of_CHV;
  }

  /**
   * Returns the number of the key to use.
   * Keys are required for PROtection, AUThentication and ENCryption.
   * The MFC cards support up to 4 keys to be used for these operations,
   * numbered 0 to 3. This method returns the number of the key that
   * has to be used to perform one of these functions.
   * The actual key to be used depends on the key domain of the object
   * to be accessed with the command.
   *
   * @return the number of the key to use (0 to 3)
   *
   * @see #requiresAuthentication
   * @see #requiresProtection
   * @see #requiresEncryption
   */
  public int getKeyNumber()
  {
    return number_of_key;
  }

  // service //////////////////////////////////////////////////////////////////

  /*
   * The following methods are used to parameterize instances of this class.
   * They have the default package visibility. It is intended that there is
   * a parser within this package that creates MFCAccessConditions, and that
   * these do not get modified anywhere else.
   */


  /**
   * Sets the CHV access condition.
   * The number of the CHV has to be set explicitly by a call
   * to <tt>setCHVNumber</tt>. It defaults to 1 after creation.
   *
   * @param required  <tt>true</tt> if CHV shall be required,
   *                  <tt>false</tt> otherwise
   *
   * @see #requiresCHV
   * @see #setCHVNumber
   */
  final void setCHV(boolean required)
  {
    requires_CHV = required;
  }


  /**
   * Sets the PROtected access condition.
   * The number of the key to use has to be set explicitly
   * by a call to <tt>setKeyNumber()</tt>. It defaults to 0
   * after creation.
   *
   * @param required  <tt>true</tt> if protection shall be required,
   *                  <tt>false</tt> otherwise
   *
   * @see #requiresProtection
   * @see #setKeyNumber
   */
  final void setProtection(boolean required)
  {
    requires_PRO = required;
  }


  /**
   * Sets the AUThentication access condition.
   * The number of the key to use has to be set explicitly
   * by a call to <tt>setKeyNumber()</tt>.
   *
   * @param required  <tt>true</tt> if authentication shall be required,
   *                  <tt>false</tt> otherwise
   *
   * @see #requiresAuthentication
   * @see #setKeyNumber
   */
  final void setAuthentication(boolean required)
  {
    requires_AUT = required;
  }


  /**
   * Sets the ENCryption access condition.
   * The number of the key to use has to be set explicitly
   * by a call to <tt>setKeyNumber()</tt>.
   *
   * @param required  <tt>true</tt> if encryption shall be required,
   *                  <tt>false</tt> otherwise
   *
   * @see #requiresEncryption
   * @see #setKeyNumber
   */
  final void setEncryption(boolean required)
  {
    requires_ENC = required;
  }


  /**
   * Sets the NEVer access condition.
   * The other access conditions will remain set as before,
   * but their contents is meaningless if NEVer is <tt>true</tt>.
   *
   * @param never  <tt>true</tt> if access shall never be permitted,
   *                  <tt>false</tt> otherwise
   *
   * @see #isNeverAccessible
   */
  final void setNeverAccessible(boolean never)
  {
    never_accessible = never;
  }



  /**
   * Sets the number of the CHV that shall be required.
   * Note that currently only numbers 1 and 2 are valid.
   * <tt>getCHVNumber()</tt> relies on that.
   * Negative numbers should be used if magic numbers are needed.
   *
   * @param number  the CHV number required (1 or 2)
   *
   * @see #getCHVNumber
   * @see #setCHV
   */
  void setCHVNumber(int number)
  {
    number_of_CHV = number;
  }


  /**
   * Sets the number of the key to be used for secure access.
   * Note that currently only key numbers 0 to 3 are valid.
   * <tt>getKeyNumber()</tt> relies on that.
   * <p>
   * MFC cards up to 4.1 support only one key for all three
   * operations that may be required: AUT, PRO, ENC. If these
   * operations have to be combined, they have to use the same
   * key.
   * <br>
   * This situation changes with MFC 4.2. This card will support
   * much more complex access conditions. It is not yet clear in
   * which way the classes for the current access conditions may
   * be reused. Overriding the methods to set and get the key
   * number may be useful, which is the reason for these methods
   * not to be <tt>final</tt>.
   *
   * @param number  the key number to use (0 to 3)
   *
   * @see #getKeyNumber
   */
  void setKeyNumber(int number)
  {
    number_of_key = number;
  }


  /**
   * Returns a human-readable representation of this object.
   *
   * @return  a string representing these access conditions
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer(super.toString());

    if (requires_CHV)
      sb.append(" CHV" + number_of_CHV);
    if (requires_AUT)
      sb.append(" AUT" + number_of_key);
    if (requires_PRO)
      sb.append(" PRO" + number_of_key);
    if (requires_ENC)
      sb.append(" ENC" + number_of_key);

    if (never_accessible)
      sb.append(" Never");

    return sb.toString();
  }

} // class MFCAccessConditions
