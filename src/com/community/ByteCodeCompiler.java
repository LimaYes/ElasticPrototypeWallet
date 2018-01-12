package com.community;


import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Stack;

import static com.community.EnigmaProgram.MEM_TARGET_STORE.*;

public class ByteCodeCompiler {

    public static void build_bytecode(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        for (int i = state.ast_func_idx; i < state.stack_exp.size(); i++) {
            convert_function(state, state.stack_exp.get(i));
        }
    }

    public static byte [] int2ByteArray (int value)
    {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    public static byte [] long2ByteArray (long value)
    {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    public static byte [] double2ByteArray (double value)
    {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value).array();
    }


    public static ArrayList<Byte> compile(Primitives.AST node){
        ArrayList<Byte> code = new ArrayList<>();
        switch(node.type){
            /**
             * These here are very straightforward
             */
            case NODE_MUL:
                code.add(EnigmaOpCode.ENIGMA_MUL.getOp());
                break;
            case NODE_ADD:
                code.add(EnigmaOpCode.ENIGMA_ADD.getOp());
                break;
            case NODE_SUB:
                code.add(EnigmaOpCode.ENIGMA_SUB.getOp());
                break;
            case NODE_DIV:
                code.add(EnigmaOpCode.ENIGMA_DIV.getOp());
                break;
            case NODE_BITWISE_AND:
                code.add(EnigmaOpCode.ENIGMA_BITWISE_AND.getOp());
                break;
            case NODE_BITWISE_OR:
                code.add(EnigmaOpCode.ENIGMA_BITWISE_OR.getOp());
                break;
            case NODE_BITWISE_XOR:
                code.add(EnigmaOpCode.ENIGMA_BITWISE_XOR.getOp());
                break;
            case NODE_MOD:
                code.add(EnigmaOpCode.ENIGMA_MOD.getOp());
                break;
            case NODE_ABS:
                code.add(EnigmaOpCode.ENIGMA_ABS.getOp());
                break;
            case NODE_ACOS:
                code.add(EnigmaOpCode.ENIGMA_ACOS.getOp());
                break;
            case NODE_SIN:
                code.add(EnigmaOpCode.ENIGMA_SIN.getOp());
                break;
            case NODE_SINH:
                code.add(EnigmaOpCode.ENIGMA_SINH.getOp());
                break;
            case NODE_TAN:
                code.add(EnigmaOpCode.ENIGMA_TAN.getOp());
                break;
            case NODE_TANH:
                code.add(EnigmaOpCode.ENIGMA_TANH.getOp());
                break;
            case NODE_OR:
                code.add(EnigmaOpCode.ENIGMA_OR.getOp());
                break;
            case NODE_AND:
                code.add(EnigmaOpCode.ENIGMA_AND.getOp());
                break;
            case NODE_ASIN:
                code.add(EnigmaOpCode.ENIGMA_ASIN.getOp());
                break;
            case NODE_ATAN:
                code.add(EnigmaOpCode.ENIGMA_ATAN.getOp());
                break;
            case NODE_ATAN2:
                code.add(EnigmaOpCode.ENIGMA_ATAN2.getOp());
                break;
            case NODE_POW:
                code.add(EnigmaOpCode.ENIGMA_POW.getOp());
                break;
            case NODE_CEIL:
                code.add(EnigmaOpCode.ENIGMA_CEIL.getOp());
                break;
            case NODE_COMPL:
                code.add(EnigmaOpCode.ENIGMA_COMPL.getOp());
                break;
            case NODE_SQRT:
                code.add(EnigmaOpCode.ENIGMA_SQRT.getOp());
                break;
            case NODE_COSH:
                code.add(EnigmaOpCode.ENIGMA_COSH.getOp());
                break;
            case NODE_FLOOR:
                code.add(EnigmaOpCode.ENIGMA_FLOOR.getOp());
                break;
            case NODE_FABS:
                code.add(EnigmaOpCode.ENIGMA_FABS.getOp());
                break;
            case NODE_FMOD:
                code.add(EnigmaOpCode.ENIGMA_FMOD.getOp());
                break;
            case NODE_FALSE:
                code.add(EnigmaOpCode.ENIGMA_PUSHUINT_1.getOp());
                code.add((byte)0x00);
                break;
            case NODE_TRUE:
                code.add(EnigmaOpCode.ENIGMA_PUSHUINT_1.getOp());
                code.add((byte)0x01);
                break;
            case NODE_EQ:
                code.add(EnigmaOpCode.ENIGMA_EQ.getOp());
                break;
            case NODE_LE:
                code.add(EnigmaOpCode.ENIGMA_LE.getOp());
                break;
            case NODE_LT:
                code.add(EnigmaOpCode.ENIGMA_LT.getOp());
                break;
            case NODE_GE:
                code.add(EnigmaOpCode.ENIGMA_GE.getOp());
                break;
            case NODE_GT:
                code.add(EnigmaOpCode.ENIGMA_GT.getOp());
                break;
            case NODE_NE:
                code.add(EnigmaOpCode.ENIGMA_NE.getOp());
                break;
            case NODE_EXPNT:
                code.add(EnigmaOpCode.ENIGMA_EXPNT.getOp());
                break;
            case NODE_GCD:
                code.add(EnigmaOpCode.ENIGMA_GCD.getOp());
                break;
            case NODE_LOG:
                code.add(EnigmaOpCode.ENIGMA_LOG.getOp());
                break;
            case NODE_LOG10:
                code.add(EnigmaOpCode.ENIGMA_LOG10.getOp());
                break;
            case NODE_RSHIFT:
                code.add(EnigmaOpCode.ENIGMA_RSHIFT.getOp());
                break;
            case NODE_LSHIFT:
                code.add(EnigmaOpCode.ENIGMA_LSHIFT.getOp());
                break;
            case NODE_LROT:
                code.add(EnigmaOpCode.ENIGMA_LROT.getOp());
                break;
            case NODE_RROT:
                code.add(EnigmaOpCode.ENIGMA_RROT.getOp());
                break;
            case NODE_VERIFY_BTY:
                code.add(EnigmaOpCode.ENIGMA_VERIFY_BTY.getOp());
                break;
            case NODE_VERIFY_POW:
                code.add(EnigmaOpCode.ENIGMA_VERIFY_POW.getOp());
                break;
            case NODE_COS:
                code.add(EnigmaOpCode.ENIGMA_COS.getOp());
                break;
            case NODE_NOT:
                code.add(EnigmaOpCode.ENIGMA_NOT.getOp());
                break;
            /*case NODE:
                code.add(EnigmaOpCode.ENIGMA_LOG10.getOp());
                break;
            case NODE_LOG10:
                code.add(EnigmaOpCode.ENIGMA_LOG10.getOp());
                break;

*/


            /**
             * Assignment operators
             */
            case NODE_VAR_CONST:
                break;
            case NODE_ASSIGN:
                break;


            /**
             * These are here to push data on the stack (constant typed)
             */

            case NODE_CONSTANT:
                code.add(EnigmaOpCode.ENIGMA_PUSH_TYPED_DATA.getOp());
                if(node.data_type == Primitives.DATA_TYPE.DT_FLOAT)
                    code.add((byte)F.ordinal());
                else if(node.data_type == Primitives.DATA_TYPE.DT_DOUBLE)
                    code.add((byte)D.ordinal());
                else if(node.data_type == Primitives.DATA_TYPE.DT_INT)
                    code.add((byte)I.ordinal());
                else if(node.data_type == Primitives.DATA_TYPE.DT_UINT)
                    code.add((byte)U.ordinal());
                else if(node.data_type == Primitives.DATA_TYPE.DT_LONG)
                    code.add((byte)L.ordinal());
                else if(node.data_type == Primitives.DATA_TYPE.DT_ULONG)
                    code.add((byte)L.ordinal());
                else
                    code.add((byte)U.ordinal());


                byte[] num;
                if(node.data_type == Primitives.DATA_TYPE.DT_FLOAT)
                    num = float2ByteArray((float)node.fvalue);
                else if(node.data_type == Primitives.DATA_TYPE.DT_DOUBLE)
                    num = double2ByteArray(node.fvalue);
                else if(node.data_type == Primitives.DATA_TYPE.DT_INT)
                    num = int2ByteArray((int)node.ivalue);
                else if(node.data_type == Primitives.DATA_TYPE.DT_UINT)
                    num = int2ByteArray((int)node.uvalue);
                else if(node.data_type == Primitives.DATA_TYPE.DT_LONG)
                    num = long2ByteArray(node.ivalue);
                else if(node.data_type == Primitives.DATA_TYPE.DT_ULONG)
                    num = long2ByteArray(node.uvalue);
                else
                    num = int2ByteArray((int)node.uvalue); // int or long? // TODO

                code.add((byte)num.length);
                for(int i=0; i<num.length; ++i)
                    code.add(num[i]);

                break;
            default:
                code.add((byte)0xff);
                break;
        }
        return code;
    }

    static ArrayList<Byte> DFS(Primitives.AST root)
    {

        // Bytecode array
        ArrayList<Byte> byteCode = new ArrayList<>();

        // Create two stacks
        Stack<Primitives.AST> s1 = new Stack<>();
        Stack<Primitives.AST> s2 = new Stack<>();

        if (root == null)
            return byteCode;

        // push root to first stack
        s1.push(root);

        // Run while first stack is not empty
        while (!s1.isEmpty())
        {
            // Pop an item from s1 and push it to s2
            Primitives.AST temp = s1.pop();
            s2.push(temp);

            // Push left and right children of
            // removed item to s1
            if (temp.left != null)
                s1.push(temp.left);
            if (temp.right != null)
                s1.push(temp.right);
        }

        // Print all elements of second stack
        while (!s2.isEmpty()) {
            Primitives.AST temp = s2.pop();
            System.out.print(temp.type + " (" + temp.svalue + ", " + temp.ivalue + ", " + temp.uvalue + ", " + temp.fvalue + ", @" + temp.data_type + ")\n");
            byteCode.addAll(compile(temp));
        }
        return byteCode;
    }

    public static void convert_function(Primitives.STATE state, Primitives.AST root) throws Exceptions.SyntaxErrorException {
        Primitives.AST ast_ptr = null;

        if (root == null)
            throw new Exceptions.SyntaxErrorException("Unable to convert NULL object.");

        ast_ptr = root;

        System.out.println("\nPARSING " + root.svalue);
        System.out.println("------------------------------------------");

        ArrayList<Byte> code = DFS(ast_ptr);
        for(Byte x : code){
            System.out.print(String.format("%02x", x));
        }
        System.out.print("\n");

    }

}
