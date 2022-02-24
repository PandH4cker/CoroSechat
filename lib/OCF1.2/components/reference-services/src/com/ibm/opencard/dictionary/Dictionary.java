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

import opencard.core.service.CardServiceException;

import com.ibm.opencard.buffer.TLVBuffer;

import opencard.opt.util.TLV;
import opencard.opt.util.Tag;
import com.ibm.opencard.util.SCTTag;
import com.ibm.opencard.util.ByteArray;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/****************************************************************************
* A Dictionary contains information required by the IBM CardAgent.
* for accessing files, e.g. file paths, access conditions, offsets and sizes.
*
* AgentDictionary is the abstract base class of all dictionaries.
* Every individual concrete dictionary is a distinct subclass of
* AgentDictionary.
*
* Instances are not created.  Rather the class has class methods only.
*
* @author  Frank Seliger (seliger@de.ibm.com)
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: Dictionary.java,v 1.7 1998/08/12 17:45:29 cvsusers Exp $
******************************************************************************/
public abstract class Dictionary
{
  private static boolean TRACE = Boolean.getBoolean("com.ibm.boeblingen.smartcard.agent.trace");

  protected TLV dictionaryTLV = null;

  /****************************************************************************
  * Return a TLVBuffer containing the dictionary structure.
  * @return A TLV buffer containing the dictionary
  ****************************************************************************/
  public abstract TLVBuffer buffer();

  public class ItemEnumeration implements Enumeration
  {
    protected boolean endReached = false;
    protected TLV dictionaryTLV  = null;
    protected TLV itemTLV        = null;

    public ItemEnumeration(Dictionary dictionary)
    {
      dictionaryTLV = new TLV(dictionary.buffer().data());
    }

    public boolean hasMoreElements()
    {
      if (!endReached) {
        // Try if there is another element. if not set endReached to true
        if(dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, itemTLV) == null) {
          endReached = true;
        }
      }
      return !endReached;
    }

    public Object nextElement() throws NoSuchElementException
    {
      itemTLV = dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, itemTLV);
      if (itemTLV == null) {
        endReached = true;
        throw new NoSuchElementException();
      }
      return new Item(itemTLV);
    }
  }

  public Enumeration items()
  {
    return new ItemEnumeration(this);
  }

  /****************************************************************************
  * Return the item entry for the specified alias name.
  * @param alias The alias name of the item entry to be returned.
  * @return A object containing the item entry, or null if no matching
  *         entry could be found.
  ****************************************************************************/
  public Item itemEntry(String alias)
  {
    return itemEntryForRawAlias(alias.getBytes());
  }

  /****************************************************************************
  * Return the item entry for the specified alias name.
  * @param alias The alias name of the item entry to be returned.
  * @return An Item object containing the item entry, or null if no matching
  *         entry could be found.
  ****************************************************************************/
  public Item itemEntryForRawAlias(byte[] alias)
  {
    if (dictionaryTLV == null) {
      dictionaryTLV = new TLV(this.buffer().data());
    }

    // find first item entry in dictionary
    TLV iterTLV = dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, null);

    while (iterTLV != null) {
      byte[] itemAlias = iterTLV.findTag(SCTTag.ITEM_ALIAS, null).valueAsByteArray();

      if (ByteArray.equal(itemAlias, alias)) {
        return new Item(iterTLV);
      }
      iterTLV = dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, iterTLV);
    }
    return null;
  }

  /****************************************************************************
  * Return the item entry for the specified item path.
  * @param itemPath The itemPath of the item entry to be returned.
  * @return A TLV object containing the item entry, or null if no matching
  *         entry could be found.
  ****************************************************************************/
  public Item itemEntry(byte[] path)
  {
    if (dictionaryTLV == null) {
      dictionaryTLV = new TLV(this.buffer().data());
    }

    // find first item entry in dictionary
    TLV iterTLV = dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, null);

    while (iterTLV != null) {
      byte[] itemPath = iterTLV.findTag(SCTTag.ITEM_PATH, null).valueAsByteArray();

      if (ByteArray.equal(itemPath, path)) {
        return new Item(iterTLV);
      }
      iterTLV = dictionaryTLV.findTag(SCTTag.ITEM_ENTRY, iterTLV);
    }
    return null;
  }

  protected static TLVBuffer restore(String fileSpec)  throws IOException
  {
    File inFile = new File(fileSpec);
    if (!inFile.exists() || !inFile.isFile())
        throw new IOException("no such source file: " + fileSpec);
    if (!inFile.canRead())
        throw new IOException("source file is unreadable: " + fileSpec);

    RandomAccessFile in = new RandomAccessFile(inFile, "r");
    int size = (int)in.length();
    byte[] fileContents = new byte[size];
    in.read(fileContents);
    return new TLVBuffer(fileContents, size);
  }
}
// $Log: Dictionary.java,v $
// Revision 1.7  1998/08/12 17:45:29  cvsusers
// Fixed exception problems (TS)
//
// Revision 1.6  1998/08/11 14:46:33  cvsusers
// *.java
//
// Revision 1.5  1998/05/05 21:15:24  schaeck
// Performance tuning
//
// Revision 1.4  1998/04/27 11:38:49  schaeck
// Additiona functions for Bean support
//
// Revision 1.3  1998/04/14 17:11:23  schaeck
// Removed TLVException
//
// Revision 1.2  1998/04/09 12:08:02  schaeck
// *.java
//
// Revision 1.1  1998/03/19 21:55:11  schaeck
// Initial version
//
