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

package com.ibm.opencard.isofs;


import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;

import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.access.MFCAccessorFactory;
import com.ibm.opencard.service.IBMCardServiceFactory;
import com.ibm.opencard.service.MFCSelectResponseParser;
import com.ibm.opencard.service.MFCGenericFactory;


/**
 * A factory class for MFC file service objects. Here, the term factory
 * refers to the design technique, not to OCF card service factories.
 * The file services implement the interfaces <tt>FileAccessCardService</tt>
 * and <tt>FileSystemCardService</tt>. This factory class allows to create
 * appropriate implementations of these services, along with some other
 * helpers used.
 *
 * @version $Id: MFCFileFactory.java,v 1.4 1999/03/11 13:32:23 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see opencard.opt.iso.fs.FileAccessCardService
 * @see opencard.opt.iso.fs.FileSystemCardService
 */
public final class MFCFileFactory
{
  /** The file access service implementation for MFC 4.0 and below. */
  private static MFCFileAccessImpl mfc35_facc = null;

  /** The file access service implementation for MFC 4.1 and above. */
  private static MFCFileAccessImpl mfc41_facc = null;


  /** The file system service implementation for MFC 4.0 and below. */
  private static MFCFileSystemImpl mfc35_fsys = null;

  /** The file system service implementation for MFC 4.1 and above. */
  private static MFCFileSystemImpl mfc41_fsys = null;


  /** The select response parser for file selects. */
  private static MFCSelectResponseParser sr_parser = null;



  /** Disabled default constructor. */
  private MFCFileFactory()
  {
    // no body
  }


  // file access service implementations //////////////////////////////////////

  /**
   * Returns a file access service implementation for the given CardOS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   a file access service implementation suitable for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCFileAccessImpl getFAImpl(int cos)
  {
    MFCFileAccessImpl fai = null;

    switch (cos)
      {
      case IBMMFCConstants.IBM_MFC_3_COS:
      case IBMMFCConstants.IBM_MFC_4_COS:
        fai = get35FAImpl();
        break;

      case IBMMFCConstants.IBM_MFC_4F_COS:
      case IBMMFCConstants.IBM_MFC_421_COS:
        fai = get41FAImpl();
        break;

      default: // will not happen
        break;
      }

    return fai;
  }


  /**
   * Returns a file access service implementation for MFC 4.0 and below.
   * The implementation is created on the first invocation and re-used later.
   *
   * @return a file access service implementation for MFC 3.5 and 4.0
   */
  public static MFCFileAccessImpl get35FAImpl()
  {
    if (mfc35_facc == null)
      mfc35_facc = new MFC35FileAccessImpl(MFCGenericFactory.get35Codes());
    return mfc35_facc;
  }


  /**
   * Returns a file access service implementation for MFC 4.1 and above.
   * The implementation is created on the first invocation and re-used later.
   *
   * @return a file access service implementation for MFC 4.1 and 4.21
   */
  public static MFCFileAccessImpl get41FAImpl()
  {
    if (mfc41_facc == null)
      mfc41_facc = new MFC35FileAccessImpl(MFCGenericFactory.get41Codes());
    return mfc41_facc;
  }


  // file system service implementations //////////////////////////////////////

  /**
   * Returns a file system service implementation for the given CardOS.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   a file system service implementation suitable for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCFileSystemImpl getFSImpl(int cos)
  {
    MFCFileSystemImpl fsi = null;

    switch (cos)
      {
      case IBMMFCConstants.IBM_MFC_3_COS:
      case IBMMFCConstants.IBM_MFC_4_COS:
        fsi = get35FSImpl();
        break;

      case IBMMFCConstants.IBM_MFC_4F_COS:
      case IBMMFCConstants.IBM_MFC_421_COS:
        fsi = get41FSImpl();
        break;

      default: // will not happen
        break;
      }

    return fsi;
  }


  /**
   * Returns a file system service implementation for MFC 4.0 and below.
   * The implementation is created on the first invocation and re-used later.
   *
   * @return a file system service implementation for MFC 3.5 and 4.0
   */
  public static MFCFileSystemImpl get35FSImpl()
  {
    if (mfc35_fsys == null)
      mfc35_fsys = new MFC35FileSystemImpl(MFCGenericFactory.get35Codes());
    return mfc35_fsys;
  }


  /**
   * Returns a file system service implementation for MFC 4.1 and above.
   * The implementation is created on the first invocation and re-used later.
   * Although the commands of the file system implementation are non-ISO, the
   * implementation returned by <tt>get35FSImpl</tt> can not be used here.
   * The default class byte used for MFC 4.0 and below specifies proprietary
   * secure messaging, while the MFC 4.1 and above services use an ISO secure
   * messenger.
   *
   * @return a file system service implementation for MFC 4.1 and 4.21
   *
   * @see #get35FSImpl
   */
  public static MFCFileSystemImpl get41FSImpl()
  {
    if (mfc41_fsys == null)
      mfc41_fsys = new MFC35FileSystemImpl(MFCGenericFactory.get41Codes());
    return mfc41_fsys;
  }


  // select response parser ///////////////////////////////////////////////////

  /**
   * Returns a file select response parser for the given CardOS.
   * Currently, there is only one select response parser for all MFC cards.
   * The argument is therefore ignored.
   * The parser is created on the first invocation and re-used later.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return       a file select response parser for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCSelectResponseParser getSRParser(int cos)
  {
    if (sr_parser == null)
      sr_parser = new MFC35FileSRParser(MFCAccessorFactory.getACParser(cos));
    return sr_parser;
  }


  // service parameters ///////////////////////////////////////////////////////

  /**
   * Initializes a <tt>MFCFileParameter</tt>.
   * The parameter gets initialized with a file select response parser and
   * a file access service implementation. The generic parts are also
   * initialized by invoking <tt>MFCGenericFactory.initParameter</tt>.
   *
   * @param param       the parameter to initialize
   * @param cos         the card OS identifier, see <tt>IBMMFCConstants</tt>
   *
   * @see com.ibm.opencard.service.MFCGenericFactory#initParameter
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static void initFileParam(MFCFileParameter param, int cos)
  {
    MFCGenericFactory.initParameter(param, cos);

    param.setSelectResponseParser(getSRParser(cos));
    param.setFileAccessImpl      ( getFAImpl (cos));
  }

  /**
   * Initializes a <tt>MFCFileSysParameter</tt>.
   * The parameter gets initialized with a file system service implementation.
   * The other parts are also initialized by invoking <tt>initFileParam</tt>.
   *
   * @param param       the parameter to initialize
   * @param cos         the card OS identifier, see <tt>IBMMFCConstants</tt>
   *
   * @see #initFileParam
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static void initFileSysParam(MFCFileSysParameter param, int cos)
  {
    initFileParam(param, cos);

    param.setFileSystemImpl(getFSImpl(cos));
  }


  /**
   * Returns a new, initialized parameter for creating a file access service.
   * The parameter is initialized by invoking <tt>initFileParam</tt>.
   *
   * @param scheduler   where to allocate channels,
   *                    gets stored in the parameter
   * @param smartcard   the smartcard to support,
   *                    gets stored in the parameter
   * @param blocking    whether to operate blocking,
   *                    gets stored in the parameter
   * @param cos         card os
   *
   * @return a parameter for instantiating <tt>MFCFileAccess</tt>
   *
   * @see #initFileParam
   * @see MFCFileAccess#MFCFileAccess(com.ibm.opencard.isofs.MFCFileParameter)
   */
  public static MFCFileParameter newFileParam(CardServiceScheduler scheduler,
                                              SmartCard            smartcard,
                                              boolean              blocking,
                                              int                  cos)
  {
    MFCFileParameter p = new MFCFileParameter(scheduler, smartcard, blocking);
    initFileParam(p, cos);
    return p;
  }


  /**
   * Returns a new, initialized parameter for creating a file system service.
   * The parameter is initialized by invoking <tt>initFileSysParam</tt>.
   *
   * @param scheduler   where to allocate channels,
   *                    gets stored in the parameter
   * @param smartcard   the smartcard to support,
   *                    gets stored in the parameter
   * @param blocking    whether to operate blocking,
   *                    gets stored in the parameter
   * @param cos         card os
   *
   * @return a parameter for instantiating <tt>MFCFileSystem</tt>
   *
   * @see #initFileSysParam
   * @see MFCFileSystem#MFCFileSystem(com.ibm.opencard.isofs.MFCFileSysParameter)
   */
  public static 
  MFCFileSysParameter newFileSysParam(CardServiceScheduler scheduler,
                                      SmartCard            smartcard,
                                      boolean              blocking,
                                      int                  cos)
  {
    MFCFileSysParameter p = new MFCFileSysParameter(scheduler,
                                                    smartcard,
                                                    blocking);
    initFileSysParam(p, cos);

    return p;
  }


} // class MFCFileFactory
