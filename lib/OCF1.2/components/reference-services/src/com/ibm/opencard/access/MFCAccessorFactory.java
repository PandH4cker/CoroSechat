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


import com.ibm.opencard.IBMMFCConstants;
import com.ibm.opencard.service.MFCGenericFactory;


/**
 * A factory class for creating card accessors.
 * Here, the term factory refers to the design technique, not to
 * OCF card service factories.
 * The MFC card services use card accessors to satisfy access conditions.
 * MFC access conditions include simple ones like password protection (CHV)
 * as well as cryptographic ones. Since cryptography causes problems with
 * US export restrictions, accessors are created in their own factory. One
 * factory supports export free password protection, the other one supports
 * cryptographic accessors, too. The Java reflection functionality is used
 * to decide whether the export restricted accessor features are available.
 *
 * @version $Id: MFCAccessorFactory.java,v 1.1 1998/08/06 14:20:51 cvsusers Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public class MFCAccessorFactory
{
  /** The factory responsible for creating card accessors. */
  private static MFCAccessorFactory accessor_factory = null;


  /** The name of the extended factory to test for. */
  private static String factory_name =
      "com.ibm.opencard.access.MFCSMAccessorFactory";


  /** The parser for access conditions. */
  private static MFCAccessParser acc_parser = null;


  // construction /////////////////////////////////////////////////////////////

  /** Instantiates a factory for export free accessors. */
  protected MFCAccessorFactory()
  {
    // no body
  }


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Provides the name of an extended accessor factory.
   * This method has to be invoked before <tt>newAccessor</tt> is invoked
   * for the first time. On the first invocation of that method, it is
   * checked whether the class with the given name can be instantiated.
   * If so, it is used as the accessor factory. Otherwise, this factory
   * will be used to create export free accessors.
   * <br>
   * If this method is not invoked in time, the default name will be used.
   * The default name is that of IBM's accessor factory supporting crypto
   * access conditions.
   *
   * @param name   the name of an extended accessor factory class
   *
   * @see #newAccessor
   */
  public static void setName(String name)
  {
    factory_name = name;
  }


  // service //////////////////////////////////////////////////////////////////

  /**
   * Creates a new card accessor.
   * On the first invocation of this method, an accessor factory will be
   * created and stored in <tt>accessor_factory</tt>. This factory will be
   * used for creating card accessors until the virtual machine terminates.
   * This method invokes <tt>newCardAccessor</tt> on the factory and returns
   * the result.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return a newly created card accessor
   *
   * @see #newCardAccessor
   * @see com.ibm.opencard.IBMMFCConstants
   */
  public static MFCCardAccessor newAccessor(int cos)
  {
    if (accessor_factory == null)
      instantiateFactory();

    return accessor_factory.newCardAccessor(cos);
  }


  /**
   * Instantiates a card accessor factory.
   * The instantiated factory is stored in <tt>accessor_factory</tt>.
   */
  private static void instantiateFactory()
  {
    // try to instantiate the secure messaging accessor factory
    try {
      Class       clazz  = Class.forName(factory_name);
      accessor_factory   = (MFCAccessorFactory) clazz.newInstance();
    } catch (Exception e) {
      // ignore
    }

    // if no factory has been instantiated, use this class
    if (accessor_factory == null)
      accessor_factory = new MFCAccessorFactory();

  } // instantiateFactory


  /**
   * Creates a new export free card accessor.
   * The card accessor returned supports password input (CHV),
   * but neither authetication nor protection nor encryption.
   * This method can be overridden to return an extended accessor.
   * It is invoked by <tt>newAccessor</tt>.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return  a newly created card accessor
   *
   * @see #newAccessor
   * @see com.ibm.opencard.IBMMFCConstants
   */
  protected MFCCardAccessor newCardAccessor(int cos)
  {
    MFCCHVProvider chv = new MFC35CHVProvider(MFCGenericFactory.getCodes(cos));

    return new MFCCardAccessor(chv);

  } // newCardAccessor


  /**
   * Returns a parser for access conditions.
   * Currently, there is only one access condition parser for all MFC cards.
   * The parser is created on the first invocation and re-used later. It is
   * needed by select response parsers.
   *
   * @param cos    the CardOS indicator, see <tt>IBMMFCConstants</tt>
   * @return   an access condition parser for the CardOS
   *
   * @see com.ibm.opencard.IBMMFCConstants
   * @see com.ibm.opencard.service.MFCSelectResponseParser
   */
  public static MFCAccessParser getACParser(int cos)
  {
    if (acc_parser == null)
      acc_parser = new MFC35AccessParser();
    return acc_parser;
  }


} // class MFCAccessorFactory
