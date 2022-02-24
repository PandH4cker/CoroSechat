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


/**
 * Provides parameterization of APDUs for MFC smartcards.
 * The majority of the methods is used to get the CLAss and INStruction
 * bytes fopr specific commands. Some other methods specify minor details
 * that are important in special cases, for example of a command does
 * neither include command data nor expect response data. Additionally,
 * there are methods that check the status word of response APDUs.
 *
 * @version $Id: MFCCodes.java,v 1.15 1999/01/18 13:29:11 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface MFCCodes
{
  /**
   * The master file ID. It is identical for all MFC cards,
   * and it conforms to the ISO standard. There is no reason
   * to expect it to change, so it is an attribute rather
   * than a method.
   */
  public final static short MASTER_FILE = 0x3f00;


  /**
   * The maximum block size for data transfers.
   *
   * @return  the block size for splitting read and write commands
   */
  public int getBlockSize();


  /**
   * Whether a command data length of zero is explicit.
   * The commands to invalidate or rehabilitate a file on the smartcard
   * neither provide nor request data. With the MFC 4.1, the command APDU
   * will consist of only 4 bytes, while previous MFC cards require an
   * explicit Lc byte with value 0.
   *
   * @return <tt>true</tt> iff a zero Lc byte has to be part of the APDU
   */
  public boolean needsZeroLc();


  /**
   * Whether an expected response data length of zero is explicit.
   * The commands to increase or decrease the value of a record in a
   * structured file return the contents of the record and the amount
   * that was sent with the command. For MFC 3.5 and 4.0, no expected
   * length byte (Le) has to be provided. With the MFC 4.1, this byte
   * is required but can be set to 0, so the card will send as many
   * bytes as there are to send.
   * <br>
   * This method is also used to build the command APDU for reading a
   * record. In the case of the MFC 4.1, the expected length must be
   * set to zero to avoid trouble with secure messaging.
   * <br>
   * Finally, there is the select command. The MFC 3.5 and 4.0 do not
   * allow an Le byte for select, the MFC 4.1 does. The german GeldKarte
   * even requires the Le byte, at least in some cases. See also
   * <tt>needsZeroLe(boolean)</tt>.
   * <br>
   * This method could have been named <tt>isZeroLeSufficient</tt> or
   * something like that, but since it corresponds to <tt>needsZeroLc</tt>,
   * a corresponding name has been chosen, too.
   *
   * @return <tt>true</tt> iff Le has to be specified for increase or decrease
   *
   * @see #needsZeroLc
   * @see #needsZeroLe(boolean)
   */
  public boolean needsZeroLe();


  /**
   * Whether an expected explicit zero Le is specified for select.
   * With the german GeldKarte, a zero Le must be given only if some
   * bytes are expected at all. This method deals with this special
   * case.
   *
   * @param info   whether a response is expected at all
   * @return   <tt>true</tt> if a zero Le has to be appended,
   *           <tt>false</tt> otherwise
   *
   * @see #needsZeroLe()
   */
  public boolean needsZeroLe(boolean info);


  /* Here are some operations defined for smartcards: */

  public final static int OP_SELECT             = 100;
  public final static int OP_GET_RANDOM         = 101;
  public final static int OP_PUT_RANDOM         = 102;
  public final static int OP_VERIFY_CHV         = 103;
  public final static int OP_EXTERNAL_AUTH      = 104;
  public final static int OP_INTERNAL_AUTH      = 105;
  public final static int NOOP_BASIC_END        = 106;  // no more

  public final static int OP_READ_BINARY        = 200;
  public final static int OP_READ_RECORD        = 201;
  public final static int OP_UPDATE_BINARY      = 202;
  public final static int OP_UPDATE_RECORD      = 203;
  public final static int OP_APPEND_RECORD      = 204;
  public final static int OP_CREATE_FILE        = 205;
  public final static int OP_DELETE_FILE        = 206;
  public final static int OP_INVALIDATE         = 207;
  public final static int OP_REHABILITATE       = 208;
  public final static int OP_INCREASE           = 209;
  public final static int OP_DECREASE           = 210;
  public final static int NOOP_FILE_END         = 211; // no more


  /**
   * Returns the instruction byte for a given operation.
   *
   * @param operation   a number indicating the operation
   * @return            the instruction byte for the operation
   */
  public byte getINS(int operation);

  /**
   * Returns the access group for a given operation.
   * Commands are grouped according to the access type, for example
   * READ or UPDATE. For different cards, different numbers of
   * these groups, and different groups for the same commands
   * may be defined. This method returns the group number for
   * a given command, which can be used to obtain the access
   * conditions from the appropriate access information.
   *
   * @param operation   a number indicating the operation
   * @return            the access group for that operation,
   *                    or -1 if no access conditions have to be satisfied
   *                    or if the operation is unknown
   */
  public int getAG(int operation);


  /**
   * The CLAss byte for ISO commands.
   * Up to and including MFC 4.0, all standard commands share the same
   * command class. MFC 4.1 distinguishes between ISO and non-ISO commands,
   * for example UPDATE BINARY and CREATE. This method returns the class
   * for the standard commands defined by ISO 7816-4.
   *
   * @return  the CLAss of ISO standard INStructions
   */
  public byte getISOClassByte();

  /**
   * The CLAss byte for non-ISO commands.
   *
   * @return  the CLAss of non-ISO standard INStructions
   */
  public byte getClassByte();

  //@@@ To keep the GeldKarte stuff compilable
  public byte getAskRandomByte();
  public byte getReadRecordByte();


  /**
   * Analyses the status of a smartcard's response.
   * If the status indicates successful completion of a command, the
   * method returns without further action. If the status indicates
   * an error, an exception is thrown. The instruction byte of the
   * command that was responded is given as second parameter, to allow
   * context-sensitive error checking.
   * <br>
   * This method should be consistent with <tt>indicatesError</tt>.
   * The easiest way to achieve this is to check the status using
   * <tt>indicatesError</tt> and to return immeadiately if <tt>false</tt>
   * is returned. If an error is indicated, an exception should be
   * thrown, even if no specific error message can be generated.
   *
   * @param status   the status word returned by the smartcard.
   *                 It is passed as an <tt>int</tt> to conform to the
   *                 method <tt>sw</tt> in class <tt>ResponseAPDU</tt>.
   * @param command  the INStruction byte that was sent to the smartcard.
   *                 It can be used to determine a context-sensitive
   *                 exception message.
   * @param context  an object specifying the context in which the response
   *                 was received. This parameter is optional and may get
   *                 evaluated to determine an appropriate exception message.
   *                 For example, if a select command was issued, the context
   *                 would be the path to the selected card file.
   *
   * @exception CardServiceException   the status indicates an error
   *
   * @see opencard.core.terminal.ResponseAPDU#sw
   */
  public void analyseStatus(int status, byte command, Object context)
       throws CardServiceException; //@@@ OpenCardIOException


  /**
   * Checks whether a status word indicates an error.
   * Unlike <tt>analyseStatus</tt>, this method does never throw an
   * exception. There is also no context-sensitivity involved.
   *
   * @param status   the status word to check. It is passed as an <tt>int</tt>
   *                 rather than a short, to conformto the method <tt>sw</tt>
   *                 in class <tt>ResponseAPDU</tt>.
   *
   * @return    <tt>true</tt> if the status indicates an error,
   *            <tt>false</tt> otherwise
   *
   * @see #analyseStatus
   * @see opencard.core.terminal.ResponseAPDU#sw
   */
  public boolean indicatesError(int status);


} // interface MFCCodes
