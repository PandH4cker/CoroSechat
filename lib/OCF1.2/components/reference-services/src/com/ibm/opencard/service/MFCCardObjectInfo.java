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


/**
 * Holds information on objects located on a smartcard.
 * Objects on a smartcard may be files, keys, or other. The information
 * expected to be common to all kinds of objects is access information.
 * For selecting objects, it is also important whether an object is a
 * container, that is a directory.
 *
 * @version $Id: MFCCardObjectInfo.java,v 1.7 1998/12/18 14:49:32 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see com.ibm.opencard.service.AccessInformation
 */
public class MFCCardObjectInfo
{
  /**
   * Whether the object is a container or a simple object.
   * Dedicated files, also referred to as directories, are containers.
   * Elementary files are simple objects. Simple objects are organized
   * in a hierarchical tree structure, using containers as inner nodes.
   * <br>
   * The information whether an object is a container or not is required
   * when selecting objects. Selecting a different object within the same
   * container, or within a contained container, can be done faster.
   */
  boolean is_container = false;


  /** The access information to the object. */
  AccessInformation access_information = null;



  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates new object information.
   * The actual information must be stored later. This is done by a
   * select response parser. If the parser resides in the same package
   * as this class, it may set the attributes directly. For parsers in
   * other packages, access methods are provided.
   *
   * @see MFC35ObjectSRParser#parseObjectHeader
   */
  public MFCCardObjectInfo()
  {
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Gets the access information to the card object.
   * The access information combines all access conditions for all command
   * classes supported by the smartcard. Typically, only the access conditions
   * to a particular command class are of interest. They can be queried from
   * the info obtained here, or by calling <tt>getAccessConditions</tt> in
   * this class.
   *
   * @return the access information to the card object described by this object
   *
   * @see #getAccessConditions
   */
  final public AccessInformation getAccessInfo()
  {
    return access_information;
  }


  /**
   * Sets the access information to the card object.
   * This method is provided for card services that use the service
   * implementations that rely on this class, but want to implement
   * a parser which resides in a different package.
   *
   * @param accinfo   the access information for the card object
   */
  final public void setAccessInfo(AccessInformation accinfo)
  {
    access_information = accinfo;
  }


  /**
   * Gets access conditions to the card object for a particular command class.
   * This is a convenience method that invokes the corresponding method in the
   * access information that can be obtained by calling <tt>getAccessInfo</tt>.
   * For a description of the argument and return values, refer to that class.
   *
   * @param cmdclass   the command class for which to return access conditions
   * @return           the access conditions for the specified command class
   *
   * @see com.ibm.opencard.service.AccessInformation#getAccessConditions
   */
  final public AccessConditions getAccessConditions(int cmdclass)
  {
    return access_information.getAccessConditions(cmdclass);
  }


  /**
   * Queries whether the object is a container object.
   *
   * @return <tt>true</tt> iff the object is a container
   *
   * @see #is_container
   */
  final public boolean isContainer()
  {
    return is_container;
  }


  /**
   * Specifies whether the object is a container object.
   * This method is provided for card services that use the service
   * implementations that rely on this class, but want to implement
   * a parser which resides in a different package.
   *
   * @param container  <tt>true</tt> if the object represents a
   *                    container, that is the object is a directory
   */
  final public void setContainer(boolean container)
  {
    is_container = container;
  }



  // service //////////////////////////////////////////////////////////////////

  /*
   * This class only represents data, it does not provide any service methods.
   */


  // debug ////////////////////////////////////////////////////////////////////

  /** A cached string representation of this info. */
  private String cached_tostring = null;

  /**
   * Builds a human-readable string representation of this object.
   *
   * @return a string describing this object info
   */
  public String toString()
  {
    if (cached_tostring == null)
      {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(";").append(access_information);
        toStringHook(sb);
        cached_tostring = sb.toString();
      }

    return cached_tostring;
  }

  /**
   * A hook where derived classes may add info to the string representation.
   *
   * @param sb  the buffer that holds the string composed so far
   */
  protected void toStringHook(StringBuffer sb)
  {
  }

} // class MFCCardObjectInfo
