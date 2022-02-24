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


/**
 * Represents access information to smartcard objects.
 * Typically, smartcards define several command classes, for
 * example READ and UPDATE. Objects on the smartcard, like
 * files, have access conditions for each command class. These
 * conditions must be satisfied before a command of that
 * class can be executed on the object.
 * <br>
 * This class is meant to represent the access conditions for
 * all command classes that are relevant for a smartcard object.
 * It is abstract since the number of command classes and the
 * representation of the access conditions are implementation
 * dependent.
 *
 * @version $Id: AccessInformation.java,v 1.1 1998/12/18 14:47:42 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public abstract class AccessInformation
{
  /**
   * The access information in a card-specific encoding.
   * Access conditions for a given command class are
   * extracted on demand. The actual position of the
   * first byte of the encoded access information in
   * this array is indicated by <tt>encoded_offset</tt>.
   *
   * @see #encoded_offset
   */
  protected byte[] encoded_conditions = null;

  /**
   * The offset of the access conditions in <tt>encoded_conditions</tt>.
   * Typically, the encoded access conditions are returned by the
   * smartcard as the result of a SELECT operation. Only a part of
   * the select response will encode the relevant information.
   * This index gives the first byte of the relevant information
   * within the array stored in <tt>encoded_conditions</tt>. This
   * allows to deal with the actual select response as well as with
   * an extracted subarray, or data obtained by some other means.
   *
   * @see #encoded_conditions
   */
  protected int encoded_offset = 0;

  /**
   * An array of decoded access conditions.
   * Typically, the various command classes will be identified
   * by a compact range of <tt>int</tt>. Each time the access
   * conditions for a command class have been decoded, they can
   * be stored here for further references to the same command
   * class. The size of this array is specified as a constructor
   * argument.
   */
  protected AccessConditions[] access_conditions = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Initializes new access information.
   * The encoded access conditions are stored, and an array to
   * hold the decoded ones will be allocated. Actual decoding
   * is done on demand.
   *
   * @param encoded     an array holding the encoded access conditions
   * @param offset      the first relevant byte in <tt>encoded</tt>
   * @param cmdclasses  the number of command classes that can be
   *                    represented by a compact range of <tt>int</tt>
   *
   * @see #encoded_conditions
   * @see #encoded_offset
   * @see #access_conditions
   */
  protected AccessInformation(byte[] encoded, int offset, int cmdclasses)
  {
    encoded_conditions  = encoded;
    encoded_offset      = offset;
    access_conditions   = new AccessConditions [cmdclasses];
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Returns access conditions for a given command class.
   * If the access conditions have been decoded before, they are
   * taken from <tt>access_conditions</tt>. Otherwise, they are
   * decoded now and stored there.
   * <br>
   * If storing the access conditions in an array is not appropriate,
   * this method has to be redefined by the respective implementation.
   *
   * @param cmdclass  the command class for which to return access conditions.
   *                  The definition of command classes is implementation
   *                  dependent.
   * @return    the access conditions for the given command class,
   *            or <tt>null</tt> if none are defined
   * <!--
   * @@@ Exception on parse error.
   * -->
   */
  public AccessConditions getAccessConditions(int cmdclass)
  {
    if ((cmdclass < 0) || (cmdclass >= access_conditions.length))
      return null;      // out of known bounds

    AccessConditions accond = access_conditions[cmdclass];

    if (accond == null)
      {
        accond = parseAccessConditions(encoded_conditions,
                                       encoded_offset,
                                       cmdclass);
        access_conditions[cmdclass] = accond;
      }

    return accond;

  } // getAccessConditions



  // service //////////////////////////////////////////////////////////////////


  /**
   * Parses encoded access conditions.
   * This method has to be defined by derived classes to extract the
   * access conditions for a given command class from the implementation
   * dependent encoding.
   *
   * @param encoded     an array holding the encoded access conditions
   * @param offset      the start of the relevant data in <tt>encoded</tt>
   * @param cmdclass    the command class for which to decode the access
   *                    conditions
   * <!--
   * @@@ Exception thrown on parse error
   * -->
   */
  protected abstract AccessConditions parseAccessConditions(byte[] encoded,
                                                            int    offset,
                                                            int    cmdclass)
       ;

} // class AccessInformation
