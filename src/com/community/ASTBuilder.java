package com.community;
import com.community.Primitives.AST;

import java.util.Stack;

import static com.community.Constants.*;
import static com.community.Primitives.DATA_TYPE.*;
import static com.community.Primitives.EPL_TOKEN_TYPE.*;
import static com.community.Primitives.EXP_TYPE.EXP_EXPRESSION;
import static com.community.Primitives.EXP_TYPE.EXP_FUNCTION;
import static com.community.Primitives.NODE_TYPE.*;

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

public class ASTBuilder {

    private static void push_op(Primitives.STATE state, int token_id) {
        state.stack_op.push(token_id);
    }

    private static int pop_op(Primitives.STATE state){
        return state.stack_op.pop();
    }

    private static int top_op(Primitives.STATE state){
        return state.stack_op.peek();
    }

    private static void push_exp(Primitives.STATE state, AST exp) {

        state.stack_exp.push(exp);
        if (!exp.end_stmnt)
            state.num_exp++;
    }

    private static AST pop_exp(Primitives.STATE state){

        if(state.stack_exp.size()>0){
            AST exp = state.stack_exp.pop();
            if (!exp.end_stmnt)
                state.num_exp--;
            return exp;
        }

        return null;

    }

    private static AST top_exp(Primitives.STATE state){
        return state.stack_exp.peek();
    }

    private static AST add_exp(Primitives.STATE state, Primitives.NODE_TYPE node_type, Primitives.EXP_TYPE exp_type, boolean is_vm_mem, boolean is_vm_storage, long val_int64, long val_uint64, double val_double, String svalue, int token_num, int line_num, Primitives.DATA_TYPE data_type, AST left, AST right){
        Primitives.DATA_TYPE dt_l, dt_r;
        AST e = new AST();

        if (e!=null) {
            e.type = node_type;
            e.exp = exp_type;
            e.is_vm_mem = is_vm_mem;
            e.is_vm_storage = is_vm_storage;
            e.ivalue = val_int64;
            e.uvalue = val_uint64;
            e.fvalue = val_double;
            e.svalue = svalue;
            e.token_num = token_num;
            e.line_num = line_num;
            e.end_stmnt = false;
            e.data_type = data_type;
            e.left = left;
            e.right = right;

            // ElasticPL Operator Nodes Inherit Data Type From Child Nodes
            // Precedence Is Based On C99 Standard:
            // double <- float <- uint64_t <- int64_t <- uint32_t <- int32_t
            if ((data_type != DT_NONE) && (node_type != NODE_VAR_CONST) && (node_type != NODE_VAR_EXP) && (node_type != NODE_CONSTANT)) {
                dt_l = left!=null ? left.data_type : DT_NONE;
                dt_r = right!=null ? right.data_type : DT_NONE;

                if ((dt_l == DT_DOUBLE) || (dt_r == DT_DOUBLE))
                    e.data_type = DT_DOUBLE;
                else if ((dt_l == DT_FLOAT) || (dt_r == DT_FLOAT))
                    e.data_type = DT_FLOAT;
                else if ((dt_l == DT_ULONG) || (dt_r == DT_ULONG))
                    e.data_type = DT_ULONG;
                else if ((dt_l == DT_LONG) || (dt_r == DT_LONG))
                    e.data_type = DT_LONG;
                else if ((dt_l == DT_UINT) || (dt_r == DT_UINT))
                    e.data_type = DT_UINT;
                else
                    e.data_type = DT_INT;
            }

            // Set Indicators Based On Data Type
            switch (e.data_type) {
                case DT_INT:
                    e.is_64bit = false;
                    e.is_signed = true;
                    e.is_float = false;
                    break;
                case DT_UINT:
                    e.is_64bit = false;
                    e.is_signed = false;
                    e.is_float = false;
                    break;
                case DT_LONG:
                    e.is_64bit = true;
                    e.is_signed = true;
                    e.is_float = false;
                    break;
                case DT_ULONG:
                    e.is_64bit = true;
                    e.is_signed = false;
                    e.is_float = false;
                    break;
                case DT_FLOAT:
                    e.is_64bit = false;
                    e.is_signed = true;
                    e.is_float = true;
                    break;
                case DT_DOUBLE:
                    e.is_64bit = true;
                    e.is_signed = true;
                    e.is_float = true;
                    break;
                default:
                    e.is_64bit = false;
                    e.is_signed = false;
                    e.is_float = false;
            }

            if (left!=null)
                e.left.parent = e;
            if (right!=null)
                e.right.parent = e;
        }
        return e;
    }

    private static Primitives.NODE_TYPE get_node_type(Primitives.STATE state, Primitives.SOURCE_TOKEN token, int token_num) {
        Primitives.NODE_TYPE node_type;

        switch (token.type) {
            case TOKEN_VAR_END:
                if (state.stack_exp.size()>0 && top_exp(state).type == NODE_CONSTANT)
                node_type = NODE_VAR_CONST;
		else
                node_type = NODE_VAR_EXP;
                break;
            case TOKEN_INCREMENT:
                if (state.stack_exp.size()>0 && (top_exp(state).token_num > token_num))
                node_type = NODE_INCREMENT_R;
		else
                node_type = NODE_INCREMENT_L;
                break;
            case TOKEN_DECREMENT:
                if (state.stack_exp.size()>0 && (top_exp(state).token_num > token_num))
                node_type = NODE_DECREMENT_R;
		else
                node_type = NODE_DECREMENT_L;
                break;
            case TOKEN_COMPL:			node_type = NODE_COMPL;			break;
            case TOKEN_NOT:				node_type = NODE_NOT;			break;
            case TOKEN_NEG:				node_type = NODE_NEG;			break;
            case TOKEN_LITERAL:			node_type = NODE_CONSTANT;		break;
            case TOKEN_TRUE:			node_type = NODE_CONSTANT;		break;
            case TOKEN_FALSE:			node_type = NODE_CONSTANT;		break;
            case TOKEN_MUL:				node_type = NODE_MUL;			break;
            case TOKEN_DIV:				node_type = NODE_DIV;			break;
            case TOKEN_MOD:				node_type = NODE_MOD;			break;
            case TOKEN_ADD_ASSIGN:		node_type = NODE_ADD_ASSIGN;	break;
            case TOKEN_SUB_ASSIGN:		node_type = NODE_SUB_ASSIGN;	break;
            case TOKEN_MUL_ASSIGN:		node_type = NODE_MUL_ASSIGN;	break;
            case TOKEN_DIV_ASSIGN:		node_type = NODE_DIV_ASSIGN;	break;
            case TOKEN_MOD_ASSIGN:		node_type = NODE_MOD_ASSIGN;	break;
            case TOKEN_LSHFT_ASSIGN:	node_type = NODE_LSHFT_ASSIGN;	break;
            case TOKEN_RSHFT_ASSIGN:	node_type = NODE_RSHFT_ASSIGN;	break;
            case TOKEN_AND_ASSIGN:		node_type = NODE_AND_ASSIGN;	break;
            case TOKEN_XOR_ASSIGN:		node_type = NODE_XOR_ASSIGN;	break;
            case TOKEN_OR_ASSIGN:		node_type = NODE_OR_ASSIGN;		break;
            case TOKEN_ADD:				node_type = NODE_ADD;			break;
            case TOKEN_SUB:				node_type = NODE_SUB;			break;
            case TOKEN_LROT:			node_type = NODE_LROT;			break;
            case TOKEN_LSHIFT:			node_type = NODE_LSHIFT;		break;
            case TOKEN_RROT:			node_type = NODE_RROT;			break;
            case TOKEN_RSHIFT:			node_type = NODE_RSHIFT;		break;
            case TOKEN_LE:				node_type = NODE_LE;			break;
            case TOKEN_GE:				node_type = NODE_GE;			break;
            case TOKEN_LT:				node_type = NODE_LT;			break;
            case TOKEN_GT:				node_type = NODE_GT;			break;
            case TOKEN_EQ:				node_type = NODE_EQ;			break;
            case TOKEN_NE:				node_type = NODE_NE;			break;
            case TOKEN_BITWISE_AND:		node_type = NODE_BITWISE_AND;	break;
            case TOKEN_BITWISE_XOR:		node_type = NODE_BITWISE_XOR;	break;
            case TOKEN_BITWISE_OR:		node_type = NODE_BITWISE_OR;	break;
            case TOKEN_AND:				node_type = NODE_AND;			break;
            case TOKEN_OR:				node_type = NODE_OR;			break;
            case TOKEN_BLOCK_END:		node_type = NODE_BLOCK;			break;
            case TOKEN_CONDITIONAL:		node_type = NODE_CONDITIONAL;	break;
            case TOKEN_COND_ELSE:		node_type = NODE_COND_ELSE;		break;
            case TOKEN_IF:				node_type = NODE_IF;			break;
            case TOKEN_ELSE:			node_type = NODE_ELSE;			break;
            case TOKEN_REPEAT:			node_type = NODE_REPEAT;		break;
            case TOKEN_BREAK:			node_type = NODE_BREAK;			break;
            case TOKEN_CONTINUE:		node_type = NODE_CONTINUE;		break;
            case TOKEN_ASSIGN:			node_type = NODE_ASSIGN;		break;
            case TOKEN_SIN:				node_type = NODE_SIN;			break;
            case TOKEN_COS:				node_type = NODE_COS; 			break;
            case TOKEN_TAN:				node_type = NODE_TAN; 			break;
            case TOKEN_SINH:			node_type = NODE_SINH;			break;
            case TOKEN_COSH:			node_type = NODE_COSH;			break;
            case TOKEN_TANH:			node_type = NODE_TANH;			break;
            case TOKEN_ASIN:			node_type = NODE_ASIN;			break;
            case TOKEN_ACOS:			node_type = NODE_ACOS;			break;
            case TOKEN_ATAN:			node_type = NODE_ATAN;			break;
            case TOKEN_ATAN2:			node_type = NODE_ATAN2;			break;
            case TOKEN_EXPNT:			node_type = NODE_EXPNT;			break;
            case TOKEN_LOG:				node_type = NODE_LOG;			break;
            case TOKEN_LOG10:			node_type = NODE_LOG10;			break;
            case TOKEN_POW:				node_type = NODE_POW;			break;
            case TOKEN_SQRT:			node_type = NODE_SQRT;			break;
            case TOKEN_CEIL:			node_type = NODE_CEIL;			break;
            case TOKEN_FLOOR:			node_type = NODE_FLOOR;			break;
            case TOKEN_ABS:				node_type = NODE_ABS;			break;
            case TOKEN_FABS:			node_type = NODE_FABS;			break;
            case TOKEN_FMOD:			node_type = NODE_FMOD; 			break;
            case TOKEN_GCD:				node_type = NODE_GCD; 			break;
            case TOKEN_ARRAY_INT:		node_type = NODE_ARRAY_INT; 	break;
            case TOKEN_ARRAY_UINT:		node_type = NODE_ARRAY_UINT; 	break;
            case TOKEN_ARRAY_LONG:		node_type = NODE_ARRAY_LONG; 	break;
            case TOKEN_ARRAY_ULONG:		node_type = NODE_ARRAY_ULONG; 	break;
            case TOKEN_ARRAY_FLOAT:		node_type = NODE_ARRAY_FLOAT; 	break;
            case TOKEN_ARRAY_DOUBLE:	node_type = NODE_ARRAY_DOUBLE; 	break;
            case TOKEN_STORAGE_SZ:		node_type = NODE_STORAGE_SZ; 	break;
            case TOKEN_STORAGE_IDX:		node_type = NODE_STORAGE_IDX; 	break;
            case TOKEN_FUNCTION:		node_type = NODE_FUNCTION;		break;
            case TOKEN_CALL_FUNCTION:	node_type = NODE_CALL_FUNCTION;	break;
            case TOKEN_VERIFY_BTY:		node_type = NODE_VERIFY_BTY;	break;
            case TOKEN_VERIFY_POW:		node_type = NODE_VERIFY_POW;	break;
            default: return NODE_ERROR;
        }

        return node_type;
    }

    private static void create_exp(Primitives.STATE state, Primitives.SOURCE_TOKEN token, int token_num) throws Exceptions.SyntaxErrorException {
        int i;
        int len;
        boolean is_signed = false;
        boolean is_vm_mem = false;
        boolean is_vm_storage = false;
        long val_int64 = 0;
        long val_uint64 = 0;
        double val_double = 0.0;
        String svalue = null;
        Primitives.NODE_TYPE node_type;
        Primitives.DATA_TYPE data_type;
        AST exp, left = null, right = null;

        node_type = get_node_type(state, token, token_num);
        data_type = token.data_type;

        // Map Token To Node Type
        if (node_type == NODE_ERROR) {
            throw new Exceptions.SyntaxErrorException("Unknown Token in ElasticPL Source.  Line: " + token.line_num + ", Token Type: " + token.type);
        }

        // Confirm Required Number / Types Of Expressions Are On Stack
        validate_inputs(state, token, token_num, node_type);

        switch (token.exp) {

            case EXP_EXPRESSION:

                // Constant Expressions
                if (token.inputs == 0) {

                    if (token.type == TOKEN_TRUE) {
                        val_uint64 = 1;
                        data_type = DT_UINT;
                    }
                    else if (token.type == TOKEN_FALSE) {
                        val_uint64 = 0;
                        data_type = DT_UINT;
                    }
                    else if (node_type == NODE_CONSTANT) {

                        data_type = DT_NONE;

                        if (token.literal.charAt(0) == '-')
                            is_signed = true;
                        else
                            is_signed = false;

                        len = token.literal.length();

                        if (token.data_type == DT_INT) {

                            // Convert Hex Numbers
                            if ((len > 2) && (token.literal.charAt(0) == '0') && (token.literal.charAt(1) == 'x')) {
                                if (len > 18) {
                                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Hex value exceeds 64 bits");
                                }
                                else if (len < 11) {
                                    data_type = DT_UINT;
                                }
                                else {
                                    data_type = DT_ULONG;
                                }

                                // todo val_uint64 = strtoull(&token.literal[2], null, 16);
                            }

                            // Convert Binary Numbers
                            else if ((len > 2) && (token.literal.charAt(0) == '0') && (token.literal.charAt(1) == 'b')) {
                                if (len > 66) {
                                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Binary value exceeds 64 bits");
                                }
                                else if (len < 35) {
                                    data_type = DT_UINT;
                                }
                                else {
                                    data_type = DT_ULONG;
                                }

                                // todo val_uint64 = strtoull(&token.literal[2], null, 2);
                            }

                            // Convert Integer Numbers
                            else if (len > 0) {

                                try {
                                    if (is_signed)
                                        val_int64 = Long.valueOf(token.literal);
                                    else
                                        val_uint64 = Long.valueOf(token.literal);
                                }catch(NumberFormatException n){
                                    n.printStackTrace();
                                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Integer value exceeds 64 bits");
                                }

                                if (is_signed) {
                                    if ((val_int64 >= Integer.MIN_VALUE) && (val_int64 <= Integer.MAX_VALUE)) {
                                        data_type = DT_INT;
                                    }
                                    else {
                                        data_type = DT_LONG;
                                    }
                                }
                                else {
                                    long lm = Integer.MAX_VALUE;
                                    if (val_uint64 <= lm*2) {
                                        data_type = DT_UINT;
                                    }
                                    else {
                                        data_type = DT_ULONG;
                                    }
                                }
                            }
                        }
                        else if (token.data_type == DT_FLOAT) {

                            try {
                                val_double = Double.valueOf(token.literal);
                            }catch(NumberFormatException n){
                                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Decimal value exceeds 64 bits");
                            }


                            if ((val_double >= Float.MIN_VALUE) && (val_double <= Float.MAX_VALUE)) {
                                data_type = DT_FLOAT;
                            }
                            else {
                                data_type = DT_DOUBLE;
                            }
                        }
                        else {
                            svalue = token.literal;
                        }
                    }
                }
                // Unary Expressions
                else if (token.inputs == 1) {

                    left = pop_exp(state);

                    // Remove Expression For Variables w/ Constant Index
                    if (node_type == NODE_VAR_CONST) {
                        val_uint64 = left.uvalue;
                        left = null;
                    }

                    // Set Indicator For m[] Array
                    if (((node_type == NODE_VAR_CONST) || (node_type == NODE_VAR_EXP)) && (token.data_type == DT_UINT_M)) {
                        is_vm_mem = true;
                        data_type = DT_UINT;
                    }

                    // Set Indicator For s[] Array
                    if (((node_type == NODE_VAR_CONST) || (node_type == NODE_VAR_EXP)) && (token.data_type == DT_UINT_S)) {
                        is_vm_storage = true;
                        data_type = DT_UINT;
                    }

                }
                // Binary Expressions
                else if (token.inputs == 2) {
                    right = pop_exp(state);
                    left = pop_exp(state);
                }

                break;

            case EXP_STATEMENT:

                // Unary Statements
                if (token.inputs == 1) {
                    left = pop_exp(state);
                    if (node_type == NODE_CALL_FUNCTION) {
                        svalue = left.svalue;
                        left = null;
                    }
                }
                // Binary Statements
                else if (token.inputs == 2) {
                    if (node_type == NODE_BLOCK && top_exp(state).type != NODE_BLOCK)
                    right = null;
			else
                    right = pop_exp(state);
                    left = pop_exp(state);

                    if (node_type == NODE_FUNCTION) {
                        svalue = left.svalue;
                        left = null;
                    }
                }
                // Repeat Statements
                else if (node_type == NODE_REPEAT) {
                    right = pop_exp(state);				// Block
                    val_int64 = pop_exp(state).uvalue;	// Max # Of Iterations
                    left = pop_exp(state);				// # Of Iterations
                    val_uint64 = pop_exp(state).uvalue;	// Loop Counter

                    if (val_int64 <= 0) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Invalid value for max iterations");
                    }

                    if ((left.type == NODE_CONSTANT) && (left.uvalue > val_int64)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Number of iterations exceeds maximum");
                    }
                }
                break;

            case EXP_FUNCTION:

                if (token.inputs > 0) {
                    // First Paramater
                    left = pop_exp(state);
                    exp = add_exp(state, NODE_PARAM, EXP_EXPRESSION, false, false, 0, 0, 0.0, null, 0, 0, DT_NONE, left, null);
                    push_exp(state, exp);

                    // Remaining Paramaters
                    for (i = 1; i < token.inputs; i++) {
                        right = pop_exp(state);
                        left = pop_exp(state);
                        exp = add_exp(state, NODE_PARAM, EXP_EXPRESSION, false, false, 0, 0, 0.0, null, 0, 0, DT_NONE, left, right);
                        push_exp(state, exp);
                    }
                    left = null;
                    right = pop_exp(state);
                }
                else {
                    left = null;
                    right = null;
                }
        }

        exp = add_exp(state, node_type, token.exp, is_vm_mem, is_vm_storage, val_int64, val_uint64, val_double, svalue, token_num, token.line_num, data_type, left, right);

        if (exp!=null) {
            // Update The "End Statement" Indicator For If/Else/Repeat/Block/Function/Result
            if ((exp.type == NODE_IF) || (exp.type == NODE_ELSE) || (exp.type == NODE_REPEAT) || (exp.type ==
                    NODE_BLOCK) || (exp.type == NODE_FUNCTION) || (exp.type == NODE_VERIFY_BTY) || (exp.type ==
                    NODE_VERIFY_POW))
                exp.end_stmnt = true;

            push_exp(state, exp);
        }
        else
            throw new Exceptions.SyntaxErrorException("Unknown syntax error occured");

    }

    public static void parse_token_list(Primitives.STATE state) throws Exceptions.SyntaxErrorException {

        int i, j, token_id;
        AST left, right; // byref anyway
        boolean found;

        // Used To Validate Inputs
        state.num_exp = 0;

        for (i = 0; i < state.token_list.size(); i++) {

            // Process Existing Items On The Stack
            if ((state.token_list.get(i).type == TOKEN_END_STATEMENT) ||
                    (state.token_list.get(i).type == TOKEN_BLOCK_END) ||
                    (state.token_list.get(i).type == TOKEN_VAR_END) ||
                    (state.token_list.get(i).type == TOKEN_CLOSE_PAREN) ||
                    (state.token_list.get(i).type == TOKEN_COMMA) ||
                    (state.token_list.get(i).type == TOKEN_COND_ELSE)) {

                while (state.stack_op.size()>0 && (top_op(state) >= 0) && (state.token_list.get(top_op(state)).prec >= state.token_list.get(i).prec)) {

                    // The Following Operators Require Special Handling
                    if ((state.token_list.get(top_op(state)).type == TOKEN_OPEN_PAREN) ||
                            (state.token_list.get(top_op(state)).type == TOKEN_BLOCK_BEGIN) ||
                            (state.token_list.get(top_op(state)).type == TOKEN_VAR_BEGIN) ||
                            (state.token_list.get(top_op(state)).type == TOKEN_IF) ||
                            (state.token_list.get(top_op(state)).type == TOKEN_ELSE) ||
                            (state.token_list.get(top_op(state)).type == TOKEN_REPEAT) ) {
                        break;
                    }
                    
                    // Add Expression To Stack
                    token_id = pop_op(state);
                    create_exp(state, state.token_list.get(token_id), token_id);
                }
            }

            // Process If/Else/Repeat Operators On Stack
            while ( (state.stack_op.size() > 1) && (top_op(state) >= 0) &&
                    ((state.token_list.get(top_op(state)).type == TOKEN_IF) || (state.token_list.get(top_op(state)).type == TOKEN_ELSE) || (state.token_list.get(top_op(state)).type == TOKEN_REPEAT))) {

                // Validate That If/Repeat Condition Is On The Stack
                if (((state.token_list.get(top_op(state)).type == TOKEN_IF) || (state.token_list.get(top_op(state)).type == TOKEN_REPEAT)) &&
                        ((top_exp(state).token_num < top_op(state)) || (top_exp(state).end_stmnt)))
                break;

                // Validate That Else Left Statement Is On The Stack
                if ((state.token_list.get(top_op(state)).type == TOKEN_ELSE) && (!top_exp(state).end_stmnt))
                break;

                // Validate That If/Else/Repeat Statement Is On The Stack
                if ((top_exp(state).token_num < top_op(state)) || (!top_exp(state).end_stmnt))
                break;

                // Add If/Else/Repeat Expression To Stack
                token_id = pop_op(state);
                create_exp(state, state.token_list.get(token_id), token_id);

                // Only Process A Single Statement When "Else" Token Arrives.  Still Need To Process Rest Of Else
                if (state.token_list.get(i).type == TOKEN_ELSE)
                    break;
            }

            // Process Token
            switch (state.token_list.get(i).type) {

                case TOKEN_COMMA:
                    break;

                case TOKEN_LITERAL:
                case TOKEN_TRUE:
                case TOKEN_FALSE:
                    create_exp(state, state.token_list.get(i), i);
                break;

                case TOKEN_END_STATEMENT:
                    // Flag Last Item On Stack As A Statement
                    if (!top_exp(state).end_stmnt) {
                    top_exp(state).end_stmnt = true;
                    state.num_exp--;
                }
                break;

                case TOKEN_VAR_END:
                    // Validate That The Top Operator Is The Var Begin
                    if (state.token_list.get(top_op(state)).type != TOKEN_VAR_BEGIN) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - Missing '['");
                    }
                    if ((state.stack_exp.size()==0) || top_exp(state).token_num < top_op(state)) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - Missing variable index");
                }

                // Set TOKEN_VAR_END To Match Data Type
                state.token_list.get(i).data_type = state.token_list.get(top_op(state)).data_type;

                pop_op(state);
                create_exp(state, state.token_list.get(i), i);

                // Check For Unary Operators On The Variable
                while ((top_op(state) >= 0) && (state.token_list.get(top_op(state)).type != TOKEN_VAR_BEGIN) && (state.token_list.get(top_op(state)).exp == EXP_EXPRESSION) && (state.token_list.get(top_op(state)).inputs <= 1)) {
                    token_id = pop_op(state);
                    create_exp(state, state.token_list.get(token_id), token_id);
                }

                break;

                case TOKEN_CLOSE_PAREN:
                    // Validate That The Top Operator Is The Open Paren
                    if (state.token_list.get(top_op(state)).type != TOKEN_OPEN_PAREN) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - Missing '('");
                    }
                    pop_op(state);

                    // Check If We Need To Link What's In Parentheses To A Function
                    if ((top_op(state) >= 0) && (state.token_list.get(top_op(state)).exp == EXP_FUNCTION)) {
                        token_id = pop_op(state);
                        create_exp(state, state.token_list.get(token_id), token_id);
                    }
                    break;

                case TOKEN_BLOCK_END:
                    // Validate That The Top Operator Is The Block Begin
                    if (state.token_list.get(top_op(state)).type != TOKEN_BLOCK_BEGIN) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - Missing '{'");
                    }

                    // Create Block For First Statement
                    if ((state.stack_exp.size()>1)) {
                        create_exp(state, state.token_list.get(i), top_op(state));
                        top_exp(state).end_stmnt = true;
                    }
                    else {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - '{}' Needs to include at least one statement");
                    }

                    // Create A Linked List Of Remaining Statements In The Block
                    while ((state.stack_exp.size()>1) && state.stack_exp.get(state.stack_exp.size()-2).token_num > top_op(state) && top_exp(state).token_num < i) {
                   create_exp(state, state.token_list.get(i), top_op(state));
                    top_exp(state).end_stmnt = true;
                }
                pop_op(state);

                // Link Block To If/Repeat/Function Statement
                while (state.stack_op.size()>0 && (top_op(state) >= 0) && (state.token_list.get(top_op(state)).type == TOKEN_IF || state.token_list.get(top_op(state)).type == TOKEN_ELSE || state.token_list.get(top_op(state)).type == TOKEN_REPEAT || state.token_list.get(top_op(state)).type == TOKEN_FUNCTION)) {
                    token_id = pop_op(state);
                    create_exp(state, state.token_list.get(token_id), token_id);
                }
                break;

                case TOKEN_ELSE:
                    // Validate That "Else" Has A Corresponding "If"
                    if ((state.stack_exp.size()==0) || top_exp(state).type != NODE_IF) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " +  state.token_list.get(i).line_num + " - Missing 'If'");
                }

                // Put If Operator Back On Stack For Later Processing
                push_op(state, top_exp(state).token_num);

                left = top_exp(state).left;
                right = top_exp(state).right;

                // Remove If Expression From Stack
                pop_exp(state);

                // Return Left & Right Expressions Back To Stack
                push_exp(state, left);
                push_exp(state, right);
                push_op(state, i);
                break;

                case TOKEN_COND_ELSE:
                    // Validate That The Top Operator Is The Conditional
                    if ((state.stack_exp.size()==0) || state.token_list.get(top_op(state)).type != TOKEN_CONDITIONAL) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line:  + state.token_list.get(token_id).line_num +  - Invalid 'Conditional' Statement");
                    }
                    push_op(state,i);
                    break;

                case TOKEN_BREAK:
                case TOKEN_CONTINUE:
                    // Validate That "Break" & "Continue" Are Tied To "Repeat"
                    found = false;
                    
                    for (j = 0; j < (state.stack_exp.size() - 1); j++) {
                        if (state.token_list.get(state.stack_op.get(j)).type == TOKEN_REPEAT) {
                            found = true;
                            push_op(state,i);
                            break;
                        }
                    }

                    if (!found) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.token_list.get(i).line_num + " - Invalid '" + (state.token_list.get(i).type == TOKEN_BREAK ? "Break" : "Continue") + "' Statement");
                    }
                    break;

                default:
                    // Process Expressions Already In Stack Based On Precedence
                    while (state.stack_op.size()>0 && (top_op(state) >= 0) && (state.token_list.get(top_op(state)).prec <= state.token_list.get(i).prec)) {

                        // The Following Operators Require Special Handling
                        if ((state.token_list.get(top_op(state)).type == TOKEN_FUNCTION) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_OPEN_PAREN) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_BLOCK_BEGIN) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_VAR_BEGIN) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_IF) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_ELSE) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_REPEAT) ||
                                (state.token_list.get(top_op(state)).type == TOKEN_CONDITIONAL)) {
                            break;
                        }

                        token_id = pop_op(state);
                        create_exp(state, state.token_list.get(token_id), token_id);
                    }

                    push_op(state, i);
                    break;
            }
        }

       validate_ast(state);
    }

    private static void validate_ast(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        int i, storage_sz_idx = 0, storage_idx_idx = 0;

        state.ast_func_idx = 0;

        if ((state.stack_exp.size()==0) || (state.stack_op.size()>0)) {
            throw new Exceptions.SyntaxErrorException("Fatal Error: Unable to parse source into ElasticPL");
        }

        // Get Index Of First Function
        for (i = 0; i < state.stack_exp.size(); i++) {
            if ((state.stack_exp.get(i).type != NODE_ARRAY_INT) && (state.stack_exp.get(i).type != NODE_ARRAY_UINT) && (state.stack_exp.get(i).type != NODE_ARRAY_LONG) && (state.stack_exp.get(i).type != NODE_ARRAY_ULONG) && (state.stack_exp.get(i).type != NODE_ARRAY_FLOAT) && (state.stack_exp.get(i).type != NODE_ARRAY_DOUBLE) && (state.stack_exp.get(i).type != NODE_STORAGE_SZ) && (state.stack_exp.get(i).type != NODE_STORAGE_IDX)) {
                break;
            }
            state.ast_func_idx++;
        }

        if (state.ast_func_idx == 0) {
            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.stack_exp.get(0).line_num + " - At least one variable array must be declared");
        }

        // Get Index Of Storage Declarations
        for (i = 0; i < state.stack_exp.size(); i++) {
            if (state.stack_exp.get(i).type == NODE_STORAGE_SZ) {
                if (storage_sz_idx > 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.stack_exp.get(i).line_num + " - Storage declaration 'storage_sz' can only be declared once");
                }
                storage_sz_idx = i;
            }
		else if (state.stack_exp.get(i).type == NODE_STORAGE_IDX) {
                if (storage_idx_idx > 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + state.stack_exp.get(i).line_num + " - Storage declaration 'storage_import_idx' can only be declared once");
                }
                storage_idx_idx = i;
            }
        }

        // todo: check if storage import index needs to be checked not to overlap the end?
        // If Storage Is Declared, Ensure Both Count & Index Are There
        if (storage_sz_idx>0 || storage_idx_idx>0) {
            if (storage_sz_idx == 0 || state.ast_storage_sz == 0) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: 'storage_sz' must be declared and greater than zero");
            }
            else if (storage_idx_idx==0) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Missing 'storage_idx' declaration");
            }
        }

        validate_functions(state);
    }

    private static void validate_functions(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        int i;
        AST exp;

        state.ast_main_idx = 0;
        state.ast_verify_idx = 0;

        for (i = state.ast_func_idx; i < state.stack_exp.size(); i++) { //todo: why was <= here before??

            if (state.stack_exp.get(i).type != NODE_FUNCTION) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " +  state.stack_exp.get(i).line_num + " - Statements must be contained in functions");
            }

            // Validate That Only One Instance Of "Main" Function Exists
            if (state.stack_exp.get(i).svalue.equalsIgnoreCase("main")) {
                if (state.ast_main_idx > 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: \" +  state.stack_exp.get(i).line_num + \" - \"main\" function already declared");
                }
                state.ast_main_idx = i;
            }

            // Validate That Only One Instance Of "Verify" Function Exists
		else if (state.stack_exp.get(i).svalue.equalsIgnoreCase("verify")) {
                if (state.ast_verify_idx > 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: \" +  state.stack_exp.get(i).line_num + \" - \"verify\" function already declared");
                }
                state.ast_verify_idx = i;
            }

            // Validate Function Has Brackets
            if (state.stack_exp.get(i).right == null) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: \" +  state.stack_exp.get(i).line_num + \" - Function missing {} brackets");
            }

            // Validate Function Has At Least One Statement
            if (state.stack_exp.get(i).right.left == null) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: \" +  state.stack_exp.get(i).line_num + \" - Functions must have at least one statement");
            }

            // Validate Function Only Contains Valid Statements

            // todo: to be fixed
            /*exp = state.stack_exp.get(i);
            while (exp.right != null) {
                if (exp.right.left != null && !exp.right.left.end_stmnt) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " +  exp.line_num + " - Invalid Statement");
                }
                exp = exp.right;
            }*/
        }

        // Validate That "Main" Function Exists
        if (state.ast_main_idx == 0) {
            throw new Exceptions.SyntaxErrorException("Syntax Error: \"main\" function not declared");
        }

        // Validate That "Verify" Function Exists
        if (state.ast_verify_idx == 0) {
            throw new Exceptions.SyntaxErrorException("Syntax Error: \"verify\" function not declared");
        }

        // Check For Recursive Calls To Functions
        validate_function_calls(state);
    }

    private static void validate_inputs(Primitives.STATE state, Primitives.SOURCE_TOKEN token, int token_num, Primitives.NODE_TYPE node_type) throws Exceptions.SyntaxErrorException {

        if ((token.inputs == 0) || (node_type == NODE_BLOCK))
            return;

        // Validate That There Are Enough Expressions / Statements On The Stack
        if (node_type == NODE_FUNCTION) {
            if (state.stack_exp.size() == 0) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Invalid number of inputs ");
            }
        }
        else if ((node_type == NODE_IF) || (node_type == NODE_ELSE) || (node_type == NODE_REPEAT)) {
            if (state.stack_exp.size() < 2) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Invalid number of inputs ");
            }
        }
        else if (state.num_exp < token.inputs) {
            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Invalid number of inputs ");
        }

        // Validate The Inputs For Each Node Type Are The Correct Type
        switch (node_type) {

            // VM Memory Declarations
            case NODE_ARRAY_INT:
            case NODE_ARRAY_UINT:
            case NODE_ARRAY_LONG:
            case NODE_ARRAY_ULONG:
            case NODE_ARRAY_FLOAT:
            case NODE_ARRAY_DOUBLE:
                if ((top_exp(state).token_num > token_num) && !top_exp(state).is_signed && !top_exp(state).is_float) {

                if (top_exp(state).uvalue == 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array size must be greater than zero");
                }

                // Check That There Is Only One Instance Of Each Data Type Array
                switch (node_type) {
                    case NODE_ARRAY_INT:
                        if (state.ast_vm_ints != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Int array already declared");
                        }
                        state.ast_vm_ints = ((Long)top_exp(state).uvalue).intValue();
                        break;
                    case NODE_ARRAY_UINT:
                        if (state.ast_vm_uints != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Unsigned Int array already declared");
                        }
                        state.ast_vm_uints = ((Long)top_exp(state).uvalue).intValue();
                        break;
                    case NODE_ARRAY_LONG:
                        if (state.ast_vm_longs != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Long array already declared");
                        }
                        state.ast_vm_longs = ((Long)top_exp(state).uvalue).intValue();
                        break;
                    case NODE_ARRAY_ULONG:
                        if (state.ast_vm_ulongs != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Unsigned Long array already declared");
                        }
                        state.ast_vm_ulongs = ((Long)top_exp(state).uvalue).intValue();
                        break;
                    case NODE_ARRAY_FLOAT:
                        if (state.ast_vm_floats != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Float array already declared");
                        }
                        state.ast_vm_floats = ((Long)top_exp(state).uvalue).intValue();
                        break;
                    case NODE_ARRAY_DOUBLE:
                        if (state.ast_vm_doubles != 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Double array already declared");
                        }
                        state.ast_vm_doubles = ((Long)top_exp(state).uvalue).intValue();
                        break;
                }

                // Check If Total Allocated VM Memory Is Less Than Max Allowed
                if ((((state.ast_vm_ints + state.ast_vm_uints + state.ast_vm_floats) * 4) + ((state.ast_vm_longs + state.ast_vm_ulongs + state.ast_vm_doubles) * 8)) > ast_vm_MEMORY_SIZE) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error - Requested VM Memory (" + (((state.ast_vm_ints + state.ast_vm_uints + state.ast_vm_floats) * 4) + ((state.ast_vm_longs + state.ast_vm_ulongs + state.ast_vm_doubles) * 8)) + " bytes) exceeds allowable (" + ast_vm_MEMORY_SIZE + " bytes)");
                }
                return;
            }
            break;

            // VM Storage Declarations
            case NODE_STORAGE_SZ:
            case NODE_STORAGE_IDX:
                if ((state.stack_exp.size() > 1) &&
                        (top_exp(state).token_num > token_num) &&
                (top_exp(state).type == NODE_CONSTANT) &&
                (top_exp(state).data_type == DT_UINT)) {

                // Check That Global Unsigned Int Array Has Been Declared
                if (state.ast_vm_uints == 0) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Unsigned Int array must be declared before 'storage' statements");
                }

                // Save Storage Value
                if (node_type == NODE_STORAGE_SZ)
                    state.ast_storage_sz = ((Long)top_exp(state).uvalue).intValue();
			    else if (node_type == NODE_STORAGE_IDX)
                    state.ast_storage_idx = ((Long)top_exp(state).uvalue).intValue();

                // Check That Storage Indexes Are Within Unsigned Int Array
                if (state.ast_vm_uints < (state.ast_storage_sz + state.ast_storage_idx)) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - 'storage_sz' + 'storage_idx' must be within Unsigned Int array range");
                }
                return;
            }
            break;

            // CONSTANT Declaration (1 Number)
            case NODE_CONSTANT:
                if ((top_exp(state).token_num < token_num) && (top_exp(state).data_type != DT_NONE))
                return;
            break;

            // Variable Declaration (1 Unsigned Int/Long)
            case NODE_VAR_CONST:
            case NODE_VAR_EXP:
                if ((top_exp(state).token_num < token_num) &&
                ((top_exp(state).data_type == DT_UINT) || (top_exp(state).data_type == DT_ULONG))) {

                switch (token.data_type) {
                    case DT_INT:
                        if (state.ast_vm_ints == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Int array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_ints)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_UINT:
                        if (state.ast_vm_uints == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Unsigned Int array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_uints)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_LONG:
                        if (state.ast_vm_longs == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Long array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_longs)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_ULONG:
                        if (state.ast_vm_ulongs == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Unsigned Long array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_ulongs)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_FLOAT:
                        if (state.ast_vm_floats == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Float array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_floats)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_DOUBLE:
                        if (state.ast_vm_doubles == 0) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Double array not declared");
                        }
                        else if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_vm_doubles)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_UINT_M: // m[]
                        if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= VM_M_ARRAY_SIZE)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                    case DT_UINT_S: // s[]
                        if ((node_type == NODE_VAR_CONST) && (top_exp(state).uvalue >= state.ast_storage_sz)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Array index out of bounds");
                    }
                    break;
                }
                return;
            }
            break;

            // Function Declarations (1 Constant & 1 Block)
            case NODE_FUNCTION: // todo: is -2 correct here`
                // todo: i dont understand this
                if ((state.stack_exp.get(state.stack_exp.size()-2).type == NODE_CONSTANT) || (state.stack_exp.get
                        (state.stack_exp.size()-2).type == NODE_FUNCTION))
                return;
            break;

            // Function Call Declarations (1 Constant)
            case NODE_CALL_FUNCTION:
                if ((top_exp(state).type == NODE_CONSTANT) && top_exp(state).svalue!=null)
                return;
            break;

            // IF Statement (1 Number & 1 Statement)
            case NODE_IF:
                if ((state.stack_exp.get(state.stack_exp.size()-2).data_type != DT_NONE) &&
                ((top_exp(state).end_stmnt == true) || (top_exp(state).type == NODE_IF) || (top_exp(state).type == NODE_ELSE) || (top_exp(state).type == NODE_REPEAT) || (top_exp(state).type == NODE_BREAK) || (top_exp(state).type == NODE_CONTINUE))) {

                if (top_exp(state).type == NODE_REPEAT) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - A 'repeat' statement under an 'if' statement must be enclosed in {} brackets");
                }

                return;
            }
            break;

            // ELSE Statement (2 Statements)
            case NODE_ELSE:
                if ((state.stack_exp.get(state.stack_exp.size()-2).end_stmnt == true) && (top_exp(state).end_stmnt == true)) {

                if (top_exp(state).type == NODE_REPEAT) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - A 'repeat' statement under an 'else' statement must be enclosed in {} brackets");
                }

                return;
            }
            break;

            // REPEAT Statement (2 Unsigned Int & 1 Constant Unsigned Int & 1 Block)
            case NODE_REPEAT:
                if (state.stack_exp.size()>3 &&
                        (state.stack_exp.get(state.stack_exp.size()-4).type == NODE_VAR_CONST) &&
                (state.stack_exp.get(state.stack_exp.size()-4).data_type == DT_UINT) &&
                ((state.stack_exp.get(state.stack_exp.size()-3).type == NODE_VAR_CONST) || (state.stack_exp.get(state.stack_exp.size()-3).type == NODE_VAR_EXP) || (state.stack_exp.get(state.stack_exp.size()-3).type == NODE_CONSTANT)) &&
                (state.stack_exp.get(state.stack_exp.size()-3).data_type == DT_UINT) &&
                (state.stack_exp.get(state.stack_exp.size()-2).type == NODE_CONSTANT) &&
                (state.stack_exp.get(state.stack_exp.size()-2).data_type == DT_UINT) &&
                (top_exp(state).type == NODE_BLOCK))
                return;
            break;

            // Expressions w/ 1 Number (Right Operand)
            case NODE_VERIFY_BTY:
            case NODE_NOT:
                if ((top_exp(state).token_num > token_num) && (top_exp(state).data_type != DT_NONE))
                return;
            break;

            // Expressions w/ 1 Int/Uint/Long/ULong (Right Operand)
            case NODE_COMPL:
            case NODE_ABS:
                if ((top_exp(state).token_num > token_num) &&
                (top_exp(state).data_type != DT_NONE) &&
                (!top_exp(state).is_float))
                return;
            break;

            // Expressions w/ 1 Int/Long/Float/Double (Right Operand)
            case NODE_NEG:
                if ((top_exp(state).token_num > token_num) &&
                (top_exp(state).data_type != DT_NONE) &&
                (top_exp(state).is_signed))
                return;
            break;

            // Expressions w/ 1 Variable (Left Operand)
            case NODE_INCREMENT_R:
            case NODE_DECREMENT_R:
                if (top_exp(state).is_vm_mem /* || top_exp(state).is_vm_storage */ /* changed: allow to write into storage */) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Illegal assignment to m/s array");
            }

            if ((top_exp(state).token_num > token_num) &&
            ((top_exp(state).type == NODE_VAR_CONST) || (top_exp(state).type == NODE_VAR_EXP)))
            return;
            break;

            // Expressions w/ 1 Variable (Right Operand)
            case NODE_INCREMENT_L:
            case NODE_DECREMENT_L:
                if (top_exp(state).is_vm_mem /* || top_exp(state).is_vm_storage */ /* changed: allow to write into storage */) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Illegal assignment to m/s array");
            }

            if ((top_exp(state).token_num < token_num) &&
            ((top_exp(state).type == NODE_VAR_CONST) || (top_exp(state).type == NODE_VAR_EXP)))
            return;
            break;

            // Expressions w/ 1 Variable (Left Operand) & 1 Number (Right Operand)
            case NODE_ASSIGN:
            case NODE_ADD_ASSIGN:
            case NODE_SUB_ASSIGN:
            case NODE_MUL_ASSIGN:
            case NODE_DIV_ASSIGN:
                if (state.stack_exp.get(state.stack_exp.size()-2).is_vm_mem /*|| state.stack_exp.get(state.stack_exp.size()-2).is_vm_storage */ /* changed: allow to write into storage */) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Illegal assignment to m/s array");
            }

            if (((state.stack_exp.get(state.stack_exp.size()-2).token_num < token_num) && (top_exp(state).token_num > token_num)) &&
            ((state.stack_exp.get(state.stack_exp.size()-2).type == NODE_VAR_CONST) || (state.stack_exp.get(state.stack_exp.size()-2).type == NODE_VAR_EXP)) &&
            (top_exp(state).data_type != DT_NONE))
            return;
            break;

            // Expressions w/ 1 Int/Uint/Long/Ulong Variable (Left Operand) & 1 Int/Uint/Long/Ulong (Right Operand)
            case NODE_MOD_ASSIGN:
            case NODE_LSHFT_ASSIGN:
            case NODE_RSHFT_ASSIGN:
            case NODE_AND_ASSIGN:
            case NODE_XOR_ASSIGN:
            case NODE_OR_ASSIGN:
                if (state.stack_exp.get(state.stack_exp.size()-2).is_vm_mem /*|| state.stack_exp.get(state.stack_exp.size()-2).is_vm_storage */ /* changed: allow to write into storage */) {
                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Illegal assignment to m/s array");
            }

            if (((state.stack_exp.get(state.stack_exp.size()-2).token_num < token_num) && (top_exp(state).token_num > token_num)) &&
            ((state.stack_exp.get(state.stack_exp.size()-2).type == NODE_VAR_CONST) || (state.stack_exp.get(state.stack_exp.size()-2).type == NODE_VAR_EXP)) &&
            (!state.stack_exp.get(state.stack_exp.size()-2).is_float) &&
            (top_exp(state).data_type != DT_NONE) &&
            (!top_exp(state).is_float))
            return;
            break;

            // Expressions w/ 2 Numbers
            case NODE_MUL:
            case NODE_DIV:
            case NODE_MOD:
            case NODE_ADD:
            case NODE_SUB:
            case NODE_LE:
            case NODE_GE:
            case NODE_LT:
            case NODE_GT:
            case NODE_EQ:
            case NODE_NE:
            case NODE_AND:
            case NODE_OR:
            case NODE_CONDITIONAL:
            case NODE_COND_ELSE:
                if (((state.stack_exp.get(state.stack_exp.size()-2).token_num < token_num) && (top_exp(state).token_num > token_num)) &&
                ((state.stack_exp.get(state.stack_exp.size()-2).data_type != DT_NONE)) &&
                (top_exp(state).data_type != DT_NONE))
                return;
            break;

            // Expressions w/ 2 Ints/Uints/Longs/Ulongs
            case NODE_LROT:
            case NODE_LSHIFT:
            case NODE_RROT:
            case NODE_RSHIFT:
            case NODE_BITWISE_AND:
            case NODE_BITWISE_XOR:
            case NODE_BITWISE_OR:
                if (((state.stack_exp.get(state.stack_exp.size()-2).token_num < token_num) && (top_exp(state).token_num > token_num)) &&
                (state.stack_exp.get(state.stack_exp.size()-2).data_type != DT_NONE) &&
                (!state.stack_exp.get(state.stack_exp.size()-2).is_float) &&
                (top_exp(state).data_type != DT_NONE) &&
                (!top_exp(state).is_float))
                return;
            break;

            // Expressions w/ 4 Uints
            case NODE_VERIFY_POW:
                if (((state.stack_exp.get(state.stack_exp.size()-4).token_num > token_num) &&
                            (state.stack_exp.get(state.stack_exp.size()-1).token_num > token_num)) &&
                			(state.stack_exp.get(state.stack_exp.size()-4).data_type == DT_UINT) &&
                			(state.stack_exp.get(state.stack_exp.size()-3).data_type == DT_UINT) &&
                			(state.stack_exp.get(state.stack_exp.size()-2).data_type == DT_UINT) &&
                			(state.stack_exp.get(state.stack_exp.size()-1).data_type == DT_UINT))
                			return;
            	break;



            // Built-in Functions w/ 1 Number
            case NODE_SIN:
            case NODE_COS:
            case NODE_TAN:
            case NODE_SINH:
            case NODE_COSH:
            case NODE_TANH:
            case NODE_ASIN:
            case NODE_ACOS:
            case NODE_ATAN:
            case NODE_EXPNT:
            case NODE_LOG:
            case NODE_LOG10:
            case NODE_SQRT:
            case NODE_CEIL:
            case NODE_FLOOR:
            case NODE_FABS:
                if ((top_exp(state).token_num > token_num) &&
                (top_exp(state).data_type != DT_NONE))
                return;
            break;

            // Built-in Functions w/ 2 Numbers
            case NODE_ATAN2:
            case NODE_POW:
            case NODE_FMOD:
            case NODE_GCD:
                if (((state.stack_exp.get(state.stack_exp.size()-2).token_num > token_num) && (top_exp(state).token_num > token_num)) &&
                ((state.stack_exp.get(state.stack_exp.size()-2).data_type != DT_NONE)) &&
                (top_exp(state).data_type != DT_NONE))
                return;
            break;

            default:
                break;
        }

       throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + token.line_num + " - Invalid inputs for '" + get_node_str(node_type) + "'"); // todo nodestr function
    }

    private static String get_node_str(Primitives.NODE_TYPE node_type) {
            switch (node_type) {
                case NODE_ARRAY_INT:
                    return "array_int";
                case NODE_ARRAY_UINT:
                    return "array_uint";
                case NODE_ARRAY_LONG:
                    return "array_long";
                case NODE_ARRAY_ULONG:
                    return "array_ulong";
                case NODE_ARRAY_FLOAT:
                    return "array_float";
                case NODE_ARRAY_DOUBLE:
                    return "array_double";
                case NODE_STORAGE_SZ:
                    return "storage_sz";
                case NODE_STORAGE_IDX:
                    return "storage_idx";
                case NODE_CONSTANT:
                    return "(const)";
                case NODE_VAR_CONST:
                    return "array[]";
                case NODE_VAR_EXP:
                    return "array[x]";
                case NODE_FUNCTION:
                    return "function";
                case NODE_CALL_FUNCTION:
                    return "(call)";
                case NODE_VERIFY_BTY:
                    return "verify_bty";
                case NODE_VERIFY_POW:
                    return "verify_pow";
                case NODE_ASSIGN:
                    return "=";
                case NODE_OR:
                    return "||";
                case NODE_AND:
                    return "&&";
                case NODE_BITWISE_OR:
                    return "|";
                case NODE_BITWISE_XOR:
                    return "^";
                case NODE_BITWISE_AND:
                    return "&";
                case NODE_EQ:
                    return "==";
                case NODE_NE:
                    return "!=";
                case NODE_LT:
                    return "<";
                case NODE_GT:
                    return ">";
                case NODE_LE:
                    return "<=";
                case NODE_GE:
                    return ">=";
                case NODE_INCREMENT_R:
                    return "++";
                case NODE_INCREMENT_L:
                    return "++";
                case NODE_ADD_ASSIGN:
                    return "+=";
                case NODE_SUB_ASSIGN:
                    return "-=";
                case NODE_MUL_ASSIGN:
                    return "*=";
                case NODE_DIV_ASSIGN:
                    return "/=";
                case NODE_MOD_ASSIGN:
                    return "%=";
                case NODE_LSHFT_ASSIGN:
                    return "<<=";
                case NODE_RSHFT_ASSIGN:
                    return ">>=";
                case NODE_AND_ASSIGN:
                    return "&=";
                case NODE_XOR_ASSIGN:
                    return "^=";
                case NODE_OR_ASSIGN:
                    return "|=";
                case NODE_ADD:
                    return "+";
                case NODE_DECREMENT_R:
                    return "--";
                case NODE_DECREMENT_L:
                    return "--";
                case NODE_SUB:
                    return "-";
                case NODE_NEG:
                    return "'-'";
                case NODE_MUL:
                    return "*";
                case NODE_DIV:
                    return "/";
                case NODE_MOD:
                    return "%";
                case NODE_RSHIFT:
                    return ">>";
                case NODE_LSHIFT:
                    return "<<";
                case NODE_RROT:
                    return ">>>";
                case NODE_LROT:
                    return "<<<";
                case NODE_COMPL:
                    return "~";
                case NODE_NOT:
                    return "!";
                case NODE_TRUE:
                    return "true";
                case NODE_FALSE:
                    return "false";
                case NODE_BLOCK:
                    return "{}";
                case NODE_IF:
                    return "if";
                case NODE_ELSE:
                    return "else";
                case NODE_REPEAT:
                    return "repeat";
                case NODE_BREAK:
                    return "break";
                case NODE_CONTINUE:
                    return "continue";
                case NODE_SIN:
                    return "sin";
                case NODE_COS:
                    return "cos";
                case NODE_TAN:
                    return "tan";
                case NODE_SINH:
                    return "sinh";
                case NODE_COSH:
                    return "cosh";
                case NODE_TANH:
                    return "tanh";
                case NODE_ASIN:
                    return "asin";
                case NODE_ACOS:
                    return "acos";
                case NODE_ATAN:
                    return "atan";
                case NODE_ATAN2:
                    return "atan2";
                case NODE_EXPNT:
                    return "exp";
                case NODE_LOG:
                    return "log";
                case NODE_LOG10:
                    return "log10";
                case NODE_POW:
                    return "pow";
                case NODE_SQRT:
                    return "sqrt";
                case NODE_CEIL:
                    return "ceil";
                case NODE_FLOOR:
                    return "floor";
                case NODE_ABS:
                    return "abs";
                case NODE_FABS:
                    return "fabs";
                case NODE_FMOD:
                    return "fmod";
                case NODE_GCD:
                    return "gcd";
                default:
                    return "Unknown";
            }
    }

    private static void validate_function_calls(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        int i;
        boolean downward = true;
        AST root = null;
        AST ast_ptr = null;
        Stack<AST> call_stack = new Stack<>();
        Stack<AST> rpt_stack = new Stack<>();

        // Set Root To Main Function
        root = state.stack_exp.get(state.ast_main_idx);
        call_stack.add(root);

        ast_ptr = root;

        while (ast_ptr!=null) {

            // Navigate Down The Tree
            if (downward) {

                // Navigate To Lowest Left Parent Node
                while (ast_ptr.left != null) {
                    ast_ptr = ast_ptr.left;

                    // Validate Repeat Node
                    if (ast_ptr.type == NODE_REPEAT) {

                        // Validate That Repeat Counter Has Not Been Used
                        for (i = 0; i < rpt_stack.size(); i++) {
                            if (ast_ptr.uvalue == rpt_stack.get(i).uvalue) {
                                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - Repeat loop counter already used");
                            }
                        }

                        rpt_stack.add(ast_ptr);

                        if (rpt_stack.size() >= REPEAT_STACK_SIZE) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " -" +
                                    " Repeat statements can only be nested up to " + (REPEAT_STACK_SIZE - 1) +
                                    " levels");
                        }

                    }
                }

                // Switch To Root Of Called Function
                if (ast_ptr.type == NODE_CALL_FUNCTION) {
                    if (ast_ptr.left != null)
                        ast_ptr = ast_ptr.left;
                    // Get AST Index For The Function
                    if (ast_ptr.uvalue == 0) {

                        for (i = 0; i < state.stack_exp.size(); i++) {
                            if ((state.stack_exp.get(i).type == NODE_FUNCTION) && state.stack_exp.get(i).svalue
                                    .equalsIgnoreCase(ast_ptr.svalue)) {
                                ast_ptr.uvalue = i;
                            }
                        }
                    }

                    // Validate Function Exists
                    if (ast_ptr.uvalue == 0) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - " +
                                "Function '" +  ast_ptr.svalue + "' not found");
                    }

                    // Validate That "main" Function Is Not Called
                    if ((ast_ptr.uvalue == state.ast_main_idx)) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - Illegal function call");
                    }

                    // Validate That Functions Is Not Recursively Called
                    for (i = 0; i < call_stack.size(); i++) {
                        if (ast_ptr.uvalue == call_stack.get(i).uvalue) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - Illegal recursive function call");
                        }
                    }

                    // Store The Lowest Level In Call Stack For The Function
                    // Needed To Determine Order Of Processing Functions During WCET Calc
                    if (call_stack.size() > state.stack_exp.get((int)ast_ptr.uvalue).uvalue) { // todo: check if > or >=
                        state.stack_exp.get((int) ast_ptr.uvalue).uvalue = call_stack.size(); // same here (todo)
                    }

                    call_stack.push(ast_ptr);
                    ast_ptr = state.stack_exp.get((int)ast_ptr.uvalue);

                    if (call_stack.size() >= CALL_STACK_SIZE) {
                        throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - " +
                                "Functions can only be nested up to " + (CALL_STACK_SIZE - 1) + " levels");
                    }

                }

                // If There Is A Right Node, Switch To It
                if (ast_ptr.right!=null) {
                    ast_ptr = ast_ptr.right;

                    // Validate Repeat Node
                    if (ast_ptr.type == NODE_REPEAT) {

                        // Validate That Repeat Counter Has Not Been Used
                        for (i = 0; i < rpt_stack.size(); i++) {
                            if (ast_ptr.uvalue == rpt_stack.get(i).uvalue) {
                                throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " - Repeat loop counter already used");
                            }
                        }

                        rpt_stack.push(ast_ptr);

                        if (rpt_stack.size() >= REPEAT_STACK_SIZE) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error: Line: " + ast_ptr.line_num + " -" +
                                    " Repeat statements can only be nested up to " + (REPEAT_STACK_SIZE - 1) + " " +
                                    "levels");
                        }
                    }
                }
                // Otherwise, Print Current Node & Navigate Back Up The Tree
                else {
                    downward = false;
                }
            }

            // Navigate Back Up The Tree
            else {

                // Quit When We Reach The Root Of Main Function
                if (ast_ptr == root)
                    break;

                // Remove 'Repeat' From Stack
                if (ast_ptr.type == NODE_REPEAT) {
                    rpt_stack.pop();
                }

                // Return To Calling Function When We Reach The Root Of Called Function
                if (ast_ptr.parent.type == NODE_FUNCTION) {
                    //call_stack.pop();
                    ast_ptr = call_stack.pop();
                }
                else {
                    // Check If We Need To Navigate Back Down A Right Branch
                    if ((ast_ptr == ast_ptr.parent.left) && (ast_ptr.parent.right!=null)) {
                        ast_ptr = ast_ptr.parent.right;
                        downward = true;
                    }
                    else {
                        ast_ptr = ast_ptr.parent;
                    }
                }
            }
        }
    }
}
