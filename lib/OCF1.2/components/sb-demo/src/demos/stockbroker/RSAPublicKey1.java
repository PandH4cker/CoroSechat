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

import java.math.BigInteger;

import opencard.opt.security.RSAPublicKey;

/*
 * @author   Thomas Schaeck (schaeck@de.ibm.com)
 * @version  $Id: RSAPublicKey1.java,v 1.2 1998/09/02 09:11:29 cvsusers Exp $
 */

class RSAPublicKey1 extends RSAPublicKey
{
  // Key data
  private final static byte[] Exponent_1 =
  {
    (byte) 0x01, (byte) 0x00, (byte) 0x01
  };
  private final static byte[] Modulus_1 =
  {
    (byte) 0x25, (byte) 0x05, (byte) 0x16, (byte) 0x76, (byte) 0x67,
    (byte) 0x39, (byte) 0xb7, (byte) 0xd5, (byte) 0x93, (byte) 0x2d,
    (byte) 0xc5, (byte) 0xed, (byte) 0xd6, (byte) 0x85, (byte) 0xcc,
    (byte) 0xae, (byte) 0x98, (byte) 0xcf, (byte) 0x7c, (byte) 0x74,
    (byte) 0xfc, (byte) 0xf3, (byte) 0xe3, (byte) 0x1d, (byte) 0x22,
    (byte) 0xe0, (byte) 0x6a, (byte) 0x90, (byte) 0x60, (byte) 0x20,
    (byte) 0x42, (byte) 0xae, (byte) 0xbc, (byte) 0x10, (byte) 0x89,
    (byte) 0x33, (byte) 0x9e, (byte) 0xa7, (byte) 0xe0, (byte) 0x0b,
    (byte) 0xd2, (byte) 0x09, (byte) 0x2c, (byte) 0x22, (byte) 0xd0,
    (byte) 0xad, (byte) 0x49, (byte) 0xd0, (byte) 0x4d, (byte) 0x33,
    (byte) 0x8c, (byte) 0x0a, (byte) 0xc9, (byte) 0xd8, (byte) 0x0e,
    (byte) 0x2a, (byte) 0x97, (byte) 0xc8, (byte) 0xbe, (byte) 0xb8,
    (byte) 0x97, (byte) 0x47, (byte) 0x76, (byte) 0xc0
  };

  static {
    // Reverse byte oreder of exponent and modulus
    swap(Exponent_1);
    swap(Modulus_1);
  }

  /****************************************************************************
  * Reverse an array.
  *
  * @param b - the byte array to be reversed
  ****************************************************************************/
  private static void swap(byte[] b)
  {
    int  i    = 0;
    byte temp = 0;

    for (i=0; i<b.length/2; i++)
    {
      temp = b[i]; b[i] = b[b.length-i-1]; b[b.length-i-1] = temp;
    }
  }

  RSAPublicKey1()
  {
    super(new BigInteger(1, Exponent_1),
          new BigInteger(1, Modulus_1));
  }
}
