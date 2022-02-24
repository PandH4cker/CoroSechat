/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 *  - In addition,  modification and redistribution of this  source
 *    code must retain the below original copyright notice.
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */

/*
 * Copyright © 1997, 1998 IBM Corporation.
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
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE. ANY
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

package com.gemplus.opencard.terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;

import opencard.opt.terminal.protocol.*;

import opencard.core.util.HexString;

/** Implementation of an OpenCard <tt>T1Protocol</tt> for
 *  GemPlus serial readers using the javax.comm package.
 *
 * IMPORTANT: javax.comm does not work with applets under jdk1.1.x !!!
 *
 * @version $Id: GemplusBlockProtocol.java,v 3.0 1999/02/11 17:19:45 root Exp root $<br>
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)<br>
 * Patched by Gilles.Pauzie@gemplus.com (Mon, 18 Jan 1999) Renamed in<br>
 *   GemplusBlockProtocol<br>
 * Patched by Gilles.Pauzie@gemplus.com (Tue, 9 Feb 1999) Bug fixed for
 *   cards only using T=1 protocol<br>
 *
 * @see opencard.opt.terminal.protocol.T1Protocol
 */

public class GemplusBlockProtocol extends T1Protocol {

  /**
   * Constructor of T1Protocol implementation
   * for Gemplus serial readers.
   *
   * @param hostID
   *        identification for host (default is 2 for Oros and GemCore)
   * @param remoteID
   *        identification for reader (default is 4 for Oros and GemCore)
   * @param timeout
   * @param in
   * @param out
   * @see opencard.opt.terminal.protocol.T1Protocol
   */
  GemplusBlockProtocol(int hostID,
                       int remoteID,
                       int timeout,
                       InputStream in,
                       OutputStream out) {

    super(hostID, remoteID, timeout);
    this.in = in;
    this.out = out;
  }

  /** used for getting data from reader */
  private InputStream in;

  /** used for sending data to the reader */
  private OutputStream out;

  /**
   * Implementation of exchangeData
   *
   * @param sendBlock
   *        The T1 block to send.
   * @param T1IOException
   *        Thrown when error occured on data transfer.
   *        (This exception is a wrapper for IOException)
   * @param T1TimeoutException
   *        Thrown when block receive is not complete within expected time.
   * @param T1BlockLengthException
   *        Thrown when expected block length differs from received data.
   * @param T1UnknownBlockException
   *        Thrown when <tt>T1Block</tt> could not identify the type of the
   *        given block.
   * @param T1BlockEDCErrorException
   *        Thrown when <tt>T1Block</tt> detected error in checksum.
   * @see   opencard.opt.terminal.protocol.T1Protocol#exchangeData
   */
  protected T1Block exchangeData(T1Block sendBlock)
          throws T1IOException,
                 T1TimeoutException,
                 T1BlockLengthException,
                 T1UnknownBlockException,
                 T1BlockEDCErrorException {

    byte[] receiveBuf = null;
    T1Block receiveBlock = null;

    long stopTime;

    int receiveCount = 0;
    int receiveChar;

    byte[] tmpBuf = new byte[256];

    try {

      // send block to reader
      out.write(sendBlock.getBlock());

      // calculate the timeout
      stopTime = System.currentTimeMillis() + getBlockWaitingTime();

      do {
        receiveChar = in.read();
        if (receiveChar != -1) {
          tmpBuf[receiveCount] = (byte)receiveChar;
          receiveCount++;
        }

        if (System.currentTimeMillis() > stopTime)
          throw new T1TimeoutException("GemplusBlockProtocol.exchangeData: "
                                       + "timeout reached...");

      } while (((receiveChar == -1) && (receiveCount == 0)) ||
               (!isBlockComplete(tmpBuf, receiveCount) && (receiveCount > 0)));

      /*
       * - create buffer with proper size
       * - copy data from tmpBuf
       * - create T1Block with data from receiveBuf
       */
      receiveBuf = new byte[receiveCount];
      System.arraycopy(tmpBuf, 0, receiveBuf, 0, receiveCount);
      receiveBlock = new T1Block(receiveBuf, T1Block.EDC_LDR);

    // remap IOException to T1IOException
    } catch(IOException e) {
      throw new T1IOException("GemplusBlockProtocol.internalTransmit: "
                              + e.getMessage());
    }

    return receiveBlock;
  } // exchangeData
}
