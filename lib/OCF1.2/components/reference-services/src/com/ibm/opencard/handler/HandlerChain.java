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

import java.util.Vector;

/******************************************************************************
* This class implements a recognized design pattern, called Chain of
* Responsibility: A Request is handled by delegating it to the Handlers in the
* chain. The Request is propagated through the chain until it is handled by
* some Handler.
* <br>
* Each Handler must know which requests it can handle.
* A handler is expected to return <tt>true</tt> if it has handled a request,
* <tt>false</tt> otherwise.
*
* @author  Thomas Schaeck (schaeck@de.ibm.com)
* @version $Id: HandlerChain.java,v 1.5 1998/08/11 13:23:15 cvsusers Exp $
*
* @see Handler
******************************************************************************/
public class HandlerChain
{
  private Vector children = new Vector(); // This vector holds the handlers

  /****************************************************************************
  * This method returns the number of handlers which are currenty part of the
  * HandlerChain.
  * @return Number of handlers in the chain.
  ****************************************************************************/
  public int size()
  {
    return children.size();
  }

  /****************************************************************************
  * This function adds a handler to the chain of responsibility at the first
  * position. This implies that handlers which have been added previously may
  * be overridden by adding a new handler.
  * @param handler The Handler to be added.
  ****************************************************************************/
  public void add(Handler handler)
  {
    children.insertElementAt(handler, 0);
  }

  /****************************************************************************
  * Add a Handler at a given position in the chain of responsibility.
  * @param handler The Handler to be added.
  ****************************************************************************/
  public void addAt(Handler handler, int pos)
  {
    children.insertElementAt(handler, pos);
  }

  /****************************************************************************
  * Remove a handler component.
  * @param id The id of the handler to be removed.
  ****************************************************************************/
  public void remove(String id)
  {
    int i = 0;
    boolean found = false;
    while((i < children.size()) && (found == false)) {
      found = id.equals(((Handler) children.elementAt(i)).getID());
      i++;
    }
    children.removeElementAt(i);
  }

  /****************************************************************************
  * Get the handler with the given identifier.
  * @param id identifier of the handler to be returned
  * @return a handler with the given id
  ****************************************************************************/
  public Handler get(String id)
  {
    int i = 0;
    while(i < children.size()) {
      if (id.equals( ((Handler) children.elementAt(i)).getID())) {
        return (Handler) children.elementAt(i);
      }
      i++;
    }
    return null;
  }

  /****************************************************************************
  * Handle a Request by delegating it to the handlers in the
  * Chain of Responsibility.
  * @param request The request to be handled.
  * @return true if the request has been handled by some child,
  *         false otherwise.
  * @exception     opencard.core.service.CardServiceException
  *                !!! Change this !!!
  ****************************************************************************/
  public boolean handle(Request request)
       throws CardServiceException, CardTerminalException
  {
    int i = 0;
    boolean handled = false;
    while((i < children.size()) && (handled == false)) {
      handled = ((Handler) children.elementAt(i)).handle(request);
      i++;
    }
    return handled;
  }

} // class HandlerChain
