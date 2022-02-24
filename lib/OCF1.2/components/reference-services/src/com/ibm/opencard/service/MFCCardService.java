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

import opencard.core.service.SmartCard;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardServiceException;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CHVDialog;
import opencard.core.service.CardType;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;

import opencard.opt.service.CardServiceObjectNotAvailableException;
import opencard.opt.service.CardServiceUnexpectedResponseException;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.security.SecureService;
import opencard.opt.security.SecurityDomain;
import opencard.opt.security.CredentialStore;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.SignCredential;

import com.ibm.opencard.access.MFCCardAccessor;
import com.ibm.opencard.access.MFCCredentialSet;
import com.ibm.opencard.access.MFCCredentialStore;


/**
 * The base class for all MFC card services.
 * It provides channel allocation and key information handling,
 * both not directly related to but required by card services.
 * The only card service actually provided in this class is the
 * <tt>select</tt> command, which is protected and will be used
 * by derived services. The rest defines the infrastructure for
 * card services.
 *
 * <p>
 * The select command implemented by MFC smartcards is rather tricky.
 * The implementation of the select command in <tt>MFC35CardServiceImpl</tt>
 * makes some assumptions on the card layout. These are <i>common sense</i>
 * assumptions that should not affect existing or future layouts. They
 * are mentioned here anyway, to save the time of those people that like
 * to check the behaviour of programs under strange circumstances.
 * <br>
 * A file ID that has to be interpreted by the select command of an MFC
 * smartcard will be checked to be an EF or DF within the current DF, to
 * be the parent DF, or the current DF itself. The MFC card services
 * assume that the file ID of the parent DF, the current DF, and all EFs
 * and DFs within the current directory are distinct.
 * <br>
 * <A NAME="ref1">
 * For MFC smartcards that support selection by application names, it is
 * further assumed that application names and the location of the respective
 * files are defined only in the MF. Subapplications are not supported.
 *
 * @version $Id: MFCCardService.java,v 1.34 1999/03/11 13:32:26 pbendel Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public abstract class MFCCardService extends CardService
    implements SecureService
{
  /* The tracer for debugging output. */
  //private final static Tracer ctracer = new Tracer(MFCCardService.class);


  /**
   * The accessor used for CHV and secure messaging.
   * It gets stored in a channel after allocation.
   */
  private CardAccessor card_accessor;


  /**
   * The MFC channel to use for communication with the smartcard.
   * There is a corresponding attribute in the base class that usually
   * holds a reference to the same object, but has a more general type.
   * Storing the channel with the custom type reduces the number of
   * downcasts.
   */
  private MFCCardChannel mfc_channel = null;

  /**
   * Whether the current channel is a parent's service channel.
   * If a service is not used directly by an application, but via a
   * meta service, the meta service will provide the channel to use,
   * already instrumented with an accessor. This attribute indicates
   * whether the channel stored in <tt>mfc_channel</tt> is a meta
   * (or parent) service's channel.
   *
   * @see #mfc_channel
   * @see #setParentChannel
   */
  private boolean       is_parent_channel = false;


  /** The card service implementation for this card service. */
  private MFCCardServiceImpl service_impl = null;


  /** The parser for select responses. */
  private MFCSelectResponseParser select_parser = null;


  /** Path to the root of the file system. */
  private final static CardFilePath master_file = new CardFilePath(":3f00");

  /** Type of card for which this service is customized */
  protected int card_type;

  // construction /////////////////////////////////////////////////////////////


  /**
   * Creates a new MFC card service.
   * The service cannot be used immediately. The actual initialization
   * is triggered by an invocation of <tt>initialize</tt>.
   */
  protected MFCCardService()
  {
    // no body
  }


  /**
   * Does not initialize this service.
   * In the base class <tt>CardService</tt>, this method is provided as
   * an entry point for extended initializations. For MFC card services,
   * initialization is done in hierachically invoked methods named
   * <tt>initXXX</tt>, where XXX indicates the service class. The
   * initialization method for this class is <tt>initGeneric</tt>.
   * <br>
   * Every MFC card service class has to override this method. The
   * implementation creates a parameter object suitable for the derived
   * class and invokes the single-argument initialization method for
   * that class. That method will initialize the local class attributes
   * from the parameter and invoke the single-argument initialization
   * method for the base class.
   * <br>
   * Since this class is abstract, the initialization method here cannot
   * create an appropriate parameter object. Instead, it throws an
   * exception indicating that the developer of the derived service
   * made a programming error.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   which smartcard to contact
   * @param blocking    whether operation shall be blocking
   *
   * @exception CardServiceException
   *            if this method is invoked
   *
   * @see opencard.core.service.CardService
   * @see opencard.core.service.CardService#initialize
   * @see #initGeneric
   */
  protected void initialize(CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking )
    throws CardServiceException
  {
    throw new CardServiceInabilityException
      ("MFCCardService cannot initialize itself");
  }


  /**
   * Initializes this service from encapsulated parameters.
   * This method initializes the local attributes and invokes
   * <tt>initialize</tt> in the base class. It has to be invoked
   * by each MFC card service. The parameters are encapsulated
   * in an object of class <tt>MFCCardServiceParameter</tt>.
   * Derived services should derive their own parameter classes
   * from that to add more initialization arguments.
   *
   * @param parameter   an object encapsulating the parameters to this service
   *
   * @exception CardServiceException
   *            if the initialization failed.
   *            With the current implementation, this cannot happen.
   *
   * @see MFCCardServiceParameter
   */
  protected final void initGeneric(MFCCardServiceParameter parameter)
       throws CardServiceException
  {
    super.initialize(parameter.cs_scheduler,
                     parameter.smart_card,
                     parameter.is_blocking );

    service_impl  = parameter.service_impl;
    card_accessor = parameter.card_accessor;
    select_parser = parameter.select_parser;
  }



  // access ///////////////////////////////////////////////////////////////////


  /**
   * Provides a channel to use, without setting the accessor.
   * This method is invoked by meta services, that is services that use
   * other services to do their job. A meta service will want to provide
   * a channel with the meta service's accessor. Unlike with the method
   * <tt>setCardChannel</tt>, the card service will not store it's own
   * accessor into the channel provided here. To unset the parent channel,
   * invoke this method with argument <tt>null</tt>.
   * <br>
   * This method may be used simultaneously with <tt>setCardChannel</tt>,
   * which is invoked by <tt>SmartCard.beginMutex</tt> and
   * <tt>SmartCard.endMutex</tt>. The channel provided here will take
   * precedence over the channel provided with <tt>setCardChannel</tt>.
   * Typically, the channels will be the same anyway, but a channel
   * provided via <tt>setCardChannel</tt> will be instrumented with the
   * service's own accessor.
   * <br>
   * Neither the use of <tt>setCardChannel</tt> nor the use of this method
   * relief a card service from using the allocation methods embracing
   * card accesses, as described in <tt>CardService</tt>. By convention,
   * this method is invoked only from outside the service it is invoked
   * on. This implies that no channel is currently explicitly allocated
   * by the service.
   *
   * @see opencard.core.service.CardService#setCardChannel
   * @see opencard.core.service.SmartCard#beginMutex
   * @see opencard.core.service.SmartCard#endMutex
   * @see #allocateCardChannel
   * @see #releaseCardChannel
   * @see opencard.core.service.CardService
   */
  final public void setParentChannel(MFCCardChannel channel)
  {
    if (channel != null)
      {
        mfc_channel       = channel;
        is_parent_channel = true;
      }
    else // unset the parent channel
      {
        // If a channel has been preset, get it from the base class.
        // If no such channel has been set, null will be returned.
        mfc_channel       = (MFCCardChannel) getCardChannel();
        is_parent_channel = false;
      }
  } // setParentChannel


  /**
   * Returns the MFC channel to use.
   * This method usually returns the same channel as <tt>getCardChannel</tt>
   * in the base class, but the type of the channel is more specific, thus
   * saving downcasts.
   *
   * @return the card channel that has been allocated
   *
   * @see opencard.core.service.CardService#getCardChannel
   */
  final protected MFCCardChannel getMFCChannel()
  {
    return mfc_channel;
  }


  /**
   * Sets an application-defined CHV dialog.
   *
   * @param dialog    the dialog to use for CHV input
   */
  final public void setCHVDialog(CHVDialog dialog)
  {
    super.setCHVDialog(dialog);
    if ((card_accessor != null) && (card_accessor instanceof MFCCardAccessor))
      ((MFCCardAccessor)card_accessor).setCHVDialog(dialog); //@@@
  }


  /**
   * Returns a path to the master file of the smartcard.
   * This method is required by the interface <tt>FileAccessCardService</tt>.
   * Since paths are considered to be object identifiers, not only file
   * identifiers, it is offered in this base class.
   *
   * @return a path to the root of the file system
   *
   * @see opencard.opt.iso.fs.FileAccessCardService#getRoot
   */
  final public CardFilePath getRoot()
  {
    return master_file;
  }


  // service //////////////////////////////////////////////////////////////////



  /**
   * Allocates a card channel.
   * After allocation, the accessor gets stored in the channel, unless the
   * channel was provided via <tt>setParentChannel</tt>. In this case, the
   * service providing the channel will have set the accessor to use.
   * CHV, authentication and secure messaging are therefore transparent
   * to this service or service implementation.
   * <br>
   * The transparency gets lost if the CardOS requires commands to be
   * sent without intervening commands. This is the case for some of
   * the signature commands of the MFC 4.0 and 4.21 smartcards.
   *
   * @see #releaseCardChannel
   * @see #setParentChannel
   * @see com.ibm.opencard.access.MFCCardAccessor
   */
  final protected void allocateCardChannel()
       throws InvalidCardChannelException
  {
    if (!is_parent_channel)
      {
        super.allocateCardChannel();
        mfc_channel = (MFCCardChannel) getCardChannel();
        mfc_channel.setAccessor(card_accessor);
      }
    // else: mfc_channel already holds the channel to use
  }


  /**
   * Releases the allocated card channel.
   * The accessor stored in the channel after allocation will be cleared,
   * unless the channel was provided via <tt>setParentChannel</tt>. In this
   * case, it has not been instrumented with this service's accessor.
   * Any way, the next service to use the channel will not be able to use
   * the credentials stored in this service's accessor.
   * <br>
   * If this method is invoked while no channel is allocated, it simply
   * returns. This behavior is required to put the release invocation in
   * the <tt>finally</tt> clause of a <tt>try</tt> statement. The allocation
   * could fail and throw an exception, which should not lead to another
   * exception being thrown in the <tt>finally</tt> block.
   *
   * @see #allocateCardChannel
   * @see #setParentChannel
   */
  final protected void releaseCardChannel()
       throws InvalidCardChannelException
  {
    if (mfc_channel == null)    // no channel to release ?
      return;

    if (!is_parent_channel)
      {
        mfc_channel.setAccessor(null);
        super.releaseCardChannel();
        mfc_channel = null;
      }
    // else: don't touch it
  }


  /**
   * Checks whether the given path is <tt>null<tt>.
   * This method is used for parameter checking in derived services.
   *
   * @param file   the path to check for <tt>null</tt>
   * @exception CardServiceInvalidParameterException
   *            if the parameter is <tt>null</tt>
   */
  final protected void checkFileArg(CardFilePath file)
  {
    if (file == null)
      throw new CardServiceInvalidParameterException
        ("file == null");
  }


  /////////////////////////////////////////////////////////////////////////////
  // Card Services
  /////////////////////////////////////////////////////////////////////////////


  /**
   * Selects an object on the smartcard.
   * This method is called by derived services. Unlike the typical
   * service methods in derived services, this method expects a channel
   * to be allocated and passed as argument. The third argument specifies
   * whether information about the selected object should be returned, or
   * whether the select can be done quietly. The smartcard's response is
   * interpreted using the <tt>MFCSelectResponseParser</tt> passed to the
   * constructor.
   *
   * @param channel   contact to the smartcard
   * @param path      target of the select
   * @param info      <tt>true</tt> if the object info is required
   *
   * @return information about the selected object,
   *         if <tt>info</tt> was <tt>true</tt> and nothing went wrong
   *
   * @exception CardServiceObjectNotAvailableException
   *            if the target could not be selected
   * @exception CardServiceUnexpectedResponseException
   *            if the select response could not be interpreted
   * @exception CardTerminalException
   *            if the terminal encountered an error
   *
   * @see MFCSelectResponseParser
   */
  final protected MFCCardObjectInfo selectObject(MFCCardChannel channel,
                                                 CardFilePath   path,
                                                 boolean        info)
       throws CardServiceObjectNotAvailableException,
              CardServiceUnexpectedResponseException,
              CardTerminalException
  {
    return service_impl.selectObject(channel, path, info, select_parser);
  }


  /**
   * Provides the credentials for a key domain.
   * MFC smartcards distinguish key domains, which are basically subtrees of
   * the file hierarchies. A key domain can therefore be identified by the
   * path to the root directory. Within a key domain, new keys can be defined
   * and keys of the enclosing domain can be redefined. Within a key domain,
   * the keys are identified by a unique key number.
   * <br>
   * The credential bag to be passed as the second argument must be prepared
   * by the application, so it holds all credentials required to perform the
   * commands the application will execute. The bag itself contains one or
   * more credential stores, which hold card-specific credentials in
   * card and card service specific representations.
   * The credentials for MFC card services must be implementations of
   * <tt>SignCredential</tt>s, or subinterfaces, and they must be stored in a
   * <tt>MFCCredentialStore</tt>, using integers as identifiers.
   * <br>
   * The relevant credential store will be extracted and evaluated during the
   * invocation of this method. If the contents of that store is modified,
   * this method must be invoked again to make those changes take effect.
   *
   * @param domain    The identifier of the key domain.
   *                  The MFCCardService requires a CardFilePath to be
   *                  provided as SecurityDomain.
   * @param credbag   The collection of credentials for that domain
   *
   * @exception CardServiceInvalidParameterException
   *            SecurityDomain must be a CardFilePath
   *
   * @see opencard.opt.security.SecurityDomain
   * @see opencard.opt.iso.fs.CardFilePath
   * @see opencard.opt.security.CredentialBag
   * @see opencard.opt.security.CredentialStore
   * @see opencard.opt.security.SignCredential
   * @see com.ibm.opencard.access.SecureCredential
   * @see com.ibm.opencard.access.MFCCredentialStore
   */
  public void provideCredentials(SecurityDomain domain, CredentialBag credbag)
    throws CardServiceInvalidParameterException
  {
    if (! (domain instanceof CardFilePath)) {
      throw new CardServiceInvalidParameterException
        ("SecurityDomain must be CardFilePath");
    }
    CardFilePath path = new CardFilePath((CardFilePath)domain); // clone the path once

    MFCCredentialSet creds = ((MFCCardAccessor)card_accessor).getCredentials(); //@@@

    creds.deleteCredentials(path);

    MFCCredentialStore[] stores = (MFCCredentialStore[])
      credbag.getCredentialStores(getCard().getCardID(),
                                  MFCCredentialStore.class);

    if (stores != null)
      {
        for(int i=0; i<stores.length; i++)
          {
            Enumeration ids = stores[i].enumerateIDs();
            while(ids.hasMoreElements())
              {
                Integer id = (Integer) ids.nextElement();
                SignCredential cred = stores[i].retrieveCredential(id);
                creds.storeCredential(cred, path, id.intValue());
              }
          }
      } // if

  } // provideCredentials

  /** Store the card type to customize the service according to the card.
      Package visbility intended */
  void setCardType(CardType type) {
    card_type = type.getType();
  }
  


} // class MFCCardService
