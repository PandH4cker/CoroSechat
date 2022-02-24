/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */

package com.gemplus.opencard.terminal;

import java.io.*;
import java.lang.*;
import opencard.core.terminal.*;

/**
 * Class <code>APDUDataOutputStream</code> provides facilities to
 * encode an APDU command field. An output stream  stream can be build
 * from a byte array or directly a <code>ResponseAPDU</code> object.
 * Stream can be read as <code>byte</code>, <code>short</code> or
 * <code>int</code> or arrays of those types.
 *
 * @version $Id: APDUDataOutputStream.java,v 0.2 1999/03/24 16:08:07 root Exp root $
 * @author  Patrick.Biget@research.gemplus.com
 * @see     opencard.core.terminal.ResponseAPDU
 * @see     com.gemplus.opencard.terminal.APDUDataOutputStream
 **/
public class APDUDataOutputStream
{
    private boolean bigEndian = true;
    private ByteArrayOutputStream baos;
    private int size;

    /**
     * Constructs an <code>APDUDataOutputStream</code> object.
     * This stream lets an application write primitive Java data types
     * into an underlying APDU data.<br>
     * Data entered are then accessible as a byte array
     * (using method <code>toByteArray()</code>)
     **/
    public APDUDataOutputStream()
    {
        baos=new ByteArrayOutputStream();
        baos.reset();
        size=0;
    }

    /**
     * Returns total number of <code>byte</code>s entered in the stream.
     *
     * @return  the size of data entered
     **/
    public int size()
    {
       return size;
    }

    /**
     * Sets big endian or little endian mode.
     *
     * @param bigEndian<br>
     * if <code>true</code>, big endian mode is set (default mose)<br>
     * if <code>false</code>, little endian mode is set
     **/
    public void setBigEndian(boolean bigEndian)
    {
        this.bigEndian=bigEndian;
    }

    /**
     * Writes a <code>boolean</code> at current position in the stream.
     *
     * @param value  value of the boolean to be written
     **/
    public void write(boolean value)
    {
        baos.write(value?1:0);
        size++;
    }

    /**
     * Writes a <code>boolean</code> array at the current position
     * in the stream.
     *
     * @param value  value of the array to be written
     **/
    public void write(boolean[] value)
    {
        for (int i=0; i<value.length; i++)
            baos.write(value[i]?1:0);
        size+=value.length;
    }

    /**
     * Writes a <code>boolean</code> array at the current position
     * in the stream.
     *
     * @param value     value of the array to be written
     * @param offset    offset from which the array will be written
     * in the stream
     * @param length    number of <code>boolean</code>s to write
     **/
    public void write(boolean[] value, int offset, int length)
    {
        for (int i=0; i<length; i++)
            baos.write(value[offset+i]?1:0);
        size+=length;
    }

    /**
     * Writes a <code>byte</code> at current position in the stream.
     *
     * @param value  value of the <code>byte</code> to be written
     **/
    public void write(byte value)
    {
        baos.write((int)value);
        size++;
    }

    /**
     * Writes a <code>byte</code> array at current position in the stream.
     *
     * @param value  value of the <code>byte</code> array to be written
     **/
    public void write(byte[] value)
    {
        baos.write(value, 0, value.length);
        size+=value.length;
    }

    /**
     * Writes a <code>byte</code> array at current position in the stream.
     *
     * @param value     value of the <code>byte</code> array to be written
     * @param offset    offset from which the array will be written
     * in the stream
     * @param length    number of <code>byte</code>s to write
     **/
    public void write(byte[] value, int offset, int length)
    {
        baos.write(value, offset, length);
        size+=length;
    }

    /**
     * Writes a <code>short</code> at current position in the stream.
     *
     * @param value  value of the <code>short</code> tobe written
     **/
    public void write(short value)
    {
        if (bigEndian)
        {
            baos.write((int)((value&0x0000FF00)>>8));
            baos.write((int) (value&0x000000FF));
        }
        else
        {
            baos.write((int) (value&0x000000FF));
            baos.write((int)((value&0x0000FF00)>>8));
        }
        size+=2;
    }

    /**
     * Writes a <code>short</code> array at the current position in the stream.
     *
     * @param value  value of the array to be written
     **/
    public void write(short[] value)
    {
        for (int i=0; i<value.length; i++)
        {
            if (bigEndian)
            {
                baos.write((int)((value[i]&0x0000FF00)>>8));
                baos.write((int) (value[i]&0x000000FF));
            }
            else
            {
                baos.write((int) (value[i]&0x000000FF));
                baos.write((int)((value[i]&0x0000FF00)>>8));
            }
        }
        size+=(value.length*2);
    }

    /**
     * Writes a <code>short</code> array at the current position in the stream.
     *
     * @param value     value of the array to be written
     * @param offset    offset from which the array will be written
     * in the stream
     * @param length    number of <code>short</code>s to write
     **/
    public void write(short[] value, int offset, int length)
    {
        for (int i=0; i<length; i++)
        {
            if (bigEndian)
            {
                baos.write((int)((value[offset+i]&0x0000FF00)>>8));
                baos.write((int) (value[offset+i]&0x000000FF));
            }
            else
            {
                baos.write((int) (value[offset+i]&0x000000FF));
                baos.write((int)((value[offset+i]&0x0000FF00)>>8));
            }
        }
        size+=(length*2);
    }

    /**
     * Writes an <code>int</code> at current position in the stream.
     *
     * @param value  value of the <code>int</code> to be written
     **/
    public void write(int value)
    {
        if (bigEndian)
        {
            baos.write((int)((value&0xFF000000)>>24));
            baos.write((int)((value&0x00FF0000)>>16));
            baos.write((int)((value&0x0000FF00)>> 8));
            baos.write((int) (value&0x000000FF));
        }
        else
        {
            baos.write((int) (value&0x000000FF));
            baos.write((int)((value&0x0000FF00)>> 8));
            baos.write((int)((value&0x00FF0000)>>16));
            baos.write((int)((value&0xFF000000)>>24));
        }
        size+=4;
    }

    /**
     * Writes an <code>int</code> array at the current position in the stream.
     *
     * @param value  value of the array to be written
     **/
    public void write(int[] value)
    {
        for (int i=0; i<value.length; i++)
        {
            if (bigEndian)
            {
                baos.write((int)((value[i]&0xFF000000)>>24));
                baos.write((int)((value[i]&0x00FF0000)>>16));
                baos.write((int)((value[i]&0x0000FF00)>> 8));
                baos.write((int) (value[i]&0x000000FF));
            }
            else
            {
                baos.write((int) (value[i]&0x000000FF));
                baos.write((int)((value[i]&0x0000FF00)>> 8));
                baos.write((int)((value[i]&0x00FF0000)>>16));
                baos.write((int)((value[i]&0xFF000000)>>24));
            }
        }
        size+=(value.length*4);
    }

    /**
     * Writes an <code>int</code> array at the current position in the stream.
     *
     * @param value     value of the array to be written
     * @param offset    offset from which the array will be written
     * in the stream
     * @param length    number of <code>int</code>s to write
     **/
    public void write(int[] value, int offset, int length)
    {
        for (int i=0; i<length; i++)
        {
            if (bigEndian)
            {
                baos.write((int)((value[offset+i]&0xFF000000)>>24));
                baos.write((int)((value[offset+i]&0x00FF0000)>>16));
                baos.write((int)((value[offset+i]&0x0000FF00)>> 8));
                baos.write((int) (value[offset+i]&0x000000FF));
            }
            else
            {
                baos.write((int) (value[offset+i]&0x000000FF));
                baos.write((int)((value[offset+i]&0x0000FF00)>> 8));
                baos.write((int)((value[offset+i]&0x00FF0000)>>16));
                baos.write((int)((value[offset+i]&0xFF000000)>>24));
            }
        }
        size+=(length*4);
    }
    
    /**
     * Writes a <code>String</code> at the current position in the stream.
     *
     * @param value     value of the <code>String</code> to be written
     **/
    public void write(String value)
    {
        baos.write(value.getBytes(),0,value.length());
        size+=(value.length());
    }
    
    /**
     * Writes a <code>String</code> at the current position in the stream.
     *
     * @param value     value of the <code>String</code> to be written
     * @param offset    offset from which the string will be written
     * in the stream
     * @param length    number of characters to write
     **/
    public void write(String value, int offset, int length)
    {
        baos.write(value.getBytes(),offset,length);
        size+=length;
    }
    
    /**
     * Convert the data entered in the stream as a <code>byte</code> array
     *
     * @return  the <code>byte</code> array
     **/
    public byte[] toByteArray() throws APDUDataStreamException
    {
        try
        {
            baos.flush();
        }
        catch (IOException ioe)
        {
            throw new APDUDataStreamException("Error when trying to convert the APDU data output stream to a byte array");
        }
        return baos.toByteArray();
    }
    
    /**
     * Reset the stream
     *
     * @return  void
     **/
    public void reset()
    {
        size=0;
        baos.reset();
    }
    
    /**
     * Close the stream (release attached resources)
     *
     * @return  void
     **/
    public void close() throws APDUDataStreamException
    {
        try
        {
            baos.close();
        }
        catch (IOException ioe)
        {
            throw new APDUDataStreamException("Error when closing the APDU data output stream");
        }
    }
    
    /**
     * Close the stream (release attached resources)
     *
     * @return  void
     **/
    protected void finalize()
    {
        try
        {
            baos.close();
        }
        catch (Exception e)
        {
            // Exceptions are not handled
        }
    }

}
