/*
 *     (C) COPYRIGHT INTERNATIONAL BUSINESS MACHINES CORPORATION 1997 - 1999
 *                       ALL RIGHTS RESERVED
 *              IBM Deutschland Entwicklung GmbH, Boeblingen
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
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IBM
 * DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS SOFTWARE WILL MEET
 * THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL BE UNINTERRUPTED
 * OR ERROR-FREE. IN NO EVENT, UNLESS REQUIRED BY APPLICABLE LAW, SHALL IBM BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. ALSO, IBM IS UNDER NO OBLIGATION TO MAINTAIN,
 * CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */

package demos.stockbroker;

import java.awt.Frame;
import java.awt.Dimension;

import opencard.core.service.CHVDialog;
import com.ibm.opencard.handler.IDDialog;


/**
 * An example for an application-provided CHV dialog.
 *
 * @author Thomas Schaeck (schaeck@de.ibm.com)
 * @version $Id: SBCHVDialog.java,v 1.3 1998/09/04 09:44:04 cvsusers Exp $
 */
public class SBCHVDialog implements CHVDialog
{
  /**
   * Query for a password.
   *
   * @param chvNumber   which password to query for
   *
   * @return    a string holding the password entered by the user,
   *            or <tt>null</tt> if none has been entered
   */
  public String getCHV(int chvNumber)
  {
    Frame  frame  = new Frame("");
    frame.setVisible(false);

    // The actual dialog used here is defined in the IBM reference services.
    IDDialog dialog = new IDDialog(frame,
                                   "Stock Broker CHV Verification",
                                   "Please enter your password #",
                                   chvNumber);

    Dimension dim = frame.getToolkit().getScreenSize();
    dialog.setSize(dim.width/3, dim.height/3);
    dialog.setLocation(dim.width/2-dim.width/6, dim.height/2-dim.height/6);

    dialog.show();               // popup dialog for PIN entry, wait for CHV
    frame.dispose();             // kill the frame

    String pin = dialog.pin();

    // You wouldn't do that in a real application!
    System.out.println("Pin = " + pin);

    if ((pin != null) && (pin.length() == 0))   // empty password
      pin = null;                               // not allowed

    return pin;
  }
}
