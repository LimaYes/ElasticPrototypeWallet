package nxt.computation;

import nxt.*;
import nxt.db.DbIterator;
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

    public void push(IComputationAttachment work) throws NxtException, IOException{
        Appendix.PrunablePlainMessage[] messages = MessageEncoder.encodeAttachment(work);
        System.out.println("[!!]\tmessage chunks length: " + messages.length);

        JSONStreamAware[] individual_txs = MessageEncoder.encodeTransactions(messages, AbstractForgingTest.testForgingSecretPhrase);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        for(int i=0;i<individual_txs.length;++i){
            individual_txs[i].writeJSONString(pw);
        }

        StringBuffer sb = sw.getBuffer();
        System.out.println("TX:\n" + sb.toString());

        MessageEncoder.pushThemAll(individual_txs);
    }
    @Test
    public void newWorkTest() throws NxtException, IOException {

        redeemPubkeyhash();

        String code = "Testing some code, which for sure will get encoded / gzipped or whatever! This is truly large yet it will become pretty pretty small on the blockchain! Test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test  !!!";

        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(100, (short)15,1000001,1000001,10,10, code.getBytes());
        push(work);

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
        push(cancel);

        // Mine a bit so the work gets confirmed
        AbstractBlockchainTest.forgeNumberOfBlocks(5, AbstractForgingTest.testForgingSecretPhrase);

        System.out.println("LAST BLOCK:");
        System.out.println(Nxt.getBlockchain().getLastBlock().getJSONObject().toJSONString());

        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(0, Work.getActiveCount());

    }

    @Test
    public void newWorkTestWithNaturalTimeout() throws NxtException, IOException {

        redeemPubkeyhash();
        String code = "Testing some code, which for sure will get encoded / gzipped or whatever! This is truly large yet it will become pretty pretty small on the blockchain! Test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test  !!!";
        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(100, (short)15,1000001,1000001,10,10, code.getBytes());
        push(work);

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

    }

    @Test
    public void newWorkTestWithEnoughBounties() throws NxtException, IOException {

        redeemPubkeyhash();
        String code = "Testing some code, which for sure will get encoded / gzipped or whatever! This is truly large yet it will become pretty pretty small on the blockchain! Test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test  !!!";
        System.out.println("[!!]\tcode length: " + code.length());
        CommandNewWork work = new CommandNewWork(10, (short)100,1000001,1000001,10,10, code.getBytes());
        push(work);

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
            push(pow);
            // Mine a bit so the work times out
            AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);
        }

        // After getting enough Pow work must be closed
        // Test work db table
        Assert.assertEquals(1, Work.getCount());
        Assert.assertEquals(0, Work.getActiveCount());

    }
}
