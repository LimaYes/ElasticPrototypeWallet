package com.community;

import com.community.CTypeInts.IntegerType;
import com.community.CTypeInts.Uint32_t;
import nxt.util.Convert;

import java.util.*;

import static com.community.EnigmaProgram.MEM_TARGET_GET.*;
import static com.community.EnigmaProgram.MEM_TARGET_STORE.*;

/******************************************************************************
 * Copyright © 2017 The XEL Core Developers.                                  *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * XEL software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/
public class EnigmaProgram {


    static class StackElement {
        private byte[] content;
        private MEM_TARGET_STORE type;

        public byte[] getContent() {
            return content;
        }

        public MEM_TARGET_STORE getType() {
            return type;
        }

        public StackElement(byte[] content, MEM_TARGET_STORE type) {
            this.content = content;
            this.type = type;
        }

        public StackElement(){
            this.content = null;
            this.type = null;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public void setType(MEM_TARGET_STORE type) {
            this.type = type;
        }
    }

    enum MEM_TARGET_GET {
        GET_U,
        GET_I,
        GET_D,
        GET_L,
        GET_UL,
        GET_F,
        GET_M
    }

    enum MEM_TARGET_STORE {
        U,
        I,
        D,
        L,
        UL,
        F
    }

    // Limits
    private static final int MAX_DEPTH = 1024;
    private static final int MAX_STACKSIZE = 1024;
    private static final long MAX_MEMORY_BYTES = 5 * 1024 * 1024; // 5 Megabytes is ok?

    // Temporary Storage Limits
    Map<Integer, byte[]> u_storage = new HashMap<>();
    Map<Integer, byte[]> f_storage = new HashMap<>();
    Map<Integer, byte[]> d_storage = new HashMap<>();
    Map<Integer, byte[]> l_storage = new HashMap<>();
    Map<Integer, byte[]> ul_storage = new HashMap<>();
    Map<Integer, byte[]> i_storage = new HashMap<>();
    int[] m_array;


    // VM internals
    private Stack stack;
    private byte[] returnDataBuffer;
    private byte[] operations;
    private int pc;
    private byte lastOperation;
    private byte previouslyExecutedOp;
    private boolean stopped;
    private long currently_used_memory;

    private MEM_TARGET_STORE mapMemTarget(MEM_TARGET_GET t) {
        switch (t) {
            case GET_U:
                return U;
            case GET_I:
                return I;
            case GET_UL:
                return UL;
            case GET_L:
                return L;
            case GET_F:
                return U;
            case GET_D:
                return U;
            default:
                return U; // UNSIGNED INT 32 IS FALLBACK !!!
        }
    }

    public void load(MEM_TARGET_GET target, Integer key) throws EnigmaVM.EnigmaException {
        Map<Integer, byte[]> tgt_map = u_storage;
        if (target == GET_U) {
            tgt_map = u_storage;
        } else if (target == GET_I) {
            tgt_map = i_storage;
        } else if (target == GET_L) {
            tgt_map = l_storage;
        } else if (target == GET_UL) {
            tgt_map = ul_storage;
        } else if (target == GET_F) {
            tgt_map = f_storage;
        } else if (target == GET_D) {
            tgt_map = d_storage;
        } else if (target == GET_M) {
            tgt_map = null;
        }

        if (tgt_map == null) {
            // special case, M array
            if (key.intValue() < 0 || key.intValue() >= m_array.length) {
                stackPush(new StackElement(new byte[0], mapMemTarget(target)));
            } else {
                stackPush(new StackElement(Convert.nullToEmptyPacked(Convert.int2byte(new int[]{m_array[key.intValue
                        ()]}), 32 / 8), mapMemTarget(target)));
            }
        } else {
            if (!tgt_map.containsKey(key)) {
                stackPush(new StackElement(new byte[0], mapMemTarget(target)));
            } else {
                stackPush(new StackElement(tgt_map.get(key), mapMemTarget(target)));
            }
        }
    }

    public void store(MEM_TARGET_STORE target, Integer key, byte[] value) throws EnigmaVM.EnigmaException {

        Map<Integer, byte[]> tgt_map = u_storage;
        int cap = 32;
        if (currently_used_memory + cap / 8 > MAX_MEMORY_BYTES) {
            throw new EnigmaVM.EnigmaException("Maximum memory of " + MAX_MEMORY_BYTES + " bytes exceeded.");
        }
        if (target == U) {
            tgt_map = u_storage;
            cap = 32;
        } else if (target == I) {
            tgt_map = i_storage;
            cap = 32;
        } else if (target == L) {
            tgt_map = l_storage;
            cap = 64;
        } else if (target == UL) {
            tgt_map = ul_storage;
            cap = 64;
        } else if (target == F) {
            tgt_map = f_storage;
            cap = 64;
        } else if (target == D) {
            tgt_map = d_storage;
            cap = 64;
        }

        boolean nullbyte = false;
        if (Convert.emptyToNull(value) == null)
            nullbyte = true;

        if (!tgt_map.containsKey(key)) {
            if (nullbyte == true)
                currently_used_memory -= cap / 8;
            else
                currently_used_memory += cap / 8; // 32bit=4byte stored into U
        }

        if (nullbyte == true)
            tgt_map.remove(key);
        else
            tgt_map.put(key, Convert.nullToEmptyPacked(value, cap / 8));
    }

    public void dumpStack() {
        System.out.println("Final Stack Dump:");
        System.out.println("=================");
        for (int i = 0; i < getStackSize(); ++i) {
            System.out.println(i + ": " + Convert.toHexString((byte[]) stack.get(i)));
        }
    }

    public void dumpStorage(MEM_TARGET_STORE target) throws EnigmaVM.EnigmaException {
        Map<Integer, byte[]> tgt_map = u_storage;

        if (target == U) {
            tgt_map = u_storage;
            System.out.println("Dumping VM Storage (U):");
            System.out.println("=======================");
        } else if (target == I) {
            tgt_map = i_storage;
            System.out.println("Dumping VM Storage (I):");
            System.out.println("=======================");
        } else if (target == L) {
            tgt_map = l_storage;
            System.out.println("Dumping VM Storage (L):");
            System.out.println("=======================");
        } else if (target == UL) {
            tgt_map = ul_storage;
            System.out.println("Dumping VM Storage (UL):");
            System.out.println("========================");
        } else if (target == F) {
            tgt_map = f_storage;
            System.out.println("Dumping VM Storage (F):");
            System.out.println("=======================");
        } else if (target == D) {
            tgt_map = d_storage;
            System.out.println("Dumping VM Storage (D):");
            System.out.println("=======================");
        }
        List<Integer> keyList = new ArrayList<Integer>(tgt_map.keySet());
        Collections.sort(keyList);
        for (int i = 0; i < keyList.size(); i++) {
            System.out.println(keyList.get(i) + ":\t" + Convert.toHexString(tgt_map.get(keyList.get(i))));
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
        if (this.pc < 0) this.pc = 0;
        if (this.pc >= this.operations.length) stop();
    }

    public void stepForward() {
        setPc(this.pc + 1);
    }

    public byte getOperation(int pc) {
        return (operations.length <= pc) ? 0 : operations[pc];
    }

    public byte getCurrentOperation() {
        if (operations.length == 0 || this.stopped) return 0x00;
        return operations[pc];
    }

    public void stackPush(StackElement ctype) {
        stack.push(ctype);
    }

    public int getStackSize() {
        return this.stack.size();
    }

    public Stack getStack() {
        return this.stack;
    }

    public StackElement stackPop() {
        return (StackElement) stack.pop();
    }

    public byte[] getProgramByteCode() {
        return operations;
    }

    public byte[] sweepNextOperations(int n) {
        if (pc + n >= operations.length)
            stop();

        byte[] data = Arrays.copyOfRange(operations, pc, Math.min(pc + n, operations.length));
        pc += n;

        return data;
    }


    public EnigmaProgram(byte[] ops, int[] m_array) {
        this.operations = Convert.nullToEmpty(ops);
        this.stack = new Stack();
        this.pc = 0;
        this.stopped = false;
        this.lastOperation = 0x00;
        this.previouslyExecutedOp = 0x00;
        this.returnDataBuffer = new byte[256]; // Note: Lets reserve enough space for future return values, currently
        // we just have isPow and isBty
        this.m_array = m_array;
    }


}
