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
import java.security.*;
import java.math.BigInteger;
import java.lang.reflect.*;
import java.security.Key;

import opencard.opt.util.Tag;
import opencard.opt.util.TLV;
// due to export restrictions, the opencard class is not referenced here:
//import opencard.opt.security.RSAPublicKey;

/****************************************************************************
* IBServerF is the server applet for the Internet Broker Demo
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @version  $Id: IBServerF.java,v 1.6 1998/09/03 09:21:38 cvsusers Exp $
*
* @see IBIssuerF
* @see IBClientF
* @see IBEveF
* @see IBProt
*****************************************************************************/
public class IBServerF extends Frame
{
  // Actually, this should be RSAPublicKey. To minimize references to export
  // restricted classes in OpenCard, the concrete class is used here instead
  private RSAPublicKey1 publicKey;

  // This one is a special case, since there is an export license for the
  // SB demo, due to which this class can be exported in object-only format.
  private RSASignature rsaSignature;

  // GUI elements
  private TextArea    status;
  private Label       label;
  private ImageCanvas imageCanvas;

  private IBClientF client = null;

  // Sequence number used to detect replay attacks
  private int lastSequenceNumber = 0;

  // Initial chaining value for DES
  private byte[]   iv =
  {
    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
  };

  /****************************************************************************
  * Create an instance of IBServerF.
  *
  * @param title - the title of the server frame
  ****************************************************************************/
  public IBServerF(String title, IBClientF client,
                   Image redBallImage, Image greenBallImage)
  {
    super(title);
    this.client = client;
    setFont(new Font("Helvetica", Font.BOLD, 16));

    try
    {
      // Get a signature object that uses SHA-1 for hashing.
      rsaSignature = (RSASignature) RSASignature.getInstance("RSA");
      rsaSignature.setParameter("MessageDigest", MessageDigest.getInstance("SHA"));

      // Create the public key to be used for signature verification.
      publicKey = new RSAPublicKey1();
    }
    // ! Quick and dirty for testing
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(0);
    }

    // Build the user interface
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    setLayout(gridBag);

    Image[] images = {greenBallImage.getScaledInstance(40, 40, 0),
                      redBallImage.getScaledInstance(40, 40, 0)};
    imageCanvas = new ImageCanvas(images, this, 40, 40, true);
    c.gridx = 0; c.gridy = 0;
    c.gridwidth = 1; c.gridheight = 1;
    gridBag.setConstraints(imageCanvas, c);
    add(imageCanvas);

    label = new Label("Last Order was O.K.", Label.LEFT);
    c.gridx = 1;     c.gridy = 0;
    c.gridwidth = 5; c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridBag.setConstraints(label, c);
    add(label);

    status = new TextArea(5, 60);
    status.setEditable(false);
    c.anchor = GridBagConstraints.CENTER;       // reset to the default
    c.fill = GridBagConstraints.BOTH;           // make this big
    c.weightx = 1.0; c.weighty = 1.0;
    c.gridx = 0;     c.gridy = 1;
    c.gridwidth = 6; c.gridheight = 1;
    gridBag.setConstraints(status, c);
    add(status);

    setLocation(new Point(0,0));
    setSize(530, 255);
    show();
  }

  public void printInfo(String info)
  {
    status.append(info);
  }

  /**************************************************************************
  * Process a request coming in from the client or from eve.
  *
  * @param senderName - The name of the sender of this message
  * @param data       - The message that has been received
  **************************************************************************/
  public void processRequestFrom(String senderName, byte[] data)
  {
    /*
    int i = 0;
    printInfo("------------------------------------------------------------\n");
    printInfo("Checking message format ...\n");

    TLV message = new TLV(data);
    byte[] orderData = message.valueAsByteArray();

    if (message.tag().equals(IBProt.ENCRYPTED_MESSAGE))
    {
      // The message is enciphered
      try
      {
        DESKey desKey = new DESKey0();
        DESCipher desCipher = DESCipher.getInstance("DES/CBC");
        desCipher.setInitializationVector(iv);
        desCipher.engineInitDecrypt(desKey);
        desCipher.engineUpdate(orderData, 0, orderData.length, orderData, 0);
        System.out.println("Data decipered by server:");
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        System.exit(0);
      }
    }
    */

    int i = 0;
    printInfo("------------------------------------------------------------\n");
    printInfo("Checking message format ...\n");

    TLV message = new TLV(data);
    byte[] orderData = message.valueAsByteArray();

    if (message.tag().equals(IBProt.ENCRYPTED_MESSAGE))
    {
      // The message is enciphered.
      decrypt(orderData);
    }

    TLV signedMessage = new TLV(orderData);

    if (!signedMessage.tag().equals(IBProt.SIGNED_MESSAGE))
    {
      printInfo("SIGNED_MESSAGE tag not found\n");
      return;
    }
    TLV stockOrder = signedMessage.findTag(IBProt.STOCK_ORDER, null);
    if (stockOrder == null)
    {
      printInfo("STOCK_ORDER tag not found\n");
      return;
    }
    byte[] order = stockOrder.toBinary();

    TLV signature  = signedMessage.findTag(IBProt.SIGNATURE, null);
    if (signature == null)
    {
      printInfo("SIGNATURE tag not found\n");
      return;
    }

    int    number     = (stockOrder.findTag(IBProt.NUMBER, null)).valueAsNumber();
    String price      = new String((stockOrder.findTag(IBProt.PRICE, null)).valueAsByteArray());
    String company    = new String((stockOrder.findTag(IBProt.COMPANY, null)).valueAsByteArray());
    String cardHolder = new String((stockOrder.findTag(IBProt.CARD_HOLDER_DATA, null)).valueAsByteArray());
    int sequenceNumber = (stockOrder.findTag(IBProt.SEQUENCE_NUMBER, null)).valueAsNumber();

    printInfo("Order #" + sequenceNumber + " sent by \n" +
              cardHolder + "\n" +
              "Buy "+number+" "+company+" stocks for $" +
              Double.toString(Double.valueOf(price).doubleValue()*number)+"\n");

    byte[] sig = signature.valueAsByteArray();

    printInfo("Signature: "+ (new BigInteger(1, sig)) + "\n");


    printInfo("Checking Digital Signature...\n");
    try
    {
      rsaSignature.initVerify(publicKey);
      rsaSignature.update(order, 0, order.length);
      if (rsaSignature.verify(sig))
      {
        if (lastSequenceNumber >= sequenceNumber)
        {
          label.setText("Last order was replayed");
          label.setForeground(Color.red);
          imageCanvas.setActualImage(1);
          printInfo("Last order was replayed\n");
          client.receiveResponse("Replay attack\nWe will buy no stocks.\n");
          repaint();
          return;
        }
        else if (lastSequenceNumber == sequenceNumber - 2) {
          String warning = "Warning: Order " + (sequenceNumber - 1) + " was lost\n";
          printInfo(warning);
          client.receiveResponse(warning);
        } else if (lastSequenceNumber < sequenceNumber - 2) {
          String warning = "Warning: Orders " + (lastSequenceNumber + 1) + " to " + (sequenceNumber - 1) + " were lost\n";
          printInfo(warning);
          client.receiveResponse(warning);
        }
        lastSequenceNumber = sequenceNumber;
        label.setText("Last order was O.K.");
        label.setForeground(Color.black);
        printInfo("Last order was O.K.\n");
        imageCanvas.setActualImage(0);
        client.receiveResponse("Signature is correct.\nBuying stocks...\n");
      }
      else
      {
        label.setText("Last order was modified");
        label.setForeground(Color.red);
        printInfo("Last order was modified\n");
        imageCanvas.setActualImage(1);
        client.receiveResponse("Signature is not correct !!!\n"
                              +"No stocks will be bought.\n");
      }
    }

    // ! Quick and dirty for prototype
    catch(Exception e)
    {
      e.printStackTrace();
      System.exit(0);
    }
    printInfo("Sending response to client");
    for (i=0; i<10; i++)
    {
      try { Thread.sleep(100); } catch (InterruptedException e) { }
      printInfo(".");
    }
    printInfo("\n");

    repaint();
  }

  private void decrypt(byte[] data)
  {
    byte[]   iv =         { (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00 };


    try
    {
      // Because of US export restrictions on cryptography, we can not
      // be sure that the class DESCipher is available.
      // Therefore, they may not be directly used if the program shall also compile
      // without the class. The following code using the Java Reflection API
      // is a workaround for this problem.

      Class[] parameterTypes;
      Object[] parameters;

      // Key key = new DESKey0();
      Class DESKey0Class = Class.forName("demos.stockbroker.DESKey0");
      parameterTypes = new Class[0];
      parameters    = new Object[0];
      Constructor desKey0Constructor = DESKey0Class.getConstructor(parameterTypes);
      Key key = (Key) desKey0Constructor.newInstance(parameters);

      // DESCipher desCipher = DESCipher.getInstance("DES/CBC");
      Class desCipherClass   = Class.forName("com.ibm.opencard.crypto.DESCipher");
      parameterTypes = new Class[1];
      parameterTypes[0] = java.lang.String.class;
      parameters    = new Object[1];
      parameters[0] = new String("DES/CBC");
      Method getInstance     = desCipherClass.getMethod("getInstance", parameterTypes);
      Object desCipher = getInstance.invoke(null, parameters); // getInstance is static

      // desCipher.setInitializationVector(iv);
      parameterTypes = new Class[1];
      parameterTypes[0] = byte[].class;
      parameters    = new Object[1];
      parameters[0] = iv;
      Method setInitializationVector = desCipherClass.getMethod("setInitializationVector", parameterTypes);
      setInitializationVector.invoke(desCipher, parameters);

      // desCipher.engineInitDecrypt(desKey);
      parameterTypes = new Class[1];
      parameterTypes[0] = java.security.Key.class;
      parameters    = new Object[1];
      parameters[0] = key;
      Method engineInitDecrypt = desCipherClass.getMethod("engineInitDecrypt", parameterTypes);
      engineInitDecrypt.invoke(desCipher, parameters);

      // desCipher.engineUpdate(orderData, 0, orderData.length, orderData, 0);
      parameterTypes = new Class[5];
      parameterTypes[0] = byte[].class;
      parameterTypes[1] = java.lang.Integer.TYPE;
      parameterTypes[2] = java.lang.Integer.TYPE;
      parameterTypes[3] = byte[].class;
      parameterTypes[4] = java.lang.Integer.TYPE;

      parameters    = new Object[5];
      parameters[0] = data;
      parameters[1] = new Integer(0);
      parameters[2] = new Integer(data.length);
      parameters[3] = data;
      parameters[4] = new Integer(0);

      Method engineUpdate = desCipherClass.getMethod("engineUpdate", parameterTypes);
      engineUpdate.invoke(desCipher, parameters);

      System.out.println("Data decipered by server using DES Algorithm:");
    } catch (InvocationTargetException e) {
      System.out.println("No DES available due to US export restrictions");
    } catch (NoClassDefFoundError e) {
      System.out.println("No DES available due to US export restrictions, transmitting clear");
    } catch (ClassNotFoundException e) {
      System.out.println("No DES available due to US export restrictions");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

} // class IBServerF
