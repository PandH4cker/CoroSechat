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


import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CHVDialog;
import opencard.core.service.CardServiceInabilityException;

import com.ibm.opencard.service.MFCCardChannel;


/**
 * The CHV provider is responsible to query the user for a password.
 * It is one of the helpers of <tt>MFCCardAccessor</tt>. A dialog
 * may be set to use for the query.
 * <br>
 * Card holder verification requires exchanging APDUs with the smartcard.
 * Since the command for this purpose may be card-specific, this base
 * class must be abstract.
 *
 * @version $Id: MFCCHVProvider.java,v 1.4 1998/08/11 07:43:28 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCardAccessor
 */
public abstract class MFCCHVProvider
{
  /**
   * This dialog will be invoked if CHV is required.
   * It may be provided by an application via the card service.
   */
  private CHVDialog CHV_dialog = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new CHV provider.
   * No arguments, since the attributes in this class are
   * zero-initialized.
   */
  protected MFCCHVProvider()
  {
    // nothing to be done
  }


  // access ///////////////////////////////////////////////////////////////////


  /**
   * Set an application-defined dialog for CHV input.
   * This method is invoked via the <tt>MFCCardAccessor</tt> in this
   * package, therefore the default visibility.
   *
   * @param dialog   the dialog to use for password query
   */
  final void setCHVDialog(CHVDialog dialog)
  {
    CHV_dialog = dialog;
  }


  /**
   * Get the application-defined dialog for CHV input.
   *
   * @return   the CHV dialog to use,
   *           or <tt>null</tt> if none has been set
   */
  final protected CHVDialog getCHVDialog()
  {
    return CHV_dialog;
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Perform a card holder verification.
   * This method is invoked by the <tt>MFCCardAccessor</tt> in this
   * package, therefore the default visibility.
   *
   * @param channel   the channel to the smartcard
   * @param numCHV    the number of the CHV, currently 1 or 2
   *
   * @return   <tt>true</tt> if CHV succeeded,
   *           <tt>false</tt> if it failed
   *
   * @exception CardServiceInabilityException
   *              The CHV is blocked.
   * @exception CardTerminalException
   *              The terminal encountered a problem.
   */
  abstract boolean performCHV(MFCCardChannel channel, int numCHV)
       throws CardServiceInabilityException,
              CardTerminalException;
  
} // class MFCCHVProvider
