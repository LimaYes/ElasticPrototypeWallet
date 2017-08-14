package com.community;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.community.Constants.CODE_STACK_SIZE;

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
public class Primitives {

    public enum NODE_TYPE {
        NODE_ERROR,
                NODE_END_STATEMENT,
                NODE_CONSTANT,
                NODE_VAR_CONST,
                NODE_VAR_EXP,
                NODE_VERIFY,
                NODE_ASSIGN,
                NODE_OR,
                NODE_AND,
                NODE_BITWISE_OR,
                NODE_BITWISE_XOR,
                NODE_BITWISE_AND,
                NODE_COMPL,
                NODE_EQ,
                NODE_NE,
                NODE_LT,
                NODE_GT,
                NODE_LE,
                NODE_GE,
                NODE_INCREMENT_R,
                NODE_INCREMENT_L,
                NODE_ADD_ASSIGN,
                NODE_SUB_ASSIGN,
                NODE_MUL_ASSIGN,
                NODE_DIV_ASSIGN,
                NODE_MOD_ASSIGN,
                NODE_LSHFT_ASSIGN,
                NODE_RSHFT_ASSIGN,
                NODE_AND_ASSIGN,
                NODE_XOR_ASSIGN,
                NODE_OR_ASSIGN,
                NODE_CONDITIONAL,
                NODE_COND_ELSE,
                NODE_ADD,
                NODE_DECREMENT_R,
                NODE_DECREMENT_L,
                NODE_SUB,
                NODE_NEG,
                NODE_MUL,
                NODE_DIV,
                NODE_MOD,
                NODE_RSHIFT,
                NODE_LSHIFT,
                NODE_RROT,
                NODE_LROT,
                NODE_NOT,
                NODE_TRUE,
                NODE_FALSE,
                NODE_BLOCK,
                NODE_IF,
                NODE_ELSE,
                NODE_REPEAT,
                NODE_BREAK,
                NODE_CONTINUE,
                NODE_PARAM,
                NODE_SIN,
                NODE_COS,
                NODE_TAN,
                NODE_SINH,
                NODE_COSH,
                NODE_TANH,
                NODE_ASIN,
                NODE_ACOS,
                NODE_ATAN,
                NODE_ATAN2,
                NODE_EXPNT,
                NODE_LOG,
                NODE_LOG10,
                NODE_POW,
                NODE_SQRT,
                NODE_CEIL,
                NODE_FLOOR,
                NODE_ABS,
                NODE_FABS,
                NODE_FMOD,
                NODE_GCD,
                NODE_ARRAY_INT,
                NODE_ARRAY_UINT,
                NODE_ARRAY_LONG,
                NODE_ARRAY_ULONG,
                NODE_ARRAY_FLOAT,
                NODE_ARRAY_DOUBLE,
                NODE_STORAGE_SZ,
                NODE_STORAGE_IDX,
                NODE_FUNCTION,
                NODE_CALL_FUNCTION,
                NODE_VERIFY_BTY,
                NODE_VERIFY_POW
    };

    public enum EPL_TOKEN_TYPE {
        TOKEN_COMMA,
                TOKEN_ASSIGN,
                TOKEN_OR,
                TOKEN_AND,
                TOKEN_BITWISE_OR,
                TOKEN_BITWISE_XOR,
                TOKEN_BITWISE_AND,
                TOKEN_EQ,
                TOKEN_NE,
                TOKEN_LT,
                TOKEN_GT,
                TOKEN_LE,
                TOKEN_GE,
                TOKEN_INCREMENT,
                TOKEN_ADD_ASSIGN,
                TOKEN_SUB_ASSIGN,
                TOKEN_MUL_ASSIGN,
                TOKEN_DIV_ASSIGN,
                TOKEN_MOD_ASSIGN,
                TOKEN_LSHFT_ASSIGN,
                TOKEN_RSHFT_ASSIGN,
                TOKEN_AND_ASSIGN,
                TOKEN_XOR_ASSIGN,
                TOKEN_OR_ASSIGN,
                TOKEN_CONDITIONAL,
                TOKEN_COND_ELSE,
                TOKEN_ADD,
                TOKEN_DECREMENT,
                TOKEN_SUB,
                TOKEN_NEG,
                TOKEN_MUL,
                TOKEN_DIV,
                TOKEN_MOD,
                TOKEN_RSHIFT,
                TOKEN_LSHIFT,
                TOKEN_RROT,
                TOKEN_LROT,
                TOKEN_COMPL,
                TOKEN_NOT,
                TOKEN_CONSTANT,
                TOKEN_TRUE,
                TOKEN_FALSE,
                TOKEN_IF,
                TOKEN_ELSE,
                TOKEN_REPEAT,
                TOKEN_VAR_BEGIN,
                TOKEN_VAR_END,
                TOKEN_BLOCK_BEGIN,
                TOKEN_BLOCK_END,
                TOKEN_OPEN_PAREN,
                TOKEN_CLOSE_PAREN,
                TOKEN_LITERAL,
                TOKEN_END_STATEMENT,
                TOKEN_BREAK,
                TOKEN_CONTINUE,
                TOKEN_VERIFY,
                TOKEN_COMMENT,
                TOKEN_BLOCK_COMMENT,
                TOKEN_SIN,
                TOKEN_COS,
                TOKEN_TAN,
                TOKEN_SINH,
                TOKEN_COSH,
                TOKEN_TANH,
                TOKEN_ASIN,
                TOKEN_ACOS,
                TOKEN_ATAN,
                TOKEN_ATAN2,
                TOKEN_EXPNT,
                TOKEN_LOG,
                TOKEN_LOG10,
                TOKEN_POW,
                TOKEN_SQRT,
                TOKEN_CEIL,
                TOKEN_FLOOR,
                TOKEN_ABS,
                TOKEN_FABS,
                TOKEN_FMOD,
                TOKEN_GCD,
                TOKEN_ARRAY_INT,
                TOKEN_ARRAY_UINT,
                TOKEN_ARRAY_LONG,
                TOKEN_ARRAY_ULONG,
                TOKEN_ARRAY_FLOAT,
                TOKEN_ARRAY_DOUBLE,
                TOKEN_STORAGE_SZ,
                TOKEN_STORAGE_IDX,
                TOKEN_FUNCTION,
                TOKEN_CALL_FUNCTION,
                TOKEN_VERIFY_BTY,
                TOKEN_VERIFY_POW
    };

    public enum EXP_TYPE {
        EXP_NONE,
        EXP_STATEMENT,
        EXP_EXPRESSION,
        EXP_FUNCTION
    };

    public enum DATA_TYPE {
        DT_NONE,
        DT_STRING,
        DT_INT,
        DT_UINT,
        DT_LONG,
        DT_ULONG,
        DT_FLOAT,
        DT_DOUBLE,
        DT_UINT_M,
        DT_UINT_S
    };

    public static class SOURCE_TOKEN {
        int token_id;
        EPL_TOKEN_TYPE type;
        String literal;
        EXP_TYPE exp;

        public SOURCE_TOKEN(int token_id, EPL_TOKEN_TYPE type, String literal, EXP_TYPE exp, int inputs, int prec, int line_num, DATA_TYPE data_type) {
            this.token_id = token_id;
            this.type = type;
            this.literal = literal;
            this.exp = exp;
            this.inputs = inputs;
            this.prec = prec;
            this.line_num = line_num;
            this.data_type = data_type;
        }

        int inputs;
        int prec;
        int line_num;
        DATA_TYPE data_type;
    };

    public static class EXP_TOKEN_LIST {
        String str;
        int len;
        EPL_TOKEN_TYPE type;
        EXP_TYPE exp;
        int inputs;
        int prec;
        DATA_TYPE data_type;

        public EXP_TOKEN_LIST(String str, int len, EPL_TOKEN_TYPE type, EXP_TYPE exp, int inputs, int prec, DATA_TYPE data_type) {
            this.str = str;
            this.len = len;
            this.type = type;
            this.exp = exp;
            this.inputs = inputs;
            this.prec = prec;
            this.data_type = data_type;
        }
    };

    public static class AST {
        NODE_TYPE type;
        EXP_TYPE exp;
        long ivalue;
        long uvalue;
        double fvalue;
        String svalue;
        int token_num;
        int line_num;
        boolean end_stmnt;
        DATA_TYPE data_type;
        boolean is_64bit;
        boolean is_signed;

        public AST() {
        }

        boolean is_float;
        boolean is_vm_mem;
        boolean is_vm_storage;
        AST	parent;
        AST	left;
        AST	right;
    }

    public static class STATE {
        // Max Array Variable Index For Each Data Type
        int ast_vm_ints;
        int ast_vm_uints;
        int ast_vm_longs;
        int ast_vm_ulongs;
        int ast_vm_floats;
        int ast_vm_doubles;

        // Number Of Unsigned Ints To Store Per Interation / Import & Export Index
        int ast_storage_sz;
        int ast_storage_idx;

        // Index Value Of Main & Verify Functions In AST Array
        int ast_func_idx;
        int ast_main_idx;
        int ast_verify_idx;

        // Precalculated AST depth and WCET
        int calculated_wcet;
        boolean initialized_ast_stats;

        Stack<Integer> stack_op;
        Stack<AST> stack_exp;
        List<SOURCE_TOKEN> token_list;


        int num_exp;

        // CODE related stuff
        Stack<String> stack_code;
        int tabs;


        public STATE() {
            stack_op = new Stack<>();
            stack_exp = new Stack<>();
            token_list = new ArrayList<>();

            // Reset Stack Counters
            num_exp = 0;

            // Reset Global Variable Array Size
            ast_vm_ints = 0;
            ast_vm_uints = 0;
            ast_vm_longs = 0;
            ast_vm_ulongs = 0;
            ast_vm_floats = 0;
            ast_vm_doubles = 0;

            // Reset Storage Variables
            ast_storage_sz = 0;
            ast_storage_idx = 0;

            // Statistics about WCET
            calculated_wcet = 0;
            initialized_ast_stats = false;

            // Code stuff
            stack_code = new Stack<>();
            tabs = 0;
        }
    }
}
