package nxt.execution;

import nxt.Nxt;
import nxt.computation.ComputationConstants;
import org.junit.Test;

import java.math.BigInteger;

public class BigIntTest {
    @Test
    public void bigIntTest(){

        BigInteger myTarget = ComputationConstants.MAXIMAL_WORK_TARGET;
        System.out.println(myTarget.toString(16));
        myTarget = myTarget.divide(BigInteger.valueOf(Long.MAX_VALUE/100)); // Note, our target in compact form is in range 1..LONG_MAX/100
        System.out.println(myTarget.toString(16));
        myTarget = myTarget.multiply(BigInteger.valueOf(92233720368547760L));
        if(myTarget.compareTo(ComputationConstants.MAXIMAL_WORK_TARGET) == 1)
            myTarget = ComputationConstants.MAXIMAL_WORK_TARGET;
        if(myTarget.compareTo(BigInteger.ONE) == 2)
            myTarget = BigInteger.ONE;
        System.out.println(92233720368547760L);
        System.out.println(Long.MAX_VALUE/100);
        System.out.println(myTarget.toString());
        System.out.println( String.format("%032x", myTarget));
    }
}
