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

import opencard.core.terminal.CardTerminalException;
import opencard.core.service.CardServiceException;


/**
 * The base class for the handlers used for executing scripts.
 * Each handler to be used in a <tt>HandlerChain</tt> has to be
 * derived from this class.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 * @version $Id: Handler.java,v 1.6 1998/08/18 14:45:06 cvsusers Exp $
 *
 * @see HandlerChain
 */
public class Handler
{
  /** The identifier of this handler */
  protected String handler_id;

  /**
   * This constructor creates an object with the given ID.
   * @deprecated use default constructor, followed by initialize(String)
   * @see #initialize
   */
  public Handler(String id)
  {
    handler_id = id;
  }


  /**
   * Default constructor.
   * Before this handler can actually be used, <tt>initialize</tt>
   * has to be invoked.
   */
  protected Handler()
  {
    // no body
  }


  /**
   * Initializes this handler.
   * This method is meant to be overridden in derived handlers that
   * have to perform extended initialization. The implementation in
   * this class <i>has to be invoked</i> anyway.
   *
   * @param id   the identifier of this handler
   * @param sp   where to obtain MFC card services from, if required
   *
   * @exception CardServiceException   if the initialization failed
   */
  public void initialize(String id, ServiceProvider sp)
       throws CardServiceException
  {
    handler_id = id;
  }

  /**
   * Returns the identifier of this handler.
   * @return a string indentifying this handler
   */
  final public String getID()
  {
    return handler_id;
  }


  /**
   * Handles a request.
   * This method is meant to be overridden by derived handlers that
   * actually handle requests. The default implementation here is to
   * return <tt>false</tt>, indicating that the request was not
   * recognized and therefore not handled.
   *
   * @param request   the request to handle
   * @return <TT>true</TT> if the request has been recognized,
   *         <TT>false</TT> otherwise
   *
   * @exception CardServiceException
   *            if the request was recognized, but the handler or a service
   *            used by the handler encountered an error when handling it
   * @exception CardTerminalException
   *            if the request was recognized, but the underlying terminal
   *            encountered an error when communicating with the smartcard
   */
  public boolean handle(Request request)
       throws CardServiceException, CardTerminalException
  {
    return false;
  }

} // class Handler
