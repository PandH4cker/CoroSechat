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

import opencard.core.service.CardServiceException;

import com.ibm.opencard.access.MFCAccessParser;
import com.ibm.opencard.access.BadHeaderException;


/**
 * A select response parser for generic select commands.
 * This parser decodes the access information contained in a typical
 * MFC select response to a file or directory. It also determines
 * whether the object is an elementary file or a directory. It does
 * not parse any additional file or directory information.
 *
 * <p>
 * This class is rather a sample implementation of the interface
 * <tt>MFCSelectResponseParser</tt> than useful in itself. Typically,
 * the object info will be extended to include file or key information.
 * An appropriate select response parse will be required, which may
 * look very much like this one, or may even extend this one.
 *
 * @version $Id: MFC35ObjectSRParser.java,v 1.4 1998/08/11 11:12:21 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35ObjectSRParser implements MFCSelectResponseParser
{
  /** The standard ASN.1 tag for a MFC file header. */
  protected final static byte FILE_HEADER_TAG = (byte) 0x63;

  /** The file type DF without ASC execution. */
  protected final static byte FT_DF_NOASC       = (byte)16;

  /** The file type DF with ASC execution. */
  protected final static byte FT_DF_WITHASC     = (byte)17;



  /** The underlying parser for access conditions. */
  protected MFCAccessParser access_parser = null;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a generic select response parser for MFC 3.5 and above.
   *
   * @param accpar    the parser for access information
   */
  public MFC35ObjectSRParser(MFCAccessParser accpar)
  {
    access_parser = accpar;
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Interprets a smartcard's response to a select command.
   * This method is required in the interface <tt>MFCSelectResponseParser</tt>.
   * The implementation here just parses the access information encoded in
   * a standard MFC select response and checks whether a dedicated file was
   * selected.
   *
   * @param response   the response data sent by the smartcard
   * @return   the generic information gathered from the smartcard's response
   *
   * @exception BadHeaderException
   *    the response could not be parsed correctly
   */
  public MFCCardObjectInfo parseSelectResponse(byte[] response)
       throws BadHeaderException
  {
    MFCCardObjectInfo info = new MFCCardObjectInfo();

    parseObjectHeader(info, response);

    return info;
  }


  /**
   * Parses an object header and sets up information accordingly.
   * Actually, all objects on a MFC smartcard are files. The term object
   * is used since only part of the file header information is evaluated
   * here.
   * <br>
   * This method must be invoked <i>exactly once</i> for a particular
   * object (or derived) info. Before the invocation, the information
   * is not set up appropriately. It is not possible to re-use object
   * information by invoking this method a second time with a different
   * header.
   *
   * @param info     the information object to initialize
   * @param header   the information in an encoded form
   *
   * @exception BadHeaderException
   *    the header could not be parsed
   */
  protected void parseObjectHeader(MFCCardObjectInfo info, byte[] header)
       throws BadHeaderException
  {
    if (header[0] != FILE_HEADER_TAG)
      throw new BadHeaderException
        ("bad header tag 0x" + Integer.toHexString(header[0]));

    info.access_information = access_parser.parseAccessInformation(header, 6);
    info.is_container       = ((header[13] == FT_DF_NOASC) ||
                               (header[13] == FT_DF_WITHASC) );
  }

} // class MFC35ObjectSRParser
