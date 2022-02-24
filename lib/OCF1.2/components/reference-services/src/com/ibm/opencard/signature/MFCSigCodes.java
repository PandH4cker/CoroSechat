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

import com.ibm.opencard.service.MFCCodes;
import opencard.core.service.CardServiceInabilityException;

/**
 * Provides parameterization of signature related APDUs for MFC smartcards.
 * The majority of the methods is used to get the CLAss and INStruction
 * bytes for specific commands. Some other methods specify minor details
 * that are important in special cases, for example of a command does
 * neither include command data nor expect response data. Additionally,
 * there are methods that check the status word of response APDUs.
 *
 * @version $Id: MFCSigCodes.java,v 1.3 1998/08/19 12:39:50 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public interface MFCSigCodes extends MFCCodes {
  /**
   * instruction byte for calculate hash
   */
  public byte getCalculateHashByte();

  /**
   * Get the maximum effective data length for commands in key group.
   * @return int
   */
  int getEffectiveDataLength();

  /**
   * P1 for finish stage of commands where the input data is provided to the card in several blocks
   */
  public byte getFinishByte();

  /**
   * instruction byte for generate signature
   */
  public byte getGenerateSignatureByte();

  /**
   * instruction byte for generate key pari
   * @return byte
   */
  byte getGenKeyByte();

  /**
   * instruction byte for import key
   * @return byte
   */
  byte getImportKeyByte();

  /**
   * The maximum input block length of the generate signature command for all key strengths and
   * padding algorithms supported by the MFC 4.0 card.
   * @return int
   */
  int getMaxInputBlockLength();

  /**
   * P1 for prepare stage of commands where the input data is provided to the card in several blocks
   */
  public byte getPrepareByte();

  /**
   * Get the maximum prepare hash command data input length for a given hash algorithm block length.
   * The value must be an exact multiple of the Input Block Length for the hash algorithm being used
   * and be below the maximum effective data length supported by the card.
   * @param hashAlgorithm hash algorithm for which input length is to be calculated
   * @return int
   */
  int getPrepareHashInputLength(String hashAlgorithm);

  /**
   * instruction byte for read key info
   * @return byte
   */
  byte getReadKeyInfoByte();

  /**
   * instruction byte for validate key
   * @return byte
   */
  byte getValKeyByte();

  /**
   * instruction byte for verify signature
   */
  byte getVerifySignatureByte();
}