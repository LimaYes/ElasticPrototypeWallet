package com.community;


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
            System.out.print(temp.type + " (" + temp.svalue + ")\n");
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
