package com.community;

import static com.community.Constants.MAX_AST_DEPTH;
import static com.community.Constants.REPEAT_STACK_SIZE;
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
public class WCETCalculator {
    public static int calc_wcet(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        int i, call_depth = 0;
        int wcet;

        // return if already done
        if(state.initialized_ast_stats == true){
            return state.calculated_wcet;
        }

        // Get Max Function Call Depth
        for (i = state.ast_func_idx; i < state.stack_exp.size(); i++) {
            if (state.stack_exp.get(i).uvalue > call_depth)
            call_depth = (int)state.stack_exp.get(i).uvalue;
        }

        // Calculate WCET For Each Function Beginning With The Lowest One In Call Stack
        while (call_depth >= 0) {
            for (i = state.ast_func_idx; i < state.stack_exp.size(); i++) {
                if (state.stack_exp.get(i).uvalue == call_depth) {
                    wcet = calc_function_weight(state, state.stack_exp.get(i));
                    // Store WCET Value In Function's 'fvalue' Field
                    state.stack_exp.get(i).fvalue = wcet;
                }
            }
            call_depth--;
        }

        /*for (i = state.ast_func_idx; i < state.stack_exp.size(); i++) {
            System.out.println("[dbg] function " + state.stack_exp.get(i).svalue + ", WCET: " + state.stack_exp
                    .get(i).fvalue + ", DEPTH: " + state.stack_exp.get(i).uvalue);
        }*/



        state.initialized_ast_stats = true;
        state.calculated_wcet = (int)state.stack_exp.get(state.ast_main_idx).fvalue;
        return state.calculated_wcet;
    }

    public static int get_verify_wcet(Primitives.STATE state){
        if(state.calculated_wcet>0)
            return (int)state.stack_exp.get(state.ast_verify_idx).fvalue;
        else
            return -1;
    }

    private static int calc_function_weight(Primitives.STATE state, Primitives.AST root) throws Exceptions.SyntaxErrorException {
        int depth = 0,  weight = 0, total_weight = 0;
        int[] block_weight = new int[REPEAT_STACK_SIZE];
        int block_level = -1;
        boolean downward = true;
        Primitives.AST ast_ptr;

        if (root == null)
            return 0;

        ast_ptr = root;
        depth = 1;

        while (ast_ptr!=null) {
            weight = 0;

            // Navigate Down The Tree
            if (downward) {

                // Navigate To Lowest Left Node
                while (ast_ptr.left != null) {
                    ast_ptr = ast_ptr.left;

                    if ((ast_ptr.type == NODE_IF) || (ast_ptr.type == NODE_ELSE))
                        weight = get_node_weight(state, ast_ptr);

                    // Check For Built In Function
                    if ((ast_ptr.exp == EXP_FUNCTION))
                        weight = get_node_weight(state, ast_ptr);

                    // Check For "Repeat" Blocks
                    if (ast_ptr.type == NODE_REPEAT) {
                        weight = get_node_weight(state, ast_ptr);
                        weight += get_node_weight(state, ast_ptr.left);
                        block_level++;
                        block_weight[block_level] = 0;
                        break;
                    }

                    // difference to sprocket: immediately exit do not allow to build a too larget tree in the first
                    // place
                    if (++depth > MAX_AST_DEPTH) {
                        throw new Exceptions.SyntaxErrorException("ERROR: Max allowed AST depth exceeded (" + depth + ")");
                    }
                }

                // If There Is A Right Node, Switch To It
                if (ast_ptr.right!=null) {
                    ast_ptr = ast_ptr.right;
                    // difference to sprocket: immediately exit do not allow to build a too larget tree in the first
                    // place
                    if (++depth > MAX_AST_DEPTH) {
                        throw new Exceptions.SyntaxErrorException("ERROR: Max allowed AST depth exceeded (" + depth + ")");
                    }
                }
                // Otherwise, Get Weight Of Current Node & Navigate Back Up The Tree
                else {
                    weight = get_node_weight(state, ast_ptr);
                    downward = false;
                }
            }

            // Navigate Back Up The Tree
            else {
                if (ast_ptr == root)
                    break;

                // Check If We Need To Navigate Back Down A Right Branch
                if ((ast_ptr == ast_ptr.parent.left) && (ast_ptr.parent.right!=null)) {

                    ast_ptr = ast_ptr.parent.right;
                    downward = true;

                    if ((ast_ptr.type == NODE_IF) || (ast_ptr.type == NODE_ELSE))
                        weight = get_node_weight(state, ast_ptr);

                    // Check For Built In Function
                    if ((ast_ptr.exp == EXP_FUNCTION))
                        weight = get_node_weight(state, ast_ptr);

                    // Check For "Repeat" Blocks
                    if (ast_ptr.type == NODE_REPEAT) {
                        weight = get_node_weight(state, ast_ptr);
                        weight += get_node_weight(state, ast_ptr.left);
                        block_level++;
                        block_weight[block_level] = 0;
                        ast_ptr = ast_ptr.right;
                    }
                    else {
                        weight = get_node_weight(state, ast_ptr.parent);
                        depth--;
                    }
                }
                else {
                    if (((ast_ptr.type == NODE_IF) && (ast_ptr.right.type != NODE_ELSE) ) || (ast_ptr.type ==
                            NODE_ELSE))
                        weight = get_node_weight(state, ast_ptr);
                    ast_ptr = ast_ptr.parent;
                }
            }

            if ((block_level >= 0) && (ast_ptr.parent.type != NODE_REPEAT))
                block_weight[block_level] += weight;
            else
                total_weight += weight; // todo: check with original code, was there a overflow check here?

            // Get Total weight For The "Repeat" Block
            if ((!downward) && (block_level >= 0) && (ast_ptr.type == NODE_REPEAT)) {
                if (block_level == 0)
                    total_weight += ((int)ast_ptr.ivalue * block_weight[block_level]);
                else
                    block_weight[block_level - 1] += ((int)ast_ptr.ivalue * block_weight[block_level]);
                block_level--;
            }
        }

        return total_weight;
    }
    
    private static int get_node_weight(Primitives.STATE state, Primitives.AST node) {
        int weight = 1;

        if (node==null)
            return 0;

        // Increase Weight For 64bit Operations
        if (node.is_64bit)
            weight = 2;

        switch (node.type) {
            case NODE_IF:
            case NODE_ELSE:
            case NODE_COND_ELSE:
                return weight * 4;

            case NODE_REPEAT:
                return weight * 10;

            case NODE_BREAK:
            case NODE_CONTINUE:
                return weight;

            // Variable / Constants (Weight x 1)
            case NODE_CONSTANT:
            case NODE_VAR_CONST:
            case NODE_VAR_EXP:
                return weight;

            // Assignments (Weight x 1)
            case NODE_ASSIGN:
            case NODE_ADD_ASSIGN:
            case NODE_SUB_ASSIGN:
            case NODE_MUL_ASSIGN:
            case NODE_DIV_ASSIGN:
            case NODE_MOD_ASSIGN:
            case NODE_LSHFT_ASSIGN:
            case NODE_RSHFT_ASSIGN:
            case NODE_AND_ASSIGN:
            case NODE_XOR_ASSIGN:
            case NODE_OR_ASSIGN:
                return weight;

            // Simple Operations (Weight x 1)
            case NODE_AND:
            case NODE_OR:
            case NODE_BITWISE_AND:
            case NODE_BITWISE_XOR:
            case NODE_BITWISE_OR:
            case NODE_EQ:
            case NODE_NE:
            case NODE_GT:
            case NODE_LT:
            case NODE_GE:
            case NODE_LE:
                return weight;

            case NODE_NOT:
            case NODE_COMPL:
            case NODE_NEG:
            case NODE_INCREMENT_R:
            case NODE_INCREMENT_L:
            case NODE_DECREMENT_R:
            case NODE_DECREMENT_L:
                return weight;

            // Medium Operations (Weight x 2)
            case NODE_ADD:
            case NODE_SUB:
            case NODE_LSHIFT:
            case NODE_RSHIFT:
            case NODE_VERIFY:
            case NODE_CONDITIONAL:
                return weight * 2;

            // Complex Operations (Weight x 3)
            case NODE_MUL:
            case NODE_DIV:
            case NODE_MOD:
            case NODE_LROT:
            case NODE_RROT:
                return weight * 3;

            // Complex Operations (Weight x 2)
            case NODE_ABS:
            case NODE_CEIL:
            case NODE_FLOOR:
            case NODE_FABS:
                return weight * 2;

            // Medium Functions (Weight x 4)
            case NODE_SIN:
            case NODE_COS:
            case NODE_TAN:
            case NODE_SINH:
            case NODE_COSH:
            case NODE_TANH:
            case NODE_ASIN:
            case NODE_ACOS:
            case NODE_ATAN:
            case NODE_FMOD:
                return weight * 4;

            // Complex Functions (Weight x 6)
            case NODE_EXPNT:
            case NODE_LOG:
            case NODE_LOG10:
            case NODE_SQRT:
            case NODE_ATAN2:
            case NODE_POW:
            case NODE_GCD:
                return weight * 6;

            // Function Calls (4 + Weight Of Called Function)
            case NODE_CALL_FUNCTION:
                return 4 + (int)state.stack_exp.get((int)node.uvalue).fvalue;

            case NODE_BLOCK:
            case NODE_PARAM:
                break;

            default:
                break;
        }

        return 0;
    }

}
