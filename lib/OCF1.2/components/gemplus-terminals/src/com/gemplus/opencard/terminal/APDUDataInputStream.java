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
import opencard.core.terminal.*;

/**
 * Class <code>APDUDataInputStream</code> provides facilities to
 * decode an APDU response field. An input stream can be build
 * from a byte array or directly a <code>ResponseAPDU</code> object.
 * Stream can be read as <code>byte</code>, <code>short</code> or
 * <code>int</code> or arrays of those types.
 *
 * @version $Id: APDUDataInputStream.java,v 0.2 1999/03/24 16:25:13 root Exp root $
 * @author  Patrick.Biget@research.gemplus.com
 * @see     opencard.core.terminal.ResponseAPDU
 * @see     com.gemplus.opencard.terminal.APDUDataOutputStream
 **/
public class APDUDataInputStream
{
    private boolean bigEndian = true;
    private byte[] data;
    private int index;
    private int size;

    /**
     * Constructs an <code>APDUDataInputStream</code> object.
     * This stream lets an application read primitive Java data types
     * from an underlying APDU data (passed as an array of bytes).
     *
     * @param data  array on which is applied the stream
     **/
    public APDUDataInputStream(byte[] data)
    {
        this.data=data;
        index=0;
        if (data==null) size=0; else size=data.length;
    }

    /**
     * Constructs an <code>APDUDataInputStream</code> object.
     * This stream lets an application read primitive Java data types
     * from an underlying APDU data (passed as an APDU response).
     *
     * @param response  APDU response on which data is applied the stream
     **/
    public APDUDataInputStream(ResponseAPDU response)
    {
        if (response==null) data=null; else data=response.data();
        index=0;
        if (data==null) size=0; else size=data.length;
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
     * Returns the number of remaining bytes available in the stream.
     *
     * @return  the remaining bytes available
     **/
    public int available()
    {
       return size-index;
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
     * Returns a boolean representation of the byte at current position
     * in the stream.
     *
     * @return  <code>false</code> if the value of the current byte
     * is set to 0, <code>true</code> else.
     **/
    public boolean readBoolean()
    {
        return (data[index++]!=0);
    }

    /**
     * Returns a <code>boolean</code> array representation of the
     * entire stream.
     *
     * @return  value of the entire stream
     **/
    public boolean[] readBooleanArray()
    {
        boolean[] result=new boolean[data.length];

        for (int i=0; i<result.length; i++)
            result[i]=(data[i]!=0);
            
        return result;
    }

    /**
     * Returns a <code>boolean</code> array representation of the bytes
     * at the current position in the stream.
     *
     * @param size  size of the resulting boolean array
     *
     * @return  value of the <code>size</code> <code>boolean</code>s
     * at the current position in the stream
     **/
    public boolean[] readBooleanArray(int size)
    {
        boolean[] result=new boolean[size];

        for (int i=0; i<result.length; i++)
            result[i]=(data[index++]!=0);

        return result;
    }

    /**
     * Returns the <code>byte</code> at current position in the stream.
     *
     * @return  value of the <code>byte</code> at current position in
     * the stream
     **/
    public byte readByte()
    {
        return data[index++];
    }

    /**
     * Returns a <code>byte</code> array representation of the entire stream.
     *
     * @return  value of the entire stream
     **/
    public byte[] readByteArray()
    {
        return data;
    }

    /**
     * Returns a <code>byte</code> array representation of the bytes at the
     * current position in the stream.
     *
     * @param size  size (in <code>byte</code>s) of the resulting byte array
     *
     * @return  value of the <code>size</code> <code>byte</code>s at the
     * current position in the stream
     **/
    public byte[] readByteArray(int size)
    {
        byte[] result=new byte[size];
        System.arraycopy(data,index,result,0,size);
        index+=size;
        return result;
    }

    /**
     * Returns a <code>short</code> representation of the 2 bytes at
     * current position in the stream.
     *
     * @return  value of the <code>short</code> at current position
     * in the stream
     **/
    public short readShort()
    {
        short result;

        if (bigEndian)
            result=(short)(((data[index++]&0x000000FF)<<8)+(data[index++]&0x000000FF));
        else
            result=(short)((data[index++]&0x000000FF)+((data[index++]&0x000000FF)<<8));

        return result;
    }

    /**
     * Returns a <code>short</code> array representation of the entire stream.
     *
     * @return  value of the entire stream
     **/
    public short[] readShortArray()
    {
        short[] result=new short[data.length/2];

        for (int i=0; i<result.length; i++)
        {
            if (bigEndian)
                result[i]=(short)(((data[i*2]&0x000000FF)<<8)+(data[(i*2)+1]&0x000000FF));
            else
                result[i]=(short)((data[i*2]&0x000000FF)+((data[(i*2)+1]&0x000000FF)<<8));
        }

        return result;
    }

    /**
     * Returns a <code>short</code> array representation of the bytes
     * at the current position in the stream.
     *
     * @param size  size (in <code>short</code>s) of the resulting byte array
     *
     * @return  value of the <code>size</code> <code>short</code>s
     * at the current position in the stream
     **/
    public short[] readShortArray(int size)
    {
        short[] result=new short[size];

        for (int i=0; i<result.length; i++)
        {
            if (bigEndian)
                result[i]=(short)(((data[index++]&0x000000FF)<<8)+(data[index++]&0x000000FF));
            else
                result[i]=(short)((data[index++]&0x000000FF)+((data[index++]&0x000000FF)<<8));
        }

        return result;
    }

    /**
     * Returns an <code>int</code> representation of the 4 bytes
     * at current position in the stream.
     *
     * @return  value of the <code>int</code> at current position in the stream
     **/
    public int readInt()
    {
        int result;

        if (bigEndian)
            result=((data[index++]&0x000000FF)<<24)
                  +((data[index++]&0x000000FF)<<16)
                  +((data[index++]&0x000000FF)<<8)
                  + (data[index++]&0x000000FF);
        else
            result= (data[index++]&0x000000FF)
                  +((data[index++]&0x000000FF)<<8)
                  +((data[index++]&0x000000FF)<<16)
                  +((data[index++]&0x000000FF)<<24);

        return result;
    }

    /**
     * Returns a <code>int</code> array representation of the entire stream.
     *
     * @return  value of the entire stream
     **/
    public int[] readIntArray()
    {
        int[] result=new int[data.length/4];

        for (int i=0; i<result.length; i++)
        {
            if (bigEndian)
                result[i]=((data[i*4    ]&0x000000FF)<<24)
                         +((data[(i*4)+1]&0x000000FF)<<16)
                         +((data[(i*4)+2]&0x000000FF)<<8)
                         + (data[(i*4)+3]&0x000000FF);
            else
                result[i]= (data[i*4    ]&0x000000FF)
                         +((data[(i*4)+1]&0x000000FF)<<8)
                         +((data[(i*4)+2]&0x000000FF)<<16)
                         +((data[(i*4)+3]&0x000000FF)<<24);
        }

        return result;
    }

    /**
     * Returns a <code>int</code> array representation of the bytes
     * at the current position in the stream.
     *
     * @param size  size (in <code>int</code>s) of the resulting byte array
     *
     * @return  value of the <code>size int</code>s at the current
     * position in the stream
     **/
    public int[] readIntArray(int size)
    {
        int[] result=new int[size];

        for (int i=0; i<result.length; i++)
        {
            if (bigEndian)
                result[i]=((data[index++]&0x000000FF)<<24)
                         +((data[index++]&0x000000FF)<<16)
                         +((data[index++]&0x000000FF)<<8)
                         + (data[index++]&0x000000FF);
            else
                result[i]= (data[index++]&0x000000FF)
                         +((data[index++]&0x000000FF)<<8)
                         +((data[index++]&0x000000FF)<<16)
                         +((data[index++]&0x000000FF)<<24);
        }

        return result;
    }

    /**
     * Returns a <code>String</code> representation of the entire stream.
     *
     * @return  value of the entire stream
     **/
    public String readString()
    {
        return new String(data);
    }

    /**
     * Returns a <code>String</code> representation of the bytes
     * at the current position in the stream.
     *
     * @param size  size (in characters) of the resulting <code>String</code>
     *
     * @return  value of the <code>size</code> characters at the
     * current position in the stream
     **/
    public String readString(int size)
    {
        String result=new String(data,index,size);
        index+=size;
        return result;
    }

    /**
     * Skip <code>size</code> bytes from the current position in the stream.
     *
     * @param size  number of bytes to skip
     *
     * @return  void
     **/
    public void skipBytes(int size)
    {
        index+=size;
    }

}
