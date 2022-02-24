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

/****************************************************************************
* IBClient is the client applet for the Internet Broker Demo
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @author Mike Wendler   (mwendler@de.ibm.com)
* @version  $Id: IBClient.java,v 1.2 1998/09/02 09:11:29 cvsusers Exp $
*
* @see demos.stockbroker.IBServerF
* @see demos.stockbroker.IBEveF
* @see demos.stockbroker.IBProt
****************************************************************************/

public class IBClient extends Applet
{
  // Images loaded by this applet on behalf of the frames
  private Image redBallImage, greenBallImage, sigCardImage;
  private Image nyseImage, headerImage, CISImage;

  // Frames which will be used by this applet
  private IBServerF      server;       // Internet Broker Server Frame
  private IBEveF         eve;          // Internet Broker Interceptor Frame
  private IBRequestCardF requestCard;  // Internet Broker RequestCard Frame
  private IBIssuerF      issuer;       // Internet Broker Issuer Frame
  private IBClientF      client;       // Internet Broker Client Frame

  public void init()
  {
    // Load images.
    if ((CISImage = getImage(getCodeBase(), "karte2.gif")) == null) {
      System.out.println("karte2.gif not found\n");
    }
    if ((headerImage = getImage(getCodeBase(), "header3.gif")) == null) {
      System.out.println("header3.gif not found\n");
    }
    if ((sigCardImage = getImage(getCodeBase(), "signcrd.gif")) == null) {
      System.out.println("signcrd.jpg not found\n");
    }
    if ((nyseImage = getImage(getCodeBase(), "nyse.gif")) == null) {
      System.out.println("nyse.gif not found\n");
    }
    if ((redBallImage = getImage(getCodeBase(), "redBall.gif")) == null) {
      System.out.println("red-ball.gif not found\n");
    }
    if ((greenBallImage = getImage(getCodeBase(), "greenBall.gif")) == null) {
      System.out.println("green-ball.gif not found\n");
    }

    // Create the other windows
    client = new IBClientF("Stock Broker Demo 1.0", eve, nyseImage, headerImage, sigCardImage);
    server = new IBServerF("Internet Brokerage Server", client, redBallImage, greenBallImage);
    eve    = new IBEveF("United Internet Criminals Inc.", server);
    client.setEve(eve);
    issuer = new IBIssuerF("Signature Card Issuer", client.getSignatureCard(), sigCardImage, CISImage);
  }

  /**
   * Clean up applet.
   */
  public void destroy () {
    client.close ();
  }

  public String getAppletInfo()
  {
    return "Internet Stock Broker Demo 1.0, Thomas Schaeck (c)IBM 1997";
  }
}
