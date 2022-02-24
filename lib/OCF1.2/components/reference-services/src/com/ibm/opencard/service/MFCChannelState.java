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

import opencard.core.util.Tracer;
import opencard.opt.iso.fs.CardFilePath;


/**
 * The smartcard's internal state, as seen through a logical channel.
 * Every smartcard maintains an internal state, consisting of at least
 * the selected directory and some permanent access conditions, namely
 * CHV and authentication. If a card provides several logical channels
 * for communication, only a part of this state is relevant for a service
 * that uses this channel.
 * <br>
 * This class represents the internal state, as it is seen through one
 * logical channel. If there are several channels, there could be another
 * class that represents the global part of the state, which would be
 * accessed from here. However, the current MFC cards do not support
 * logical channels, so there is no such class.
 * <br>
 * The state represented here consist of the selected directory and file,
 * the file cache, and information on the currently selected file. Access
 * conditions are not represented, since we do not want to re-program the
 * Card OS. Instead, commands are issued following an optimistic approach:
 * The card will tell us if the access conditions are not satisfied.
 * <br>
 * The file cache found here is a candidate for a global state if there
 * are logical channels. Obviously, the file cache does not represent
 * the full hierarchical directory structure found on the card, but only
 * the part of it that has been accessed by now.
 *
 * @version $Id: MFCChannelState.java,v 1.8 1998/07/02 11:15:06 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCChannelState
{
  /** The Tracer for this class. */
  private static Tracer ctracer = new Tracer(MFCChannelState.class);


  /** The path to the currently selected smartcard object. */
  private CardFilePath          current_path  = null;

  /** Information about the current object. */
  private MFCCardObjectInfo     current_info = null;

  /** The object info cache. */
  private MFCCardObjectInfoCache info_cache = null;


  // construction /////////////////////////////////////////////////////////////


  /**
   * Creates a new channel state.
   */
  public MFCChannelState()
  {
    info_cache = new MFCCardObjectInfoCache();

    current_path  = null;  // could be master file (root directory)
    current_info  = null;
  }



  // access ///////////////////////////////////////////////////////////////////


  /**
   * Sets the current path.
   * The information about the current selection gets
   * implicitly reset to <tt>null</tt>.
   *
   * @param path   the currently selected directory or file,
   *               or <tt>null</tt> to invalidate
   *
   * @see #getCurrentPath
   * @see #getCurrentInfo
   * @see #setCurrentInfo
   */
  final public void setCurrentPath(CardFilePath path)
  {
    // The path must be cloned, since the application could change it.
    if (path != null)
      current_path = new CardFilePath(path);
    else
      current_path = null;

    current_info = null;
  }


  /**
   * Gets the current path.
   *
   * @return  the path to the currently selected object on the card,
   *          or <tt>null</tt> if not set
   *
   * @see #setCurrentPath
   * @see #setCurrentInfo
   */
  final public CardFilePath getCurrentPath()
  {
    return current_path;
  }


  /**
   * Sets the object information about the current selection.
   * The current selection can be a file, a directory, a key, and so on.
   * Depending on the kind of selected object, an information structure
   * derived from <tt>MFCCardObjectInfo</tt> can be stored here.
   * <br>
   * The generic card service implementation uses this method to store
   * information after a select has been performed. It uses an object
   * passed to it's constructor to parse the smartcard's response to the
   * command. It is a card service factory's responsibility to supply a
   * parser that generates the information that will be needed by the
   * respective service after a select has been performed. For example,
   * a file service will require file information in which the record
   * size of a structured file can be queried.
   * <br>
   * If a select command fails, the information should be set to <tt>null</tt>
   * to avoid a mismatch between current selection and current information.
   * This can be achieved by setting a new current path, which will implicitly
   * clear the current info.
   *
   * @param info  information about the current selection
   *
   * @see #getCurrentInfo
   */
  final public void setCurrentInfo(MFCCardObjectInfo info)
  {
    current_info = info;
  }


  /**
   * Gets the information about the current selection.
   *
   * @return   the information set with <tt>setCurrentInfo</tt>,
   *           or <tt>null</tt> if none is set
   *
   * @see #setCurrentInfo
   */
  final public MFCCardObjectInfo getCurrentInfo()
  {
    return current_info;
  }



  // service //////////////////////////////////////////////////////////////////

  /**
   * Retrieves object information from the cache.
   *
   * @param obj   a card file path as object identifier
   * @return      the object information, or <tt>null</tt> on cache miss
   */
  final public MFCCardObjectInfo lookupObjectInfo(CardFilePath obj)
  {
    return info_cache.lookupObjectInfo(obj);
  }


  /**
   * Stores object information in the cache.
   *
   * @param obj     a file path as object identifier
   * @param info    the information to store
   */
  final public void cacheObjectInfo(CardFilePath      obj,
                                    MFCCardObjectInfo info)
  {
    /*
     * A cloned path has to be used as the actual key, since an application
     * could decide to change the path that was supplied as identifier.
     */
    CardFilePath keyclone = new CardFilePath(obj);
    info_cache.cacheObjectInfo(keyclone, info);
  }

  /**
   * Removes object information from the cache.
   *
   * @param key   a card file path as object identifier
   */
  final public void removeObjectInfo(CardFilePath obj)
  {
    info_cache.removeObjectInfo(obj);
  }


} // class MFCChannelState
