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

package com.ibm.opencard.dictionary;

import com.ibm.opencard.util.SCTTag;
import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

import opencard.core.util.HexString;

/*****************************************************************************
* Item represents an item entry contained in an AgentDictionary.
* Item objects are only instanciated by the itemEntry method of
* AgentDictionary (or derived classes).
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: Item.java,v 1.8 1998/08/11 14:46:34 cvsusers Exp $
*
* @see opencard.core.util.TLV
* @see com.ibm.opencard.dictionary.AgentDictionary
******************************************************************************/
public class Item
{
  /** Item belongs to an elementary file */
  public final static int EF = 0;
  /** Item belongs to a dedicated file */
  public final static int DF = 1;
  /** Access is undefined */
  public final static int ACCESS_UNDEFINED = 0;
  /** Access only possible in binary mode */
  public final static int ACCESS_BINARY    = 1;
  /** Access only possible in record mode */
  public final static int ACCESS_RECORD    = 2;
  /** Access only possible in select mode */
  public final static int ACCESS_SELECT    = 3;
  /** Access only possible in tagged mode */
  public final static int ACCESS_TAGGED    = 4;

  /** No record mode defined */
  public final static int RECORD_UNDEFINED = 0;
  /** Access first record */
  public final static int RECORD_FIRST     = 1;
  /** Access last record */
  public final static int RECORD_LAST      = 2;
  /** Access next record */
  public final static int RECORD_NEXT      = 3;
  /** Access previous record */
  public final static int RECORD_PREVIOUS  = 4;
  /** Access record using absolute record number */
  public final static int RECORD_ABSOLUTE  = 5;

  /** This value indicates an undefined record number */
  public final static int RECORD_NUMBER_UNDEFINED = -1;

  /** TLV holding Item information */
  private TLV tlv = null;

  /**************************************************************************
  * Create an item from a given TLV.
  **************************************************************************/
  Item(TLV tlv)
  {
    this.tlv = tlv;
  }

  /**************************************************************************
  * Return the access mode of this item.
  * @return <tt>Item.ACCESS_BINARY</tt> for Transparent Files,
  *         <br><tt>Item.ACCESS_RECORD</tt> for Record Files.
  **************************************************************************/
  public int accessMode()
  {
    TLV accessModeTLV;
    if((accessModeTLV = tlv.findTag(SCTTag.ACCESS_MODE, null)) == null)
      return Item.ACCESS_UNDEFINED;
    else
      return accessModeTLV.valueAsNumber();
  }

  /**************************************************************************
  * Return the type of this item.
  * @return <tt>Item.DF</tt> for Dedicated Files,
  *         <br><tt>Item.EF</tt> for Elementary Files.
  **************************************************************************/
  public int type()
  {
    TLV itemType;
    if((itemType = tlv.findTag(SCTTag.ITEM_TYPE, null)) == null)
      return Item.EF;
    else
      return itemType.valueAsNumber();
  }

  /**************************************************************************
  * Return the path of this item.
  * @return Item path
  **************************************************************************/
  public byte[] path()
  {
    TLV itemPath = tlv.findTag(SCTTag.ITEM_PATH, null);
    if(itemPath != null)
      return itemPath.valueAsByteArray();
    else
      return null;
  }

  /**************************************************************************
  * Return the alias name of this item.
  * @return alias name
  **************************************************************************/
  public String aliasName()
  {
    TLV itemAlias = tlv.findTag(SCTTag.ITEM_ALIAS, null);
    if(itemAlias != null)
      return new String(itemAlias.valueAsByteArray());
    else
      return null;
  }

  /**************************************************************************
  * Return the size of this item.
  * @return The length of this Item in bytes.
  **************************************************************************/
  public int size()
  {
    TLV sizeTLV = tlv.findTag(SCTTag.SIZE, null);
    if (sizeTLV != null)
      return sizeTLV.valueAsNumber();
    else
      return 0;
  }

  /**************************************************************************
  * Return the offset of this item.
  * @return The offset of this Item.
  **************************************************************************/
  public int offset()
  {
    TLV offsetTLV = tlv.findTag(SCTTag.OFFSET, null);
    if (offsetTLV != null)
      return offsetTLV.valueAsNumber();
    else
      return 0;
  }

  /**************************************************************************
  * Return the record mode of this item.
  * @return The record mode of this Item or RECORD_UNDEFINED if none is
  *         defined.
  **************************************************************************/
  public int recordMode()
  {
    TLV recordModeTLV = tlv.findTag(SCTTag.RECORD_MODE, null);
    if (recordModeTLV != null)
      return recordModeTLV.valueAsNumber();
    else
      return RECORD_UNDEFINED;
  }

  /**************************************************************************
  * Return the record number of this item.
  * @return The record number of this Item or RECORD_NUMBER_UNDEFINED
  *         if none is defined.
  **************************************************************************/
  public int recordNumber()
  {
    TLV recordNumberTLV = tlv.findTag(SCTTag.RECORD_NUMBER, null);
    if (recordNumberTLV != null)
      return recordNumberTLV.valueAsNumber();
    else
      return RECORD_NUMBER_UNDEFINED;
  }
}
// $Log: Item.java,v $
// Revision 1.8  1998/08/11 14:46:34  cvsusers
// *.java
//
// Revision 1.7  1998/05/05 21:15:24  schaeck
// Performance tuning
//
// Revision 1.6  1998/04/28 12:45:16  schaeck
// Added recordMode() and recordNumber()
//
// Revision 1.5  1998/04/27 11:38:49  schaeck
// Additiona functions for Bean support
//
// Revision 1.4  1998/04/14 17:40:11  schaeck
// Removed TLVException
//
// Revision 1.3  1998/03/25 20:00:00  schaeck
// added offset()
//
// Revision 1.2  1998/03/24 19:28:02  schaeck
// nicer comments
//
// Revision 1.1  1998/03/19 21:55:10  schaeck
// Initial version
//
