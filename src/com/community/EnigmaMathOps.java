package com.community;

public class EnigmaMathOps {

    
    public static EnigmaProgram.MEM_TARGET_STORE getCast(EnigmaProgram.MEM_TARGET_STORE a, EnigmaProgram.MEM_TARGET_STORE b){

        /*
        Hopefully, I succeed in mimicking C's implicit cast logic here ...
        This logic is needed for:
        - binary arithmetic *, /, %, +, -
        - relational operators <, >, <=, >=, ==, !=
        - binary bitwise arithmetic &, ^, |,
         */

        if (a == EnigmaProgram.MEM_TARGET_STORE.D || b == EnigmaProgram.MEM_TARGET_STORE.D) {
            // If one type is double, make em all double
            return EnigmaProgram.MEM_TARGET_STORE.D;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.F || b == EnigmaProgram.MEM_TARGET_STORE.F) {
            // Otherwise, if one type is float, make em all float
            return EnigmaProgram.MEM_TARGET_STORE.F;
        } else if (a == b) {
            // Otherwise, both types must be integer types. If they have the same signedness AND the same bitlength, the result just adopts that common type
            return a;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.UL && b == EnigmaProgram.MEM_TARGET_STORE.U) {
            // (Part 1) Otherwise, if the signedness IS SAME (IMPORTANT) but the bit lengths are different, the operand with the shorter bit length is converted to the bigger one
            // > This if branch handles the case where "a" is unsigned long, means: b is converted to unsigned long
            return EnigmaProgram.MEM_TARGET_STORE.UL;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.U && b == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // (Part 2) This if branch handles the same case, with the little change that now "b" is unsigned long, means: a is converted to unsigned long
            return EnigmaProgram.MEM_TARGET_STORE.UL;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.L && b == EnigmaProgram.MEM_TARGET_STORE.I) {
            // (Part 3) This if branch handles the same case, with the little change that now "a" is SIGNED long, means: a is converted to SIGNED long
            return EnigmaProgram.MEM_TARGET_STORE.L;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.I && b == EnigmaProgram.MEM_TARGET_STORE.L) {
            // (Part 4) This if branch handles the same case, with the little change that now "b" is SIGNED long, means: a is converted to SIGNED long
            return EnigmaProgram.MEM_TARGET_STORE.L;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // In this case, we are sure to have integer types with different signednesses and different bit lengths!

            // Check if there is an unsigned operand with a higher or equal rank / bit length.
            // (Part 1) In this case, assume "a" is the higher rank unsigned op, if so convert b to unsigned long
            // Note, due to the earlier checks, we can assume that b has either signed int or signed long type
            return EnigmaProgram.MEM_TARGET_STORE.UL;

        } else if (b == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // (Part 2) Same as above, but assume "b" is the higher rank unsigned op, if so convert b to unsigned long
            // Note, due to the earlier checks, we can assume that a has either signed int or signed long type
            return EnigmaProgram.MEM_TARGET_STORE.UL;
        } else if (a == EnigmaProgram.MEM_TARGET_STORE.L && b == EnigmaProgram.MEM_TARGET_STORE.U) {
            // Now, Check if there is an signed operand with a higher or equal rank / bit length.
            // (Part 1) In this case, assume "a" is the higher rank SIGNED (IMPORTANT) op, and "b"'s value range entirely fits in a (only the case when it's a uint)
            // ... in this case just cast b to signed long (value won't get changed)
            return EnigmaProgram.MEM_TARGET_STORE.L;

        } else if (b == EnigmaProgram.MEM_TARGET_STORE.L && a == EnigmaProgram.MEM_TARGET_STORE.U) {
            // (Part 2) In this case, assume "b" is the higher rank SIGNED (IMPORTANT) op, and "a"'s value range entirely fits in a (only the case when it's a uint)
            // ... in this case just cast a to signed long (value won't get changed)
            return EnigmaProgram.MEM_TARGET_STORE.L;
        } else{
            // What remains is U,I and I,U
            return EnigmaProgram.MEM_TARGET_STORE.U;
        }
    }


    private static EnigmaProgram.MEM_TARGET_STORE getCastAndTransformResult(EnigmaStackElement a, EnigmaStackElement b, EnigmaStackElement result) {
        EnigmaProgram.MEM_TARGET_STORE cast = getCast(a.getType(), b.getType());
        result.convertType(cast);
        return cast;
    }

    public static EnigmaStackElement add(EnigmaStackElement a, EnigmaStackElement b) {

        EnigmaStackElement result = new EnigmaStackElement(); // create new stack element
        getCastAndTransformResult(a, b, result); // cast everything
        switch(result.getType()){
            case I:
            case U:
                int x1 = a.getInt();
                int y1 = b.getInt();
                result.setInt(x1+y1);
                break;
            case L:
            case UL:
                long x2 = a.getLong();
                long y2 = b.getLong();
                result.setLong(x2+y2);
                break;
            case F:
                float x3 = a.getFloat();
                float y3 = b.getFloat();
                result.setFloat(x3+y3);
                break;
            case D:
                double x4 = a.getDouble();
                double y4 = b.getDouble();
                result.setDouble(x4+y4);
                break;
        }
        return result;
    }
}
