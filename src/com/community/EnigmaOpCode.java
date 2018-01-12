package com.community;

import java.util.HashMap;
import java.util.Map;

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
public enum EnigmaOpCode {

    // Load From Arrays
    ENIGMA_ARRAY_INT_LOAD((byte) 0x01, 1, 0, "array_int_load"),
    ENIGMA_ARRAY_UINT_LOAD((byte) 0x02, 1, 0, "array_uint_load"),
    ENIGMA_ARRAY_LONG_LOAD((byte) 0x03, 1, 0, "array_long_load"),
    ENIGMA_ARRAY_ULONG_LOAD((byte) 0x04, 1, 0, "array_ulong_load"),
    ENIGMA_ARRAY_FLOAT_LOAD((byte) 0x05, 1, 0, "array_float_load"),
    ENIGMA_ARRAY_DOUBLE_LOAD((byte) 0x06, 1, 0, "array_double_load"),
    ENIGMA_ARRAY_M_LOAD((byte) 0x07, 1, 0, "array_m_load"),
    ENIGMA_ARRAY_CUSTOM_LOAD((byte) 0x58, 2, 0, "array_load"),

    // Store into Arrays
    ENIGMA_ARRAY_INT_STORE((byte) 0x08, 2, 0, "array_int_store"),
    ENIGMA_ARRAY_UINT_STORE((byte) 0x09, 2, 0, "array_uint_store"),
    ENIGMA_ARRAY_LONG_STORE((byte) 0x0a, 2, 0, "array_long_store"),
    ENIGMA_ARRAY_ULONG_STORE((byte) 0x0b, 2, 0, "array_ulong_store"),
    ENIGMA_ARRAY_FLOAT_STORE((byte) 0x0c, 2, 0, "array_float_store"),
    ENIGMA_ARRAY_DOUBLE_STORE((byte) 0x0d, 2, 0, "array_double_store"),
    ENIGMA_ARRAY_CUSTOM_STORE((byte) 0x59, 3, 0, "array_load"),

    // Store other stuff
    ENIGMA_SUBMIT_SZ_STORE((byte) 0x0e, 2, 0, "submit_sz"),
    ENIGMA_SUBMIT_IDX_STORE((byte) 0x0f, 2, 0, "submit_idx"),

    // Jump and Compare
    ENIGMA_JUMP((byte) 0x10, 1, 2, "jump"),
    ENIGMA_JUMP_TRUE((byte) 0x11, 2, 2, "jump_true"),
    ENIGMA_JUMP_FALSE((byte) 0x12, 2, 2, "jump_false"),

    // RELATIVE JUMPS
    ENIGMA_JUMP_REL((byte) 0x4f, 1, 2, "jumprel"),
    ENIGMA_JUMP_REL_TRUE((byte) 0x50, 2, 2, "jumprel_true"),
    ENIGMA_JUMP_REL_FALSE((byte) 0x51, 2, 2, "jumprel_false"),
    ENIGMA_JUMP_REL_NEG((byte) 0x52, 1, 2, "jumprelneg"),
    ENIGMA_JUMP_REL_NEG_TRUE((byte) 0x52, 2, 2, "jumprelneg_true"),
    ENIGMA_JUMP_REL_NEG_FALSE((byte) 0x53, 2, 2, "jumprelneg_false"),

    // Push Data To Stack
    ENIGMA_PUSHDATA((byte) 0x13, 0, 2, "pushdata"), // This one is tricky since it does not use input from stack but
    // from the bytecode following this opcode
    ENIGMA_PUSH_TYPED_DATA((byte) 0x14, 0, 2, "pushtypeddata"),
    ENIGMA_PUSHUINT_1((byte) 0x54, 0, 2, "pushuint1"),
    ENIGMA_PUSHUINT_2((byte) 0x55, 0, 2, "pushuint2"),
    ENIGMA_PUSHUINT_3((byte) 0x56, 0, 2, "pushuint3"),
    ENIGMA_PUSHUINT_4((byte) 0x57, 0, 2, "pushuint4"),

    // Verification Ops
    ENIGMA_VERIFY_BTY((byte) 0x15, 1, 2, "verify_bty"),
    ENIGMA_VERIFY_POW((byte) 0x16, 4, 2, "verify_pow"),

    // Rest
    ENIGMA_ADD((byte) 0x23, 2, 7, "+"),
    ENIGMA_SUB((byte) 0x24, 2, 7, "-"),
    ENIGMA_NEG((byte) 0x25, 1, 5, "-"),
    ENIGMA_MUL((byte) 0x26, 2, 6, "*"),
    ENIGMA_DIV((byte) 0x27, 2, 6, "/"),
    ENIGMA_MOD((byte) 0x28, 2, 6, "%"),
    ENIGMA_LROT((byte) 0x29, 2, 8, "<<<"),
    ENIGMA_LSHIFT((byte) 0x2a, 2, 8, "<<"),
    ENIGMA_RROT((byte) 0x2b, 2, 8, ">>>"),
    ENIGMA_RSHIFT((byte) 0x2c, 2, 8, ">>"),
    ENIGMA_LE((byte) 0x2d, 2, 9, "<="),
    ENIGMA_GE((byte) 0x2e, 2, 9, ">="),
    ENIGMA_LT((byte) 0x2f, 2, 9, "<"),
    ENIGMA_GT((byte) 0x30, 2, 9, ">"),
    ENIGMA_EQ((byte) 0x31, 2, 10, "=="),
    ENIGMA_NE((byte) 0x32, 2, 10, "!="),
    ENIGMA_AND((byte) 0x33, 2, 14, "&&"),
    ENIGMA_OR((byte) 0x34, 2, 15, "||"),

    ENIGMA_BITWISE_AND((byte) 0x35, 2, 11, "and"),
    ENIGMA_BITWISE_XOR((byte) 0x36, 2, 12, "xor"),
    ENIGMA_BITWISE_OR((byte) 0x37, 2, 13, "or"),

    ENIGMA_COMPL((byte) 0x38, 1, 5, "~"),
    ENIGMA_NOT((byte) 0x39, 1, 5, "!"),

    ENIGMA_SINH((byte) 0x3a, 1, 2, "sinh"),
    ENIGMA_SIN((byte) 0x3b, 1, 2, "sin"),
    ENIGMA_COSH((byte) 0x3c, 1, 2, "cosh"),
    ENIGMA_COS((byte) 0x3d, 1, 2, "cos"),
    ENIGMA_TANH((byte) 0x3e, 1, 2, "tanh"),
    ENIGMA_TAN((byte) 0x3f, 1, 2, "tan"),
    ENIGMA_ASIN((byte) 0x40, 1, 2, "asin"),
    ENIGMA_ACOS((byte) 0x41, 1, 2, "acos"),
    ENIGMA_ATAN2((byte) 0x42, 2, 2, "atan2"),
    ENIGMA_ATAN((byte) 0x43, 1, 2, "atan"),
    ENIGMA_EXPNT((byte) 0x44, 1, 2, "exp"),
    ENIGMA_LOG10((byte) 0x45, 1, 2, "log10"),
    ENIGMA_LOG((byte) 0x46, 1, 2, "log"),
    ENIGMA_POW((byte) 0x47, 2, 2, "pow"),
    ENIGMA_SQRT((byte) 0x48, 1, 2, "sqrt"),
    ENIGMA_CEIL((byte) 0x49, 1, 2, "ceil"),
    ENIGMA_FLOOR((byte) 0x4a, 1, 2, "floor"),
    ENIGMA_FABS((byte) 0x4b, 1, 2, "fabs"),
    ENIGMA_ABS((byte) 0x4c, 1, 2, "abs"),
    ENIGMA_FMOD((byte) 0x4d, 2, 2, "fmod"),
    ENIGMA_GCD((byte) 0x4e, 2, 2, "gcd");

    private byte op;
    private int inputs;
    private int precedence;
    String stringRepr;
    private static final EnigmaOpCode[] intToTypeMap = new EnigmaOpCode[256];
    private static final Map<String, Byte> stringToByteMap = new HashMap<>();

    static {
        for (EnigmaOpCode type : EnigmaOpCode.values()) {
            intToTypeMap[type.getOp()] = type;
        }
    }

    EnigmaOpCode(byte op, int inputs, int precedence, String stringRepr) {
        this.op = op;
        this.inputs = inputs;
        this.precedence = precedence;
        this.stringRepr = stringRepr;
    }

    public byte getOp() {
        return op;
    }

    public int getInputs() {
        return inputs;
    }

    public int getPrecedence() {
        return precedence;
    }

    public String getStringRepr() {
        return stringRepr;
    }

    public static EnigmaOpCode findOpCode(byte code) {
        return intToTypeMap[code];
    }
}
