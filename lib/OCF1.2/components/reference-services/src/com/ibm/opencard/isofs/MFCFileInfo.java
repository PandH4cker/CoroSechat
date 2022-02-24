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


import opencard.opt.iso.fs.CardFileInfo;
import com.ibm.opencard.service.MFCCardObjectInfo;


/**
 * Information about a smartcard file or directory.
 * <I>This class should be renamed to and replace <tt>MFCCardFileInfo</tt>.
 *    Currently, this is not done to avoid confusion with two different
 *    classes sharing a single name.</I>
 *
 * @version $Id: MFCFileInfo.java,v 1.8 1999/01/19 08:36:16 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCFileInfo extends MFCCardObjectInfo
   implements CardFileInfo
{
  /**
   * The file header.
   * This is the smartcard's response to a selection of the file.
   */
  byte[] file_header = null;

  /**
   * The file identifier.
   * It is not yet clear whether this attribute provides useful
   * information. It is currently included for debugging output.
   */
  short file_ID = 0;

  /** Specifies whether this file is a transparent file. */
  boolean is_transparent = false;

  /** Specifies whether this file is a cyclical file. */
  boolean is_cyclic = false;

  /** Holds the total size of this file. */
  int file_size = 0;

  /**
   * Holds the record size of this file.
   * For files with variable sized records, this attribute should hold 0.
   * For files that are not structured, it's value doesn't matter.
   */
  int record_size = 0;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates new file information.
   * The information is not yet set up after creation.
   * It has to be set explicitly afterwards.
   */
  public MFCFileInfo()
  {
    super();
  }


  // access ///////////////////////////////////////////////////////////////////


  /**
   * Sets the file header.
   * Upon selection of a file, the smartcard will respond with a file header
   * and, optionally, some additional information. This method is used to
   * store the smartcard's response in the info structure.
   *
   * @param header  the header of the file
   */
  final public void setHeader(byte[] header)
  {
    file_header = header;
  }


  /**
   * Sets the ID of the file on which to keep information.
   * Every file on a smartcard has a 2 byte identifier that is unique
   * within the respective directory.
   *
   * @param fileID   the identifier of the file
   */
  final public void setFileID(short fileID)
  {
    file_ID = fileID;
  }


  /**
   * Specifies whether the file on which to keep information is transparent.
   * Files that are not transparent are either structured  or dedicated
   * files. To distinguish these, a base class attribute indicates whether
   * the file is a container, and therefore a dedicated file.
   *
   * @param isTransparent  <tt>true</tt> if the file is transparent,
   *                       <tt>false</tt> otherwise
   *
   * @see com.ibm.opencard.service.MFCCardObjectInfo#isContainer
   */
  final public void setTransparent(boolean isTransparent)
  {
    is_transparent = isTransparent;
  }


  /**
   * Specifies whether the file on which to keep information is cyclic.
   * This attribute is relevant only if the file is neither transparent
   * nor a dedicated file. In this case it has to be a structured file.
   * If the structure is <i>cyclic, fixed size</i>, this method must be
   * used to specify that.
   *
   * @param isCyclic    <tt>true</tt> if the file has a cyclic structure,
   *                    <tt>false</tt> otherwise. If this method is not
   *                    invoked at all, the value defaults to <tt>false</tt>.
   */
  final public void setCyclic(boolean isCyclic)
  {
    is_cyclic = isCyclic;
  }


  /**
   * Specifies the size of the file on which to keep information.
   * Any file, whether it's transparent, structured, or a dedicated file,
   * has an associated file size. The file size can be specified by
   * invoking this method.
   *
   * @param fileSize   the number of bytes of the file contents
   */
  final public void setFileSize(int fileSize)
  {
    file_size = fileSize;
  }


  /**
   * Specifies the record size of the file on which to keep information.
   * This attribute is relevant only for files with a fixed record length.
   * For a file with variable record sizes, 0 must be specified. For
   * files that are not structured, this method should not be invoked
   * at all.
   *
   * @param recordSize   the number of bytes in a record
   */
  final public void setRecordSize(int recordSize)
  {
    record_size = recordSize;
  }



  /**
   * Returns the identifier of the file.
   *
   * @return the 2 byte identifier of the file
   *
   * @see opencard.opt.iso.fs.CardFileInfo#getFileID
   */
  final public short getFileID()
  {
    return file_ID;
  }


  /**
   * Tests whether the file is a DF.
   *
   * @return <tt>true</tt> iff this file is a directory
   *
   * @see opencard.opt.iso.fs.CardFileInfo#isDirectory
   */
  final public boolean isDirectory()
  {
    return isContainer();
  }


  /**
   * Tests whether this file is transparent.
   *
   * @return <tt>true</tt> iff this file is transparent
   *
   * @see opencard.opt.iso.fs.CardFileInfo#isTransparent
   */
  final public boolean isTransparent()
  {
    return is_transparent;
  }


  /**
   * Tests whether this file is cyclic.
   *
   * @return <tt>true</tt> iff this file is cyclic.
   *
   * @see opencard.opt.iso.fs.CardFileInfo#isCyclic
   */
  final public boolean isCyclic()
  {
    return is_cyclic;
  }


  /**
   * Tests whether this file has a variable record size.
   *
   * @return <tt>true</tt> iff this file is variable.
   *
   * @see opencard.opt.iso.fs.CardFileInfo#isVariable
   */
  final public boolean isVariable()
  {
    return ((!is_transparent) && (record_size == 0));
  }


  /**
   * Returns the length of this file.
   *
   * @return the number of bytes in this file
   *
   * @see opencard.opt.iso.fs.CardFileInfo#getLength
   */
  final public int getLength()
  {
    return file_size;
  }


  /**
   * Returns the record size of this file.
   *
   * @return the number of bytes in a record of this file
   *
   * @see opencard.opt.iso.fs.CardFileInfo#getRecordSize
   */
  final public int getRecordSize()
  {
    return record_size;
  }


  /**
   * Returns the header of this file.
   *
   * @return the header as a byte array
   *
   * @see opencard.opt.iso.fs.CardFileInfo#getHeader
   */
  final public byte[] getHeader()
  {
    return file_header;
  }


  // service //////////////////////////////////////////////////////////////////

  /*
   * This class only represents data, it does not provide any service methods.
   */


  // debug ////////////////////////////////////////////////////////////////////


  /** Add information to the base class toString() method. */
  protected void toStringHook(StringBuffer sb)
  {
    sb.append(";").append(isContainer()?"D":"E").append("F 0x");
    sb.append(Integer.toHexString(file_ID & 0xffff));
    sb.append(",size ").append(file_size);

    if (!isContainer())
      {
        if (is_transparent)     sb.append(", binary");
        else {                  sb.append(", record");
          if (is_cyclic)        sb.append(" cyclic");
          if (record_size == 0) sb.append(" variable");
          else                  sb.append(", rec size ").append(record_size);
        }
      }
  }

} // class MFCFileInfo
