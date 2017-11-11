package nxt.execution;

import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class BigIntegerComparison {
    @Test
    public void testTargetBigint(){
        BigInteger f = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        int[] ints = Convert.bigintToInts(f);
        System.out.println("ByteArrayInteger: " + Convert.toHexString(f.toByteArray()));
        System.out.println("Integers in HEX format:");
        for(int i=0;i<ints.length;++i){
            System.out.println(Convert.toHexString(Convert.int2byte(new int[]{ints[i]})));
        }
        Assert.assertTrue(ints[0]==0x00000fff);
        Assert.assertTrue(ints.length==4);

        // With minlength
        ints = Convert.bigintToInts(f,5);
        System.out.println("ByteArrayInteger: " + Convert.toHexString(f.toByteArray()));
        System.out.println("Integers in HEX format:");
        for(int i=0;i<ints.length;++i){
            System.out.println(Convert.toHexString(Convert.int2byte(new int[]{ints[i]})));
        }
        Assert.assertTrue(ints[0]==0x00);
        Assert.assertTrue(ints[1]==0x00000fff);
        Assert.assertTrue(ints.length==5);
    }
}

