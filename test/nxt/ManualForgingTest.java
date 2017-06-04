/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

package nxt;

import nxt.crypto.Crypto;
import nxt.helpers.RedeemFunctions;
import nxt.util.Time;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class ManualForgingTest extends AbstractForgingTest {

    @Test
    public void manualForgingTest() {


        Properties properties = ManualForgingTest.newTestProperties();
        properties.setProperty("nxt.enableFakeForging", "true");
        properties.setProperty("nxt.timeMultiplier", "1");
        AbstractForgingTest.init(properties);

        Nxt.getBlockchainProcessor().popOffTo(0);

        Assert.assertTrue("nxt.fakeForgingAccount must be defined in nxt.properties", Nxt.getStringProperty("nxt.fakeForgingAccount") != null);
        final byte[] testPublicKey = Crypto.getPublicKey(testForgingSecretPhrase);
        Nxt.setTime(new Time.CounterTime(Nxt.getEpochTime()));

        // We have to redeem first to be able to create blocks
        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.", RedeemFunctions.redeem(address,testForgingSecretPhrase, privkeys));

        try {
            for (int i = 0; i < 10; i++) {
                blockchainProcessor.generateBlock(testForgingSecretPhrase, Nxt.getEpochTime());
                Assert.assertArrayEquals(testPublicKey, blockchain.getLastBlock().getGeneratorPublicKey());
            }
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
        Assert.assertEquals(startHeight + 10 + 1 /* Redeem Block */, blockchain.getHeight());
        AbstractForgingTest.shutdown();
    }

}
