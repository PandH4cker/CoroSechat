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

import java.applet.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/****************************************************************************
* IBPersF is the applet for "personalization" of demo cards.
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @version  $Id: IBPersF.java,v 1.2 1998/09/02 09:11:29 cvsusers Exp $
*
* @see IBServerF
* @see IBClientF
* @see IBProt
*****************************************************************************/
public class IBPersF extends Dialog
{
  // GUI elements -----------------------------------------------------------
  private Label      nameLabel, emailLabel;
  private TextField  nameField, emailField;
  private Button     persButton;

  private IBIssuerF   issuer;

  /**************************************************************************
  * Create an instance of IBPersF.
  *
  * @param title  - the title of the frame
  * @param server - the object to which messages are sent
  **************************************************************************/
  public IBPersF(String title, IBIssuerF issuer)
  {
    super(issuer, "Please enter your personal data", true);
    this.issuer = issuer;

    setFont(new Font("Helvetica", Font.BOLD, 16));

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    setLayout(gridBag);

    c.weightx = 1.0; c.weighty = 1.0;

    c.fill = GridBagConstraints.HORIZONTAL;

    Label nameLabel = new Label("Name:", Label.LEFT);
    c.gridx = 0; c.gridy = 0;
    gridBag.setConstraints(nameLabel, c);
    add(nameLabel);

    Label emailLabel = new Label("E-mail:", Label.LEFT);
    c.gridx = 0; c.gridy = 1;
    gridBag.setConstraints(emailLabel, c);
    add(emailLabel);

    c.weightx = 1.0;
    nameField = new TextField("", 32);
    nameField.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { nameFieldChange(); }
    });
    c.gridx = 1; c.gridy = 0;
    c.gridwidth = 1;
    gridBag.setConstraints(nameField, c);
    add(nameField);

    c.weightx = 1.0;
    emailField = new TextField("", 32);
    emailField.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { emailFieldChange(); }
    });
    c.gridx = 1; c.gridy = 1;
    c.gridwidth = 1;
    gridBag.setConstraints(emailField, c);
    add(emailField);

    c.weightx = 0.8;
    persButton = new Button("Personalize");
    persButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { persButtonPressed(); }
    });
    c.gridx = 2; c.gridy = 0;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.gridwidth  = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gridBag.setConstraints(persButton, c);
    add(persButton);

    setLocation(new Point(0, 260));
    pack();
    show();
  }

  // -------------------------------------------------------- End of GUI part

  // Application logic ------------------------------------------------------

  private String     name;
  private String     email;


  void nameFieldChange()
  {
    name = nameField.getText();
  }

  void emailFieldChange()
  {
    email = emailField.getText();
  }

  void persButtonPressed()
  {
    name  = nameField.getText();
    email = emailField.getText();
    issuer.setCardHolderData(name + "\n" + email);
    dispose();
  }

  // ----------------------------------------------------- End of application logic
}
