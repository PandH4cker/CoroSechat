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

import com.ibm.opencard.service.MFC35Codes;
import com.ibm.opencard.service.MFCResponseAPDUCodes;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceOperationFailedException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceInvalidCredentialException;

import opencard.opt.service.CardServiceInvalidCommandException;
import opencard.opt.service.CardServiceObjectNotAvailableException;

/**
 * CLAss and INStruction codes for signature commands of MFC 4.0 and 4.21 smartcards.
 * This class implements exactly the interface <tt>MFCSigCodes</tt>.
 * Most methods are trivial and therefore not commented.
 *
 * @version $Id: MFC40SigCodes.java,v 1.7 1999/03/23 10:15:13 pbendel Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @see MFCSigCodes
 */
public class MFC40SigCodes extends MFC35Codes implements MFCSigCodes {
  public MFC40SigCodes()
  {
    ;
  }

  /**
    * Analyses the status of a smartcard's response to a key command.
    * If the command completed successfully, the method returns without
    * further action. If an error occurred, an exception is thrown.
    * This method uses <tt>indicatesError</tt> to determine whether an
    * exception has to be thrown.
    *
    * @param status   the status word returned by the smartcard
    * @param command  the INStruction sent to the smartcard
    * @param context  the context in which the command was issued
    *
    * @exception CardServiceException  iff the status indicates an error
    *
    * @see com.ibm.opencard.service.MFC35Codes#indicatesError
    */
  public void analyseStatus(int status, byte command, Object context)
  throws CardServiceException
  {
    if (!indicatesError(status))
      return;

    StringBuffer sb = new StringBuffer();

    if (context != null)
      sb.append(context.toString()).append(": ");

    switch (status) {
      case MFCResponseAPDUCodes.RAPDU_REFERENCE_DATA_NOT_FOUND:
        {
          sb.append("key (file) not found");
          throw new CardServiceObjectNotAvailableException(sb.toString());
        }
        // no break because of previous throw
      case MFCResponseAPDUCodes.RAPDU_LE_INCORRECT:
        {
          sb.append("Le is not correct");
          throw new CardServiceInvalidCommandException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_KEY_STATUS_NOT_FULFILLED:
        {
          sb.append("key status no fulfilled");
          throw new CardServiceInvalidParameterException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_KEY_CONDITIONS_OF_USE_NOT_SATISFIED:
        {
          if (command == getGenKeyByte()) {
            sb.append("invalid key file headers on card");
          } else {
            sb.append("key is a hash key");
          }
          throw new CardServiceInvalidParameterException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_KEY_USAGE_ERROR:
        {
          sb.append("key usage error");
          throw new CardServiceInvalidParameterException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_FUNCTION_NOT_SUPPORTED:
        {
          sb.append("algorithm not supported by card");
          throw new CardServiceInvalidParameterException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_NOT_AUTHENTICATED:
        {
          // should never happen because this is already checked by MFCCardAccessor
          sb.append("access condition not satisfied");
          throw new CardServiceInvalidCredentialException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_OUT_OF_RANGE:
        {
          sb.append("key file too small");
          throw new CardServiceInvalidParameterException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_COPROCESSOR_NO_PRIMES:
        {
          sb.append("coprocessor could not generate primes");
          throw new CardServiceOperationFailedException(sb.toString());
        }
      case MFCResponseAPDUCodes.RAPDU_EEPROM_PROG_FAILURE:
        {
          sb.append("EEPROM programming failure");
          throw new CardServiceOperationFailedException(sb.toString());
        }
      default:
        {
          sb.append("INS 0x");
          sb.append(Integer.toHexString(command & 0xff));
          sb.append(" bad status 0x");
          sb.append(Integer.toHexString(status & 0xffff));
          throw new CardServiceOperationFailedException(sb.toString());
        }

    } // switch
  } // analyseStatus

  /**
   * instruction byte for calculate hash
   */
  public byte getCalculateHashByte() {
    return (byte) 0x50;
  }

  /**
   * Get the maximum effective data length for commands in key group.
   * @return int
   */
  public int getEffectiveDataLength() { return 249 ;} // PTR0216

  /**
   * P1 for finish stage of commands where the input data is provided to the card in several blocks
   */
  public byte getFinishByte() {return (byte)0x0;}

  /**
   * instruction byte for generate signature
   */
  public byte getGenerateSignatureByte() {
    return (byte) 0x52;
  }

  /**
   * instruction byte for generate key pair
   * @return byte
   */
  public byte getGenKeyByte() {return (byte)0x46;}

  /**
   * instruction byte for import key
   * @return byte
   */
  public byte getImportKeyByte() {return (byte)0xde;}

  /**
   * The maximum input block length of the generate signature command for all key strengths and
   * padding algorithms supported by the MFC 4.0 card.
   * @return int
   */
  public int getMaxInputBlockLength() {
    return 64;
  }

  /**
   * P1 for prepare stage of commands where the input data is provided to the card in several blocks
   */
  public byte getPrepareByte() {return (byte)1;}

  /**
   * get the maximum prepare hash command data input length for a given hash algorithm block length.
   * The value must be an exact multiple of the Input Block Length for the hash algorithm being used
   * and be below the maximum effective data length supported by the card.
   * @param hashAlgorithm algorithm for which the length should be returned
   * @return int
   */
  public int getPrepareHashInputLength(String hashAlgorithm)
  {
    // input block length is 64 for all hash algorithms supported by MFC familiy
    int maxLen=getEffectiveDataLength();
    return (maxLen - (maxLen % 64));
  }

  /**
   * instruction byte for read key info
   */
  public byte getReadKeyInfoByte() {
    return (byte) 0xF4;
  }

  /**
   * instruction byte for validate key
   * @return byte
   */
  public byte getValKeyByte() {return (byte)0x42;}

  /**
   * instruction byte for verify signature
   */
  public byte getVerifySignatureByte() {
    return (byte) 0x54;
  }
}
