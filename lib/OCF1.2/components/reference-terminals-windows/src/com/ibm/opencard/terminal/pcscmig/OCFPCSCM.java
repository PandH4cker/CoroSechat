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

package com.ibm.opencard.terminal.pcscmig;

import opencard.core.util.Tracer;

/** <tt>OCFPCSCM</tt> for PCSC card terminals.
 *
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: OCFPCSCM.java,v 1.6 1999/04/07 15:20:10 breid Exp $
 */

public class OCFPCSCM {

  private Tracer iTracer = new Tracer(this, OCFPCSCM.class);

  /** Constructor with initialization of the OCF tracing mechanism.
   *  @exception com.ibm.opencard.terminal.pcscmig.PcscException
   *		 thrown when error occured in PCSC Interface
   */
  public OCFPCSCM() throws PcscException {
    initTrace();
  }

  /* load the Wrapper-DLL */
  static public void loadLib() {
    try {

      //netscape.security.PrivilegeManager.enablePrivilege("UniversalLinkAccess");
      //System.loadLibrary("OCFPCSC1");
      opencard.core.util.SystemAccess.getSystemAccess().loadLibrary("OCFPCSCM");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**************************************************************/
  /*								*/
  /* native Methods						*/
  /*								*/
  /**************************************************************/

  /* initialize the native tracing mechanism */
  public native void initTrace();

  /* returns a list of terminals found in the PCSC resource manager */
  public native synchronized String[] SCardListReaders(String groups)
				  throws PcscException;

  /* returns the context */
  public native synchronized int  SCardEstablishContext(int scope)
				  throws PcscException;

  public native synchronized void SCardReleaseContext(int context)
				  throws PcscException;

  /* returns the SCARDHANDLE */
  public native synchronized int  SCardConnect(int context, String reader,
			      int shareMode, int preferredProtocol, Integer activeProtocol)
				  throws PcscException;

  public native synchronized void SCardReconnect(int card, int shareMode,
			      int preferredProtocoll,  int initialization, Integer activeProtocol)
				  throws PcscException;

  public native synchronized void SCardDisconnect(int card, int disposition)
				  throws PcscException;

  public native synchronized void SCardGetStatusChange(int context, int timeout, PcscReaderState[] readerState)
				  throws PcscException;

  /* returns the AttributeBuffer */
  public native synchronized byte[] SCardGetAttrib(int card, int attrId)
				  throws PcscException;

  /* returns the count of received bytes in OutBuffer */
  public native synchronized byte[] SCardControl(int card, int controlCode, byte[] inBuffer)
				  throws PcscException;

  /* returns the receiveBuffer */
  /* the DLL has to manage the special behaviour of the T0/T1 protocol */
  public native synchronized byte[] SCardTransmit(int card, byte[] sendBuffer)
				  throws PcscException;

  /* is called by the native methods to trace via OCF trace mechanism */
  protected void msg(int level, String methodName, String aLine) {
    iTracer.error("OCFPCSCM." + methodName, aLine);
  }
}
