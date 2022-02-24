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


/**
 * A container class for IBM's scripting request codes.
 *
 * @version $Id: Code.java,v 1.4 1998/08/21 12:36:53 cvsusers Exp $
 *
 * @author Thomas Schaeck (schaeck@de.ibm.com)
 */
public abstract class Code
{
  public final static int ID                      = 0;
  public final static int SIZE                    = 1;
  public final static int OFFSET                  = 2;
  public final static int DATA                    = 3;
  public final static int DATA_ENTRY              = 4;
  public final static int ITEM_ENTRY              = 5;
  public final static int ITEM_PATH               = 6;
  public final static int ITEM_TYPE               = 7;
  public final static int ACCESS_MODE             = 8;
  public final static int RECORD_MODE             = 9;
  public final static int RECORD_NUMBER           = 10;
  public final static int CHV_NUMBER              = 11;
  public final static int CHV_DOMAIN              = 12;
  public final static int KEY_NUMBER              = 13;
  public final static int KEY_DOMAIN              = 14;
  public final static int AUTHENTICATION_DOMAIN   = 15;
  public final static int ARGUMENT                = 16;
  public final static int PARAMETER               = 17;
  public final static int RESULT                  = 18;
  public final static int STATUSWORDS             = 19;
  public final static int CARD_REQUEST            = 20;
  public final static int AGENT_REQUEST           = 21;
  public final static int BUFFER_REQUEST          = 22;
  public final static int BUFFER_ENTRY            = 23;
  public final static int BUFFER_PART             = 24;
  public final static int CONSTANT_PART           = 25;
  public final static int PLACEHOLDER_PART        = 26;
  public final static int AGENT_COMMAND           = 27;
  public final static int ITEM_ALIAS              = 28;
  public final static int ATR_HISTORICAL_DATA     = 29;
  public final static int CARD_OS                 = 30;
  public final static int ACCESS_CONDITION        = 31;
  public final static int AGENCY_REQUEST          = 32;
  public final static int ALGORITHM               = 33;
  public final static int APDU_HEADER             = 34;
  public final static int APPLICATION             = 35;
  public final static int AUTHENTICATION_REQUEST  = 36;
  public final static int CARD_LAYOUT             = 37;
  public final static int CARD_LEVEL              = 38;
  public final static int CARD_TABLE              = 39;
  public final static int CHAINING_REQUEST        = 40;
  public final static int CHV_ACCESS              = 41;
  public final static int CLOSE_LINK_REQUEST      = 42;
  public final static int COMMENT                 = 43;
  public final static int CONDITION               = 44;
  public final static int CONTEXT                 = 45;
  public final static int DECIPHER_REQUEST        = 46;
  public final static int DESCRIPTION             = 47;
  public final static int DESTINATION             = 48;
  public final static int DEVICE                  = 49;
  public final static int DICTIONARY              = 50;
  public final static int ENCIPHER_REQUEST        = 51;
  public final static int ENVIRONMENT             = 52;
  public final static int EXPORT_REQUEST          = 53;
  public final static int FUNCTION                = 54;
  public final static int FUNCTION_REQUEST        = 55;
  public final static int HANDLE                  = 56;
  public final static int IDENTIFICATION_REQUEST  = 57;
  public final static int IMPORT_REQUEST          = 58;
  public final static int INITIAL_CHAINING_VALUE  = 59;
  public final static int ITEM_DF_NAME            = 60;
  public final static int ITEM_TAG                = 61;
  public final static int KEY_ENTRY               = 62;
  public final static int KEY_REFERENCE           = 63;
  public final static int KEY_TABLE               = 64;
  public final static int MASK                    = 65;
  public final static int MATCH                   = 66;
  public final static int MESSAGE_REQUEST         = 67;
  public final static int OPEN_LINK_REQUEST       = 68;
  public final static int ORDER                   = 69;
  public final static int PATTERN                 = 70;
  public final static int PERSONALIZATION_DATA    = 71;
  public final static int PROCEDURE               = 72;
  public final static int PROCEDURE_CALL          = 73;
  public final static int REFERENCE               = 74;
  public final static int REFERENCE_ENTRY         = 75;
  public final static int REPOSITORY              = 76;
  public final static int REPOSITORY_ENTRY        = 77;
  public final static int RESOURCE                = 78;
  public final static int RETURN_CODE             = 79;
  public final static int SCOPE                   = 80;
  public final static int SCRIPT                  = 81;
  public final static int SHORT_IDENTIFIER        = 82;
  public final static int SIGNATURE               = 83;
  public final static int SIGNATURE_REQUEST       = 84;
  public final static int SOURCE                  = 85;
  public final static int TIMESTAMP               = 86;
  public final static int VALIDATION_REQUEST      = 87;
  public final static int VERSION                 = 88;
  public final static int ANSWER_TO_RESET         = 89;
  public final static int SERVER                  = 90;
  public final static int BUFFER_DELETE           = 91;
  public final static int PAD_BYTE                = 92;
  public final static int PAD_SIZE                = 93;
  public final static int PADDING                 = 94;
  public final static int KEY_TYPE                = 95;
  public final static int KEY_OFFSET              = 96;
  public final static int MODULE                  = 97;
  public final static int BUFFER_SIZE             = 98;
  public final static int ITEM_ORGANIZATION       = 99;
  public final static int USER                    = 1024;

  private final static String[] strings = {
    "ID",
    "SIZE",
    "OFFSET",
    "DATA",
    "DATA_ENTRY",
    "ITEM_ENTRY",
    "ITEM_PATH",
    "ITEM_TYPE",
    "ACCESS_MODE",
    "RECORD_MODE",
    "RECORD_NUMBER",
    "CHV_NUMBER",
    "CHV_DOMAIN",
    "KEY_NUMBER",
    "KEY_DOMAIN",
    "AUTHENTICATION_DOMAIN",
    "ARGUMENT",
    "PARAMETER",
    "RESULT",
    "STATUSWORDS",
    "CARD_REQUEST",
    "AGENT_REQUEST",
    "BUFFER_REQUEST",
    "BUFFER_ENTRY",
    "BUFFER_PART",
    "CONSTANT_PART",
    "PLACEHOLDER_PART",
    "AGENT_COMMAND",
    "ITEM_ALIAS",
    "ATR_HISTORICAL_DATA",
    "CARD_OS",
    "ACCESS_CONDITION",
    "AGENCY_REQUEST",
    "ALGORITHM",
    "APDU_HEADER",
    "APPLICATION",
    "AUTHENTICATION_REQUEST",
    "CARD_LAYOUT",
    "CARD_LEVEL",
    "CARD_TABLE",
    "CHAINING_REQUEST",
    "CHV_ACCESS",
    "CLOSE_LINK_REQUEST",
    "COMMENT",
    "CONDITION",
    "CONTEXT",
    "DECIPHER_REQUEST",
    "DESCRIPTION",
    "DESTINATION ",
    "DEVICE",
    "DICTIONARY",
    "ENCIPHER_REQUEST",
    "ENVIRONMENT",
    "EXPORT_REQUEST",
    "FUNCTION",
    "FUNCTION_REQUEST",
    "HANDLE",
    "IDENTIFICATION_REQUEST",
    "IMPORT_REQUEST",
    "INITIAL_CHAINING_VALUE",
    "ITEM_DF_NAME",
    "ITEM_TAG",
    "KEY_ENTRY",
    "KEY_REFERENCE",
    "KEY_TABLE",
    "MASK",
    "MATCH",
    "MESSAGE_REQUEST",
    "OPEN_LINK_REQUEST",
    "ORDER",
    "PATTERN",
    "PERSONALIZATION_DATA",
    "PROCEDURE",
    "PROCEDURE_CALL",
    "REFERENCE",
    "REFERENCE_ENTRY",
    "REPOSITORY",
    "REPOSITORY_ENTRY",
    "RESOURCE",
    "RETURN_CODE",
    "SCOPE",
    "SCRIPT",
    "SHORT_IDENTIFIER",
    "SIGNATURE",
    "SIGNATURE_REQUEST",
    "SOURCE",
    "TIMESTAMP",
    "VALIDATION_REQUEST",
    "VERSION",
    "ANSWER_TO_RESET",
    "SERVER",
    "BUFFER_DELETE",
    "PAD_BYTE",
    "PAD_SIZE",
    "PADDING",
    "KEY_TYPE",
    "KEY_OFFSET",
    "MODULE",
    "BUFFER_SIZE",
    "ITEM_ORGANIZATION"
  };

  /** Disabled default constructor. */
  private Code() {}


  /**
   * Converts a request code into a request name.
   *
   * @param code        the request code to stringify
   * @return            a string holding the request name
   */
  public static String toString(int code)
  {
    if (code < strings.length)
      return strings[code];
    else
      return Integer.toString(code);
  }

} // class Code
