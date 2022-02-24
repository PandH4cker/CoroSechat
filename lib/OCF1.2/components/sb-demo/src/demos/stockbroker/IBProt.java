/*
 *     (C) COPYRIGHT INTERNATIONAL BUSINESS MACHINES CORPORATION 1997 - 1999
 *                       ALL RIGHTS RESERVED
 *              IBM Deutschland Entwicklung GmbH, Boeblingen
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
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IBM
 * DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS SOFTWARE WILL MEET
 * THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL BE UNINTERRUPTED
 * OR ERROR-FREE. IN NO EVENT, UNLESS REQUIRED BY APPLICABLE LAW, SHALL IBM BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. ALSO, IBM IS UNDER NO OBLIGATION TO MAINTAIN,
 * CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */

package demos.stockbroker;

import opencard.opt.util.Tag;

/****************************************************************************
* IBProt defines the tags which are used to encode order messages.
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @version  $Id: IBProt.java,v 1.3 1998/09/02 09:12:09 cvsusers Exp $
*
* @see IBIssuerF
* @see IBClientF
* @see IBEveF
* @see IBServerF
*****************************************************************************/
class IBProt
{
  public final static Tag STOCK_ORDER       = new Tag(1000, (byte) 2, false);
  public final static Tag NUMBER            = new Tag(1001, (byte) 2, false);
  public final static Tag PRICE             = new Tag(1002, (byte) 2, false);
  public final static Tag SIGNATURE         = new Tag(1003, (byte) 2, false);
  public final static Tag SIGNED_MESSAGE    = new Tag(1004, (byte) 2, false);
  public final static Tag COMPANY           = new Tag(1005, (byte) 2, false);
  public final static Tag SEQUENCE_NUMBER   = new Tag(1006, (byte) 2, false);
  public final static Tag CARD_HOLDER_DATA  = new Tag(1007, (byte) 2, false);
  public final static Tag ENCRYPTED_MESSAGE = new Tag(1008, (byte) 2, false);
  public final static Tag CLEAR_MESSAGE     = new Tag(1009, (byte) 2, false);
}
