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

package com.ibm.opencard.script;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

import com.ibm.opencard.buffer.TLVBuffer;

/*******************************************************************************
* A Script is a set of procedures to be executed by an IBM CardAgent.
* It does contain card commands, but the CardAgent is interpreting and
* modifying the data before sending them to the CardTerminal.
*
* AgentScript is the abstract base class of all scripts.
* Every individual concrete script is a distinct subclass of AgentScript.
*
* Instances are not created.  Rather the class has class methods only.
*
* @author Frank Seliger (seliger@de.ibm.com)
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: Script.java,v 1.3 1998/04/09 12:58:16 schaeck Exp $
*******************************************************************************/
public abstract class Script
{
  /*****************************************************************************
  * The Cardagent operates on the entire script buffer.  It does curently not
  * care to access parts or procedures of the script in any other way than
  * reading the entire buffer itself
  *****************************************************************************/
  public abstract TLVBuffer buffer();

  /*****************************************************************************
  * Load the contents of the given file into a TLV buffer and return this buffer.
  * @exception java.io.IOException
  *            The file from which the script should be restored could not be
  *            read.
  *****************************************************************************/
  protected static TLVBuffer restore(String fileSpec)  throws IOException
  {
    File inFile = new File(fileSpec);
    if (!inFile.exists() || !inFile.isFile())
        throw new IOException("no such source file: " + fileSpec);
    if (!inFile.canRead())
        throw new IOException("source file " +
                        "is unreadable: " + fileSpec);

    RandomAccessFile in = new RandomAccessFile(inFile, "r");

    int size = (int)in.length();
    byte[] fileContents = new byte[size];
    in.read(fileContents);
    return new TLVBuffer(fileContents, size);
  }
}
