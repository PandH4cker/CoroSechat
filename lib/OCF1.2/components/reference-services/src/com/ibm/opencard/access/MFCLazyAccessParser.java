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


/**
 * Lazy evaluation interface for access information parsing.
 * Smartcard objects, like files or keys, typically define access conditions
 * for all command classes distinguished by the smartcard's operating system.
 * Most of this information is not required by an application, either because
 * there is no command of this class that can be executed on the object, or
 * because the application does not use any command of that class.
 * <br>
 * To avoid the overhead of creating instances of <tt>MFCAccessConditions</tt>
 * for all command classes, parsers may implement a lazy evaluation policy.
 * The instance of <tt>MFCAccessInformation</tt> will store the encoded access
 * information, and parse the access conditions for a command class on demand.
 * <br>
 * While <tt>MFCAccessParser</tt> defines a generic interface for access
 * parsers, this interface adds a method for lazy evaluation.
 *
 * @version $Id: MFCLazyAccessParser.java,v 1.2 1998/08/11 10:16:10 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCAccessInformation
 * @see MFCAccessConditions
 * @see MFCAccessParser
 */
public interface MFCLazyAccessParser extends MFCAccessParser
{
  /**
   * Interpret status information for a specific command class.
   * Extract and return the access information encoded.
   * Since this method does not throw a checked exception, the data to
   * be parsed has to be checked when creating the access information.
   *
   * @param data        status information to parse
   * @param offset      index of first byte of access information
   * @param cmdclass    command class for which access conditions are needed
   *
   * @exception CardServiceInvalidParameterException
   *            if the access conditions cannot be parsed
   *
   * @return  a Java representation of the access conditions for the specified
   *          command class
   */
  public MFCAccessConditions parseAccessConditions(byte[] data,
                                                   int    offset,
                                                   int    cmdclass)
       throws CardServiceInvalidParameterException;

} // interface MFCLazyAccessParser
