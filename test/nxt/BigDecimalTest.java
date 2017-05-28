package nxt;

import nxt.Constants;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by anonymous on 27.05.17.
 */
public class BigDecimalTest {

    @Test
    public void testBigDecimalFraction(){
        long alreadyClaimed=9999999L*Constants.ONE_NXT;
        BigDecimal bd = new BigDecimal(alreadyClaimed, MathContext.DECIMAL32);
        BigDecimal ad = new BigDecimal(Constants.MAX_BALANCE_NQT, MathContext.DECIMAL32);
        ad = ad.divide(bd, 0, RoundingMode.HALF_UP);

        BigInteger bal = new BigInteger(String.valueOf(alreadyClaimed));
        bal = bal.multiply(ad.toBigInteger());

        System.out.println("Factor: " + ad.toPlainString());
        System.out.println("Scaled: " + bal.toString());

        Assert.assertTrue(bal.longValue()<=Constants.MAX_BALANCE_NQT);
    }
}
