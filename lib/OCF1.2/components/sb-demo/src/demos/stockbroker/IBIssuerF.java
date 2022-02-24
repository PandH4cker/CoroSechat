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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/****************************************************************************
* IBIssuerF is the issuer for the Internet Broker Demo. It's used for
* initializing MFC 4.0 cards.
*
* @author Thomas Schaeck
* @version  $Id: IBIssuerF.java,v 1.2 1998/09/02 09:11:29 cvsusers Exp $
*
* @see IBClientF
* @see IBEveF
* @see IBServerF
* @see IBProt
*****************************************************************************/
public class IBIssuerF extends Frame
{
  Button issueButton;
  SignatureCard signatureCard;
  Image sigCardImage;
  ImageCanvas canvas = null;

  String cardHolderData = null;

  void setCardHolderData(String s)
  {
    cardHolderData = s;
  }

  /***************************************************************************
  * Create an instance if IBIssuerF
  *
  * @param title        - the title of the issuer window
  * @param t            - the card object to be initialized
  * @param sigCardImage - the image to be shown when no card is available
  ***************************************************************************/
  // ! passing the image here is not elegant, this will be chaged when
  // ! I have the time...
  public IBIssuerF(String title, SignatureCard signatureCard,
                   Image sigCardImage, Image CISImage)
  {
    super(title);
    setFont(new Font("Helvetica", Font.BOLD, 16));

    this.signatureCard = signatureCard;
    this.sigCardImage = sigCardImage;

    // build user interface
    GridBagLayout      gridBag = new GridBagLayout();
    GridBagConstraints c       = new GridBagConstraints();

    setLayout(gridBag);

    // Add the internet broker picture
    canvas = new ImageCanvas(CISImage, this, 250, 200, true);
    c.gridx = 0; c.gridy = 0;
    c.gridwidth = 4; c.gridheight = 1;
    gridBag.setConstraints(canvas, c);
    add(canvas);

    issueButton = new Button("Issue Card");
    issueButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e ) { issueButtonPressed(); }
    });
    c.gridx = 0; c.gridy = 1;
    gridBag.setConstraints(issueButton, c);
    add(issueButton);

    setLocation(new Point(530, 0));
    setSize(270, 255);
    show();
  }

  void issueButtonPressed()
  {
    boolean retry = false;
    IBPersF persDialog = new IBPersF("Please enter personailzation data:", IBIssuerF.this);
    do
    {
      retry = false;
      IBIssuerF.this.signatureCard.initialize();
      // IBIssuerF.this.signatureCard.importKeys();
      if (! IBIssuerF.this.signatureCard.writeCardHolderData(cardHolderData))
      {
          Frame f = new Frame();
          IBRequestCardF r = new IBRequestCardF(f, sigCardImage,
                                             "Please insert your Signature Card");
          retry = r.requestCard();
          f.dispose();
      }
    } while (retry == true);
  }
}
