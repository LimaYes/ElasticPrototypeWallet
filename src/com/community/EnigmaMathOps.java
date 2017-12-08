package com.community;

public class EnigmaMathOps {

    public static EnigmaProgram.StackElement add(EnigmaProgram.StackElement a, EnigmaProgram.StackElement b) {
        EnigmaProgram.StackElement result = new EnigmaProgram.StackElement();

        /*
        Hopefully, I succeed in mimicking C's implicit cast logic here ...
        This logic is needed for:
        - binary arithmetic *, /, %, +, -
        - relational operators <, >, <=, >=, ==, !=
        - binary bitwise arithmetic &, ^, |,
         */

        if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.D || b.getType() == EnigmaProgram.MEM_TARGET_STORE.D) {
            // If one type is double, make em all double
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.F || b.getType() == EnigmaProgram.MEM_TARGET_STORE.F) {
            // Otherwise, if one type is float, make em all float
        } else if (a.getType() == b.getType()) {
            // Otherwise, both types must be integer types. If they have the same signedness AND the same bitlength, the result just adopts that common type
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.UL && b.getType() == EnigmaProgram.MEM_TARGET_STORE.U) {
            // (Part 1) Otherwise, if the signedness IS SAME (IMPORTANT) but the bit lengths are different, the operand with the shorter bit length is converted to the bigger one
            // > This if branch handles the case where "a" is unsigned long, means: b is converted to unsigned long
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.U && b.getType() == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // (Part 2) This if branch handles the same case, with the little change that now "b" is unsigned long, means: a is converted to unsigned long
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.L && b.getType() == EnigmaProgram.MEM_TARGET_STORE.I) {
            // (Part 3) This if branch handles the same case, with the little change that now "a" is SIGNED long, means: a is converted to SIGNED long
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.I && b.getType() == EnigmaProgram.MEM_TARGET_STORE.L) {
            // (Part 4) This if branch handles the same case, with the little change that now "b" is SIGNED long, means: a is converted to SIGNED long
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // In this case, we are sure to have integer types with different signednesses and different bit lengths!

            // Check if there is an unsigned operand with a higher or equal rank / bit length.
            // (Part 1) In this case, assume "a" is the higher rank unsigned op, if so convert b to unsigned long
            // Note, due to the earlier checks, we can assume that b has either signed int or signed long type

        } else if (b.getType() == EnigmaProgram.MEM_TARGET_STORE.UL) {
            // (Part 2) Same as above, but assume "b" is the higher rank unsigned op, if so convert b to unsigned long
            // Note, due to the earlier checks, we can assume that a has either signed int or signed long type
        } else if (a.getType() == EnigmaProgram.MEM_TARGET_STORE.L && b.getType() == EnigmaProgram.MEM_TARGET_STORE.U) {
            // Now, Check if there is an signed operand with a higher or equal rank / bit length.
            // (Part 1) In this case, assume "a" is the higher rank SIGNED (IMPORTANT) op, and "b"'s value range entirely fits in a (only the case when it's a uint)
            // ... in this case just cast b to signed long (value won't get changed)

        } else if (b.getType() == EnigmaProgram.MEM_TARGET_STORE.L && a.getType() == EnigmaProgram.MEM_TARGET_STORE.U) {
            // (Part 2) In this case, assume "b" is the higher rank SIGNED (IMPORTANT) op, and "a"'s value range entirely fits in a (only the case when it's a uint)
            // ... in this case just cast a to signed long (value won't get changed)
        }


        return result;
    }

}
