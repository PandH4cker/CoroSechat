/*
 * (C)Copyright IBM Corporation 1997 - 1999
 * All Rights Reserved.
 */

package com.ibm.opencard.service;


/**
 * CLAss and INStruction codes for MFC 4.1 smartcards.
 * This class implements exactly the interface <tt>MFCCodes</tt>.
 * The methods are trivial and therefore not commented.
 * The base class already defines codes for MFC 3.5 and 4.0
 * smartcards, only the differences to MFC 4.1 cards can
 * be found here.
 *
 * @version $Id: MFC41Codes.java,v 1.5 1998/10/07 06:55:20 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCodes
 * @see MFC35Codes
 */
public class MFC41Codes extends MFC35Codes implements MFCCodes
{
  public MFC41Codes()
  {
    super();
  }

  /**
   * Return the block size for data transfer.
   * The block size depends on whether secure messaging is used or not.
   * If secure messaging is used, it is larger. The current structure
   * of the MFC card services does not support this distinction, so the
   * minimum of both block sizes is returned.
   *
   * @return  the block size to use for data transfers
   */
  public int getBlockSize() { return 0x70; }

  public boolean needsZeroLc() { return false; }
  public boolean needsZeroLe() { return true ; }

  public byte getISOClassByte() { return (byte) 0x00; }
  public byte getClassByte()    { return (byte) 0xa0; }

  public byte getAppendRecordByte() { return (byte) 0xe2; }

} // class MFC41Codes
