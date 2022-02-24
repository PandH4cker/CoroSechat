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

import opencard.core.util.HexString;
import opencard.opt.util.Tag;
import opencard.opt.util.TLV;

import java.security.Key;
import java.util.Enumeration;
import java.math.BigInteger;
import java.lang.reflect.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;


public class IBClientF extends Frame
{
  // GUI elements -----------------------------------------------------------
  private TextField   numberField;
  private TextArea    status;
  private Button      orderButton, readCHDButton;
  private Label       priceLabel, costLabel;
  private ImageCanvas nyseCanvas, canvas;
  private Choice      companyChoice;
  private Checkbox    encryptCheckBox;
  private Image       sigCardImage;
  private IBEveF      eve;          // Internet Broker Interceptor Frame

  public IBClientF(String title, IBEveF eve, Image nyseImage, Image headerImage, Image sigCardImage)
  {
    super(title);
    setFont(new Font("Helvetica", Font.BOLD, 16));
    this.eve = eve;
    this.sigCardImage = sigCardImage;

    int i = 0;

    // We use a grid bag layout to place the GUI elements and pictures
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    setLayout(gridBag);
    c.weightx = 1.0; c.weighty = 1.0;

    // Add the New York Stock Exchange picture
    nyseCanvas = new ImageCanvas(nyseImage, this, 260, 200, true);
    c.gridx = 4; c.gridy = 1; c.gridwidth = 1; c.gridheight = 7;
    gridBag.setConstraints(nyseCanvas, c);
    add(nyseCanvas);

    // Add the internet broker picture
    canvas = new ImageCanvas(headerImage, this, 420, 59, false);
    c.gridx = 0; c.gridy = 0; c.gridwidth = 5; c.gridheight = 1;
    gridBag.setConstraints(canvas, c);
    add(canvas);

    // Add encryption checkbox
    encryptCheckBox = new Checkbox("Encrypt", true);
    encryptCheckBox.addItemListener( new ItemListener ()
    {
      public void itemStateChanged(ItemEvent e ) {
        if (e.getStateChange() == ItemEvent.SELECTED)
          enableEncryption();
        else
          disableEncryption();
      }
    });
    c.gridx = 0; c.gridy = 1;
    c.gridwidth = 2;
    gridBag.setConstraints(encryptCheckBox, c);
    add(encryptCheckBox);

    c.gridwidth = 1;
    Label companyLabel = new Label("Company:", Label.LEFT);
    c.gridx = 0; c.gridy = 2;
    gridBag.setConstraints(companyLabel, c);
    add(companyLabel);

    companyChoice = new Choice();
    companyChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e ) { selectOtherStock(); }
    });
    for (i = 0; i < companies.length; i++)
    {
      companyChoice.addItem(companies[i]);
    }
    c.gridx = 1; c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridBag.setConstraints(companyChoice, c);
    add(companyChoice);

    // Add "Price:" label and label for displaying the price
    Label priceLabel_ = new Label("Price ($):", Label.LEFT);
    c.gridx = 0; c.gridy = 3;
    gridBag.setConstraints(priceLabel_, c);
    add(priceLabel_);

    priceLabel = new Label(Double.toString(prices[companyChoice.getSelectedIndex()]));
    c.gridx = 1; c.gridy = 3;
    gridBag.setConstraints(priceLabel, c);
    add(priceLabel);

    // Add "Number:" label and text field for entering the number
    Label numberLabel = new Label("Number:", Label.LEFT);
    c.gridx = 0; c.gridy = 4;
    gridBag.setConstraints(numberLabel, c);
    add(numberLabel);

    numberField = new TextField("0", 10);
    numberField.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { changeNumberOfStocks(); }
    });
    c.gridx = 1; c.gridy = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridBag.setConstraints(numberField, c);
    add(numberField);

    // Add "Cost ($):" label and label for displaying the cost.
    Label costLabel_ = new Label("Cost ($):", Label.LEFT);
    c.gridx = 0; c.gridy = 5;
    gridBag.setConstraints(costLabel_, c);
    add(costLabel_);

    costLabel = new Label("0", Label.LEFT);
    c.gridx = 1; c.gridy = 5;
    gridBag.setConstraints(costLabel, c);
    add(costLabel);

    // Add "Read Card Holder Data"button
    readCHDButton = new Button("Read Card Holder Data");
    readCHDButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { readCardHolderData(); }
    });
    c.gridx = 0; c.gridy = 6;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    gridBag.setConstraints(readCHDButton, c);
    add(readCHDButton);

    // Add "Send Order" button
    orderButton = new Button("Send Order");
    orderButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { createSignAndSendOrder(); }
    });
    c.gridx = 0; c.gridy = 7;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    gridBag.setConstraints(orderButton, c);
    add(orderButton);

    status = new TextArea(5, 35);
    status.setEditable(false);
    c.fill = GridBagConstraints.BOTH; //make this big
    c.gridx = 3; c.gridy = 1;
    c.gridwidth = 1; c.gridheight = 7;
    c.weightx = 0.8; c.weighty = 0.8;
    gridBag.setConstraints(status, c);
    add(status);

    setLocation(new Point(0, 350));
    setSize(800, 250);
    show();
  }

  public void printInfo(String info)
  {
    status.append(info);
  }

  // -------------------------------------------------------- End of GUI part

  // Application logic ------------------------------------------------------

  // companies not available as shares are marked with brackets
  private String[] companies = { "Bull (Paris)",
                                 "Dallas",
                                 "(First Access)",
                                 "(Gemplus)",
                                 "IBM",
                                 "Netscape",
                                 "(NC)",
                                 "Schlumberger",
                                 "SCM",
                                 "Sun",
                                 "(UbiQ)",
                                 "(Visa)" };

  // in $ as of close NYSE/NASDAQ Sep 1, 1998, unless indicated otherwise
  private double[] prices = {
    ( 61.00  ), // Bull: FF/Paris
    ( 27.25  ), // Dallas
    ( 22.22  ), // First Access: no shares, used arbitrary number
    ( 55.55  ), // Gemplus: no shares, used arbitrary number
    (117.9375), // IBM
    ( 21.125 ), // Netscape
    ( 20.5625), // Network Computer: no shares, used Oracle's price
    ( 46.625 ), // Schlumberger
    ( 43.0   ), // SCM
    ( 40.625 ), // Sun
    ( 44.44  ), // UbiQ: no shares, used arbitrary number
    ( 33.33  )  // VISA: no shares, used arbitrary number
  };

  private double  number         = 0;
  private double  price          = 0;
  private double  cost           = 0;
  private int     sequenceNumber = 0;
  private boolean encryption     = true;

  SignatureCard signatureCard = new SignatureCard();

  // The following methods are called by the anonymous
  // GUI AdapterClasses.

  protected void enableEncryption()
  {
    encryption = true;
  }

  protected void disableEncryption()
  {
    encryption = false;
  }

  protected boolean selectOtherStock()
  {
    priceLabel.setText(Double.toString(prices[companyChoice.getSelectedIndex()]));
    price = prices[companyChoice.getSelectedIndex()];
    cost  =  price * number;
    costLabel.setText(Double.toString(cost));
    return true;
  }

  protected boolean changeNumberOfStocks()
  {
    cost   = prices[companyChoice.getSelectedIndex()] * Double.valueOf(numberField.getText()).doubleValue();
    number = Double.valueOf(numberField.getText()).doubleValue();
    costLabel.setText(Double.toString(cost));
    return true;
  }

  protected boolean readCardHolderData()
  {
    // Read card holder data from the card
    byte[]  cardHolderData = null;
    boolean retry          = true;

    do {
      cardHolderData = signatureCard.getCardHolderData();
      if (cardHolderData != null) {
        String chd = (new String(cardHolderData)).trim();
        printInfo("\n\nThis card belongs to:\n" + chd + "\n\n");
        return true;
      } else {
        Frame f = new Frame();
        IBRequestCardF r = new IBRequestCardF(f, sigCardImage, "Please insert your Signature Card");
        retry = r.requestCard();
        f.dispose();
      }
    } while (cardHolderData == null && retry);

    if (cardHolderData == null)
      return false;
    else
      return true;
  }

  protected boolean createSignAndSendOrder()
  {
    int i = 0;
    TLV order = null;
    TLV signedMessage = null;
    byte[] message = null;
    byte[] cardHolderData = null;
    String company = new String(companyChoice.getSelectedItem());
    price = prices[companyChoice.getSelectedIndex()];
    number = Double.valueOf(numberField.getText()).doubleValue();
    cost = price * number;
    costLabel.setText(Double.toString(cost));
    sequenceNumber++;

    printInfo("\nBuy "+number+" "+company+" stocks for $"+cost+"\n");
    try {
      byte[]  signature = null;
      boolean retry     = true;

      do {
        cardHolderData = signatureCard.getCardHolderData();
        if (cardHolderData == null) {
          Frame f = new Frame();
          IBRequestCardF r = new IBRequestCardF(f, sigCardImage, "Please insert your Signature Card");
          retry = r.requestCard();
          f.dispose();
        }
      } while (cardHolderData == null && retry);

      if (cardHolderData == null)
        throw new Exception("Can not read card holder data");

      printInfo("Generating order...\n");

      // 0-bytes must be cut to prevent trouble in interceptor,
      // the text field will cut all 0-bytes except one and this invalidate
      // the signature.
      int len = 0;
      for (i=0; i<cardHolderData.length; i++) {
        if (cardHolderData[i] == (byte)0x00) {
          len = i; break;
        }
      }

      byte[] temp = new byte[len];
      System.arraycopy(cardHolderData, 0, temp, 0, len);

      String chs = new String(temp);
      order = new TLV(IBProt.STOCK_ORDER, new TLV(IBProt.NUMBER, (int) number));
      order.add(new TLV(IBProt.PRICE, Double.toString(price).getBytes()));
      order.add(new TLV(IBProt.COMPANY, company.getBytes()));
      order.add(new TLV(IBProt.SEQUENCE_NUMBER, sequenceNumber));
      order.add(new TLV(IBProt.CARD_HOLDER_DATA, chs.getBytes()));
      signedMessage = new TLV(IBProt.SIGNED_MESSAGE, order);

      printInfo("Signing order...\n");

      do {
        signature = signatureCard.sign(0, order.toBinary());
        if (signature != null) {
          BigInteger signatureBigInt = new BigInteger(1, signature);
          signedMessage.add(new TLV(IBProt.SIGNATURE, signatureBigInt.toByteArray()));

          byte[] clearText = signedMessage.toBinary();

          if (encryption) {
            printInfo("Encrypting message...\n");

            // Do padding
            byte[] data = new byte[((clearText.length + 7) / 8) * 8];
            System.arraycopy(clearText, 0, data, 0, clearText.length);

            encipher(data);
            message = (new TLV(IBProt.ENCRYPTED_MESSAGE, data)).toBinary();
          } else {
            message = (new TLV(IBProt.CLEAR_MESSAGE, clearText)).toBinary();
          }

          printInfo("Sending message to server");
          for (i=0; i<10; i++) {
            try { Thread.sleep(100); } catch (InterruptedException e) { }
            printInfo(".");
          }
          printInfo("\n");
          eve.processRequestFrom("Client", message);
        } else {
          Frame f = new Frame();
          IBRequestCardF r = new IBRequestCardF(f, sigCardImage, "Please insert your Signature Card");
          retry = r.requestCard();
          f.dispose();
        }
      } while (signature == null && retry);

      if (signature == null)
        throw new Exception("Can not generate signature");
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public void receiveResponse(String response)
  {
    printInfo(response);
  }

  private void encipher(byte[] data)
  {
    byte[]   iv =         { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

    try {
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
      Method engineInitEncrypt = desCipherClass.getMethod("engineInitEncrypt", parameterTypes);
      engineInitEncrypt.invoke(desCipher, parameters);

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

      System.out.println("Data encipered by client using DES Algorithm:");
    } catch (InvocationTargetException e) {
      System.out.println("No DES available due to US export restrictions, transmitting clear");
    } catch (NoClassDefFoundError e) {
      System.out.println("No DES available due to US export restrictions, transmitting clear");
    } catch (ClassNotFoundException e) {
      System.out.println("No DES available due to US export restrictions, transmitting clear");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  public void setEve(IBEveF eve)
  {
    this.eve = eve;
  }

  public void close()
  {
    signatureCard.close ();
  }

  public SignatureCard getSignatureCard()
  {
    return signatureCard;
  }
  // ------------------------------------------------- End of application logic
}
