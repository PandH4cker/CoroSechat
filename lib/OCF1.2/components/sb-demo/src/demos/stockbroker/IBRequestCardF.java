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

import java.awt.*;
import java.awt.event.*;

/****************************************************************************
* IBRequestCardF is used for requesting the card.
*
* @author Thomas Schaeck
* @version  $Id: IBRequestCardF.java,v 1.2 1998/09/02 09:11:29 cvsusers Exp $
*
* @see IBIssuerF
* @see IBServerF
* @see IBClientF
* @see IBEveF
* @see IBProt
****************************************************************************/
public class IBRequestCardF extends Dialog
{
  // GUI elements -----------------------------------------------------------
  private Label  requestLabel;
  private Button okButton, cancelButton;
  private Canvas canvas;

  public IBRequestCardF(Frame f, Image image, String labelText)
  {
    super(f, "Please insert card", true);

    setFont(new Font("Helvetica", Font.BOLD, 16));

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    setLayout(gridBag);

    c.weightx = 1.0; c.weighty = 1.0;

    c.fill = GridBagConstraints.HORIZONTAL;

    /*
    Label requestLabel = new Label(labelText, Label.CENTER);
    c.gridx = 0; c.gridy = 0;
    gridBag.setConstraints(requestLabel, c);
    add(requestLabel);
    */

    canvas = new ImageCanvas(image, this, 250, 180, true);
    c.gridx = 0; c.gridy = 0;
    c.gridwidth = 2;
    gridBag.setConstraints(canvas, c);
    add(canvas);

    okButton = new Button("   OK   ");
    okButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { okButtonPressed(); }
    });
    c.gridx = 0; c.gridy = 2;
    c.gridwidth = 1;
    gridBag.setConstraints(okButton, c);
    add(okButton);

    cancelButton = new Button("Cancel");
    cancelButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { cancelButtonPressed(); }
    });
    c.gridx = 1; c.gridy = 2;
    c.gridwidth = 1;
    gridBag.setConstraints(cancelButton, c);
    add(cancelButton);

    Dimension dim=f.getToolkit().getScreenSize();
    setLocation(dim.width/2-dim.width/6, dim.height/2-dim.height/6);
    setSize(280,230);
  }

  public boolean requestCard()
  {
    show();
    return result;
  }

  // -------------------------------------------------------- End of GUI part

  // Application logic ------------------------------------------------------

  private boolean result = true;

  void okButtonPressed()
  {
    result = true;
    dispose();
  }

  void cancelButtonPressed()
  {
    result = false;
    dispose();
  }

  // ----------------------------------------------------- End of application logic
}
