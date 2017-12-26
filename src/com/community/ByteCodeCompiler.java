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

    public static ArrayList<Byte> DFS(Primitives.AST root) {
        ArrayList<Byte> byteCode = new ArrayList<>();
        if( root == null ) return byteCode;

        Stack<Primitives.AST> s = new Stack<Primitives.AST>();
        Primitives.AST current = root;

        s.add(root);

        while( true ) {

            if( current != null ) {
                if( current.right != null )
                    s.push( current.right );
                s.push( current );
                current = current.left;
                continue;
            }

            if( s.isEmpty( ) )
                return byteCode;
            current = s.pop( );

            if( current.right != null && ! s.isEmpty( ) && current.right == s.peek( ) ) {
                s.pop( );
                s.push( current );
                current = current.right;
            } else {
                System.out.print( current.type + " " );
                byteCode.addAll(compile(current));
                current = null;
            }
        }
    }

    public static void convert_function(Primitives.STATE state, Primitives.AST root) throws Exceptions.SyntaxErrorException {
        Primitives.AST ast_ptr = null;

        if (root == null)
            throw new Exceptions.SyntaxErrorException("Unable to convert NULL object.");

        ast_ptr = root;

        System.out.println("PARSING " + root.svalue + "\n");
        System.out.println("------------------------------------------");

        DFS(ast_ptr);

    }

}
