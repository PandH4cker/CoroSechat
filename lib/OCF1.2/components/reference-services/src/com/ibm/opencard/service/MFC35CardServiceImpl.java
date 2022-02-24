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

import java.util.Enumeration;

import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CardChannel;
import opencard.core.service.CardServiceException; //@@@ should be avoided
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.util.Tracer;

import opencard.opt.service.CardServiceObjectNotAvailableException;
import opencard.opt.service.CardServiceUnexpectedResponseException;

import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.CardFileFileID;
import opencard.opt.iso.fs.CardFileShortFileID;
//import opencard.opt.iso.fs.CommandAPDUPath;


/**
 * Implementation of generic card services for MFC 3.5 and some above.
 * This class is stateless, one object can be used by as many card services
 * as required, and for as many smartcards as required.
 *
 * @version $Id: MFC35CardServiceImpl.java,v 1.27 1999/04/13 14:20:10 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFC35CardServiceImpl implements MFCCardServiceImpl
{
  /* The tracer for debugging output. */
  private final static Tracer ctracer = new Tracer(MFC35CardServiceImpl.class);


  /** The class and instruction codes for the MFC commands. */
  /*final*/ protected MFCCodes mfc_codes;

  /*
   * The following attributes define the P1 and P2 parameter bytes
   * for the select command. They can be considered 'final' for MFC
   * smartcards. For other smartcards, like the GeldKarte, they can
   * be assigned with different values in the constructor of a derived
   * class.
   */

  /** The P1 byte for selection by a single file ID. */
  protected byte p1_file_id = (byte) 0x00;

  /** The P1 byte for selection by an absolute path. */
  protected byte p1_path_abs = (byte) 0x08;

  /** The P1 byte for selection by a relative path. */
  protected byte p1_path_rel = (byte) 0x09;


  /** The P2 byte for a select that returns the file header. */
  protected byte p2_select_info = (byte) 0x00;

  /** The P2 byte for a select that does not return anything. */
  protected byte p2_select_quiet = (byte) 0x0c;


  /*
   * Below, you will find some attributes that hold
   * temporary values. This class is 'stateless' in
   * the sense that a call to a public method does
   * not depend on any other call to a public method.
   */

  // construction /////////////////////////////////////////////////////////////

  /**
   * Creates a new generic card service implementation for MFC 3.5 and above.
   * This implementation is parameterized by the command codes, so it can
   * be used for MFC cards other than 3.5.
   *
   * @param codes   the CLAss and INStruction codes
   */
  public MFC35CardServiceImpl(MFCCodes codes)
  {
    mfc_codes = codes;
  }


  // service //////////////////////////////////////////////////////////////////

  private static final Integer valid_tag = new Integer(0);
  /**
   * Determines the currently selected path.
   * The path stored in the channel state may be invalid, if another
   * service has performed a SELECT operation. This is checked via
   * the state object that can be found in the channel itself. This
   * service stores a tag object there. If another service changes
   * the channel state, this will be detected. The other service's
   * state will be overwritten by the tag object again.
   * This mechanism will not detect state invalidations by services
   * like <tt>opencard.opt.util.PassThruCardService</tt>, that do
   * not store a state.
   *
   * @param state       the state from which to take the path
   * @param channel     the channel to check for <i>dirty</i> indication
   *
   * @return    the path that is currently selected,
   *            or <tt>null</tt> if unknown
   *
   * @see opencard.opt.util.PassThruCardService
   */
  protected final CardFilePath determinePath(MFCChannelState state,
                                             CardChannel channel)
  {
    CardFilePath path = state.getCurrentPath();
    Object       tag  = channel.getState();

    channel.setState(valid_tag);
    if (tag != valid_tag)
      path = null;

    ctracer.info("determinePath",
                 (tag == valid_tag) ? "valid: "+path : "invalid");

    return path;

  } // determinePath


  // select support
  /*
   * Although this service implementation is stateless, there
   * are some attributes. They are needed for cooperation of
   * some of the methods. Their contents is temporary. It is
   * needed between a call to initSelect() and the following
   * call to doneSelect().
   *
   * @@@optimization:
   * These attributes could be moved to an inner class. An object
   * of this class would be passed between the different methods.
   * The select method could then run without synchronization,
   * provided the command APDU is also stored there.
   */
  protected short[]      path_component = null;
  protected int          path_length    = 0;
  protected boolean      path_absolute  = false;

  protected boolean      path_error     = false;
  protected boolean      info_required  = false;
  protected ResponseAPDU last_response  = null;


  /**
   * A re-usable command APDU for select.
   * It gets initialized on <tt>initSelect</tt>,
   * is used by <tt>execSelect</tt>, and must be
   * reset to length 2 immediately after using it.
   *
   * @see #initSelect
   * @see #execSelect
   */
  protected CommandAPDU select_apdu  = null;


  /**
   * Clear the path stored.
   */
  final protected void clearPath()
  {
    path_length   = 0;
    path_absolute = false;
  }


  /**
   * Initialize a select operation.
   * A call to this method will be followed by one or more calls
   * to <tt>nextSelect</tt> for the path components, and at last
   * a call to <tt>doneSelect</tt>. The last response received
   * from the smartcard will be returned by <tt>doneSelect</tt>.
   * If <tt>info</tt> is true on invocation of this method, the
   * smartcard's response will include the header of the selected
   * file or directory, otherwise not.
   *
   * @param info  <tt>true</tt> if the file info must be evaluated
   *
   * @see #nextSelect
   * @see #doneSelect
   */
  protected void initSelect(boolean info)
  {
    // a maximum of 8 file IDs can be sent in a single select
    if (path_component == null)
      path_component = new short [8];

    clearPath();

    path_error    = false;
    info_required = info;
    last_response = null;

    if (select_apdu == null)
      {
        select_apdu = new CommandAPDU(22); // max. 16 bytes of data + Le
        select_apdu.append(mfc_codes.getISOClassByte());
        select_apdu.append(mfc_codes.getINS(MFCCodes.OP_SELECT));
      }
    else
      {
        select_apdu.setLength(2);
      }
  }


  /**
   * Add a path component to the current select operation.
   * If necessary, a command with the previously added components
   * will be sent to the smartcard. The component passed as an
   * argument will <i>never</i> be part of the select operation
   * that is performed. This ensures that a command with at least
   * one path component is sent to the smartcard on invocation of
   * <tt>doneSelect</tt>. The commands sent before can therefore
   * be quiet selects.
   * <br>
   * The MFC 3.5 and 4.0 supports selection by file ID only.
   * Short file identifiers are converted to the full file ID.
   * If other types of path components must be supported, derived
   * classes may override this method. It may be necessary to
   * override <tt>execSelect</tt>, too.
   *
   * @param chan   the contact to the smartcard
   * @param comp   the path component to add
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #doneSelect
   * @see #execSelect
   */
  protected void nextSelect(MFCCardChannel        chan,
                            CardFilePathComponent comp)
       throws CardTerminalException
  {
    short fileID;

    /*
     * Check the path component type. The MFC 3.5 supports only
     * plain file IDs. Short file identifiers can be supported
     * by this method, since they can be transformed into a plain
     * file ID.
     */
    if (comp instanceof CardFileFileID)
        fileID = (short) (((CardFileFileID)comp).toShort());
    else if (comp instanceof CardFileShortFileID)
        fileID = (short) (((CardFileShortFileID)comp).toByte() | 0xfd00);

    /* support a CommandAPDUPath*/
    /* inactivated ...
    else if (comp instanceof CommandAPDUPath) {
        // the CardFileComponents listed in the path before this
        // component have to be selected right now!
        execSelect(chan, false);

        // now build a CommandAPDU out of the Command-Sequence stored
        // in this CommandAPDUPath-CardFileComponent
        CommandAPDU apdu = new CommandAPDU (((CommandAPDUPath)comp).toByte());
        // execute the command by sending it directly to the card
        execCommand(chan, apdu);

        // reset the stack with components to be selected
        // (the following components will startup from scratch again)
        path_length=0;
        path_absolute=false;

        return; // nothing more to do for CommandAPDUPath here
        }
     ... inactivated */


    else
      throw new CardServiceInvalidParameterException
        ("unsupported path component: " + comp.toString());
    /*
     * Now we have a 16 bit file identifier.
     * Add it to the path currently stored.
     */
    if (fileID == mfc_codes.MASTER_FILE)
      {
        // master file, absolute select
        path_length   = 0;
        path_absolute = true;
      }
    else
      {
        // Plain file, relative or absolute path select
        /*
         * If the currently stored path has the maximum length
         * for a command, it must be selected right now. No file
         * info is needed, since there will follow at least one
         * other select command.
         */
        if (path_length >= path_component.length)
          execSelect(chan, false);

        path_component[path_length] = fileID;
        path_length++;
      }
  } // nextSelect


  /**
   * Finish the select operation initiated by <tt>initSelect</tt>.
   * This method will invoke <tt>execSelect</tt> to perform the select
   * operation for the last components stored by <tt>nextSelect</tt>.
   * The implementation of <tt>nextSelect</tt> ensures that there is
   * at least one component that still has to be selected.
   *
   * @return the last response received from the smartcard
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #execSelect
   * @see #nextSelect
   */
  protected ResponseAPDU doneSelect(MFCCardChannel chan)
       throws CardTerminalException
  {
    execSelect(chan, info_required);  // perform the remaining select

    return last_response;
  }


  /**
   *  Execute a ready-to-use command, given by the CommandAPDU.
   *  The command is send directly without any validation/verification
   *  or interpretation to the card.
   *
   *  @param chan  the channel to the smartcard
   *  @param apdu  the CommandAPDU, containing a ready-to-use Command sequence
   */
   protected void execCommand(MFCCardChannel chan,
                              CommandAPDU apdu)
       throws CardTerminalException
  {
     // execute a Command defined by the CommandAPDU
     last_response = chan.executeCommand(apdu);

     // evaluate the result
     if (mfc_codes.indicatesError(last_response.sw()))
       path_error = true;
  }



  /**
   * Select the path that is stored by now.
   * <tt>path_length</tt> holds the number of file IDs in the path,
   * the IDs are stored in <tt>path_component</tt>. If the path is
   * an absolute path from the master file, <tt>path_absolute</tt>
   * is true, otherwise it is false.
   *
   * @param chan  the channel to the smartcard
   * @param info  <tt>true</tt> if the file info is needed
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #nextSelect
   * @see #doneSelect
   */
   protected void execSelect(MFCCardChannel chan,
                             boolean        info)
       throws CardTerminalException
  {
    /*
     * If an error occurred before, we cannot select.
     * Reset the path stored and cancel operation.
     */
    if (path_error)
      {
        clearPath();
        return;
      }


    if (path_length == 0)
      {
        if (!path_absolute)
          return;               // no path stored, no select possible
        /*
         * Select master file. Following the MFC 3.51 spec, V1R3,
         * no absolute selection may be used, since there would be
         * no file ID which is required. Make the master file ID
         * explicit and use relative select.
         */
        path_component[0] = mfc_codes.MASTER_FILE;
        path_length       = 1;
        path_absolute     = false;
      }

    {
      byte p1, p2;

      if (path_absolute)
        p1 = p1_path_abs;         // absolute path selection
      else if (path_length == 1)
        p1 = p1_file_id;          // simple file selection
      else
        p1 = p1_path_rel;         // relative path selection

      if (info)
        p2 = p2_select_info;      // response data required
      else
        p2 = p2_select_quiet;     // quiet select, only status required

      select_apdu.append(p1);
      select_apdu.append(p2);
    }

    select_apdu.append((byte) (path_length*2) );

    for(int i=0; i < path_length; i++)
      {
        select_apdu.append((byte)((path_component[i] & 0xff00) >> 8));
        select_apdu.append((byte) (path_component[i] & 0x00ff));
      }

    clearPath();
    sendSelectAPDU(chan, info);
  }


  /**
   * Send a select command to the smartcard.
   * The command has to be stored in <tt>select_apdu</tt>. Before sending
   * the APDU, a zero Le byte will be appended if <tt>needsZeroLe</tt> of
   * the <tt>MFCCodes</tt> specifies so. The response will be stored in
   * <tt>last_response</tt>.
   * <br>
   * If an error occurs executing the command, <tt>path_error</tt>
   * is set to <tt>true</tt>. If <tt>path_error</tt> is already
   * true, nothing will be sent. The command APDU gets reset to
   * length 2 in any case.
   *
   * @param channel   how to contact the smartcard
   * @param info      whether information should be returned
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #select_apdu
   * @see #last_response
   * @see #path_error
   * @see MFCCodes
   * @see MFCCodes#needsZeroLe
   */
  protected void sendSelectAPDU(MFCCardChannel channel, boolean info)
       throws CardTerminalException
  {
    if (!path_error)
      {
        if (mfc_codes.needsZeroLe(info))
          select_apdu.append((byte) 0);

        last_response = channel.executeCommand(select_apdu);

        if (mfc_codes.indicatesError(last_response.sw()))
          path_error = true;
      }
    select_apdu.setLength(2);
  }


  /**
   * Perform a select operation for the given path.
   *
   * @param channel   how to contact the smartcard
   * @param path      what to select
   * @param info      whether file info is needed
   *
   * @return The response from the smartcard.
   *         If <tt>info</tt> was true, and no error occured,
   *         it includes the file header.
   *
   * @exception CardServiceObjectNotAvailableException
   *            the target could not be selected
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  protected ResponseAPDU performSelect(MFCCardChannel channel,
                                       CardFilePath   path,
                                       boolean        info)
       throws CardServiceObjectNotAvailableException, CardTerminalException
  {
    ResponseAPDU response   = null;
    Enumeration  components = path.components();

    initSelect(info);

    while (components.hasMoreElements())
      nextSelect(channel, (CardFilePathComponent) components.nextElement());

    response = doneSelect(channel);
    try { //@@@ exception mapping
    mfc_codes.analyseStatus(response.sw(), (byte)select_apdu.getByte(1), path);
    } catch (CardServiceException cse) { //@@@ should be changed
      CardServiceObjectNotAvailableException ona;
      if (cse instanceof CardServiceObjectNotAvailableException)
        ona = (CardServiceObjectNotAvailableException) cse;
      else
        ona = new CardServiceObjectNotAvailableException(cse.getMessage());
      throw ona;
    }

    return response;

  } // performSelect


  /////////////////////////////////////////////////////////////////////////////
  // card services //////// MUST BE SYNCHRONIZED !!! /////
  /////////////////////////////////////////////////////////////////////////////


  /**
   * Select an object on the smartcard.
   * Optionally return file information.
   *
   * @param channel   how to contact the smartcard
   * @param path      what to select
   * @param info      whether to return information, <tt>true</tt> if so
   * @param srparser  helper that interprets the select response,
   *                  may be <tt>null</tt> if no info is returned
   *
   * @return  information on the selected object, if requested
   *
   * @exception CardServiceObjectNotAvailableException
   *            if the target could not be selected
   * @exception CardServiceUnexpectedResponseException
   *            if the select response could not be parsed correctly
   * @exception CardTerminalException
   *            if the terminal encountered a problem
   */
  public synchronized MFCCardObjectInfo selectObject
                                          (MFCCardChannel          channel,
                                           CardFilePath            path,
                                           boolean                 info,
                                           MFCSelectResponseParser srparser)
       throws CardServiceObjectNotAvailableException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    MFCChannelState state = channel.getChannelState();
    MFCCardObjectInfo coi = null;

    boolean    needselect = true;
    boolean    needinfo   = info;

    /*
     * We have to distinguish two responsibilities:
     *  - select a file on the smartcard
     *  - return information about the file
     *
     * First, we check whether the select is required.
     * If not, we still may have the info to worry about.
     *
     * Second, we check whether the info is available.
     * If not, we have to perform a select that returns it.
     * If it is, we still may have to select without info,
     * which has been checked for above.
     */
    if (path.equals(determinePath(state, channel))) // already there?
      {
        coi        = state.getCurrentInfo();
        needselect = false;
        needinfo   = needinfo && (coi == null);
      }

    if (needinfo) // need info from cache?
      {
        coi      = state.lookupObjectInfo(path);
        needinfo = (coi == null);
      }

    /*
     * if 'needselect' is true, we have to execute a select
     * if 'needinfo' is true, we have to get info from a select
     */
    if (needselect || needinfo)
      {
        /*
         * The select has to be executed. 'needinfo' specifies whether
         * file info has to be returned or not.
         * Before executing, we try to reduce the length of the path.
         */
        CardFilePath    here = state.getCurrentPath(); // where we are
        MFCCardObjectInfo hi = state.getCurrentInfo(); // whether here is a DF

        state.setCurrentPath(null);          // invalidate current selection
        CardFilePath there = path;           // may be replaced

        // To reduce the selection path length, we have to know whether we
        // are in a DF or EF. If this info is not available, the complete
        // target path has to be selected.
        if ((here != null) && (hi != null))
          {
            if (!hi.isContainer())
              here.chompTail();         // was EF, now is parent DF

            if (there.startsWith(here)) // if so, modify clone, not original!
              there = (new CardFilePath(there)).chompPrefix(here);
          }

        ResponseAPDU response = performSelect(channel, there, needinfo);

        if (needinfo)
          {
            /*
             * We got a new file header from the card. Cache it.
             * We could restrict the kind of cached infos, for example
             * to elementary files only. This would require a type check
             * and downcast, and make this service dependent on the file
             * service stuff. Or, a method is added to MFCCardObjectInfo
             * to query whether the information should be cached.
             */
            coi = srparser.parseSelectResponse(response.data());
            state.cacheObjectInfo(path, coi);
          }
      } // if (needselect || needinfo)

    /*
     * Setting the path and info are currently cheap operations.
     * Otherwise, they should only be executed if necessary.
     * Remember that both may have been queried from the state!
     */
    state.setCurrentPath(path);
    state.setCurrentInfo(coi);

    return coi;

  } // selectObject()

} // class MFC35CardServiceImpl
