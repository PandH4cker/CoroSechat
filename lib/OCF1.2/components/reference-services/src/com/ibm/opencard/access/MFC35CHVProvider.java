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

import opencard.core.service.CHVDialog;
import opencard.core.service.CardServiceInabilityException;

import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalIOControl;
import opencard.core.terminal.CardTerminalException;

import com.ibm.opencard.service.MFCCodes;
import com.ibm.opencard.service.MFC35Codes;
import com.ibm.opencard.service.MFCCardChannel;
import com.ibm.opencard.service.MFCResponseAPDUCodes;


/**
 * A CHV provider for MFC 3.5 and some other smartcards.
 */
public class MFC35CHVProvider extends MFCCHVProvider
{
  /** The CLAss and INStruction codes. */
  private MFCCodes mfc_codes = null;

  /** The APDU for performing CHV. */
  private CommandAPDU performCHVCommand = new CommandAPDU(14);


  /**
   * Instantiates a new CHV provider for MFC 3.5 smartcards.
   * It uses the command codes for MFC 3.5 by default.
   */
  public MFC35CHVProvider()
  {
    super();
    mfc_codes = new MFC35Codes();
  }

  /**
   * Instantiates a new CHV provider for MFC smartcards.
   * It uses the command codes passed as argument. This
   * way, it may be possible to re-use this class for
   * MFC cards other than 3.5.
   *
   * @param codes   the command codes to use
   */
  public MFC35CHVProvider(MFCCodes codes)
  {
    super();
    mfc_codes = codes;
  }


  // service //////////////////////////////////////////////////////////////////

  /**
   * Perform a card holder verification.
   * This includes a user query for the password and sending an APDU
   * containing that password to the smartcard. This message is called
   * by the <tt>MFCCardAccessor</tt> which resides in the same package,
   * therefore the default visibility is sufficient.
   * <br>
   * Encrypted passwords are not supported. If this is required,
   * the application has to take care about it by itself.
   *
   * @param channel   the channel to the smartcard for communication
   * @param numCHV    the number of the CHV, must be 1 or 2
   *
   * @return   <tt>true</tt> if CHV succeeded,
   *           <tt>false</tt> if it failed and can be retried
   *
   * @exception CardServiceInabilityException
   *              The CHV is blocked.
   * @exception CardTerminalException
   *              The terminal encountered a problem.
   */
  boolean performCHV(MFCCardChannel channel, int numCHV)
       throws CardServiceInabilityException, CardTerminalException
  {
    /*
     * Use the card terminal to query for the password.
     * This part of the method has been adapted from
     * MFC35FileSystemCSImpl.internalPerformCHV(), CVS 1.20
     * The support for an optional cryptogram has been removed.
     */
    byte []     password = new byte [8];
    performCHVCommand.setLength(0);
    performCHVCommand.append(mfc_codes.getISOClassByte());
    performCHVCommand.append(mfc_codes.getINS(MFCCodes.OP_VERIFY_CHV));
    performCHVCommand.append((byte)0x00);    // Parameter 1
    performCHVCommand.append((byte)numCHV);  // Parameter 2
    performCHVCommand.append((byte)0x08);    // Lc
    performCHVCommand.append(password); // needed for length of APDU

    CardTerminalIOControl ioctrl =
      new CardTerminalIOControl(8, 3000, null, null);
    CHVControl chvctrl = new CHVControl("Enter your password", numCHV,
                                        CHVEncoder.STRING_ENCODING, 0,
                                        ioctrl);

    // We've got all we need to let the terminal know
    // that it has to insert a password into the APDU.
    // The response gets checked for errors.
   ResponseAPDU response = channel.sendVerifiedAPDU(performCHVCommand,
                                                    chvctrl,
                                                    getCHVDialog());
   //@@@ change response checking
   if (response.sw() == MFCResponseAPDUCodes.RAPDU_BLOCKED)
     throw new CardServiceInabilityException("CHV " + numCHV + " blocked");

   return (response.sw() == MFCResponseAPDUCodes.RAPDU_OK);

  } // performCHV


} // MFC35CHVProvider
