package nxt.computation;

import nxt.*;
import nxt.db.DbIterator;
import nxt.execution.ExecutionEngineTests;
import nxt.helpers.RedeemFunctions;
import nxt.http.JSONData;
import org.json.simple.JSONStreamAware;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
            AbstractForgingTest.init(properties);
            Assert.assertTrue("nxt.fakeForgingAccount must be defined in nxt.properties", Nxt.getStringProperty("nxt.fakeForgingAccount") != null);
            isNxtInitted = true;
        }
    }

    @After
    public void destroy() {
        AbstractForgingTest.shutdown();
    }

    public void redeemPubkeyhash(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.", RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
    }

    @Test
    public void newWorkTest() throws NxtException, IOException {

        redeemPubkeyhash();

        String code = ExecutionEngineTests.readFile("test/testfiles/test2.epl", Charset.forName("UTF-8"));
        String doublecheckcode = new String(code.getBytes());
        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(100, (short)15,1000001,1000001,10,10, code.getBytes());
        MessageEncoder.push(work, AbstractForgingTest.testForgingSecretPhrase);

        // Mine a bit so the work gets confirmed
        AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(1, Work.getActiveCount());
        long id = 0;
        try(DbIterator<Work> wxx = Work.getActiveWork()){
            Work w = wxx.next();
            id = w.getId();
            System.out.println("Found work in DB with id = " + Long.toUnsignedString(w.getId()));
        }

        CommandCancelWork cancel = new CommandCancelWork(id);
        MessageEncoder.push(cancel, AbstractForgingTest.testForgingSecretPhrase);

        // Mine a bit so the work gets confirmed
        AbstractBlockchainTest.forgeNumberOfBlocks(5, AbstractForgingTest.testForgingSecretPhrase);

        System.out.println("LAST BLOCK:");
        System.out.println(Nxt.getBlockchain().getLastBlock().getJSONObject().toJSONString());

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(0, Work.getActiveCount());

        Assert.assertEquals(7,Nxt.getBlockchain().getLastLocallyProcessedHeight());

    }

    @Test
    public void newWorkTestWithNaturalTimeout() throws NxtException, IOException {

        redeemPubkeyhash();
        String code = ExecutionEngineTests.readFile("test/testfiles/test2.epl", Charset.forName("UTF-8"));
        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(100, (short)15,1000001,1000001,10,10, code.getBytes());
        MessageEncoder.push(work, AbstractForgingTest.testForgingSecretPhrase);

        // Mine a bit so the work gets confirmed
        AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(1, Work.getActiveCount());

        // Mine a bit so the work times out
        AbstractBlockchainTest.forgeNumberOfBlocks(20, AbstractForgingTest.testForgingSecretPhrase);

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(0, Work.getActiveCount());

        Assert.assertEquals(22,Nxt.getBlockchain().getLastLocallyProcessedHeight());

    }

    @Test
    public void newWorkTestWithEnoughBounties() throws NxtException, IOException {

        redeemPubkeyhash();
        String code = ExecutionEngineTests.readFile("test/testfiles/test2.epl", Charset.forName("UTF-8"));
        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(10, (short)100,1000001,1000001,10,10, code.getBytes());
        MessageEncoder.push(work, AbstractForgingTest.testForgingSecretPhrase);

        // Mine a bit so the work gets confirmed
        AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);

        long id = 0;
        try(DbIterator<Work> wxx = Work.getActiveWork()){
            Work w = wxx.next();
            id = w.getId();
            System.out.println("Found work in DB with id = " + Long.toUnsignedString(w.getId()));
        }

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(1, Work.getActiveCount());

        byte[] testarray = new byte[32];
        for(int i=0;i<25; ++i) {
            testarray[0]=(byte)(testarray[0]+1);
            CommandPowBty pow = new CommandPowBty(id, true, testarray);
            MessageEncoder.push(pow, AbstractForgingTest.testForgingSecretPhrase);
            // Mine a bit so the work times out
            AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);
        }

        // After getting enough Pow work must be closed
        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(0, Work.getActiveCount());

    }
}
