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
import java.math.BigInteger;

import opencard.opt.util.Tag;
import opencard.opt.util.TLV;

/****************************************************************************
* IBEve is the window which allows to modify the message sent from client
* to server in the Internet Broker Demo.
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @version  $Id: IBEveF.java,v 1.4 1998/09/02 09:11:29 cvsusers Exp $
*
* @see IBServer
* @see IBClient
* @see IBProt
****************************************************************************/
public class IBEveF extends Frame
{
  // GUI part ---------------------------------------------------------------

  private Label      dataLabel, signatureLabel;
  private TextField  dataField0, dataField1, dataField2, dataField3;
  private TextField  signatureField, cardHolderField;
  private Button     sendButton;
  private Checkbox   interceptCheckBox;


  // Receiver object
  private IBServerF server = null;

  /**************************************************************************
  * Create a new instance.
  *
  * @param title  - the title of the frame
  * @param server - the object to which messages are sent
  **************************************************************************/
  public IBEveF(String title, IBServerF server)
  {
    super(title);
    setFont(new Font("Helvetica", Font.BOLD, 16));

    this.server = server;

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    setLayout(gridBag);

    c.weightx = 1.0; c.weighty = 1.0;

    c.fill = GridBagConstraints.HORIZONTAL;

    // Add intercept CheckBox
    interceptCheckBox = new Checkbox("Intercept");
    interceptCheckBox.addItemListener( new ItemListener ()
    {
      public void itemStateChanged(ItemEvent e ) { interceptStateChange(); }
    });

    c.gridx = 0; c.gridy = 0;
    gridBag.setConstraints(interceptCheckBox, c);
    add(interceptCheckBox);

    // Add Label "Data:"
    Label dataLabel = new Label("Data:", Label.LEFT);
    c.gridx = 1; c.gridy = 0;
    gridBag.setConstraints(dataLabel, c);
    add(dataLabel);

    // Add Label "Signature:"
    Label signatureLabel = new Label("Signature:", Label.LEFT);
    c.gridx = 1; c.gridy = 2;
    gridBag.setConstraints(signatureLabel, c);
    add(signatureLabel);

    // Add TextFields
    c.weightx = 1.0;
    c.gridwidth = 1;

    dataField0 = new TextField("0", 4);
    dataField0.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { dataField0Change(); }
    });
    c.gridx = 2; c.gridy = 0;
    gridBag.setConstraints(dataField0, c);
    add(dataField0);

    dataField1 = new TextField("0", 4);
    dataField1.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { dataField1Change(); }
    });
    c.gridx = 3; c.gridy = 0;
    gridBag.setConstraints(dataField1, c);
    add(dataField1);

    dataField2 = new TextField("0", 4);
    dataField2.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { dataField2Change(); }
    });
    c.gridx = 4; c.gridy = 0;
    gridBag.setConstraints(dataField2, c);
    add(dataField2);

    c.weightx = 1.0;
    dataField3 = new TextField("0", 4);
    dataField3.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { dataField3Change(); }
    });
    c.gridx = 5; c.gridy = 0;
    gridBag.setConstraints(dataField3, c);
    add(dataField3);

    // Add card holder TextField
    cardHolderField = new TextField("-", 16);
    cardHolderField.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { cardHolderFieldChange(); }
    });
    c.gridx = 2; c.gridy = 1;
    c.gridwidth = 4;
    gridBag.setConstraints(cardHolderField, c);
    add(cardHolderField);

    // Add signature TextField
    signatureField = new TextField("0", 16);
    signatureField.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { signatureFieldChange(); }
    });
    c.gridx = 2; c.gridy = 2;
    c.gridwidth = 4;
    gridBag.setConstraints(signatureField, c);
    add(signatureField);

    // Add send Button
    c.weightx = 0.8;
    sendButton = new Button("Send");
    sendButton.addActionListener( new ActionListener ()
    {
      public void actionPerformed(ActionEvent e ) { sendButtonPressed(); }
    });
    c.gridx = 6; c.gridy = 0;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gridBag.setConstraints(sendButton, c);
    add(sendButton);

    setLocation(new Point(0, 255));
    setSize(800, 95);
    show();
  }

  private void setEditable(boolean editable)
  {
    dataField0.setEditable(editable);
    dataField1.setEditable(editable);
    dataField2.setEditable(editable);
    dataField3.setEditable(editable);
    signatureField.setEditable(editable);
    cardHolderField.setEditable(editable);
  }

  // -------------------------------------------------------- End of GUI part

  // Application logic part -------------------------------------------------

  // Message parts
  private BigInteger signatureBigInt;
  private int        sequenceNumber;
  private int        number;
  private String     price;
  private String     company;
  private String     cardHolder;

  // Indicates wether intercepted message is encrypted or not
  private boolean    encrypted = true;

  // Indicates weither we are intercepting messages or not
  private boolean    intercepting = false;

  // original data received
  private byte[]    data = null;

  // The following methods are called by the anonymous
  // GUI AdapterClasses.

  protected void interceptStateChange()
  {
    intercepting = interceptCheckBox.getState();
  }

  protected void dataField0Change()
  {
    sequenceNumber = Integer.parseInt(dataField0.getText());
  }

  protected void dataField1Change()
  {
    number = Integer.parseInt(dataField1.getText());
  }

  protected void dataField2Change()
  {
    price = dataField2.getText();
  }

  protected void dataField3Change()
  {
    company = dataField3.getText();
  }

  protected void cardHolderFieldChange()
  {
    cardHolder = cardHolderField.getText();
  }

  protected void signatureFieldChange()
  {
    signatureBigInt = new BigInteger(signatureField.getText());
  }

  protected void sendButtonPressed()
  {
    TLV order = null, signedMessage = null, clearText = null;

    if (encrypted)
    {
      // If the data was encrypted, we send it as it was received.
      server.processRequestFrom("Client", data);
    }
    else
    {
      // If the data was not encrypted, we send the (eventually)
      // modified data fetched from the text fields.
      sequenceNumber  = Integer.parseInt(dataField0.getText());
      number          = Integer.parseInt(dataField1.getText());
      price           = dataField2.getText();
      company         = dataField3.getText();
      cardHolder      = cardHolderField.getText();
      signatureBigInt = new BigInteger(signatureField.getText());

      order = new TLV(IBProt.STOCK_ORDER,
                new TLV(IBProt.NUMBER,           (int) number));
      order.add(new TLV(IBProt.PRICE,            price.getBytes()));
      order.add(new TLV(IBProt.COMPANY,          company.getBytes()));
      order.add(new TLV(IBProt.SEQUENCE_NUMBER,  sequenceNumber));
      order.add(new TLV(IBProt.CARD_HOLDER_DATA, cardHolder.getBytes()));

      signedMessage = new TLV(IBProt.SIGNED_MESSAGE, order);
      signedMessage.add(new TLV(IBProt.SIGNATURE, signatureBigInt.toByteArray()));

      clearText = new TLV(IBProt.CLEAR_MESSAGE, signedMessage.toBinary());
      server.processRequestFrom("Eve", clearText.toBinary());
    }
  }

  /**************************************************************************
  * Process a request coming in from the client.
  *
  * @param senderName - The name of the sender of this message
  * @param data       - The message that has been received
  **************************************************************************/
  public void processRequestFrom(String senderName, byte[] data)
  {
    this.data = data;

    if (intercepting)
    {
      TLV message = new TLV(data);
      if (message.tag().equals(IBProt.CLEAR_MESSAGE))
      {
        encrypted = false;
        TLV signedMessage = new TLV(message.valueAsByteArray());

        if (!signedMessage.tag().equals(IBProt.SIGNED_MESSAGE))
        {
          System.err.println("IBEve - panic: SIGNED_MESSAGE tag not found");
          System.exit(0);
        }

        TLV stockOrder = signedMessage.findTag(IBProt.STOCK_ORDER, null);
        if (stockOrder == null)
        {
          System.err.println("IBEve - panic: STOCK_ORDER tag not found");
          System.exit(0);
        }
        byte[] order = stockOrder.toBinary();

        TLV signature  = signedMessage.findTag(IBProt.SIGNATURE, null);
        if (signature == null)
        {
          System.err.println("IBEve - panic: SIGNATURE tag not found");
          System.exit(0);
        }

        sequenceNumber  = (stockOrder.findTag(IBProt.SEQUENCE_NUMBER, null)).valueAsNumber();
        number          = (stockOrder.findTag(IBProt.NUMBER, null)).valueAsNumber();
        price           = new String((stockOrder.findTag(IBProt.PRICE, null)).valueAsByteArray());
        company         = new String((stockOrder.findTag(IBProt.COMPANY, null)).valueAsByteArray());
        cardHolder      = new String((stockOrder.findTag(IBProt.CARD_HOLDER_DATA, null)).valueAsByteArray());
        signatureBigInt = new BigInteger(1, signature.valueAsByteArray());

        dataField0.setText(Integer.toString(sequenceNumber));
        dataField1.setText(Integer.toString(number));
        dataField2.setText(price);
        dataField3.setText(company);
        cardHolderField.setText(cardHolder);
        signatureField.setText(signatureBigInt.toString());
      }
      else
      {
        encrypted = true;
        dataField0.setText("???");
        dataField1.setText("???");
        dataField2.setText("???");
        dataField3.setText("???");
        cardHolderField.setText("???");
        signatureField.setText("???");
      }

      // TextFields become un-editable if data is encrypted, editable otherwise
      setEditable(!encrypted);
    }
    else
    {
      server.processRequestFrom("Client", data);
    }

    repaint();
  }
}
