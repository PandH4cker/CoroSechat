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

package com.ibm.opencard.buffer;

import opencard.core.util.HexString;
import opencard.opt.util.Tag;
import opencard.opt.util.TLV;
import com.ibm.opencard.util.ByteArray;

import com.ibm.opencard.util.SCTTag;

/******************************************************************************
* TLVBuffer objects are used to store a sequence of ASN.1 BER encoded
* TLVs.<br>
* A VariableTLVBuffer object has a certain capacity which is determined by the
* size of the byte array provided to the constructor. If this capacity is
* exceeded by adding new entries, the capacity is increased by reallocation of
* memory. If you want to avoid reallocation of memory for performance reasons,
* you should provided a byte array of sufficient size to the constructor.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: TLVBuffer.java,v 1.5 1998/08/11 14:40:41 cvsusers Exp $
*
* @see Request
* @see Buffer
******************************************************************************/
public class TLVBuffer extends Buffer
{
  /****************************************************************************
  * Creates a new Buffer from a given byte arra and size.
  *
  * @param data A byte array containing data
  * @param size The size of valid data contained in the data array. If this
  *             size is bigger than the actual length of the byte array,
  *             it is set to the length of the byte array.
  ****************************************************************************/
  public TLVBuffer(byte[] data, int size)
  {
    super(data, size);
  }

  /****************************************************************************
  * Clears this buffer.
  ****************************************************************************/
  public void clear()
  {
    this.size = 0;
  }

  /****************************************************************************
  * Add a TLV.
  * @param tlv   The TLV to be added.
  * @return      This VariableTLVBuffer object
  ****************************************************************************/
  public TLVBuffer addTLV(TLV tlv)
  {
    return append(tlv.toBinary());
  }

  /****************************************************************************
  * Find the next TLV with the given tag.
  * @param  tag   The TLV to be added.
  * @return       The TLV with the given tag on success, null otherwise.
  ****************************************************************************/
  public TLV findTLV(Tag tag, int[] off)
  {
    int[] start = {0};
    int[] offset = (off != null) ? off : start;
    TLV tlv = new TLV();
    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, (TLV) null);
      if (tlv.tag().equals(tag)) {
        return tlv;
      }
    }
    return null;
  }

  /****************************************************************************
  * Return the textual representation of this TLV buffer.
  * @returns string containing textual representation of the TLV buffer.
  ****************************************************************************/
  public String toString()
  {
    String s = new String();
    int offset[] = { 0 };
    TLV tlv = new TLV();
    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, (TLV) null);
      s = s + tlv.toString() + "\n";
    }
    return s;
  }

  /****************************************************************************
  * Add an entry with given id, which contains the given positive integer value
  * to this buffer. An 'entry' is a TLV looking like this:
  * </pre>
  *   DATA_ENTRY
  *   (
  *     ID( id )
  *     DATA( value )
  *   )
  * </pre>
  *
  * @param id    The ID for the new entry.
  * @param value The positive integer to be stored in the new entry.
  *
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer addValueEntry(String id, int value)
  {
    int    i       = 0;
    byte[] oldData = null;
    byte[] b       = null;

    removeEntry(id);                   // remove old entry if present

    // Find out how many bytes we need.
    if      (value < 0x100)     b = new byte[1];
    else if (value < 0x10000)   b = new byte[2];
    else if (value < 0x1000000) b = new byte[3];
    else                        b = new byte[4];

    // Do conversion
    for (i = b.length - 1; i >= 0; i--) {
      b[i] = (byte) (value % 0x100);
      value /= 0x100;
    }

    // Create entry and add it to this buffer.
    TLV tlv = new TLV(SCTTag.DATA_ENTRY, new TLV(SCTTag.ID, id.getBytes()));
    tlv.add(new TLV(SCTTag.DATA, b));
    addTLV(tlv);
    return this;
  }


  /****************************************************************************
  * Add an entry with given id, which contains the given data to this buffer.
  * An 'entry' is a TLV looking like this:
  * </pre>
  *   DATA_ENTRY
  *   (
  *     ID( id )
  *     DATA( value )
  *   )
  * </pre>
  *
  * @param id    The ID for the new entry.
  * @param value The data to be stored in the new entry.
  *
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer addDataEntry(String id, byte[] value)
  {
    return addDataEntry(id.getBytes(), value);
  }

  /****************************************************************************
  * Add an entry with given id, which contains the given data to this buffer.
  * An 'entry' is a TLV looking like this:
  * </pre>
  *   DATA_ENTRY
  *   (
  *     ID( id )
  *     DATA( value )
  *   )
  * </pre>
  *
  * @param id    The ID for the new entry.
  * @param value The data to be stored in the new entry.
  *
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer addDataEntry(byte[] id, byte[] value)
  {
    int    i       = 0;
    byte[] oldData = null;
    byte[] b       = null;

    removeEntry(id);                   // remove old entry if present

    // Create entry and add it to this buffer.
    TLV tlv = new TLV(SCTTag.DATA_ENTRY, new TLV(SCTTag.ID, id));
    tlv.add(new TLV(SCTTag.DATA, value));
    addTLV(tlv);
    return this;
  }

  /****************************************************************************
  * Get the value from the entry with the given id.
  * @param id    The ID to be searched for.
  * @return      The positive integer value stored under the given ID on success,
  *              -1 otherwise.
  ****************************************************************************/
  public int getValue(String id)
  {
    int i      = 0;
    int[] offset = {0};
    int value  = 0;
    TLV tlv    = new TLV();
    byte[] b   = null;

    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, (TLV) null);
      if (tlv.tag().equals(SCTTag.DATA_ENTRY)) {
        // recurse into data entry
        tlv = tlv.findTag(SCTTag.ID, null);
        if (id.equals(new String(tlv.valueAsByteArray()))) {
          tlv = tlv.findTag(SCTTag.DATA, tlv);
          b = tlv.valueAsByteArray();
          for (i = 0; i < b.length; i++) {
            value = (value << 8) | (b[i] & 0xFF);
          }
          return value;
        }
      }
    }
    return 0;
  }

  /****************************************************************************
  * Get the data from the entry with the given id.
  * @param id    The ID to be searched for.
  * @return      The data stored under the given ID on success, null otherwise.
  ****************************************************************************/
  public byte[] getData(String id)
  {
    int i      = 0;
    int[] offset = {0};
    int value  = 0;
    TLV tlv    = new TLV();
    byte[] b   = null;

    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, null);
      if (tlv.tag().equals(SCTTag.DATA_ENTRY)) {
        // recurse into data entry
        tlv = tlv.findTag(SCTTag.ID, null);
        if (id.equals(new String(tlv.valueAsByteArray()))) {
          tlv = tlv.findTag(SCTTag.DATA, tlv);
          b = tlv.valueAsByteArray();
          return b;
        }
      }
    }
    return null;
  }

  /****************************************************************************
  * Get the data from the entry with the given id.
  * @param id    The ID to be searched for.
  * @return      The data stored under the given ID on success, null otherwise.
  ****************************************************************************/
  public byte[] getData(byte[] id)
  {
    int[] offset = {0};
    int value  = 0;
    TLV tlv    = new TLV();
    byte[] b   = null;

    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, null);
      if (tlv.tag().equals(SCTTag.DATA_ENTRY)) {
        // recurse into data entry
        tlv = tlv.findTag(SCTTag.ID, null);

        if (ByteArray.equal(id, tlv.valueAsByteArray())) {
          tlv = tlv.findTag(SCTTag.DATA, tlv);
          b = tlv.valueAsByteArray();
          return b;
        }
      }
      else {
        // Change this !!!
        System.out.println("invalid tag : " + tlv.tag() + ", offset[0] = " + offset[0]);
      }
    }
    return null;
  }

  /****************************************************************************
  * Check the validity of the TLV buffer.
  ****************************************************************************/
  public boolean check()
  {
    int[] offset = {0};
    TLV tlv    = new TLV();

    while (offset[0] < size) {
      TLV.fromBinary(data, offset, tlv, null);
      if (! tlv.tag().equals(SCTTag.DATA_ENTRY)) {
        System.out.println("invalid tag : " + tlv.tag() + ", offset[0] = " + offset[0]);
        return false;
      }
    }
    return true;
  }

  /****************************************************************************
  * Remove the first entry with the given id.
  * @param id The id of the entry to be removed.
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer removeEntry(String id)
  {
    return removeEntry(id.getBytes());
  }

  /****************************************************************************
  * Remove the first entry with the given id.
  * @param id The id of the entry to be removed.
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer removeEntry(byte[] id)
  {
    int i              = 0;
    int[] offset       = {0};
    int previousOffset = 0;
    int value          = 0;
    TLV tlv            = new TLV();
    byte[] b           = null;
    boolean equal      = true;

    while (offset[0] < size) {
      previousOffset = offset[0];
      TLV.fromBinary(data, offset, tlv, null);
      if (tlv.tag().equals(SCTTag.DATA_ENTRY)) {
        // recurse into data entry
        tlv = tlv.findTag(SCTTag.ID, null);
        byte[] tlvValue = tlv.valueAsByteArray();

        if (ByteArray.equal(id, tlvValue)) {
          System.arraycopy(data, offset[0], data, previousOffset, size-offset[0]);
          size -= (offset[0] - previousOffset);
          break;
        }
      }
    }
    return this;
  }

  /****************************************************************************
  * Append the contents of a given byte arras to the contents of
  * this buffer.
  * @param b The byte array to be appended to the buffer.
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer append(byte[] b)
  {
    byte[] oldData = null;

    // Check if additional data fits, if not reallocate memory and copy
    // data from previous memory.
    if (size + b.length > data.length) {
      oldData = data;
      data    = new byte[size + b.length];
      System.arraycopy(oldData, 0, data, 0, size);
    }

    // Add the contents of byte array b to this buffer.
    System.arraycopy(b, 0, data, size, b.length);
    size += b.length;

    return this;
  }

  /****************************************************************************
  * Set the contents of this data buffer to a given byte array.
  * All previous data is lost.
  * @param b The byte array to be copied into the buffer.
  * @return       This Buffer object
  ****************************************************************************/
  public TLVBuffer set(byte[] b)
  {
    clear();
    return (TLVBuffer) append(b);
  }
}
// $Log: TLVBuffer.java,v $
// Revision 1.5  1998/08/11 14:40:41  cvsusers
// *.java
//
// Revision 1.4  1998/05/05 21:20:22  schaeck
// Performance tuning
//
// Revision 1.3  1998/04/14 17:03:54  schaeck
// Removed TLVException
//
// Revision 1.2  1998/03/25 20:01:44  schaeck
// changed findTLV: offset array can now be null to search from beginning
//
// Revision 1.1  1998/03/19 21:53:52  schaeck
// Initial version
//
