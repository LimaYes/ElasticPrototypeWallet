package nxt.computation;

import nxt.*;
import org.json.simple.JSONStreamAware;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Properties;

/******************************************************************************
 * Copyright © 2017 The XEL Core Developers.                                  *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * XEL software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

public class WorkTest extends AbstractForgingTest {

    protected static boolean isNxtInitted = false;


    @Before
    public void init() {
        if(!isNxtInitted && !Nxt.isInitialized()) {
            Properties properties = AbstractForgingTest.newTestProperties();
            properties.setProperty("nxt.disableGenerateBlocksThread", "false");
            properties.setProperty("nxt.enableFakeForging", "true");
            properties.setProperty("nxt.timeMultiplier", "1000");
            AbstractForgingTest.init(properties);
            Assert.assertTrue("nxt.fakeForgingAccount must be defined in nxt.properties", Nxt.getStringProperty("nxt.fakeForgingAccount") != null);
            isNxtInitted = true;
        }
    }

    @After
    public void destroy() {
        AbstractForgingTest.shutdown();
    }
    @Test
    public void newWorkTest() throws NxtException {
        String code = "Testing some code, which for sure will get encoded / gzipped or whatever! This is truly large yet it will become pretty pretty small on the blockchain! Test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test  !!!";

        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork((short)5,1000001,1000001,10,10, code.getBytes());
        Appendix.PrunablePlainMessage[] messages = MessageEncoder.encodeAttachment(work);
        System.out.println("[!!]\tmessage chunks length: " + messages.length);

        JSONStreamAware[] individual_txs = MessageEncoder.encodeTransactions(messages, "test", true);

        /*ByteBuffer b = ByteBuffer.allocate(1000000);
        work.putMyBytes(b);
        System.out.println("[bb]\t" + b.toString());
        System.out.println("[in]\t" + work.toString() + " ... [cut]");
        IComputationAttachment out = MessageEncoder.decodeAttachment(messages);
        System.out.println("[out]\t" + out.toString() + " ... [cut]");
        */

    }
}
