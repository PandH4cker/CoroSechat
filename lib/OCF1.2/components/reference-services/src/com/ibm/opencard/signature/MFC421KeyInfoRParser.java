/*
 * (C)Copyright IBM Corporation 1997 - 1999
 * All Rights Reserved.
 */
package com.ibm.opencard.signature;

import com.ibm.opencard.access.MFCAccessParser;
/**
 * Interpreter for smartcard responses to read key info commands.
 *
 * @version $Id: MFC421KeyInfoRParser.java,v 1.1 1998/10/27 13:31:56 cvsusers Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @see MFCKeyInfo
 * @see MFC40KeyInfoRParser
 */
public class MFC421KeyInfoRParser extends MFC40KeyInfoRParser {
/**
   * Creates a new key info  response parser for read key info.
   *
   * @param accpar   a parser for access conditions, which are part
   *                 of the read key info response
   */
  public MFC421KeyInfoRParser(MFCAccessParser accpar) {
    super(accpar);
  }
  /**
   * return the offset of the PKA specific header extension
   */
  protected int getExtHdrOffset() {
    return 17;
  }   
}