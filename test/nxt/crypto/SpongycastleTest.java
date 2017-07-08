/*
 * Copyright © 2017 The XEL Core Developers.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */
package nxt.crypto;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.math.ec.ECConstants;
import org.spongycastle.math.ec.ECCurve;

import java.math.BigInteger;


/**
 * Created by anonymous on 26.05.17.
 */
public class SpongycastleTest {

    @Test
    public void testAvailabilityOfLibrary() {
        BigInteger n = new BigInteger("6277101735386680763835789423176059013767194773182842284081");

        ECCurve.Fp curve = new ECCurve.Fp(
                new BigInteger("6277101735386680763835789423207666416083908700390324961279"), // q
                new BigInteger("fffffffffffffffffffffffffffffffefffffffffffffffc", 16), // a
                new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16), // b
                n, ECConstants.ONE);
        System.out.println("Curve field size: " + curve.getFieldSize());
        if(curve.getFieldSize() != 192) Assert.fail("Spongycastle not working properly!");
    }
}
