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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import opencard.core.service.SmartCard;
import opencard.core.service.CardRequest;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardRequest;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.CTListener;
import opencard.core.event.EventGenerator;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;

import opencard.opt.iso.fs.CardFile;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.iso.fs.CardFileOutputStream;
import opencard.opt.iso.fs.CardFileInputStream;
import opencard.opt.security.PrivateKeyFile;

import com.ibm.opencard.script.MFCScriptService;
import com.ibm.opencard.buffer.TLVBuffer;
import opencard.opt.signature.JCAStandardNames;
import opencard.opt.signature.SignatureCardService;

/****************************************************************************
* This class encapsulates all Card related functionality required by the
* InternetBroker Demo. Only this class uses the OpenCardFramework.
*
* @version  $Id: SignatureCard.java,v 1.21 1999/10/14 15:44:18 pbendel Exp $
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @author Mike Wendler   (mwendler@de.ibm.com)
*****************************************************************************/
public class SignatureCard implements CTListener
{
  /** the smart card object used to get access to a smart card */
  SmartCard card = null;
  /** the file access card service used to access files on the card */
  FileAccessCardService fileService = null;
  /** the signature service used for signature generation and key import */
  SignatureCardService signatureService = null;

  /****************************************************************************
  * Constructs a SignatureCard object and makes sure that OpenCard is being
  * initialized properly.
  ****************************************************************************/
  SignatureCard () {
    try {
      System.out.println ("SignatureCard - start OpenCard");
      // Start the OpenCard Framework
      SmartCard.start ();
      // register the new SignatureCard as a Card Terminal Event Listener
      EventGenerator.getGenerator().addCTListener(this);
      // Let the registry create events for cards which are already present
      EventGenerator.getGenerator().createEventsForPresentCards(this);
      System.out.println ("SignatureCard - OpenCard is up and running");
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  /****************************************************************************
  * React on card removed events sent by OCF: Invalidate card and card service
  * @param ctEvent The card inserted event.
  ****************************************************************************/
  public synchronized void cardRemoved(CardTerminalEvent ctEvent) {
    System.out.println ("SignatureCard - got CARD_REMOVED event");
    card = null;
    fileService = null;
    signatureService  = null;
  }

  /****************************************************************************
  * React on card inserted events sent by OCF: Get new card and card service
  * @param ctEvent The card inserted event.
  ****************************************************************************/
  public void cardInserted(CardTerminalEvent ctEvent) {
    System.out.println ("SignatureCard - got CARD_INSERTED event");
    try {
      CardRequest cr = new CardRequest(CardRequest.NEWCARD,null, FileAccessCardService.class);
      card = SmartCard.getSmartCard(ctEvent);
      if (card != null)
        allocateServices(card);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
  * Check presence of a signature card.
  * @return true, if a signature card is present in a slot, false otherwise
  ****************************************************************************/
  public boolean signatureCardPresent()
  {
    return cardPresent() && (fileService != null) && (signatureService != null);
  }

  /****************************************************************************
  * Check presence of an (eventually uninitialized) smart card.
  * @return true, if a smart card is present in a slot, false otherwise
  ****************************************************************************/
  public boolean cardPresent()
  {
    return (card != null);
  }

  /****************************************************************************
  * Cleans up SignatureCard, i.e. in this case shuts down OpenCard.
  ****************************************************************************/
  void close () {
    System.out.println ("SignatureCard - stopping OpenCard");
    try {
      SmartCard.shutdown ();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /****************************************************************************
  * Sign given data using the given key.
  * @param keyNumber the number if the private key on the SmartCard to be
  *                  used for generating the signature
  * @param data      the data to be signed
  * @return           the signature
  ****************************************************************************/
  byte[] sign(int keyNumber, byte[] data)
  {
    byte[]   signature = null;
    try {
      // If no card is present, indicate to application that it must prompt for card
      if (!signatureCardPresent())
        return null;

      // specify the key used for signing
      PrivateKeyFile kf = new PrivateKeyFile (new CardFilePath(":C110"), keyNumber);

      // Let the card generate a signature
      signature = signatureService.signData(kf, JCAStandardNames.SHA1_RSA,
                                            JCAStandardNames.ZERO_PADDING, data);
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
    return signature;
  }

  /****************************************************************************
  * Get the card holder data from the SmartCard.
  * @return The card holder data read from the card.
  ****************************************************************************/
  public byte[] getCardHolderData()
  {
    try {
      // If no card is present, indicate to application that it must prompt for card
      if (!signatureCardPresent())
        return null;

      // mount file system to get access to the root directory
      CardFile root = new CardFile(fileService);

      // This is the file holding card holder name and e-Mail address
      CardFile file = new CardFile(root, ":C009");

      // Create a CardFileInputStream for file
      DataInputStream dis = new DataInputStream(new CardFileInputStream(file));

      // Read in the owner's name
      byte[] cardHolderData = new byte[file.getLength()];
      System.out.println("reading data");
      dis.read(cardHolderData);

      // Explicitly close the InputStream to yield the smart card to other applications
      dis.close();
      return cardHolderData;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /****************************************************************************
  * Write the given card holder data to the SmartCard.
  * @param cardHolderData - data to be written to the card
  ****************************************************************************/
  public boolean writeCardHolderData(String cardHolderData)
  {
    try {
      // If no card is present, indicate to application that it must prompt for card
      if (!signatureCardPresent())
        return false;

      // mount file system to get access to the root directory
      CardFile root = new CardFile(fileService);

      // This is the file holding card holder name and e-Mail address
      CardFile file = new CardFile(root, ":C009");

      // Create a CardFileInputStream for the file
      DataOutputStream dos = new DataOutputStream(new CardFileOutputStream(file));

      // Write the owner's name
      if (cardHolderData == null)
         cardHolderData = " ";
      byte[] temp = new byte[file.getLength()];
      byte[] chd = cardHolderData.getBytes();
      System.arraycopy(chd, 0, temp, 0, chd.length);
      dos.write(temp, 0, temp.length);

      // Explicitly close the InputStream to yield the smart card to other applications
      dos.close();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /****************************************************************************
  * Initialize a smart card with the demo image.
  * @return <tt>true</tt> on success, <tt>false</tt> on failure.
  ****************************************************************************/
  public boolean initialize()
  {
    TLVBuffer blackboard             = new TLVBuffer(new byte[10000], 0);
    boolean   servicesMustBeRestored = false;
    boolean   success                = true;

    try {
      // If no card is present, indicate to application that it must prompt for card
      if (!cardPresent())
        return false;

      // The card file service becomes invalid when the card is initialized,
      // so if it exists now, it must be restored later
      if (signatureCardPresent()) {
        fileService = null;
        signatureService = null;
        servicesMustBeRestored = true;
      }

      // Get a script extension for the inserted smart card
      MFCScriptService scriptService = (MFCScriptService)
        card.getCardService(MFCScriptService.class, true);

      // Execute order "INITIALIZE" to put the demo image on the card
      scriptService.executeOrder("INITIALIZE", new SBInitScript(), null,
                                 blackboard);

      // if necessary, restore services
      if (servicesMustBeRestored)
        success = allocateServices(card);

    } catch (Exception e) {
      e.printStackTrace();
      success = false;
    }
    return success;
  }


  /**
   * Allocates a file access and a signature service.
   *
   * @param card   the smartcard for which to allocate the services
   * @return    <tt>true</tt> if the services could be allocated,
   *            <tt>false</tt> otherwise
   */
  private boolean allocateServices(SmartCard card)
  {
    boolean success = true;

    try {
      fileService = (FileAccessCardService)
        card.getCardService(FileAccessCardService.class, true);
      signatureService  = (SignatureCardService)
        card.getCardService(SignatureCardService.class, true);

      SBCHVDialog dialog = new SBCHVDialog();
      fileService.setCHVDialog(dialog);
      signatureService.setCHVDialog(dialog);
    } catch (Exception e) {
      e.printStackTrace();
      success = false;
    }

    return success;
  }

} // class SignatureCard
