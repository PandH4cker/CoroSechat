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

package com.ibm.opencard.access;

import opencard.core.service.CardServiceInvalidParameterException;
import com.ibm.opencard.service.AccessInformation;
import com.ibm.opencard.service.AccessConditions;


/*
 * Maintains access conditions for the command classes of MFC smartcards.
 * Each command supported by a smartcard has a <i>command class</i> which
 * indicates the behavior of the command. Typical command classes are
 * <i>Read</i> for non-modifying access, and <i>Update</i> for modifying
 * access to a smartcard object. Smartcard objects, like files or keys,
 * define <i>access conditions</i> for each command class. These access
 * conditions have to be satisfied in order to execute a command of that
 * class that refers to the object. The access conditions for all command
 * classes are typically returned by the smartcard as the result of a
 * <i>SELECT</i> command.
 * <br>
 * The class <tt>MFCAccessConditions</tt> represents the access conditions
 * for a single command class. This class, <tt>MFCAccessInformation</tt>,
 * represents the full set of access conditions defined by a smartcard
 * object. The access conditions for each of the command classes can be
 * queried here.
 * <br>
 * For example, data may have to be read from a file. First, the file will
 * be selected, and an instance of <tt>MFCAccessInformation</tt> can be
 * created using the data returned by the smartcard. Then, the actual access
 * conditions for the command class <i>Read</i> are queried, and returned as
 * an instance of <tt>MFCAccessConditions</tt>. This instance can the be
 * queried to determine whether, for example, card holder verification (CHV)
 * is required to execute the read command.
 *
 *
 * @version $Id: MFCAccessInformation.java,v 1.12 1998/12/18 14:51:14 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCAccessConditions
 */
public class MFCAccessInformation extends AccessInformation
{
  /*
   * Symbolic constants for the various command classes
   * distinguished by MFC cards. They can be used as indices
   * for array access.
   *
   * DO NOT CHANGE the order or indices of the command classes.
   * They reflect the order in which the information is encoded
   * on MFC smartcards. The class <tt>MFC35AccessParser</tt> holds
   * an array named <tt>encoding_table</tt> that is initialized
   * in the same order. If it turns out to be necessary that this
   * order is changed, make sure to update the array there.
   */

  /** Symbolic constant for command class UPDATE. */
  public static final int CMD_CLS_UPDATE       = 0;

  /** Symbolic constant for command class READ. */
  public static final int CMD_CLS_READ         = 1;

  /** Symbolic constant for command class CREATE. */
  public static final int CMD_CLS_CREATE       = 2;

  /** Symbolic constant for command class USE KEY */
  public static final int CMD_CLS_USE_KEY      = 2;

  /** Symbolic constant for command class DELETE. */
  public static final int CMD_CLS_DELETE       = 3;

  /** Symbolic constant for command class IMPORT KEY */
  public static final int CMD_CLS_IMPORT_KEY   = 3;

  /** Symbolic constant for command class INVALIDATE. */
  public static final int CMD_CLS_INVALIDATE   = 4;

  /** Symbolic constant for command class REHABILITATE. */
  public static final int CMD_CLS_REHABILITATE = 5;


  /**
   * Symbolic constant for the number of command classes.
   * If new cards with more command classes have to be supported,
   * remove the <tt>static</tt> attribute and the initializer.
   * Initialize in the constructors, and add a constructor with
   * an argument that specifies the number of command classes.
   */
  protected static final int NO_OF_COMMAND_CLASSES = 6;


  /*
   * The access conditions for the various command classes.
   * This array holds the access conditions that have been evaluated.
   * Evaluation can be done on demand, in which case the array holds
   * <tt>null</tt> for a command classes for which the access conditions
   * have not yet been evaluated.
   *
   * @see #getAccessConditions
   */
  //protected MFCAccessConditions[]  access_conditions = null;


  // The following three attributes are needed for lazy evaluation only.

  /*
   * The access conditions for all command classes, still encoded.
   */
  //protected byte[]  encoded_conditions = null;

  /*
   * The offset of the access information in <tt>encoded_conditions</tt>.
   * In the standard file header, the access information is encoded in the
   * bytes 7 to 11. When the full file header is passed to the constructor
   * for lazy evaluation, the offset should therefore be 7. Specifying the
   * offset explicitly, however, allows to copy the relevant five bytes to
   * an own array, which can be passed with offset 0.
   *
   * @see encoded_conditions
   */
  //protected int     encoded_offset = 0;

  /**
   * The parser that decodes the encoded access conditions.
   *
   * @see #encoded_conditions
   */
  protected MFCLazyAccessParser access_parser = null;



  // construction /////////////////////////////////////////////////////////////


  /*
   * Creates new access information for MFC 4.1 and below.
   * All access conditions are initialized to ALWAYS. The actual information
   * has to be provided after creation. This can be done by getting the access
   * conditions and restricting them to the appropriate value. Since the
   * methods to restrict access conditions have default visibility, they can
   * be invoked only from within this package.
   * <br>
   * This constructor has default visibility (package) since it is
   * assumed that there is a parser somewhere in this package which
   * interprets status information received from a smartcard and
   * creates the corresponding access information. The access information
   * is not meant to be changed anywhere else.
   *
   * @see #getAccessConditions
   * @see MFCAccessConditions
   */
  /*
  MFCAccessInformation()
  {
    access_conditions = new MFCAccessConditions [NO_OF_COMMAND_CLASSES];

    for(int i=0; i < NO_OF_COMMAND_CLASSES; i++)
      access_conditions[i] = new MFCAccessConditions();
  }
  */

  /**
   * Creates new access information for MFC smartcards, using lazy evaluation.
   * Unlike the default constructor, which initializes access conditions for
   * all command classes which have to be initialized on construction, this
   * constructor expects <i>encoded</i> access information and a parser which
   * will be used to extract the access conditions for specific command classes
   * on demand.
   *
   * @param data    the access information, as encoded on the smartcard
   * @param offset  index of the first byte of the access information
   * @param parser  an object that knows how to decode access conditions
   */
  MFCAccessInformation(byte[] data, int offset, MFCLazyAccessParser parser)
  {
    super(data, offset, NO_OF_COMMAND_CLASSES);
    //access_conditions  = new MFCAccessConditions [NO_OF_COMMAND_CLASSES];
    //encoded_conditions = data;
    //encoded_offset     = offset;
    access_parser      = parser;
  }



  // access ///////////////////////////////////////////////////////////////////


  /*
   * Get the access conditions for a command class.
   *
   * @param cmdclass  the command class referred to
   * @return          the access conditions for that command class
   *
   * @exception CardServiceInvalidParameterException
   *   if an illegal command class is passed as an argument
   */
  /*
  public MFCAccessConditions getAccessConditions(int cmdclass)
	   throws CardServiceInvalidParameterException
  {
    if ((cmdclass < 0) || (cmdclass >= access_conditions.length))
      throw new CardServiceInvalidParameterException
        ("Illegal command class " + cmdclass);

    MFCAccessConditions accond = access_conditions[cmdclass];

    if (accond == null)  // if lazy evaluation and not yet decoded
      {
        // - decode access conditions for the requested command class
        // - store them in the array of access conditions
        //
        accond = access_parser.parseAccessConditions(encoded_conditions,
                                                     encoded_offset,
                                                     cmdclass);
        access_conditions[cmdclass] = accond;
      }

    return accond;

  } // getAccessConditions
  */


  // service //////////////////////////////////////////////////////////////////

  protected AccessConditions parseAccessConditions(byte[] encoded,
                                                   int    offset,
                                                   int    cmdclass)
  {
    return access_parser.parseAccessConditions(encoded, offset, cmdclass);
  }


  /*
   * Returns a string representing this object.
   *
   * @return a human-readable representation of this access information
   */
  /*
  public String toString()
  {
    StringBuffer sb=new StringBuffer("access conditions: ");

    for (int i=encoded_offset; i < encoded_offset + 5; i++)
      sb.append(" 0x"+ Integer.toHexString(0xff & encoded_conditions[i]));

    return sb.toString();
  }
  */

} // class MFCAccessInformation
