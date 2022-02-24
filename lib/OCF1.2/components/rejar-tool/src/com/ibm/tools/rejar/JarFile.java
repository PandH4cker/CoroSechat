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

package com.ibm.tools.rejar;

import java.util.Enumeration;
import java.util.zip.*;
import java.io.*;

/**
 * <code>JarFile</code> provides a few methods for creating jarfiles...
 *
 * @author   Stephan Breideneich (sbreiden@de.ibm.com)
 * @version  $Id: JarFile.java,v 1.3 1998/10/14 13:32:29 cvsusers Exp $
 */
public class JarFile {

  private final static String METAINF_DIR = "META-INF/";

  private ZipOutputStream zipOutputStream = null;
  private Object jarMonitor = "jarfile monitor";

  /** constructor for the JarFile - use create to instantiate a JarFile object
   * see #create
   */
  private JarFile(ZipOutputStream zos) {
    zipOutputStream = zos;
  }

  /** create a new JarFile instance
   *
   * @param fileName The filename for the jar-archive.
   */
  public static JarFile create(File newFile) throws Exception {
    OutputStream os = (OutputStream)(new FileOutputStream(newFile));
    return new JarFile(new ZipOutputStream(os));
  }
  
  /** add a directory to the jar archive
   * @param pathName
   *        Contains directory name to add to the jarfile.
   *        Format precondition: no leader "/" or "./" and path must end with "/"   
   */
  public void addDirectory(String pathName) throws Exception {
    synchronized(jarMonitor) {
      // ignore ZipExceptions (because of ignoring duplicate entries)
      ZipEntry e = new ZipEntry(pathName);
      e.setMethod(ZipEntry.STORED);
      e.setSize(0);
      e.setCrc(0);
      zipOutputStream.putNextEntry(e);
    }
  }
  
  
  /** add a file to the jar archive
   *
   * @param fileName The name of the entry for this file in the jar-archive.
   *                 Precondition: no leader "/".
   * @param is       Copies the whole data from the InputStream into the
   *                 jar-entry.
   */
  public void addFile(String fileName, int fileSize, InputStream is) throws Exception {
    synchronized(jarMonitor) {    
      byte[] buffer = new byte[2048];
      int len;
      int remainingBytes;        
      ZipEntry e = new ZipEntry(fileName);
      BufferedInputStream bis = new BufferedInputStream(is);
        
      // add new entry in jarfile
      zipOutputStream.putNextEntry(e);
      
      // copy input data to the jar entry
      remainingBytes = fileSize;
      while ((remainingBytes > 0) 
             && (len = bis.read(buffer, 
                                0, 
                                Math.min(buffer.length, remainingBytes))) != -1) {
        remainingBytes -= len;
        zipOutputStream.write(buffer, 0, len);
      }
      zipOutputStream.flush();

      // close input streams
      bis.close();
      is.close();
        
      // close file entry
      zipOutputStream.closeEntry();
    }
  }

  /** close jarfile
   */
  public void close() throws Exception {
    synchronized(jarMonitor) {
      zipOutputStream.close();
    }
  }

  public static void copyJar2Jar(PrintWriter msg, File destinationJar, File[] sourceJars) {
    try {
      msg.println("create " + destinationJar);
      msg.flush();

      JarFile jf = JarFile.create(destinationJar);

      for (int i=0; i<sourceJars.length; i++) {
        ZipFile zf = new ZipFile(sourceJars[i]);

        for (Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
          ZipEntry ze = (ZipEntry)entries.nextElement();
          if (ze.isDirectory()) {
            // check for META-INF and if found ignore it
            if (!ze.getName().equalsIgnoreCase(METAINF_DIR)) {
              msg.println("add directory: " + ze.getName());
              msg.flush();
              try {
                jf.addDirectory(ze.getName());
              } catch(ZipException e) {
                msg.println(e.toString());
                msg.flush();
              }              
            }
          } else {
            // check for metafile and skip this
            String s = ze.getName().substring(0, Math.min(ze.getName().length(), METAINF_DIR.length()));
            if (!s.equalsIgnoreCase(METAINF_DIR)) {
              msg.println("add file: " + ze.getName());
              msg.flush();
              try {
                jf.addFile(ze.getName(), (int)ze.getSize(), zf.getInputStream(ze));
              } catch(ZipException e) {
                msg.println(e.toString());
                msg.flush();
              }
            }
          }
        }
      }
      jf.close();
    } catch (Exception e) {
      e.printStackTrace();
      msg.println(e.toString());
      msg.flush();
    }
  }
}