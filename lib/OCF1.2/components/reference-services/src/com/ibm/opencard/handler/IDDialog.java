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

package com.ibm.opencard.handler;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/******************************************************************************
* IDDialog is used by IdentificationHandler to request the PIN from the user.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: IDDialog.java,v 1.2 1998/03/25 19:49:21 schaeck Exp $
*******************************************************************************/
public class IDDialog extends Dialog implements ActionListener
{
  protected Button         okButton;
  protected Button         cancelButton;
  protected Label          messageLabel;
  protected boolean        finished;
  protected Object         objectToNotify;
  protected TextField      textField;
  protected String         pin;

  public IDDialog(Frame parent, String title, String prompt, int chvNumber)
  {
    super(parent, title, true);
    GridBagLayout gridBag = new GridBagLayout();
    setLayout(gridBag);
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    c.weightx = 1.0;
    c.weighty = 1.0;
    messageLabel = new Label(prompt + chvNumber);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    gridBag.setConstraints(messageLabel, c);
    add(messageLabel);
    c.gridx = 0;
    c.gridy = 1;
    textField = new TextField("", 10);
    textField.addActionListener(this);
    textField.setEchoChar('*');
    gridBag.setConstraints(textField, c);
    add(textField);
    this.objectToNotify = objectToNotify;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    okButton = new Button(" OK ");
    okButton.addActionListener(this);
    gridBag.setConstraints(okButton, c);
    add(okButton);
    c.gridx = 1;
    c.gridy = 2;
    cancelButton = new Button("Cancel");
    cancelButton.addActionListener(this);
    gridBag.setConstraints(cancelButton, c);
    add(cancelButton);
    this.pack();
  }

  /** Handle action events.<p>
    * Close the dialog on buttons and RETURN in the text field.
    *
    * @param ae
    *        The <tt>ActionEvent</tt> to be handeled.
    */
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();

    if (source == okButton || source == textField) {
      pin = textField.getText();
    } else if (source == cancelButton) {
      pin = null;
    }
    setVisible(false);
  }

  public String pin() { return pin; }
}
// $Log: IDDialog.java,v $
// Revision 1.2  1998/03/25 19:49:21  schaeck
// adapted to JDK1.1 event model
//
// Revision 1.1  1998/03/19 22:44:39  schaeck
// Initial version
//
// Revision 1.5  1998/03/19 21:51:52  schaeck
// *.java
//

