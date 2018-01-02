package com.community;

import nxt.util.Convert;

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
public class EnigmaVM {

    static class EnigmaException extends Exception {

        // Parameterless Constructor
        public EnigmaException() {
        }

        // Constructor that accepts a message
        public EnigmaException(String message) {
            super(message);
        }
    }

    public static void stepProgram(EnigmaProgram prog) throws EnigmaException {

        byte[] value;
        byte[] key;
        int integerKey;
        EnigmaStackElement a,b,c,d;
        int sweep_num = 0;


        EnigmaProgram.MEM_TARGET_STORE estimatedType = null;

        if(prog.isStopped())
            return;

        EnigmaOpCode op = EnigmaOpCode.findOpCode(prog.getCurrentOperation());

        if (op == null) {
            throw new EnigmaException(String.format("Unknown OP-Code: %x", prog.getCurrentOperation()));
        }

        // Make sure stack is large enough
        if (prog.getStackSize() < op.getInputs()) {
            throw new EnigmaException(String.format("OP-Code %s requires %d elements on the stack but only %d were " +
                    "found", op.getStringRepr(), op.getInputs(), prog.getStackSize()));
        }

        // TODO: Implement computational limits similar to GAS


        // Execution
        switch (op) {
            /*
            BEGIN SECTION: STORE AND LOAD
             */
            case ENIGMA_ARRAY_INT_STORE:
                value = Convert.truncate(prog.stackPop().getContent(), 32);
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.I, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_UINT_STORE:
                value = Convert.truncate(prog.stackPop().getContent(), 32);
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.U, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_LONG_STORE:
                value = prog.stackPop().getContent();
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.L, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_ULONG_STORE:
                value = prog.stackPop().getContent();
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.UL, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_FLOAT_STORE:
                value = prog.stackPop().getContent();
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.F, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_DOUBLE_STORE:
                value = prog.stackPop().getContent();
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.store(EnigmaProgram.MEM_TARGET_STORE.D, integerKey, value);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_INT_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_I, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_UINT_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_U, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_LONG_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_L, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_ULONG_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_UL, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_FLOAT_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_F, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_DOUBLE_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_D, integerKey);
                prog.stepForward();
                break;
            case ENIGMA_ARRAY_M_LOAD:
                key = Convert.truncate(prog.stackPop().getContent(), 32); // truncated to 32bit, we allow 2^32 keys
                integerKey = Convert.bytesToInt(key);
                prog.load(EnigmaProgram.MEM_TARGET_GET.GET_M, integerKey);
                prog.stepForward();
                break;

            /*
            BEGIN SECTION: PUSHDATA
             */
            // TODO: Make sure to check what happens if ops array is at end before stepForward() the calls.
            case ENIGMA_PUSH_TYPED_DATA:
                prog.stepForward();
                integerKey = (int) prog.getCurrentOperation();
                if( integerKey >= EnigmaProgram.MEM_TARGET_STORE.U.ordinal() && integerKey <= EnigmaProgram.MEM_TARGET_STORE.F.ordinal())
                    estimatedType = EnigmaProgram.MEM_TARGET_STORE.values()[integerKey];
                // passthrough
            case ENIGMA_PUSHDATA:
                // This is tricky, a push opcode always is followed by one byte describing the length between 1 and
                // 64 and the data directy afterwards
                prog.stepForward();
                int numberToSweep = (int) prog.getCurrentOperation();
                if (numberToSweep > 64)
                    throw new EnigmaException(String.format("You can only push 64 byte at once to the " +
                            "stack"));
                if (numberToSweep < 1)
                    throw new EnigmaException(String.format("You have to push at least 1 byte to the " +
                            "stack"));
                prog.stepForward();
                byte[] toPush = prog.sweepNextOperations(numberToSweep);
                prog.stackPush(new EnigmaStackElement(Convert.nullToEmptyPacked(toPush, 64/8), estimatedType)); // type==null means unknown
                break;
            case ENIGMA_PUSHUINT_1: // easier push operations to be used in loops
                sweep_num = 1;
            case ENIGMA_PUSHUINT_2:
                if(sweep_num==0) sweep_num = 2;
            case ENIGMA_PUSHUINT_3:
                if(sweep_num==0) sweep_num = 3;
            case ENIGMA_PUSHUINT_4:
                if(sweep_num==0) sweep_num = 4;
                prog.stepForward();
                byte[] toPush_1 = prog.sweepNextOperations(sweep_num);
                prog.stackPush(new EnigmaStackElement(Convert.nullToEmptyPacked(toPush_1, 64/8), null)); // type==null means unknown, will be cast to either U or UL
                break;


            /*
            BEGIN SECTION: SIMPLE MATHEMATICAL OPERATORS
             */
            case ENIGMA_ADD:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.add(a,b));
                prog.stepForward();
                break;
            case ENIGMA_SUB:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.sub(a,b));
                prog.stepForward();
                break;
            case ENIGMA_MUL:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.mul(a,b));
                prog.stepForward();
                break;
            case ENIGMA_DIV:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.div(a,b));
                prog.stepForward();
                break;
            case ENIGMA_MOD:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.mod(a,b));
                prog.stepForward();
                break;
            case ENIGMA_BITWISE_AND:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.band(a,b));
                prog.stepForward();
                break;
            case ENIGMA_BITWISE_OR:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.bor(a,b));
                prog.stepForward();
                break;
            case ENIGMA_BITWISE_XOR:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.bxor(a,b));
                prog.stepForward();
                break;
            case ENIGMA_COMPL:
                a = prog.stackPop();
                prog.stackPush(EnigmaMathOps.compl(a));
                prog.stepForward();
                break;
            case ENIGMA_NOT:
                a = prog.stackPop();
                prog.stackPush(EnigmaMathOps.not(a));
                prog.stepForward();
                break;
            case ENIGMA_LROT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.rotl(a,b));
                prog.stepForward();
                break;
            case ENIGMA_RROT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.rotr(a,b));
                prog.stepForward();
                break;
            case ENIGMA_LSHIFT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.shl(a,b));
                prog.stepForward();
                break;
            case ENIGMA_RSHIFT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.shr(a,b));
                prog.stepForward();
                break;
            case ENIGMA_LE:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.le(a,b));
                prog.stepForward();
                break;
            case ENIGMA_LT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.lt(a,b));
                prog.stepForward();
                break;
            case ENIGMA_GE:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.ge(a,b));
                prog.stepForward();
                break;
            case ENIGMA_GT:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.gt(a,b));
                prog.stepForward();
                break;
            case ENIGMA_EQ:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.eq(a,b));
                prog.stepForward();
                break;
            case ENIGMA_NE:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.neq(a,b));
                prog.stepForward();
                break;
            case ENIGMA_AND:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.andand(a,b));
                prog.stepForward();
                break;
            case ENIGMA_OR:
                a = prog.stackPop();
                b = prog.stackPop();
                prog.stackPush(EnigmaMathOps.oror(a,b));
                prog.stepForward();
                break;
            case ENIGMA_JUMP:
                a = prog.stackPop();
                prog.setPc(a.getInt());
                break;
            case ENIGMA_JUMP_TRUE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(a.isNotZero())
                    prog.setPc(b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_JUMP_FALSE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(!a.isNotZero())
                    prog.setPc(b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_JUMP_REL:
                a = prog.stackPop();
                prog.setPc(prog.getPc()+a.getInt());
                break;
            case ENIGMA_JUMP_REL_TRUE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(a.isNotZero())
                    prog.setPc(prog.getPc()+b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_JUMP_REL_FALSE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(!a.isNotZero())
                    prog.setPc(prog.getPc()+b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_JUMP_REL_NEG:
                a = prog.stackPop();
                prog.setPc(prog.getPc()-a.getInt());
                break;
            case ENIGMA_JUMP_REL_NEG_TRUE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(a.isNotZero())
                    prog.setPc(prog.getPc()-b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_JUMP_REL_NEG_FALSE:
                a = prog.stackPop();
                b = prog.stackPop();
                if(!a.isNotZero())
                    prog.setPc(prog.getPc()-b.getInt());
                else
                    prog.stepForward();
                break;
            case ENIGMA_VERIFY_BTY:
                a = prog.stackPop();
                prog.setBounty(a.isNotZero());
                break;
            case ENIGMA_VERIFY_POW: // sprintf(str, "if (verify_pow == 1)\n\t\t*pow_found = check_pow(%s, &m[0], &target[0], &hash[0]);\n\telse\n\t\t*pow_found = 0", lstr);
                a = prog.stackPop();
                b = prog.stackPop();
                c = prog.stackPop();
                d = prog.stackPop();
                prog.setPow(a.getInt(),b.getInt(),c.getInt(),d.getInt());
                break;



        }
    }

    public static byte[] execute(EnigmaProgram prog, boolean debug) {
        try {
            while (!prog.isStopped()) {
                stepProgram(prog);
            }

            if(debug){
                prog.dumpStack();
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.U);
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.I);
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.UL);
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.L);
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.F);
                prog.dumpStorage(EnigmaProgram.MEM_TARGET_STORE.D);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
