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
package com.ibm.opencard.util;

import java.io.*;
import java.util.Date;

import opencard.core.util.HexString;

/******************************************************************************
* Utility for automatically creating OCF agent scripts or agent dictionary
* classes from a given file. The contents of the file will be contained in
* a static byte array in the created Java source file.
*
* Usage:
*
* java com.ibm.boeblingen.smartcard.agent.ConvertFile -<type> <infile> <name>
*
* <type> may equal "s" for script or "d" for dictionary generation.
* <infile> is the name of the file to be converted
* <name> is the name of the class, for which the source will be generated.
*
* @author Thomas Schaeck
* @version  $Id: ConvertFile.java,v 1.2 1998/08/21 12:24:56 cvsusers Exp $
******************************************************************************/
public class ConvertFile
{
  // Carriage return for MS-DOS. Using "\n" in Strings doesn't work.
  private static final byte[] CR = { (byte) 13, (byte) 10 };

  // The prolog for a script. Defined package and imports required classes
  private static final String scriptProlog[] = {
    "",
    "import com.ibm.opencard.script.Script;",
    "import com.ibm.opencard.buffer.TLVBuffer;",
    ""
  };

  // The prolog for a dictionary. Defined package and imports required classes
  private static final String dictionaryProlog[] = {
    "",
    "import com.ibm.opencard.dictionary.Dictionary;",
    "import com.ibm.opencard.buffer.TLVBuffer;",
    ""
  };

  // The epilog for scripts or dictionaries. Defines the buffer() method.
  private static final String epilog[] = {
    "",
    "  private static TLVBuffer buffer = new TLVBuffer(bytes, bytes.length);",
    "",
    "  public TLVBuffer buffer()",
    "  {",
    "    return buffer;",
    "  }",
    "}",
    ""
  };

  // possible output types
  protected static final int NONE       = 0;
  protected static final int SCRIPT     = 1;
  protected static final int DICTIONARY = 2;

  // number of columns for the static byte array definition
  public static final int COLUMNS = 6;

  public static void main(String[] args)
  {
    int    outputType   = NONE;  // nothing known so far
    String baseClass    = null;
    byte[] b            = null;

    // Check arguments. If wrong, print usage and exit
    if (args.length != 4 || (!args[0].equals("-s") && !args[0].equals("-d"))) {
      System.out.println("Usage: ToScript -type infile package name\n" +
                         "type: s for Script, d for Dictionary\n" +
                         "infile: file to be converted to script or dictionary\n" +
                         "package: package to which the created class shall belong\n" +
                         "name: name of the class for which the source file\n" +
                         "shall be created (\".java\" will be added automatically)");
      System.exit(-1);
    }

    // determine type of output file
    outputType = (new String(args[0]).equals("-s")) ? SCRIPT : DICTIONARY;

    try {
      // Read in the source file as binary
      File file = new File (args[1]);
      FileInputStream fis = new FileInputStream (file);
      b = new byte[fis.available()];
      fis.read(b);
      fis.close();

      // Open the output file specified in second argument
      FileOutputStream fos = new FileOutputStream(args[3] + ".java");

      // Write package definition for the class
      fos.write((new String("package " + args[2] + ";")).getBytes());

      // Write the prolog, depending on the output type
      if (outputType == SCRIPT) {
        for (int i = 0; i < scriptProlog.length; i++) {
          fos.write(scriptProlog[i].getBytes());
          fos.write(CR);
        }
      } else {
        for (int i = 0; i < dictionaryProlog.length; i++) {
          fos.write(dictionaryProlog[i].getBytes());
          fos.write(CR);
        }
      }

      // Write some comments
      fos.write(("/*********************************************************************").getBytes());
      fos.write(CR);
      fos.write(("* This file was automatically created by ConvertFile ").getBytes());
      fos.write(CR);
      fos.write(("* on " + (new Date()).toString()).getBytes());
      fos.write(CR);
      fos.write(("* from the file " + args[1]).getBytes());
      fos.write(CR);
      fos.write(("* Please DO NOT EDIT this file. If you have to change something").getBytes());
      fos.write(CR);
      fos.write(("* please change it in ConvertFile").getBytes());
      fos.write(CR);
      fos.write(("**********************************************************************/").getBytes());
      fos.write(CR);

      // Write class header
      baseClass = (outputType == SCRIPT) ? "Script" : "Dictionary";
      fos.write(("public class " + args[3] + " extends " + baseClass).getBytes());
      fos.write(CR);
      fos.write((new String("{")).getBytes());
      fos.write(CR);

      // Write the contents of the input file as a Java static byte array
      fos.write((new String("  private static final byte[] bytes = {")).getBytes());
      fos.write(CR);
      for (int i = 0; i < b.length; i++) {
        if ((i % COLUMNS) == 0) {
          fos.write((new String("    ")).getBytes());
        }
        fos.write((new String("(byte)0x")).getBytes());
        fos.write(HexString.hexify(b[i]).getBytes());
        if (i != b.length - 1)
          fos.write(new String(",").getBytes());
        if ((i % COLUMNS) == COLUMNS-1)
          fos.write(CR);
      }
      fos.write(CR);
      fos.write((new String("  };")).getBytes());
      fos.write(CR);


      // Write the epilog
      for (int i = 0; i < epilog.length; i++) {
        fos.write(epilog[i].getBytes());
        fos.write(CR);
      }
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
// $Log: ConvertFile.java,v $
// Revision 1.2  1998/08/21 12:24:56  cvsusers
// Added license tag. (rolweber)
//
// Revision 1.1  1998/03/19 21:53:15  schaeck
// Initial version
//
// Revision 1.2  1998/02/27 18:17:19  schaeck
// *** empty log message ***
//
// Revision 1.1  1998/02/23 09:48:38  schaeck
// Initial Version
//
