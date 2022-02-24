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

/**
 * Class <code>ExtendedCommandAPDU</code> constructed directly by using
 * the low-level <code>opencard.core.terminal.CommandAPDU</code> class.<br>
 * This class provides various constructors as well as a stream management
 * for the APDU input data field.
 *
 * @version $Id: ExtendedCommandAPDU.java,v 0.2 1999/03/24 16:33:28 root Exp root $
 * @author  Patrick.Biget@research.gemplus.com
 * @see     opencard.core.terminal.CommandAPDU
 **/

import opencard.core.terminal.CommandAPDU;

public class ExtendedCommandAPDU extends CommandAPDU
{
    private byte cla, ins, p1, p2, data[]=null, le=0;

    private final int CLASS         = 0;
    private final int INSTRUCTION   = 1;
    private final int PARAMETER1    = 2;
    private final int PARAMETER2    = 3;
    private final int LENGTH        = 5;
    private final int HEADER_LENGTH = 5;

    /**
     * Constructs an empty ISO 7816-4 APDU command as a
     * <code>ExtendedCommandAPDU</code> object.
     * This APDU can be build after creation using setClassByte,
     * setInstructionByte... This <code>ExtendedCommandAPDU</code>
     * must then be validated using the <code>flush()</code> method.
     *
     * @param   size
     *          Size of the buffer
     **/
    public ExtendedCommandAPDU(int size)
    {
        // Create a new CommandAPDU object
        super(size);
    }

    /**
     * Constructs an ISO 7816-4 case 1 APDU command as a
     * <code>ExtendedCommandAPDU</code> object.
     *
     * @param   cla
     *          CLASS byte
     *
     * @param   ins
     *          INSTRUCTION byte
     *
     * @param   p1
     *          PARAMETER1 byte
     *
     * @param   p2
     *          PARAMETER2 byte
     **/
    public ExtendedCommandAPDU(byte cla, byte ins, byte p1, byte p2)
    {
        // Create a new CommandAPDU object
        super(4);
        setLength(0);

        // Class byte
        this.cla=cla; append(cla);

        // Instruction byte
        this.ins=ins; append(ins);

        // Parameters bytes
        this.p1=p1; append(p1);
        this.p2=p2; append(p2);
    }

    /**
     * Constructs an ISO 7816-4 case 2 APDU command as a
     * <code>ExtendedCommandAPDU</code> object.
     *
     * @param   cla
     *          CLASS byte
     *
     * @param   ins
     *          INSTRUCTION byte
     *
     * @param   p1
     *          PARAMETER1 byte
     *
     * @param   p2
     *          PARAMETER2 byte
     *
     * @param   data
     *          Input data field
     **/
    public ExtendedCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data)
    {
        // Create a new CommandAPDU object
        super(5+data.length);
        setLength(0);

        // Class byte
        this.cla=cla; append(cla);

        // Instruction byte
        this.ins=ins; append(ins);

        // Parameters bytes
        this.p1=p1; append(p1);
        this.p2=p2; append(p2);

        // Input data length byte
        append((byte)data.length);

        // Input data bytes
        this.data=data; append(data);
    }

    /**
     * Constructs an ISO 7816-4 case 3 APDU command as a
     * <code>ExtendedCommandAPDU</code> object.
     *
     * @param   cla
     *          CLASS byte
     *
     * @param   ins
     *          INSTRUCTION byte
     *
     * @param   p1
     *          PARAMETER1 byte
     *
     * @param   p2
     *          PARAMETER2 byte
     *
     * @param   le
     *          Length of expected data
     **/
    public ExtendedCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte le)
    {
        // Create a new CommandAPDU object
        super(5);
        setLength(0);

        // Class byte
        this.cla=cla; append(cla);

        // Instruction byte
        this.ins=ins; append(ins);

        // Parameters bytes
        this.p1=p1; append(p1);
        this.p2=p2; append(p2);

        // Length expected
        this.le=le; append(le);
    }

    /**
     * Constructs an ISO 7816-4 case 3 APDU command as a
     * <code>ExtendedCommandAPDU</code> object.
     *
     * @param   cla
     *          CLASS byte
     *
     * @param   ins
     *          INSTRUCTION byte
     *
     * @param   p1
     *          PARAMETER1 byte
     *
     * @param   p2
     *          PARAMETER2 byte
     *
     * @param   data
     *          Input data field
     *
     * @param   le
     *          Length of expected data
     **/
    public ExtendedCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data, byte le)
    {
        // Create a new CommandAPDU object
        super(6+data.length);
        setLength(0);

        // Class byte
        this.cla=cla; append(cla);

        // Instruction byte
        this.ins=ins; append(ins);

        // Parameters bytes
        this.p1=p1; append(p1);
        this.p2=p2; append(p2);

        // Input data length byte
        append((byte)data.length);

        // Input data bytes
        this.data=data; append(data);

        // Length expected
        this.le=le; append(le);
    }

    /**
     * Set the CLASS byte of this <code>ExtendedCommandAPDU</code> object.
     * The method <code>flush()</code> must be used when
     * ExtendedCommandAPDU is OK.
     *
     * @param   cla
     *          value to set for CLASS byte
     **/
    public void setClassByte(byte cla)
    {
        this.cla=cla;
    }

    /**
     * Return the CLASS byte of this <code>ExtendedCommandAPDU</code> object.
     **/
    public byte getClassByte()
    {
        return cla;
    }

    /**
     * Set the INSTRUCTION byte of this <code>ExtendedCommandAPDU</code>
     * object. The method <code>flush()</code> must be used when
     * ExtendedCommandAPDU is OK.
     *
     * @param   ins
     *          value to set for INSTRUCTION byte
     **/
    public void setInstructionByte(byte ins)
    {
        this.ins=ins;
    }

    /**
     * Return the INSTRUCTION byte of this <code>ExtendedCommandAPDU</code>
     * object.
     **/
    public byte getInstructionByte()
    {
        return ins;
    }

    /**
     * Set the PARAMETER1 byte of this <code>ExtendedCommandAPDU</code> object.
     * The method <code>flush()</code> must be used when ExtendedCommandAPDU
     * is OK.
     *
     * @param   p1
     *          value to set for PARAMETER1 byte
     **/
    public void setParameter1Byte(byte p1)
    {
        this.p1=p1;
    }

    /**
     * Return the PARAMETER1 byte of this <code>ExtendedCommandAPDU</code>
     * object.
     **/
    public byte getParameter1Byte()
    {
        return p1;
    }

    /**
     * Set the PARAMETER2 byte of this <code>ExtendedCommandAPDU</code> object.
     * The method <code>flush()</code> must be used when ExtendedCommandAPDU
     * is OK.
     *
     * @param   p2
     *          value to set for PARAMETER2 byte
     **/
    public void setParameter2Byte(byte p1)
    {
        this.p2=p2;
    }

    /**
     * Return the PARAMETER2 byte of this <code>ExtendedCommandAPDU</code>
     * object.
     **/
    public byte getParameter2Byte()
    {
        return p2;
    }

    /**
     * Set the input data of this <code>ExtendedCommandAPDU</code> object.
     * The method <code>flush()</code> must be used when ExtendedCommandAPDU
     * is OK.
     *
     * @param   data
     *          value to set for input data field
     **/
    public void setInputDataBytes(byte[] data)
    {
        this.data=data;
    }

    /**
     * Return the input data of this <code>ExtendedCommandAPDU</code> object.
     **/
    public byte[] getInputDataBytes()
    {
        return data;
    }

    /**
     * Validate the modifications applied on this
     * <code>ExtendedCommandAPDU</code> object.
     **/
    public void flush()
    {
        // Reset buffer
        setLength(0);

        // Class byte
        append(cla);

        // Instruction byte
        append(ins);

        // Parameters bytes
        append(p1);
        append(p2);

        // Input data bytes
        if (data!=null)
        {
            append((byte)data.length);
            append(data);
        }

        // Length expected
        append(le);
    }
}
