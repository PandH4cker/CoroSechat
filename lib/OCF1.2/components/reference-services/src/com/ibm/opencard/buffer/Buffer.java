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

/******************************************************************************
* Buffer objects are used to store data. This data may be a raw sequence of
* bytes or a sequence of BER encoded TLVs. A Buffer object has a certain
* capacity which is determined by the size of the byte array provided to the
* constructor. If this capacity is exceeded by concatenating another Buffer
* or appending additional data, the capacity is increased by reallocation
* of memory. If you want to avoid reallocation of memory for performance
* reasons, you should provided a byte array of sufficient size to the
* constructor.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: Buffer.java,v 1.2 1998/05/05 21:20:21 schaeck Exp $
*
* @see Request
******************************************************************************/
public class Buffer
{
  int size = 0;                        /** The size of valid data contained  */
  byte[] data = null;                  /** Array containing data             */

  /****************************************************************************
  * This constructor creates a new Buffer from the given values.
  *
  * @param data A byte array containing data
  * @param size The size of valid data contained in the data array. If this
  *             size is bigger than the actual length of the byte array,
  *             it is set to the length of the byte array.
  ****************************************************************************/
  public Buffer(byte[] data, int size)
  {
    if (size <= data.length) {
      this.size = size;
      this.data = data;
    }
    else {
      this.size = data.length;
    }
  }

  /****************************************************************************
  * This method returns this buffer's size.
  * @return This buffer's size.
  ****************************************************************************/
  public int size()
  {
    return size;
  }

  /****************************************************************************
  * This method returns this buffer's data.
  * @return This buffer's data.
  ****************************************************************************/
  public byte[] data()
  {
    if (size < data.length) {
      byte[] temp = new byte[size];
      System.arraycopy(data, 0, temp, 0, temp.length);
      return temp;
    } else {
      return data;
    }
  }

  /****************************************************************************
  * This method clears this buffer.
  ****************************************************************************/
  public void clear()
  {
    this.size = 0;
  }

  /****************************************************************************
  * Converts the data in the buffer to character hex format.
  * @return      A String containing the data in hex format.
  ****************************************************************************/
  protected String toHexString()
  {
    if (size==0) return new String("<null>");
    char[] xlat = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    char[] cBuf = new char[2*size];
    for (int i=0; i<size; i++) {
      cBuf[2*i]=xlat[(data[i]>>>4)&0x0F];
      cBuf[(2*i)+1]=xlat[data[i]&0x0F];
    }
    return new String(cBuf);
  }

  /****************************************************************************
  * Converts the data in the buffer to a hexadecimal string.
  * @return      A String containing the data in hex format.
  ****************************************************************************/
  public String toString()
  {
    return toHexString();
  }
}
// $Log: Buffer.java,v $
// Revision 1.2  1998/05/05 21:20:21  schaeck
// Performance tuning
//
// Revision 1.1  1998/03/19 21:53:53  schaeck
// Initial version
//
