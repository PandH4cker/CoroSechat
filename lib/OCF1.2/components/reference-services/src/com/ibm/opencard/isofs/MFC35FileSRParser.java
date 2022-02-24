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

package com.ibm.opencard.isofs;

import opencard.core.service.CardServiceException;

import com.ibm.opencard.service.MFC35ObjectSRParser;
import com.ibm.opencard.service.MFCCardObjectInfo;

import com.ibm.opencard.access.MFCAccessParser;
import com.ibm.opencard.access.BadHeaderException;


/**
 * A select response parser for file select commands.
 * This parser decodes the information contained in a typical MFC
 * select response to a file or directory. Unlike the generic class
 * <tt>MFC35ObjectSRParser</tt>, it does not only decode the access
 * information, but also the file or directory specific information,
 * like file size or structure. It does not decode additional data
 * that is sent when selecting special files like EF_CHV1.
 * <br>
 * The name of this class, indicating MFC 3.5 support, may be a bit
 * misleading. This parser also recognizes files types that are not
 * supported by this CardOS version, like variable record files. The
 * corresponding codes had been reserved, so there is no compatibility
 * problem.
 *
 * @version $Id: MFC35FileSRParser.java,v 1.5 1998/08/11 11:13:13 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35FileSRParser extends MFC35ObjectSRParser
{
  // The header encoding of the file type.
  protected final static byte FT_EF_TRANSPARENT = (byte)0;
  protected final static byte FT_EF_FIXED       = (byte)1;
  protected final static byte FT_EF_VARIABLE    = (byte)2;
  protected final static byte FT_EF_CYCLIC      = (byte)3;
  protected final static byte FT_EF_RFU4        = (byte)4;
  protected final static byte FT_EF_RFU5        = (byte)5;
  protected final static byte FT_EF_ASC         = (byte)6;

  // Both DF types are defined in the base class.
  //protected final static byte FT_DF_NOASC     = (byte)16;
  //protected final static byte FT_DF_WITHASC   = (byte)17;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Creates a new select response parser for file and directory selects.
   *
   * @param accpar   a parser for access conditions, which are part
   *                 of the file or directory select response
   */
  public MFC35FileSRParser(MFCAccessParser accpar)
  {
    super(accpar);
  }


  // access ///////////////////////////////////////////////////////////////////


  // service //////////////////////////////////////////////////////////////////

  /**
   * Interpret a smartcard's response to a select command.
   * This method is required in the interface <tt>MFCSelectResponseParser</tt>.
   * The implementation here parses the access conditions as well as file or
   * directory specific information like the size or file structure.
   *
   * @param response   the response data sent by the smartcard
   * @return   the file information gathered from the smartcard's response
   *
   * @exception BadHeaderException
   *    The response could not be parsed correctly.
   */
  public MFCCardObjectInfo parseSelectResponse(byte[] response)
       throws BadHeaderException
  {
    MFCFileInfo fi = new MFCFileInfo();

    parseObjectHeader(fi, response);    // base class method
    parseFileHeader  (fi, response);    // defined below

    return fi;
  }



  /**
   * Parse a file header and set up file information accordingly.
   * This method parses only the file part of the information. The generic
   * part must be interpreted before or afterwards, preferrably before.
   * the base class provides the method <tt>parseObjectHeader</tt> for
   * this purpose.
   * <br>
   * This method must be invoked <i>exactly once</i> for a particular
   * file info object. Before invocation, the file info is not set up
   * appropriately. It is not possible to reuse a file info object by
   * invoking this method a second time with a different header.
   *
   * @param info    the file info to set up
   * @param header  the file header obtained from the smartcard
   *
   * @exception BadHeaderException
   *    if the header could not be parsed correctly
   */
  protected void parseFileHeader(MFCFileInfo     info,
                                 byte[]          header)
       throws BadHeaderException
  {
    // Don't check for null, will cause exception anyway.
    if (header.length < 14)
      throw new BadHeaderException("file header too small");

    info.file_header = header;
    info.file_ID     = (short)( (header[4]<<8) | (header[5]&0x00ff) );
    info.file_size   = ((header[2] & 0x00ff)*256 +
                        (header[3] & 0x00ff)     );

    switch(header[13]) // file type
      {
      case FT_EF_TRANSPARENT:
        info.is_transparent = true;
        info.record_size = -1;
        break;

      case FT_EF_CYCLIC:
        info.is_cyclic = true;
        // fall through...

      case FT_EF_FIXED:
        if (header.length < 15)
          throw new BadHeaderException("file header holds no record size");
        info.record_size = header[14] & 0x00ff;
        break;

      case FT_EF_VARIABLE:
        info.record_size = 0;  // don't know
        break;

      case FT_DF_NOASC:
      case FT_DF_WITHASC:
        // evaluated by parser for generic object info
        info.record_size = -1;
        break;

      case FT_EF_ASC: // not accessible
      default:
        throw new BadHeaderException
          ("unknown file type " + header[13]);
      }

  } // parseFileHeader


} // class MFC35FileSRParser
