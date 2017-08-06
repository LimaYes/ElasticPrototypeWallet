package com.community;

import static com.community.Primitives.DATA_TYPE.*;
import static com.community.Primitives.EPL_TOKEN_TYPE.*;
import static com.community.Primitives.EXP_TYPE.*;

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
public class Constants {

    public static final int MAX_LITERAL_SIZE = 100;			// Maximum Length Of Literal In ElasticPL
    public static final int TOKEN_LIST_SIZE = 4096;			// Maximum Number Of Tokens In ElasticPL Job - TODO: Finalize Size
    public static final int PARSE_STACK_SIZE = 24000;			// Maximum Number Of Items In AST - TODO: Finalize Size
    public static final int CALL_STACK_SIZE = 257;				// Maximum Number Of Nested Function Calls
    public static final int REPEAT_STACK_SIZE = 33;			// Maximum Number Of Nested Repeat Statements
    public static final int CODE_STACK_SIZE = 10000;			// Maximum Number Of Lines Of C / OpenCL Code - TODO: Finalize Size
    public static final int MAX_AST_DEPTH = 20000;				// Maximum Depth Allowed In The AST Tree - TODO: Finalize Size
    public static final int ast_vm_MEMORY_SIZE = 100000;		// Maximum Number Of Bytes That Can Be Used By VM Memory Model - TODO: Finalize Size
    public static final int VM_M_ARRAY_SIZE =	12;				// Number Of Unsigned Ints Initialized By VM
    public static final int MAX_SOURCE_SIZE = 1024 * 512;        // 512KB - Maximum Size Of Decoded ElasticPL Source Code
    public static final int INSTRUCTION_LIMIT = 10000000;
    public static final int SAFE_TIME_LIMIT = 15 * 1000;// 15 sek should be never reached, but lets keep it safe
    public static final int ABSOLUTELY_MAXIMUM_WCET = 1000000;
    public static final int ABSOLUTELY_MAXIMUM_VERIFY_WCET = 84000;


    /*****************************************************************************
     ElasticPL Token List

     Format:  Str, Len, Type, Exp, Inputs, Prec, Initial Data Type

     Str:		Token String
     Len:		String Length Used For "memcmp"
     Type:		Enumerated Token Type
     Exp:		Enumerated Num Of Expressions To Link To Node
     Inputs:		Number Of Required Inputs To Operator / Function
     Prec:		(Precedence) Determines Parsing Order
     Data Type:  Data Type Of Value Returned By Operator / Function
     ******************************************************************************/
    public static Primitives.EXP_TOKEN_LIST epl_token[] = {
                new Primitives.EXP_TOKEN_LIST( "//",							2,	TOKEN_COMMENT,		EXP_NONE,		0,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "/*",							2,	TOKEN_BLOCK_COMMENT,EXP_NONE,		0,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( ";",							1,	TOKEN_END_STATEMENT,EXP_NONE,		0,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( ",",							1,	TOKEN_COMMA,		EXP_NONE,		0,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "{",							1,	TOKEN_BLOCK_BEGIN,	EXP_STATEMENT,	2,	1,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "}",							1,	TOKEN_BLOCK_END,	EXP_STATEMENT,	2,	1,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "()",							2,	TOKEN_CALL_FUNCTION,EXP_STATEMENT,	1,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "(",							1,	TOKEN_OPEN_PAREN,	EXP_NONE,		0,	1,	DT_INT ),
                new Primitives.EXP_TOKEN_LIST( ")",							1,	TOKEN_CLOSE_PAREN,	EXP_NONE,		0,	1,	DT_INT ),
                new Primitives.EXP_TOKEN_LIST( "array_int",					9,	TOKEN_ARRAY_INT,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "array_uint",					10,	TOKEN_ARRAY_UINT,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "array_long",					10,	TOKEN_ARRAY_LONG,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "array_ulong",				11,	TOKEN_ARRAY_ULONG,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "array_float",				11,	TOKEN_ARRAY_FLOAT,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "array_double",				12,	TOKEN_ARRAY_DOUBLE,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "storage_sz",					10,	TOKEN_STORAGE_SZ,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "storage_idx",				11,	TOKEN_STORAGE_IDX,	EXP_STATEMENT,	1,	0,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "repeat",						6,	TOKEN_REPEAT,		EXP_STATEMENT,	4,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "if",							2,	TOKEN_IF,			EXP_STATEMENT,	2,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "else",						4,	TOKEN_ELSE,			EXP_STATEMENT,	2,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "break",						5,	TOKEN_BREAK,		EXP_STATEMENT,	0,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "continue",					8,	TOKEN_CONTINUE,		EXP_STATEMENT,	0,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "function",					8,	TOKEN_FUNCTION,		EXP_STATEMENT,	2,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "result",						6,	TOKEN_RESULT,		EXP_STATEMENT,	1,	2,	DT_NONE ),
                new Primitives.EXP_TOKEN_LIST( "i[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_INT ),
                new Primitives.EXP_TOKEN_LIST( "u[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_UINT ),
                new Primitives.EXP_TOKEN_LIST( "l[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_LONG ),
                new Primitives.EXP_TOKEN_LIST( "ul[",						3,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_ULONG ),
                new Primitives.EXP_TOKEN_LIST( "f[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_FLOAT ),
                new Primitives.EXP_TOKEN_LIST( "d[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_DOUBLE ),
                new Primitives.EXP_TOKEN_LIST( "m[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_UINT_M ),
                new Primitives.EXP_TOKEN_LIST( "s[",							2,	TOKEN_VAR_BEGIN,	EXP_EXPRESSION,	1,	4,	DT_UINT_S ),
                new Primitives.EXP_TOKEN_LIST( "]",							1,	TOKEN_VAR_END,		EXP_EXPRESSION,	1,	4,	DT_INT ),
                new Primitives.EXP_TOKEN_LIST( "++",							2,	TOKEN_INCREMENT,	EXP_EXPRESSION,	1,	5,	DT_INT ), // Increment
                new Primitives.EXP_TOKEN_LIST( "--",							2,	TOKEN_DECREMENT,	EXP_EXPRESSION,	1,	5,	DT_INT ), // Decrement
                new Primitives.EXP_TOKEN_LIST( "+=",							2,	TOKEN_ADD_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "-=",							2,	TOKEN_SUB_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "*=",							2,	TOKEN_MUL_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "/=",							2,	TOKEN_DIV_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_FLOAT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "%=",							2,	TOKEN_MOD_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "<<=",						3,	TOKEN_LSHFT_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( ">>=",						3,	TOKEN_RSHFT_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "&=",							2,	TOKEN_AND_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "^=",							2,	TOKEN_XOR_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "|=",							2,	TOKEN_OR_ASSIGN,	EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "+",							1,	TOKEN_ADD,			EXP_EXPRESSION,	2,	7,	DT_INT ), // Additive
                new Primitives.EXP_TOKEN_LIST( "-",							1,	TOKEN_SUB,			EXP_EXPRESSION,	2,	7,	DT_INT ), // Additive
                new Primitives.EXP_TOKEN_LIST( "-",							1,	TOKEN_NEG,			EXP_EXPRESSION,	1,	5,	DT_INT ), // Additive
                new Primitives.EXP_TOKEN_LIST( "*",							1,	TOKEN_MUL,			EXP_EXPRESSION,	2,	6,	DT_INT ), // Multiplicative
                new Primitives.EXP_TOKEN_LIST( "/",							1,	TOKEN_DIV,			EXP_EXPRESSION,	2,	6,	DT_FLOAT ), // Multiplicative
                new Primitives.EXP_TOKEN_LIST( "%",							1,	TOKEN_MOD,			EXP_EXPRESSION,	2,	6,	DT_INT ), // Multiplicative
                new Primitives.EXP_TOKEN_LIST( "<<<",						3,	TOKEN_LROT,			EXP_EXPRESSION,	2,	8,	DT_INT ), // Shift
                new Primitives.EXP_TOKEN_LIST( "<<",							2,	TOKEN_LSHIFT,		EXP_EXPRESSION,	2,	8,	DT_INT ), // Shift
                new Primitives.EXP_TOKEN_LIST( ">>>",						3,	TOKEN_RROT,			EXP_EXPRESSION,	2,	8,	DT_INT ), // Shift
                new Primitives.EXP_TOKEN_LIST( ">>",							2,	TOKEN_RSHIFT,		EXP_EXPRESSION,	2,	8,	DT_INT ), // Shift
                new Primitives.EXP_TOKEN_LIST( "<=",							2,	TOKEN_LE,			EXP_EXPRESSION,	2,	9,	DT_INT ), // Relational
                new Primitives.EXP_TOKEN_LIST( ">=",							2,	TOKEN_GE,			EXP_EXPRESSION,	2,	9,	DT_INT ), // Relational
                new Primitives.EXP_TOKEN_LIST( "<",							1,	TOKEN_LT,			EXP_EXPRESSION,	2,	9,	DT_INT ), // Relational
                new Primitives.EXP_TOKEN_LIST( ">",							1,	TOKEN_GT,			EXP_EXPRESSION,	2,	9,	DT_INT ), // Relational
                new Primitives.EXP_TOKEN_LIST( "==",							2,	TOKEN_EQ,			EXP_EXPRESSION,	2,	10,	DT_INT ), // Equality
                new Primitives.EXP_TOKEN_LIST( "!=",							2,	TOKEN_NE,			EXP_EXPRESSION,	2,	10,	DT_INT ), // Equality
                new Primitives.EXP_TOKEN_LIST( "&&",							2,	TOKEN_AND,			EXP_EXPRESSION,	2,	14,	DT_INT ), // Logical AND
                new Primitives.EXP_TOKEN_LIST( "||",							2,	TOKEN_OR,			EXP_EXPRESSION,	2,	15,	DT_INT ), // Logical OR
                new Primitives.EXP_TOKEN_LIST( "&",							1,	TOKEN_BITWISE_AND,	EXP_EXPRESSION,	2,	11,	DT_INT ), // Bitwise AND
                new Primitives.EXP_TOKEN_LIST( "and",						3,	TOKEN_BITWISE_AND,	EXP_EXPRESSION,	2,	11,	DT_INT ), // Bitwise AND
                new Primitives.EXP_TOKEN_LIST( "^",							1,	TOKEN_BITWISE_XOR,	EXP_EXPRESSION,	2,	12,	DT_INT ), // Bitwise XOR
                new Primitives.EXP_TOKEN_LIST( "xor",						3,	TOKEN_BITWISE_XOR,	EXP_EXPRESSION,	2,	12,	DT_INT ), // Bitwise XOR
                new Primitives.EXP_TOKEN_LIST( "|",							1,	TOKEN_BITWISE_OR,	EXP_EXPRESSION,	2,	13,	DT_INT ), // Bitwise OR
                new Primitives.EXP_TOKEN_LIST( "or",							2,	TOKEN_BITWISE_OR,	EXP_EXPRESSION,	2,	13,	DT_INT ), // Bitwise OR
                new Primitives.EXP_TOKEN_LIST( "=",							1,	TOKEN_ASSIGN,		EXP_STATEMENT,	2,	18,	DT_INT ), // Assignment
                new Primitives.EXP_TOKEN_LIST( "?",							1,	TOKEN_CONDITIONAL,	EXP_STATEMENT,	2,	16,	DT_INT ), // Conditional
                new Primitives.EXP_TOKEN_LIST( ":",							1,	TOKEN_COND_ELSE,	EXP_STATEMENT,	2,	17,	DT_INT ), // Conditional
                new Primitives.EXP_TOKEN_LIST( "~",							1,	TOKEN_COMPL,		EXP_EXPRESSION,	1,	5,	DT_INT ), // Unary Operator
                new Primitives.EXP_TOKEN_LIST( "!",							1,	TOKEN_NOT,			EXP_EXPRESSION,	1,	5,	DT_INT ), // Unary Operator
                new Primitives.EXP_TOKEN_LIST( "true",						4,	TOKEN_TRUE,			EXP_EXPRESSION,	0,	40,	DT_INT ), // Unary Operator
                new Primitives.EXP_TOKEN_LIST( "false",						5,	TOKEN_FALSE,		EXP_EXPRESSION,	0,	40,	DT_INT ), // Unary Operator
                new Primitives.EXP_TOKEN_LIST( "sinh",						4,	TOKEN_SINH,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "sin",						3,	TOKEN_SIN,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "cosh",						4,	TOKEN_COSH,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "cos",						3,	TOKEN_COS,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "tanh",						4,	TOKEN_TANH,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "tan",						3,	TOKEN_TAN,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "asin",						4,	TOKEN_ASIN,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "acos",						4,	TOKEN_ACOS,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "atan2",						5,	TOKEN_ATAN2,		EXP_FUNCTION,	2,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "atan",						4,	TOKEN_ATAN,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "exp",						3,	TOKEN_EXPNT,		EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "log10",						5,	TOKEN_LOG10,		EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "log",						3,	TOKEN_LOG,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "pow",						3,	TOKEN_POW,			EXP_FUNCTION,	2,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "sqrt",						4,	TOKEN_SQRT,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "ceil",						4,	TOKEN_CEIL,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "floor",						5,	TOKEN_FLOOR,		EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "fabs",						4,	TOKEN_FABS,			EXP_FUNCTION,	1,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "abs",						3,	TOKEN_ABS,			EXP_FUNCTION,	1,	2,	DT_INT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "fmod",						4,	TOKEN_FMOD,			EXP_FUNCTION,	2,	2,	DT_FLOAT ), // Built In Math Functions
                new Primitives.EXP_TOKEN_LIST( "gcd",						3,	TOKEN_GCD,			EXP_FUNCTION,	2,	2,	DT_FLOAT ), // Built In Math Functions
    };

}
