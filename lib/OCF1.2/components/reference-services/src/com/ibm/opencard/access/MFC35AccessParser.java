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

import opencard.core.service.CardServiceInvalidParameterException;

/**
 * Interprets access conditions in MFC 3.5 status information.
 * This parser is suitable for MFC cards 3.5 up to 4.1 at least.
 * <br>
 * The card operating systems supported encode the access conditions
 * for six command classes in five bytes. For each command class, there
 * are four bits specifying the access conditions and two bits specifying
 * an optional key, which is needed for some of the access conditions.
 *
 * @version $Id: MFC35AccessParser.java,v 1.7 1998/08/11 10:16:10 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35AccessParser implements MFCLazyAccessParser
{
  /*
   * The access condition codes as defined in MFC 4.1 programmer's
   * reference V1.0 (page 3-7). Numbers 12 to 14 are reserved for
   * future use and treated as NEVER.
   */
  private final static byte MFC_ACC_COND_ALW      =  0;
  private final static byte MFC_ACC_COND_CHV1     =  1;
  private final static byte MFC_ACC_COND_CHV2     =  2;
  private final static byte MFC_ACC_COND_PRO      =  3;
  private final static byte MFC_ACC_COND_AUT      =  4;
  private final static byte MFC_ACC_COND_ENC      =  5;
  private final static byte MFC_ACC_COND_CHV1_PRO =  6;
  private final static byte MFC_ACC_COND_CHV2_PRO =  7;
  private final static byte MFC_ACC_COND_CHV1_AUT =  8;
  private final static byte MFC_ACC_COND_CHV2_AUT =  9;
  private final static byte MFC_ACC_COND_CHV1_ENC = 10;
  private final static byte MFC_ACC_COND_CHV2_ENC = 11;

  private final static byte MFC_ACC_COND_NEV      = 15;

  /**
   * Encoding table for the six command classes.
   * The access conditions are stored in five bytes. The first two values
   * in each sub-array specify the offset and bit shift of the four bit
   * access condition. The second two values specify the offset and bit
   * shift of the two bit key identifier.
   * <br>
   * The order of the command classes is the order in which the access
   * conditions are encoded. It is identical to the numbering of the
   * command classes in <tt>MFCAccessInformation</tt>. If the numbering
   * should be changed there, either this array must be updated, or the
   * simple initialization has to be replaced by static initialization
   * code that uses the symbolic constants defined there.
   */
  private final static int[][] encoding_table =
  {
    {2, 0, 0, 0},
    {2, 4, 0, 2},
    {3, 0, 0, 4},
    {3, 4, 0, 6},
    {4, 0, 1, 0},
    {4, 4, 1, 2}
  };

  /*
   * Symbolic constants for access to the <tt>encoding_table</tt> array.
   * See comment for that attribute for details.
   */
  private final static int ACC_OFFSET = 0;
  private final static int ACC_SHIFT  = 1;
  private final static int KEY_OFFSET = 2;
  private final static int KEY_SHIFT  = 3;


  // service //////////////////////////////////////////////////////////////////

  /**
   * Interprets MFC 3.5 and above file status information.
   * Extracts and returns the access information encoded there.
   * <br>
   * This method is not static since it is required for implementing the
   * interface <tt>MFCAccessParser</tt>.
   *
   * @param data    the status information to parse
   * @param offset  index of first byte to parse in <tt>data</tt>
   *
   * @return  the decoded access information
   *
   * @exception BadHeaderException
   *            if the data cannot be parsed correctly
   *
   * @see MFCAccessParser
   */
  public MFCAccessInformation parseAccessInformation(byte[] data,
                                                     int    offset)
       throws BadHeaderException
  {
    /*
     * The offset should be 7 if the full file status information
     * is passed as the data. Since someone could have the idea of
     * passing only the 5 bytes required, we cannot check this.
     * Just make sure that there are 5 bytes to interpret.
     */
    if (offset+5 >= data.length)
      throw new BadHeaderException("file status info less than 5 bytes.");

    /*
     * Create new access information for lazy evaluation.
     * If the data passed is simply the five bytes needed, use it.
     * Otherwise, extract these five bytes into a new array.
     */
    byte[] access_data = null;

    if ((offset == 0) && (data.length == 5))
      {
        access_data = data;
      }
    else
      {
        access_data = new byte [5];

        for(int i=0; i < 5; i++)
          access_data[i] = data[offset + i];
      }

    return new MFCAccessInformation(access_data, 0, this);

  } // parseAccessInformation()


  /**
   * Interprets status information for a specific command class.
   * Extracts and returns the access information encoded.
   * <br>
   * Although this method can be invoked by several threads simultaneously,
   * it is not synchronized. There is no need for synchronization, since all
   * attribute accesses are read-only.
   * <br>
   * This method is not static since it is required for implementing the
   * interface <tt>MFCLazyAccessParser</tt>.
   *
   * @param data        status information to parse
   * @param offset      index of first byte of access information
   * @param cmdclass    command class for which access conditions are needed
   *
   * @return  a Java representation of the access conditions for the specified
   *          command class
   *
   * @exception CardServiceInvalidParameterException
   *            if the data cannot be parsed correctly
   *
   * @see MFCLazyAccessParser
   */
  public MFCAccessConditions parseAccessConditions(byte[] data,
                                                   int    offset,
                                                   int    cmdclass)
       throws CardServiceInvalidParameterException
  {
    /*
     * - create access condition ALWAYS
     * - extract the relevant bits from the status information
     * - restrict the ALWAYS access condition accordingly
     */
    MFCAccessConditions acconds = new MFCAccessConditions();
    int[]               enctble = encoding_table[cmdclass];

    int code = (data[offset+enctble[ACC_OFFSET]] >> enctble[ACC_SHIFT]) & 0x0f;
    int key  = (data[offset+enctble[KEY_OFFSET]] >> enctble[KEY_SHIFT]) & 0x03;

    restrictAccessConditions(acconds, code, key);

    return acconds;
  }


  /**
   * Restricts the ALWays access condition according to the
   * four access condition bits and the two key number bits.
   *
   * @param ac           access conditions to restrict
   * @param access_code  four bit access condition encoding
   * @param access_key   two bit access key identifier
   *
   * @exception CardServiceInvalidParameterException
   *            if the condition encoding is unknown
   */
  private static void restrictAccessConditions(MFCAccessConditions ac,
                                               int access_code,   // nibble
                                               int access_key)    // 2 bit
       throws CardServiceInvalidParameterException
  {
    /*
     * First check for CHV, NEV, and illegal codes.
     */
    switch(access_code)
      {
      case MFC_ACC_COND_ALW: // always
      case MFC_ACC_COND_PRO: // protected
      case MFC_ACC_COND_AUT: // authenticated
      case MFC_ACC_COND_ENC: // encrypted
        // no CHV at all
        break;

      case MFC_ACC_COND_NEV: // never
        // no CHV, and no access
        ac.setNeverAccessible( true );
        break;

      case MFC_ACC_COND_CHV1:     // Card Holder Verification 1
      case MFC_ACC_COND_CHV1_PRO: //   and protection
      case MFC_ACC_COND_CHV1_AUT: //   and authentication
      case MFC_ACC_COND_CHV1_ENC: //   and encryption
        ac.setCHV( true );
        ac.setCHVNumber(1);
        break;

      case MFC_ACC_COND_CHV2: // Card Holder Verification 2
      case MFC_ACC_COND_CHV2_PRO: //   and protection
      case MFC_ACC_COND_CHV2_AUT: //   and authentication
      case MFC_ACC_COND_CHV2_ENC: //   and encryption
        ac.setCHV( true );
        ac.setCHVNumber(2);
        break;


      default: // RFU or otherwise illegal
        ac.setNeverAccessible( true );
        throw new CardServiceInvalidParameterException
          ("bad access code " + access_code);
        //break;  // javac would complain
      }

    /*
     * By now we know that the access code is legal. The key number
     * is needed by 9 out of 13 conditions, and ignored by the rest.
     * Set it once and for all, then check for PROtection,
     * AUThentication, and ENCryption.
     *
     * We could check for a valid key number (0..3) first. This is not
     * done, since we know the caller as long as this method is private.
     */
    ac.setKeyNumber(access_key);

    switch(access_code)
      {
      case MFC_ACC_COND_PRO:      // protected
      case MFC_ACC_COND_CHV1_PRO: //   and CHV1
      case MFC_ACC_COND_CHV2_PRO: //   and CHV2
        ac.setProtection( true );
        break;

      case MFC_ACC_COND_AUT:      // authenticated
      case MFC_ACC_COND_CHV1_AUT: //   and CHV1
      case MFC_ACC_COND_CHV2_AUT: //   and CHV2
        ac.setAuthentication( true );
        break;

      case MFC_ACC_COND_ENC:      // encrypted
      case MFC_ACC_COND_CHV1_ENC: //   and CHV1
      case MFC_ACC_COND_CHV2_ENC: //   and CHV2
        ac.setEncryption( true );
        break;

      default:
        break;
      }

  } // restrictAccessConditions()


} // class MFC35AccessParser
