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

package com.ibm.opencard.handler;

import com.ibm.opencard.buffer.DataBuffer;
import com.ibm.opencard.buffer.TLVBuffer;

/******************************************************************************
* Objects of this class are used for communication between agents
* agency and handlers. When a Request is constructed, an ID, an argument
* buffer and a parameter buffer are assigned. The ID may changed during
* the lifetime of a request object. The buffers are supposed to be filled
* and cleared if required.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: Request.java,v 1.2 1998/03/25 19:51:47 schaeck Exp $
*
* @see Buffer
******************************************************************************/
public class Request
{
  /** Indicates that no more requests need to be handled */
  public final static int OK             =  0;
  /** request for execution of an agent command */
  public final static int AGENT          =  1;
  /** request to perform a buffer operation in the blackboard */
  public final static int BUFFER         =  2;
  /** request for sending a command to the card */
  public final static int CARD           =  3;
  /** request to create an initial chaining value */
  public final static int CHAINING       =  4;
  /** request to decipher data */
  public final static int DECIPHER       =  5;
  /** request to encipher data */
  public final static int ENCIPHER       =  6;
  /** request used to export data from script to application */
  public final static int EXPORT         =  7;
  /** request for user identification, e.g. PIN */
  public final static int IDENTIFICATION =  8;
  /** request used to import data from application to script */
  public final static int IMPORT         =  9;
  /** request to generate a digital signature (DES or RSA) */
  public final static int SIGNATURE      = 10;
  /** request to validate a digital signature (DES or RSA) */
  public final static int VALIDATION     = 11;
  /** request to start a new agency */
  public final static int AGENCY         = 36;
  /** request to perform a generic function, for extendibility */
  public final static int FUNCTION       = 37;
  /** request to send a message to a remote agency */
  public final static int MESSAGE        = 38;
  /** request to perform an internal authentication of the card */
  public final static int AUTHENTICATION = 39;
  /** request to open a connection to a server */
  public final static int OPEN_LINK      = 40;
  /** request to close connection to a server */
  public final static int CLOSE_LINK     = 41;
  /** undefined request */
  public final static int UNDEFINED      = 10000;

  /** request type */
  private int        id;
  /** request argument */
  private DataBuffer argument;
  /** request parameter */
  private TLVBuffer  parameter;

  /****************************************************************************
  * Create a request from the given arguments.
  *
  * @param id        The request id
  * @param argument  The argument buffer
  * @param parameter The parameter buffer
  * @return <TT>true</TT> if the request has been handled,
  *         <TT>false</TT> otherwise
  ****************************************************************************/
  public Request(int id, DataBuffer argument, TLVBuffer parameter)
  {
    this.id        = id;
    this.argument  = argument;
    this.parameter = parameter;
  }

  /****************************************************************************
  * Return the ID of this buffer.
  * @return This request's ID.
  ****************************************************************************/
  public int id()
  {
    return id;
  }

  /****************************************************************************
  * Set the ID of this buffer to the given value.
  * @param The new ID.
  ****************************************************************************/
  public void setID(int id)
  {
    this.id = id;
  }

  /****************************************************************************
  * Return the argument buffer of this request.
  * @return This request's argument buffer
  ****************************************************************************/
  public DataBuffer argument()
  {
    return argument;
  }

  /****************************************************************************
  * Return the parameter buffer of this request.
  * @return This request's parameter buffer
  ****************************************************************************/
  public TLVBuffer parameter()
  {
    return parameter;
  }

  /****************************************************************************
  * Set the argument buffer of this request.
  * @param arg The new argument buffer
  ****************************************************************************/
  public void argument(DataBuffer arg)
  {
    this.argument = arg;
  }

  /****************************************************************************
  * Return the parameter buffer of this request.
  * @param param The new parameter buffer
  ****************************************************************************/
  public void parameter(TLVBuffer param)
  {
    this.parameter = param;
  }

  /****************************************************************************
  * Return this request as a string.
  * @return This request's parameter buffer
  ****************************************************************************/
  public String toString()
  {
    String s = null;

    switch(id)
    {
      case Request.OK:             s = "OK";             break;
      case Request.AGENT:          s = "AGENT";          break;
      case Request.BUFFER:         s = "BUFFER";         break;
      case Request.CARD:           s = "CARD";           break;
      case Request.CHAINING:       s = "CHAINING";       break;
      case Request.DECIPHER:       s = "DECIPHER";       break;
      case Request.ENCIPHER:       s = "ENCIPHER";       break;
      case Request.EXPORT:         s = "EXPORT";         break;
      case Request.IDENTIFICATION: s = "IDENTIFICATION"; break;
      case Request.IMPORT:         s = "IMPORT";         break;
      case Request.SIGNATURE:      s = "SIGNATURE";      break;
      case Request.VALIDATION:     s = "VALIDATION";     break;
      case Request.AGENCY:         s = "AGENCY";         break;
      case Request.FUNCTION:       s = "FUNCTION";       break;
      case Request.MESSAGE:        s = "MESSAGE";        break;
      case Request.AUTHENTICATION: s = "AUTHENTICATION"; break;
      case Request.OPEN_LINK:      s = "OPEN_LINK";      break;
      case Request.CLOSE_LINK:     s = "CLOSE_LINK";     break;
      case Request.UNDEFINED:      s = "UNDEFINED";      break;
      default:                     s = "UNKNOWN " + id;  break;
    }
    return "Request("+ s +"," + ((argument != null)  ? this.argument.toString()  : "")
                         +"," + ((parameter != null) ? this.parameter.toString() : "") +")";
  }
}
// $Log: Request.java,v $
// Revision 1.2  1998/03/25 19:51:47  schaeck
// now checks argument and parameter for null references before printing
//
// Revision 1.1  1998/03/19 22:44:40  schaeck
// Initial version
//
// Revision 1.1  1998/03/19 21:51:53  schaeck
// *.java
//
