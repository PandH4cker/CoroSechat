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

/******************************************************************************
* Utility for converting TLVs representated in text to binary representation
* and vice versa.
*
* @author Thomas Schaeck (schaeck@de.ibm.com)
* @author Roland Weber  (rolweber@de.ibm.com)
* @version  $Id: ConvertTLV.java,v 1.5 1998/09/03 14:16:07 cvsusers Exp $
******************************************************************************/

import java.io.*;
import java.util.*;

import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

public class ConvertTLV
{
  private static Hashtable map_name_tag = null;
  private static Hashtable map_tag_name = null;

  private static Hashtable map_value_symbol = null;
  private static Hashtable map_symbol_value = null;


  public static int toBinaryNumber(byte[] binary, int offset, int number)
  {
    int length = number;
    int off = offset;
    if (length < 128)
    {
      binary[off] = (byte) length;
    }
    else if (length < 256)
    {
      binary[off] = (byte) 0x81;
      off++;
      binary[off] = (byte) length;
    }
    else if (length < 256*256)
    {
      binary[off] = (byte) 0x82;
      off++;
      binary[off] = (byte) (length / 256);
      off++;
      binary[off] = (byte) (length % 256);
    }
    else if (length < 256*256*256)
    {
      binary[off] = (byte) 0x83;
      off++;
      binary[off] = (byte) (length / 256 * 256);
      off++;
      binary[off] = (byte) (length / 256);
      off++;
      binary[off] = (byte) (length % 256);
    }
    off++;

    return off;
  }

  static Hashtable readTags(FileInputStream in, boolean fHashTags) throws IOException
  {
    Tag tag = null;
    Hashtable htTags = new Hashtable();

    InputStreamReader rd = new InputStreamReader(in);
    StreamTokenizer tags = new StreamTokenizer(rd);
    tags.commentChar('#');             // '#' is to-end-of-line comment
    tags.wordChars('_','_');           // '_' may be part of words

    while (tags.nextToken() != StreamTokenizer.TT_EOF) {
      String sTagName = null;
      int tagcode = 0;
      byte tagclass = 0;

      // get the tag name
      if (tags.ttype == StreamTokenizer.TT_WORD) {
        sTagName = new String(tags.sval);
        tag = new Tag();
        if (tags.nextToken() != StreamTokenizer.TT_EOF) {
          // get the tag number
          if (tags.ttype == StreamTokenizer.TT_NUMBER) {
            tagcode = (int) tags.nval;
          }

          if (tags.nextToken() != StreamTokenizer.TT_EOF) {
            // get the tag class
            if (tags.ttype == StreamTokenizer.TT_NUMBER) {
              tagclass = (byte) tags.nval;
              tag.set(tagcode, tagclass, false);
              if (fHashTags) {
                htTags.put(sTagName, tag);
              } else {
                htTags.put(tag, sTagName);
              }
            }
            // skip until ';'
            while (tags.nextToken() != StreamTokenizer.TT_EOF) {
              if ( tags.ttype == ';') break;
            }
          }
        }
      }
    }
    return htTags;
  }

  /*****************************************************************************
  * Read symbols from the given input stream and create a hash table
  * mapping the symbols to numbers or vice versa.
  *
  * @param in        The input stream from which the symbols shall be read.
  * @param fHashSyms Determines whether we want a mapping from symbol names to
  *                  numbers or vice versa
  *                  <tt>true</tt> means symbol names are mapped to numbers
  *                  <tt>false</tt> means numbers are mapped to symbol names
  * @return          A hash table containing an association between symbol names
  *                  and numbers.
  *****************************************************************************/
  static Hashtable readSyms(InputStream in, boolean fHashSyms) throws IOException
  {
    Hashtable htSyms = new Hashtable();
    int symValue = 0;
    String sSymName = null;

    InputStreamReader rd = new InputStreamReader(in);
    StreamTokenizer syms = new StreamTokenizer(rd);
    syms.commentChar('#');             // '#' is to-end-of-line comment
    syms.wordChars('_','_');           // '_' may be part of words

    while (syms.nextToken() != StreamTokenizer.TT_EOF) {
      // get the tag name
      if (syms.ttype == StreamTokenizer.TT_WORD) {
        sSymName = new String(syms.sval);
        if (syms.nextToken() != StreamTokenizer.TT_EOF) {
          // get the tag number
          if (syms.ttype == StreamTokenizer.TT_NUMBER) {
            symValue = (int) syms.nval;
          }

          if (fHashSyms) {
            htSyms.put(sSymName, new Integer(symValue));
          } else {
            htSyms.put(new Integer(symValue), sSymName);
          }
        }
        // skip until ';'
        while (syms.nextToken() != StreamTokenizer.TT_EOF) {
          if (syms.ttype == ';')
            break;
        }
      }
    }
    return htSyms;
  }

  static void levelPrint(int level, String s)
  {
    int i = 0;

    for (i=0; i<level; i++)
      System.out.print(" ");
    System.out.print(s);
  }

  /*****************************************************************************
  * Parse in a TLV tree from the given input stream, using the given hash tables
  * to convert names in the input to numbers.
  *
  * @param in The input stream from which the TLV tree shall be parsed in
  * @return The TLV tree parsed in.
  *****************************************************************************/
  static TLV parseText(InputStream in)
       throws IOException
  {
    // Set up the tokenizer to be used for parsing
    InputStreamReader rd = new InputStreamReader(in);
    StreamTokenizer tok = new StreamTokenizer(rd);
    tok.commentChar('#');             // '#' is to-end-of-line comment
    tok.wordChars('_','_');           // '_' may be part of words

    return parse(tok, null, 0);
  }

  /*****************************************************************************
  * Parse in a TLV tree from the given stream tokenizer using the given parent
  * string to determine the enclosing tag.
  *
  * This method sometimes calls itself to recurse into the TLV structure.
  *
  * @param tok    The tokenizer stream from which the TLV tree is parsed in.
  * @param parent The parent string t obe used for determining the tag of the
  *               root of the TLV tree to be created.
  * @param level  The nesting level at which we currently are.
  *****************************************************************************/
  static TLV parse(StreamTokenizer tok, String parent, int level)
       throws IOException
  {
    int i = 0;
    Tag tag = null;
    TLV newTLV = null;
    TLV iterTLV = null;

    if (parent != null) {
      tag = new Tag((Tag) map_name_tag.get(parent));
    }

    if (tok.nextToken() != StreamTokenizer.TT_EOF) {
      // get the tag name
      if (tok.ttype == StreamTokenizer.TT_WORD) {
        String newParent = new String(tok.sval);
        System.out.println();
        levelPrint(level, tok.sval);
        tok.nextToken();
        if (tok.ttype == '(') {
          System.out.print("(");
          if (tag != null) {
            newTLV = new TLV(tag, parse(tok, newParent, level + 2));
            while ( (iterTLV = parse(tok, null, level)) != null) {
              newTLV.add(iterTLV);
            }
          } else {
            newTLV = parse(tok, newParent, level + 2);
          }
        } else if (tok.ttype == ')') {
          int symbValue = ((Integer)map_symbol_value.get(newParent)).intValue();
          System.out.print("= "+ symbValue);
          newTLV = new TLV(tag, symbValue);
        } else {
          throw new IOException("Syntax Error at line " + tok.lineno() +
                                " at token " + tok.toString() + ":\n" +
                                "Expected ')'");
        }
      } else if (tok.ttype == StreamTokenizer.TT_NUMBER) {
        System.out.print((int) tok.nval);
        newTLV = new TLV(tag, (int) tok.nval);
        tok.nextToken();
        if (tok.ttype == ')') {
          System.out.print(")");
        } else {
          throw new IOException("Syntax Error at line " + tok.lineno() +
                                " at token " + tok.toString() + ":\n" +
                                "Expected ')'");
        }
      } else if (tok.ttype == '"') {
        System.out.print("\""+tok.sval+"\"");
        byte[] b = tok.sval.getBytes();
        newTLV = new TLV(tag, b);
        tok.nextToken();
        if (tok.ttype == ')') {
          System.out.print(")");
        } else {
          throw new IOException("Syntax Error at line " + tok.lineno() +
                                " at token " + tok.toString() + ":\n" +
                                "Expected ')'");
        }
      } else if (tok.ttype == '\'') {
        System.out.print("\'"+tok.sval+"\'");
        String s = new String(tok.sval);
        while(tok.nextToken() == '\'') {
          System.out.print("\'"+tok.sval+"\'");
          s.concat(tok.sval);
        }
        if (s.length() % 2 != 0) {
          throw new IOException("Syntax Error at line " + tok.lineno() +
                                " at token " + tok.toString() + ":\n" +
                                "Invalid hexadecimal constant");
        }
        byte b[] = new byte[s.length() / 2];
        for (i = 0; i < b.length; i++) {
          b[i] = (byte) Integer.parseInt(s.substring(2*i, 2*i+2), 16);
        }
        newTLV = new TLV(tag, b);
        if (tok.ttype == ')') {
          System.out.print(")");
        } else {
          throw new IOException("Syntax Error at line " + tok.lineno() +
                                " at token " + tok.toString() + ":\n" +
                                "Expected ')'");
        }
      } else if (tok.ttype == ')') {
        System.out.println();
        levelPrint(level - 2, ")");
        newTLV = null;
      }
    } else {
      newTLV = null;
    }
    return newTLV;
  }


  /**
   * The main entrance point to this tool.
   */
  public static void main(String args[])
       throws IOException
  {
    TLV currentTLV;
    int i;
    byte binary[];

    System.out.println("IBM Smart Card Toolkit TLV Builder (Java), Version 2.10");
    System.out.println("(C) Copyright IBM Corporation 1997");
    System.out.println("- Licensed Materials - Program Property of IBM - All Rigths Reserved.");

    if (args.length != 3)
      printUsage();

    initMappings();

    FileInputStream  in  = new FileInputStream(args[1]);
    FileOutputStream out = new FileOutputStream(args[2]);
    TLV              tlv = null;

    if (args[0].compareTo("-b") == 0)
    {
      tlv = parseText(in);

//      TLV procTLV = tlv.findTag((Tag) map_name_tag.get(new String("PROCEDURE")), null);
//      System.out.println("contents of PROCEDURE:");
//      System.out.println(procTLV.toString(htTagToName, 0));
      TLV argTLV = tlv.findTag((Tag) map_name_tag.get(new String("ARGUMENT")), null);
      System.out.println("contents of ARGUMENT:");

      while (argTLV != null) {
        if (argTLV.valueAsByteArray() != null) {
          System.out.println(new String(argTLV.valueAsByteArray()));
        } else {
          System.out.println("constructed");
        }
        argTLV = tlv.findTag((Tag) map_name_tag.get("ARGUMENT"), argTLV);
      }

      binary = tlv.toBinary();
      out.write(binary);
      // System.out.println(HexString.hexify(binary));
    } else if (args[0].compareTo("-d") == 0) {
      byte             bin[] = new byte[in.available()];
      in.read(bin);
      tlv = new TLV(bin);
      String result = tlv.toString(map_tag_name, 0);
      out.write(result.getBytes());
      System.out.println(result);
    } else {
      in.close();
      out.close();
      printUsage();
    }
  in.close();
  out.close();
  }


  /**
   * Initializes mappings from tags to names.
   * This operation requires the toolkit files <tt>sct_tags.txt</tt> and
   * <tt>sct_smb.txt</tt> to be located in the local directory. If the
   * files are not found, an error message is printed and the program exits.
   */
  private static void initMappings()
  {
    FileInputStream intxt = null;

    String name = null;

    try {
      // read the tags file, create two hash tables:
      // one mapping names to tags and one mapping tags to names

      name = "sct_tags.txt";

      intxt = new FileInputStream(name);
      map_name_tag = readTags(intxt, true);

      intxt = new FileInputStream(name);
      map_tag_name = readTags(intxt, false);

      // read the symbol file, create two hash tables:
      // one mapping names to numbers and one mapping numbers to names

      name = "sct_symb.txt";

      intxt = new FileInputStream("sct_symb.txt");
      map_symbol_value = readSyms(intxt, true);

      intxt = new FileInputStream("sct_symb.txt");
      map_value_symbol= readSyms(intxt, false);

    } catch (FileNotFoundException fnf) {
      System.err.print  ("Error: file ");
      System.err.print  (name);
      System.err.println(" must be available.");
      System.exit(1);

    } catch (IOException iox) {
      System.err.print  ("Error reading ");
      System.err.println(name);
      iox.printStackTrace(System.err);
      System.exit(1);
    }

  } // initMappings


  /**
   * Prints a usage message for this tool, then exits.
   */
  public static void printUsage()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("Usage: ");
    sb.append("java com.ibm.opencard.util.ConvertTLV");
    sb.append(" <option> <infile> <outfile>\n");

    sb.append("Options:\n");
    sb.append(" -b Build binary file from text file.\n");
  //sb.append("    If no output file is given, the name of the input file\n");
  //sb.append("    will be used, with the last character changed to 'B'.\n");
    sb.append(" -d Build text file from binary file.\n");
  //sb.append("    If no output file is given, the name of the input file\n");
  //sb.append("    will be used, with the last character changed to 'T'.\n");

    System.out.print(sb);

    System.exit(0);

  } // printUsage

} // class ConvertTLV
