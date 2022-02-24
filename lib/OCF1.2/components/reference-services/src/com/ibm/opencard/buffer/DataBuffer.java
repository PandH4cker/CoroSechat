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
* A DataBuffer is supposed to hold arbitrary data without a special
* structure.<br>
* A VariableTLVBuffer object has a certain capacity which is determined by the
* size of the byte array provided to the constructor. If this capacity is
* exceeded by adding new entries, the capacity is increased by reallocation of
* memory. If you want to avoid reallocation of memory for performance reasons,
* you should provided a byte array of sufficient size to the constructor.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: DataBuffer.java,v 1.2 1998/05/05 21:20:21 schaeck Exp $
*
* @see Request
* @see Buffer
* @see TLVBuffer
* @see VariableTLVBuffer
******************************************************************************/
public class DataBuffer extends Buffer
{
  /****************************************************************************
  * This constructor creates a new Buffer from the given values.
  * @param data A byte array containing data
  * @param size The size of valid data contained in the data array. If this
  *             size is bigger than the actual length of the byte array,
  *             it is set to the length of the byte array.
  ****************************************************************************/
  public DataBuffer(byte[] data, int size)
  {
    super(data, size);
  }

  /****************************************************************************
  * This method sets the size of the data in the buffer.
  * @param newSize - the new buffer size.
  * @return This buffer's size.
  ****************************************************************************/
  public void setSize(int newSize)
  {
    size = (newSize > data.length) ? data.length : newSize;
  }

  /****************************************************************************
  * This method concatenates the contents of a given Buffer to the contents of
  * this buffer.
  * @param buffer The buffer to be concatenated to the buffer.
  * @return       This Buffer object
  ****************************************************************************/
  protected Buffer concatenate(Buffer buffer)
  {
    byte[] oldData = null;
    // Check if additional data fits, if not reallocate memory and copy data from previous memory.
    if (size + buffer.size > data.length) {
      oldData = data;
      data    = new byte[size + buffer.size];
      System.arraycopy(oldData, 0, data, 0, size);
    }
    // Concatenate the contents of the given buffer to the contents of this buffer.
    System.arraycopy(buffer.data, 0, data, size, buffer.size);
    size += buffer.size;
    return this;
  }

  /****************************************************************************
  * Set the contents of this data buffer to a given byte array.
  * All previous data is lost.
  * @param b The byte array to be copied into the buffer.
  * @return       This Buffer object
  ****************************************************************************/
  public DataBuffer set(byte[] b)
  {
    clear();
    return append(b);
  }

  /****************************************************************************
  * Append the contents of a given byte arras to the contents of
  * this buffer.
  * @param b The byte array to be appended to the buffer.
  * @return       This Buffer object
  ****************************************************************************/
  public DataBuffer append(byte[] b)
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

}
// $Log: DataBuffer.java,v $
// Revision 1.2  1998/05/05 21:20:21  schaeck
// Performance tuning
//
// Revision 1.1  1998/03/19 21:53:52  schaeck
// Initial version
//
