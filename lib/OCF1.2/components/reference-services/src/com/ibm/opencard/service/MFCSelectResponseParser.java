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

package com.ibm.opencard.service;

import com.ibm.opencard.access.BadHeaderException;


/**
 * Interpreter for smartcard responses to select commands.
 * The select command can be used in different context, for example
 * to select a file on the card, or to select an application directory,
 * or to select a key directory, and so on.
 * <br>
 * Depending on the context, the smartcard's response to the command may
 * have to be interpreted in a different way, for example to create file
 * info which may include file size and type, application info which may
 * include the application name and version, key info which may include
 * the cryptographic algorithm to be used, and so on.
 * <br>
 * This interface can be implemented to define a parser for select responses.
 * The parser is expectend to return <tt>MFCCardObjectInfo</tt>, which holds
 * access information. Derived classes can be used to add the context specific
 * information supported by the respective parser.
 *
 * @version $Id: MFCSelectResponseParser.java,v 1.2 1998/08/11 11:12:21 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCardObjectInfo
 */
public interface MFCSelectResponseParser
{
  /**
   * Interpret a smartcard's response to a select command.
   * The response is passed as a plain byte array which holds only
   * the actual data. This method will be invoked only after a
   * <i>successful</i> select, so there is no need to check any
   * command status information returned by the card. If the select
   * failed, there will be nothing but the error status, so there is
   * nothing to interpret.
   *
   * @param response   the response data sent by the smartcard
   * @return   the information gathered from the smartcard's response
   *
   * @exception BadHeaderException
   *    The returned file header could not be parsed.
   */
  public MFCCardObjectInfo parseSelectResponse(byte[] response)
       throws BadHeaderException;

} // interface MFCSelectResponseParser
