package com.community;


import java.util.ArrayList;
import java.util.Stack;

public class ByteCodeCompiler {

    public static void build_bytecode(Primitives.STATE state) throws Exceptions.SyntaxErrorException {
        for (int i = state.ast_func_idx; i < state.stack_exp.size(); i++) {
            convert_function(state, state.stack_exp.get(i));
        }
    }

    public static ArrayList<Byte> compile(Primitives.AST node){
        return new ArrayList<Byte>();
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

        DFS(ast_ptr);

    }

}
