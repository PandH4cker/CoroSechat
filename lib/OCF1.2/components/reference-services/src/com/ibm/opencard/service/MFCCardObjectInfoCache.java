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


import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * A cache for information about objects located on a smartcard.
 * The information to be stored are instances of <tt>MFCCardObjectInfo</tt>
 * or derived classes. Examples for objects located on a smartcard are
 * files and keys. The information on those objects includes access
 * information.
 * <br>
 * To store and retrieve object information from this cache, a key has to
 * be used to specify the object to which information is related. For files,
 * this will be the file path. For keys, it may be the path to a file in
 * which the key is stored, or to a directory in which several files hold
 * the key information.
 * <br>
 * This cache uses a hashtable to maintain the association between keys
 * and information objects. The abstract methods defined in the base class
 * <tt>Dictionary</tt> are mapped to their implementation in the hashtable.
 * Use of these methods, especially to modify the contents of the cache, is
 * strongly <b>discouraged</b>.
 * <br>
 * Some additional methods are provided that restrict the type of the objects
 * stored in the cache from <tt>Object</tt> to <tt>MFCCardObjectInfo</tt>.
 * These methods are
 * <tt>cacheObjectInfo</tt> to store,
 * <tt>lookupObjectInfo</tt> to retrieve, and
 * <tt>removeObjectInfo</tt> to delete information in or from the cache.
 *
 * @version $Id: MFCCardObjectInfoCache.java,v 1.2 1998/12/18 14:49:32 rweber Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see MFCCardObjectInfo
 *
 * @see java.util.Dictionary
 * @see java.util.Hashtable
 *
 * @see #cacheObjectInfo
 * @see #lookupObjectInfo
 * @see #removeObjectInfo
 */
public class MFCCardObjectInfoCache extends Dictionary
{
  /** The hashtable used for implementing the cache. */
  private Hashtable hash_table;


  // construction /////////////////////////////////////////////////////////////

  public MFCCardObjectInfoCache()
  {
    super();
    hash_table = new Hashtable();
  }


  // access ///////////////////////////////////////////////////////////////////

  /*
   * The following methods are required for a dictionary, and implemented
   * by mapping them to the equivalent methods of the hashtable.
   */

  final public int size()
  {
    return hash_table.size();
  }

  final public boolean isEmpty()
  {
    return hash_table.isEmpty();
  }

  final public Enumeration keys()
  {
    return hash_table.keys();
  }

  final public Enumeration elements()
  {
    return hash_table.elements();
  }


  // service //////////////////////////////////////////////////////////////////

  /*
   * The following methods are required for a dictionary, and implemented
   * by mapping them to the equivalent methods of the hashtable.
   */

  final public Object get(Object key)
  {
    return hash_table.get(key);
  }

  final public Object put(Object key, Object value)
  {
    return hash_table.put(key, value);
  }

  final public Object remove(Object key)
  {
    return hash_table.remove(key);
  }

  /*
   * Now follow the additional methods not required by the base class.
   */


  /**
   * Store some object information in the cache.
   * The key passed as argument identifies an object located on the smartcard
   * for which information is to be stored. If the cache already contains
   * information on that object, it will be <i>replaced</i> by the one that
   * is passed as an argument. The information can be retrieved by invoking
   * <tt>lookupObjectInfo</tt> and deleted using <tt>removeObjectInfo</tt>.
   * <br>
   * The key specified has to obey the contract for the methods <tt>equals</tt>
   * and <tt>hashCode</tt> specified in the documentation of <tt>Object</tt>.
   * Especially, it may not change any attributes evaluated in any of these
   * methods while it is stored in the cache. If the key's class allows such
   * changes, make sure to pass a <i>copy</i> of the key which can be compared
   * to other keys using <tt>equals</tt>, but will not be changed since it's
   * identity is not passed to the outside world.
   *
   * @param key    an identifier for the object located on the smartcard
   * @param info   the information to store in the cache
   *
   * @see #lookupObjectInfo
   * @see #removeObjectInfo
   *
   * @see java.lang.Object#equals
   * @see java.lang.Object#hashCode
   */
  final public void cacheObjectInfo(Object key, MFCCardObjectInfo info)
  {
    hash_table.put(key, info);
  }


  /**
   * Retrieve some object information from the cache.
   * If object information has been stored in the cache by a preceeding
   * invocation of <tt>cacheObjectInfo</tt> with an equal key, and this
   * information has not yet been removed from the cache by an invocation
   * of <tt>removeObjectInfo</tt>, this information will be returned.
   * Otherwise, <tt>null</tt> is returned.
   * <br>
   * The key passed as argument and the key that has been passed when the
   * information to retrieve was stored must be equal with respect to their
   * implementations of the <tt>equals</tt> method. Also, <tt>hashCode</tt>
   * has to return the same value for both.
   *
   * @param key  an identifier for the object located on the smartcard
   * @return     any available information on that object
   *
   * @see #cacheObjectInfo
   * @see #removeObjectInfo
   *
   * @see java.lang.Object#equals
   * @see java.lang.Object#hashCode
   */
  final public MFCCardObjectInfo lookupObjectInfo(Object key)
  {
    Object obj = hash_table.get(key);

    if (!(obj instanceof MFCCardObjectInfo))
      obj = null;

    return (MFCCardObjectInfo)obj;
  }


  /**
   * Delete some object information from the cache.
   * if the cache contains information about the object identified by the
   * key, this information will be removed from the cache. Otherwise,
   * nothing will be done.
   * See <tt>cacheObjectInfo</tt> and <tt>lookupObjectInfo</tt> for details
   * on keys, and how information can be stored in or retrieved from the
   * cache.
   *
   * @param key  an identifier for the object located on the smartcard
   *
   * @see #cacheObjectInfo
   * @see #removeObjectInfo
   */
  final public void removeObjectInfo(Object key)
  {
    hash_table.remove(key);
  }

} // class MFCCardObjectInfoCache
