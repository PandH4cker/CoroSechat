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


import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;


/**
 * Encapsulation of the constructor arguments of <tt>MFCCardService</tt>.
 * The number of arguments to a card service is rather high. To simplify the
 * construction, this object encapsulates all parameters required for the
 * generic card service, except an optional pre-allocated channel. Every
 * parameter can be set explicitly by a method invocation. Access to the
 * parameters stored is provided only within this package and subclasses.
 *
 * @version $Id: MFCCardServiceParameter.java,v 1.7 1998/12/18 14:49:33 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCardService
 */
public class MFCCardServiceParameter
{
  /** The scheduler required to allocate channels. */
  protected CardServiceScheduler    cs_scheduler;

  /** The smartcard object representing the smartcard to contact. */
  protected SmartCard               smart_card;

  /** Whether channel allocation is blocking. */
  protected boolean                 is_blocking;


  /** The implementation of the generic card service. */
  protected MFCCardServiceImpl      service_impl;

  /** The parser for responses to the SELECT command. */
  protected MFCSelectResponseParser select_parser;

  /** The accessor that may be needed in derived card services. */
  protected CardAccessor            card_accessor;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Creates new parameters, initialized with <tt>null</tt>.
   * All parameters required for instantiating a generic
   * <tt>MFCCardService</tt> must be set explicitly afterwards.
   *
   * @see MFCCardService
   */
  public MFCCardServiceParameter()
  {
    // no body
  }


  /**
   * Creates new parameters, the standard ones already set.
   * In the first version of OCF, all card services had the same constructor
   * arguments. This constructor expects those arguments, and stores them.
   * The remaining arguments required for instantiating a generic
   * <tt>MFCCardService</tt> have to be set explicitly after construction.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   the smartcard to contact
   * @param blocking    whether channel allocation blocks
   *
   * @see MFCCardService
   */
  public MFCCardServiceParameter(CardServiceScheduler   scheduler,
                                 SmartCard              smartcard,
                                 boolean                blocking )
  {
    cs_scheduler = scheduler;
    smart_card   = smartcard;
    is_blocking  = blocking;
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Set the scheduler for channel allocation.
   * A card service has to allocate a channel to the smartcard before
   * issuing a command, and release it afterwards. This method can be used
   * to set the scheduler that is responsible for managing the channels to
   * the smartcard for which the card service has to be instantiated.
   *
   * @param scheduler  the scheduler for channel allocation
   *
   * @see opencard.core.service.CardServiceScheduler
   */
  final public void setScheduler(CardServiceScheduler scheduler)
  {
    cs_scheduler = scheduler;
  }


  /**
   * Set the smartcard to contact.
   * Card services get instantiated for a specific smartcard that has been
   * inserted into one of the slots of one of the card terminals. This method
   * can be used to set the object of class <tt>SmartCard</tt> that represents
   * the smartcard that will be contacted by the service to be instantiated.
   *
   * @param smartcard  the <tt>SmartCard</tt> to contact by the card service
   *
   * @see opencard.core.service.SmartCard
   */
  final public void setSmartCard(SmartCard smartcard)
  {
    smart_card = smartcard;
  }


  /**
   * Set the <tt>MFCCardServiceImpl</tt> instance to use.
   * The generic card service relies on a <i>card service implementation</i>
   * which is responsible for creating commands to send to the smartcard and
   * for interpreting the response. This method sets the instance of this
   * interface that will be used.
   *
   * @param csimpl   the <tt>MFCCardServiceImpl</tt> instance
   *                    for the generic card service
   */
  final public void setCardServiceImpl(MFCCardServiceImpl csimpl)
  {
    service_impl = csimpl;
  }


  /**
   * Set the <tt>MFCSelectResponseParser</tt> instance to use.
   * The generic card service offers a method to select an object on the
   * smartcard. This object can be a file, a key, or something else. The
   * smartcard will return status information on the selected object,
   * which has to be interpreted according to the object type.
   * <br>
   * This method sets the instance that will parse select responses for the
   * card service to create. It is assumed that a card service deals with
   * only one kind of objects that get selected, for example files in a file
   * service or keys in a signature service.
   *
   * @param srparser   the <tt>MFCSelectResponseParser</tt> instance for
   *                      the card service to instantiate
   */
  final public void setSelectResponseParser(MFCSelectResponseParser srparser)
  {
    select_parser = srparser;
  }


  /**
   * Set the accessor that will handle secure messaging.
   * Secure messaging, as well as card holder verification and other
   * access conditions, are not required for the generic card service.
   * Since the derived services rely on an accessor that handles this
   * stuff, it is supported in the base class.
   *
   * @param accessor   the <tt>CardAccessor</tt> that satisfies
   *                   access conditions
   */
  final public void setAccessor(CardAccessor accessor)
  {
    card_accessor = accessor;
  }


} // class MFCCardServiceParameter
