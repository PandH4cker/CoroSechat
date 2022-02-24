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

import opencard.core.terminal.ResponseAPDU;

/** Specifics to represent a ResponseAPDU in an MFC card
  *
  * @author   Michael Baentsch (mib@zurich.ibm.com)
  * @author   Thomas Schaeck (scheck@de.ibm.com)
  * @version  $Id: MFCResponseAPDUCodes.java,v 1.7 1998/08/07 07:56:00 cvsusers Exp $
  *
  * @see opencard.core.terminal.ResponseAPDU
  */
public interface MFCResponseAPDUCodes {
  /** Response codes.<p>
    */
  /** Everything is OK */
  public final static int RAPDU_OK                                  = 0x9000;
  /** The response data length is bigger than Le. The length required is given in the LSB of the return code */
  public final static int RAPDU_LE_INCORRECT                        = 0x6100;
  /** Lc is incorrect. The maximum allowable value for Lc or the incorrect Lc submitted in the previos command is given in the LSB */
  public final static int RAPDU_LC_INCORRECT                        = 0x6700;
  /** The key status was not fulfilled. Key is incomplete, has been invalidated or is not verified. */
  public final static int RAPDU_KEY_STATUS_NOT_FULFILLED            = 0x6984;
  /** The condition of use is not satisfied. The key is a hash key */
  public final static int RAPDU_KEY_CONDITIONS_OF_USE_NOT_SATISFIED = 0x6985;
  /** Key usage error: the key used is not allowed */
  public final static int RAPDU_KEY_USAGE_ERROR                     = 0x69C1;
  /** This function is not supported. The PKA algorithm or padding algorithm is not supported by the card. */
  public final static int RAPDU_FUNCTION_NOT_SUPPORTED              = 0x6A81;
  /** Parameter P1/P2 not correct: RFU bits are not 0 */
  public final static int RAPDU_P1P2_NOT_CORRECT                    = 0x6A86;
  /** Lc is inconsisten with P1/P2 */
  public final static int RAPDU_LC_INCONSISTENT_WITH_P1P2           = 0x6A87;
  /** Reference data was not found: A key was not found with the ID provided in P2 */
  public final static int RAPDU_REFERENCE_DATA_NOT_FOUND            = 0x6A88;
  /** Parameter P1/P2 not correct: P1 != 0x80 or 0x00 or P1 == 0x00 P2 != 0x00 */
  public final static int RAPDU_P1P2_NOT_CORRECT_PKA                = 0x6B00;
  public final static int RAPDU_OFFSET_OUT_OF_RANGE                 = 0x6B00;
  /** Command for given instruction byte is not known or invalid for the specified class. LSB holds unknown instruction byte */
  public final static int RAPDU_UNKNOWN_COMMAND                     = 0x6D00;
  /** Class not known or incorrect. LSB holds unknown class byte. */
  public final static int RAPDU_UNKNOWN_CLASS                       = 0x6E00;
  /** Command terminated abnormally: Trying to delete DF not empty, Specified key algorithm not available, key length and
      algorithm do not match or EEPROM write didn't succeed after 3 retries */
  public final static int RAPDU_ABNORMAL_TERMINATION                = 0x6F00;
  public final static int RAPDU_DF_NOT_EMPTY                        = 0x6F00;
  public final static int RAPDU_COPROCESSOR_NO_PRIMES               = 0x6F00;
  /** Key parity error, Key data is inconsistent */
  public final static int RAPDU_KEY_DATA_INCONSISTENT               = 0x6F01;
  /** Increase/Decrease would exceed maximum/minimum value */
  public final static int RAPDU_KEY_INCDEC_EXCEEDS_MINMAX           = 0x6F02;
  /** Wrong verification pattern */
  public final static int RAPDU_VERIFICATION_ERROR                  = 0x6F05;
  /** Incompatible ENC access conditions for read/update */
  public final static int RAPDU_INCOMPATIBLE_ENC_READ_UPDATE        = 0x6F07;
  /** The current DF or MF is without ASC authority and the DF to be created has ACS authority */
  public final static int RAPDU_ASC_AUTHORITY_MISMATCH              = 0x6F08;
  /** The card state is Personalized B and must be Personalized A */
  public final static int RAPDU_ALREADY_PERSONALIZED_B              = 0x6F09;
  /** Record size out of range */
  public final static int RAPDU_RECORD_SIZE_OUT_OF_RANGE            = 0x6F0A;
  /** Incorrect CHV presentation definition in relevant EF_CHV */
  public final static int RAPDU_INVALID_CHV_PRES_DEFINITION         = 0x6F0B;
  /** SW Stack overflow - wrong or corrupted EEPROM image */
  public final static int RAPDU_SW_STACK_OVERFLOW                   = 0x6F48;
  /** SW invalid EE pointer - wrong or corrupted EEPROM image */
  public final static int RAPDU_SW_INVALIE_EE_POINTER               = 0x6F50;
  /** SW no current DF - wrong or corrupted EEPROM image */
  public final static int RAPDU_SW_NO_CURRENT_DF                    = 0x6F51;
  /** EEPROM programming failure */
  public final static int RAPDU_EEPROM_PROG_FAILURE                 = 0x6FE2;
  /** Memory size not sufficient */
  public final static int RAPDU_MEMORY_EXCEEDED                     = 0x9210;
  /** File ID already exists in this directory */
  public final static int RAPDU_FILE_ALREADY_EXISTS                 = 0x9220;
  /** Update not successful due to memory problem. May also be caused by non-updating commands by internal updates in counters */
  public final static int RAPDU_UPDATE_NOT_SUCCESSFUL               = 0x9240;
  /** No EF selected */
  public final static int RAPDU_NO_EF_SELECTED                      = 0x9400;
  /** Out of range */
  public final static int RAPDU_RECORD_OUT_OF_RANGE                 = 0x9402;
  public final static int RAPDU_OUT_OF_RANGE                        = 0x9402;
  /** File ID, Record or search pattern not found */
  public final static int RAPDU_FILE_NOT_FOUND                      = 0x9404;
  /** Presented command doesn't match current file type */
  public final static int RAPDU_COMMAND_FILETYPE_MISMATCH           = 0x9408;
  /** No card holder verification defined */
  public final static int RAPDU_NO_CHV_DEFINED                      = 0x9802;
  /** Access condition not satisfied */
  public final static int RAPDU_NOT_AUTHENTICATED                   = 0x9804;
  /** CHV change not possible */
  public final static int RAPDU_CHV_CHANGE_NOT_POSSIBLE             = 0x9808;
  /** Data file not invalidated or already inhabilitated */
  public final static int RAPDU_UNNECESSARY_VALIDATION_CMD          = 0x9810;
  /** No previous ASK RANDOM/GIVE RANDOM */
  public final static int RAPDU_NO_ASK_GIVE_RANDOM                  = 0x9835;
  /** CHV is blocked or key error counter is 0 */
  public final static int RAPDU_BLOCKED                             = 0x9840;
}
// $Log: MFCResponseAPDUCodes.java,v $
// Revision 1.7  1998/08/07 07:56:00  cvsusers
// add some 4.21 response SW codes (pbendel)
//
// Revision 1.6  1998/03/19 21:46:01  schaeck
// Added more codes
//
// Revision 1.5  1998/02/26 11:04:15  rhe
// *** empty log message ***
//
// Revision 1.4  1998/02/23 15:38:09  rhe
// + fixed some import problems
//
// Revision 1.3  1998/02/18 15:39:18  rhe
// + moved stuff to new place
//
// Revision 1.2  1998/02/17 15:17:48  rhe
// + first cut at the stuff!
//
// Revision 1.1  1998/02/16 14:33:49  rhe
// + adding files for iso.fs CS
