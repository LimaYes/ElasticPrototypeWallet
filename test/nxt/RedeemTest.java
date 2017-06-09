package nxt;

import nxt.crypto.Crypto;
import nxt.helpers.RedeemFunctions;
import nxt.util.Logger;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.h2.schema.Constant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static nxt.helpers.TransactionBuilder.make;

/*

=================================================================

Important notices about this test suite:

Donation from Pubkeyhash Address:

Address: 1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h
Privkey: 5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp

=================================================================

Donation from 2-of-3 Multisig Address:

Address 1: 1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG
Privkey 1: 5HxrnFauyY35upz5KwLu9eu5xmdksyqer4RRUCUL6fihWP3VUz8
Pubkey  1: 04c7f2197a8a2bff393d774b7cb6c1402ec00264a9e23974166ebbfa6ac8c9432961a99c863a5229cc05a805f184a8710420db7949d62c941b577df710aea5d8a2

Address 2: 1XELoBbvh23fN1PamH6a5YouSB8szN6jU
Privkey 2: 5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS
Pubkey  2: 046df638b38bbd3d7d49ca2c3fec80ac33eb16eb36e18eb3f511090182043014ad9a8190c6bb0f28865f5f24dc996f2db3ad23e64f070c75dd214507c7d65eb26e

Address 3: 1XELAgw2poSW9ADAhs7eUg7jXePpoye9i
Privkey 3: 5JB8vpKcezmnsyBopsFLt3u7ojuR6nuuQx9yztPpkBWgXscM1Mt
Pubkey  3: 043f3ee66dc3d4ccfa3a538e151985350a56e884c3d9b144767042490da4f517adacac28e891de29447157947519f75e67c358c9bba9f880d4a0506f981ecd49c3

Here is the pay 2-of-3 address: 38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC

=================================================================

*/
public class RedeemTest extends AbstractForgingTest {

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
    public void redeemPubkeyhash(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));

        // Forge 10 blocks
        AbstractBlockchainTest.forgeNumberOfBlocks(10, AbstractForgingTest.testForgingSecretPhrase);

        // Verify Balance, Guaranteed must be 0, Balance must be the redeemed
        Assert.assertEquals((long)AbstractBlockchainTest.getGuaranteedBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), 0);
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), (long)Redeem.getClaimableAmount(address));

        // Forge 20 blocks
        AbstractBlockchainTest.forgeNumberOfBlocks(20, AbstractForgingTest.testForgingSecretPhrase);

        // Verify Balance, Guaranteed must be 0, Balance must be the redeemed
        Assert.assertEquals((long)AbstractBlockchainTest.getGuaranteedBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), (long)Redeem.getClaimableAmount(address));
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), (long)Redeem.getClaimableAmount(address));
    }

    private static boolean redeem_different_sig_and_receipient(String address_entry, String secretPhrase, long accountId, String[] bitcoin_privkeys, boolean graceblock)
    {
        String secp_signatures = "";
        NetworkParameters params = MainNetParams.get();
        long target_id = Account.getId(Crypto.getPublicKey(secretPhrase));
        String message = Redeem.getSignMessage(Redeem.getClaimableAmount(address_entry), address_entry, target_id);

        // Sign inputs
        for(int i=0; i<bitcoin_privkeys.length; ++i){
            ECKey key;
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, bitcoin_privkeys[i]);
            key = dumpedPrivateKey.getKey();
            String signed = key.signMessage(message);
            bitcoin_privkeys[i] = signed;
        }

        secp_signatures = String.join("-", bitcoin_privkeys);

        final Attachment attachment = new Attachment.RedeemAttachment(address_entry, secp_signatures);
        boolean success = false;
        try {
            make(attachment, secretPhrase, accountId, Redeem.getClaimableAmount(address_entry), true, graceblock);
            success = true;
        } catch (Exception e) {
            Logger.logErrorMessage(e.getMessage());
        }
        return success;
    }
    @Test
    public void redeemPubkeyhashWithWrongSignature(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertFalse("Should fail to create redeem transaction with wrong key.",redeem_different_sig_and_receipient(address, AbstractForgingTest.testForgingSecretPhrase, 1234567890L,  privkeys, true));

    }

    @Test
    public void redeemPubkeyhashDisallowTwice(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        Assert.assertEquals(blockchain.getHeight(), 1);

        // Redeeming same entry again should fail
        String evilSecretPhrase = "EvilDude";
        privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertFalse("Duplicate redeem transaction should fail hard.",RedeemFunctions.redeem(address, evilSecretPhrase, privkeys));
        //Assert.assertEquals(blockchain.getHeight(), 1);

        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), (long)Redeem.getClaimableAmount(address));
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(evilSecretPhrase), 0);
    }

    @Test
    public void redeemPubkeyhashAllowTwiceAfterPopoff(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        Assert.assertEquals(blockchain.getHeight(), 1);

        // Revert
        Nxt.getBlockchainProcessor().popOffTo(0);

        // Redeeming same entry again should be fine after rollback
        String evilSecretPhrase = "EvilDude";
        privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Duplicate redeem transaction should be okay after rollback hard.",RedeemFunctions.redeem(address, evilSecretPhrase, privkeys));
        Assert.assertEquals(blockchain.getHeight(), 1);

        // But amounts must be fine here
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), 0);
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(evilSecretPhrase),  (long)Redeem.getClaimableAmount(address));
    }

    @Test
    public void redeemPubkeyhashDisallowTwiceInSameBlock(){
        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "1XELjH6JgPS48ZL7ew1Zz2xxczyzqit3h";
        String[] privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertTrue("Failed to create redeem transaction.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys, false));
        Assert.assertEquals(blockchain.getHeight(), 0);

        // Redeeming same entry again should fail
        String evilSecretPhrase = "EvilDude";
        privkeys = new String[]{"5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertFalse("Duplicate redeem transaction should fail hard.",RedeemFunctions.redeem(address, evilSecretPhrase, privkeys));
    }


    @Test
    public void redeemMultisig(){

        Nxt.getBlockchainProcessor().popOffTo(0);

        String address = "2-1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG-1XELoBbvh23fN1PamH6a5YouSB8szN6jU-1XELAgw2poSW9ADAhs7eUg7jXePpoye9i;38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC";
        String[] privkeys = new String[]{"5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS","5JB8vpKcezmnsyBopsFLt3u7ojuR6nuuQx9yztPpkBWgXscM1Mt"};
        Assert.assertTrue("Failed to create redeem transaction.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));

        // Forge 10 blocks
        AbstractBlockchainTest.forgeNumberOfBlocks(1, AbstractForgingTest.testForgingSecretPhrase);

        // Verify Balance,Balance must be the redeemed
        Assert.assertEquals((long)AbstractBlockchainTest.getBalanceBySecretPhrase(AbstractForgingTest.testForgingSecretPhrase), (long)Redeem.getClaimableAmount(address));
    }

    @Test
    public void redeemMultisigWithTooManySignatures(){
        Nxt.getBlockchainProcessor().popOffTo(0);
        String address = "2-1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG-1XELoBbvh23fN1PamH6a5YouSB8szN6jU-1XELAgw2poSW9ADAhs7eUg7jXePpoye9i;38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC";
        String[] privkeys = new String[]{"5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS","5JB8vpKcezmnsyBopsFLt3u7ojuR6nuuQx9yztPpkBWgXscM1Mt","5HxrnFauyY35upz5KwLu9eu5xmdksyqer4RRUCUL6fihWP3VUz8"};
        Assert.assertFalse("Transaction with more or less signatures than required should fail.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        // Make sure no "grace block" was created
        Assert.assertEquals(blockchain.getHeight(), 0);
    }

    @Test
    public void redeemMultisigWithTooFewSignatures(){
        Nxt.getBlockchainProcessor().popOffTo(0);
        String address = "2-1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG-1XELoBbvh23fN1PamH6a5YouSB8szN6jU-1XELAgw2poSW9ADAhs7eUg7jXePpoye9i;38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC";
        String[] privkeys = new String[]{"5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS"};
        Assert.assertFalse("Transaction with more or less signatures than required should fail.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        // Make sure no "grace block" was created
        Assert.assertEquals(blockchain.getHeight(), 0);
    }

    @Test
    public void redeemMultisigWithWrongSignatures(){
        Nxt.getBlockchainProcessor().popOffTo(0);
        String address = "2-1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG-1XELoBbvh23fN1PamH6a5YouSB8szN6jU-1XELAgw2poSW9ADAhs7eUg7jXePpoye9i;38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC";
        String[] privkeys = new String[]{"5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS","5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertFalse("Transaction with wrong signatures should fail.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        // Make sure no "grace block" was created
        Assert.assertEquals(blockchain.getHeight(), 0);
    }

    @Test
    public void redeemMultisigWithCorrectAndIncorrectSignatures(){
        Nxt.getBlockchainProcessor().popOffTo(0);
        String address = "2-1XEL4ZDnnGjePheYMyFX8tM9LWHjEAFjG-1XELoBbvh23fN1PamH6a5YouSB8szN6jU-1XELAgw2poSW9ADAhs7eUg7jXePpoye9i;38pNL6YDpLmTHJ7A4WVLpMoLRQaTE9t3oC";
        String[] privkeys = new String[]{"5HypJKY53t7dahoYa4DyvNiVL3CNx4wdv8QkhkRqTDxXkeaUWHS","5JB8vpKcezmnsyBopsFLt3u7ojuR6nuuQx9yztPpkBWgXscM1Mt","5JDSuYmvAAF85XFQxPTkHGFrNfAk3mhtZKmXvsLJiFZ7tDrSBmp"};
        Assert.assertFalse("Transaction should not be allowed to contain wrong signatures even if correct ones are there.",RedeemFunctions.redeem(address, AbstractForgingTest.testForgingSecretPhrase, privkeys));
        // Make sure no "grace block" was created
        Assert.assertEquals(blockchain.getHeight(), 0);
    }


    public static Map<String, Double> amounts = new HashMap<>();

    public static void addUnvoidsCheck(String addr, double val){
        amounts.put(addr, val);
    }

    public static void addUnvoidsCheckExceptionWithMessage(String addr_old, String addr_new, String reason){
        Assert.assertTrue(amounts.containsKey(addr_old));
        Assert.assertFalse(amounts.containsKey(addr_new));
        double amount = amounts.get(addr_old);
        amounts.remove(addr_old);
        if(addr_new.length()!=0) {
            amounts.put(addr_new, amount);
            Assert.assertTrue(amounts.containsKey(addr_new));
        }
    }

    @Test
    public void checkRedeemAmountsUnvoidStyle(){
        addUnvoidsCheck("1GYvGYrUqLGzQoErLhsegPw3nygpNFwZR9",6004095.8871);
        addUnvoidsCheck("36mdzMuHhKsz4qxByXt3v4X3QMS9wRcGEH",5494938.2018);
        addUnvoidsCheck("1Jgdk1NcMVck9nXPjJSE4LxBtG9SsEEWRy",2655939.4806);
        addUnvoidsCheck("1LovVqqwnLYzVUjFsycPDtx334BmCtcha7",2585185.8458);
        addUnvoidsCheck("3ArMzrktxBNg2rBe4iVWtwH38sJt5NrCzb",2449378.5558);
        addUnvoidsCheck("1KDm91GSEjob7PyVF5GUPmSiMg4KMSAXsY",2267532.6097);
        addUnvoidsCheck("1K5pv63rag715mN2REYcfMbQs7RyukPuSK",2200220.0601);
        addUnvoidsCheck("16XNyCf9rEnn7qxPeaQPDXz4JY1FhzDbDA",1993710.3658);
        addUnvoidsCheck("14MKronX9pQJui5vVENveDqwKFE6bxWVk4",1546077.3518);
        addUnvoidsCheck("1Ahauxg3uxhcnxnSdmqKVhomz3MF61ynxA",1487727.1022);
        addUnvoidsCheck("1NUtkYVqjF6rmUkSrZEdmmRjcq3fgWAag3",1461472.7726);

        addUnvoidsCheck("15Z5BggJhi1cgv4CV9TFGZ5WNbH7aF7hGf",1423054.1101);


        addUnvoidsCheck("1Kv6jbP9JWHagfrQjjfVzMcnKEGynZ7pf3",1329550.453);
        addUnvoidsCheck("1B7jF7NDfFSdToSiPvYWnHAxEUmNaMfYe",1312926.4877);
        addUnvoidsCheck("1EranfPkcFFAwbXDjzTix1m3AxkXjr7ZDD",1292602.4006);
        addUnvoidsCheck("16Ztzi7YofzJx1Wm6xreM1VsrQybcsWem5",1240974.0098);
        addUnvoidsCheck("1888888UhkzqRGNWBVcmkXcFXeca676BuY",1197606.6902);
        addUnvoidsCheck("1AZMaW5vswDpboeGfNVA1ueoNab1nfHfCe",1187799.8965);
        addUnvoidsCheck("1EAhDVNZK4kPAoBswQeDWeHkV4taVkqfsb",1152193.6365);
        addUnvoidsCheck("17GiWvV6Sb4damGNKUFzNdesBxYXG7cknm",1113593.2615);
        addUnvoidsCheck("13iCYHRpivtTNhYKc7sgC2AxzS3SzNTuVD",1064764.6874);
        addUnvoidsCheck("1FecDmcz9uLbDFrFQz7jbxCi9mFNHnTisW",1023812.1994);
        addUnvoidsCheck("12GTLir6rEfwJpShcSqYxybjoZYVhF9t8d",949881.052);
        addUnvoidsCheck("13jRQ6xKNiRq7uvMFVmV9kQnPVbxRe385W",949257.9738);
        addUnvoidsCheck("1BrKwsVmHvHxnbgGnnQ4U2KC4rV1a2nUS3",925463.1169);
        addUnvoidsCheck("1GVDe4y5VwZ8jHNuF8HxXuKzKSXPfjF223",901476.1213);
        addUnvoidsCheck("14UwzH27d7YGKvkzQLbp2EbnAQErkSHTKB",893413.6397);
        addUnvoidsCheck("13L6Qb9VJXS4PvGPbtWwyz8roHwC7DEHZX",886079.4394);
        addUnvoidsCheck("12oipr5BLUhanfsPy9xG5X4Foai4Gh9FNv",885658.2031);
        addUnvoidsCheck("1J75pvyVUyRt7JpW6RQHzYi99QzSwkfjAS",875505.9316);
        addUnvoidsCheck("1PeCuCMcHcvBHzQTKr1egZRjZJqQ1UnLb1",871300.5578);
        addUnvoidsCheck("141Np5ZGtNhbms7ArCrSmALN8q7vVUXDTw",853452.7723);
        addUnvoidsCheck("1B8vxvHfc9whEkhKyUoE9fMVNUbAaKehfU",822098.8749);
        addUnvoidsCheck("16ayhut16RXFeKYTnWaBhCj7DNocRL3u8Q",753464.5522);
        addUnvoidsCheck("1J4Z4VQtrnFw3MBPtgeXyAgqf1jedNhQzS",747815.4188);
        addUnvoidsCheck("1KKVprQg2zYLceAYbx8kJxbzGFUn6bUX1C",725713.794);
        addUnvoidsCheck("1STmcsewju8ykyuUGkmjVEuPiVyjXc9Pj",710004.2805);
        addUnvoidsCheck("1C1ZXeQUJQGi8GL7iyjoi6zfGd4EC25981",700984.3908);
        addUnvoidsCheck("1HqxgWn4yc76auxd5GTAfP1ShzHmYHg6RA",697061.3842);
        addUnvoidsCheck("1BeyK8UPBAuvKahaeLbdDBwuqu1WJRdnca",691174.7982);
        addUnvoidsCheck("1FtdAZvrTYNNkgUR6WHmM8q9r6mt2g7tT3",673696.2838);
        addUnvoidsCheck("19MxTmoksJEj9CgzifZKcy145Q6sDpMo7t",624122.5526);
        addUnvoidsCheck("1PXbfNGNGB65kTTd7bxeD7RBkPiL1AJDiR",618938.9278);
        addUnvoidsCheck("1F1NVgJe6KTNRucyZTZvWoEqBUFnNWRSMY",614287.3196);
        addUnvoidsCheck("1PACs4zsCX3CLeybt1ijj74Jj4q71wrBeL",613765.9338);
        addUnvoidsCheck("17y4rTjnqLe6AST5Ygv4DixagaRj4o75Wf",597792.5675);
        addUnvoidsCheck("1JoGVcZdSXHKKH2W5YjxwqectqsTT2QAj5",558791.3266);
        addUnvoidsCheck("1Pw6qbzQivyb7kBpaBrQedcXJt7d4uQu9G",554385.2212);
        addUnvoidsCheck("12B571aTWKsrq6fohnQXjT1nhHcC2617rx",542904.8579);
        addUnvoidsCheck("1M6pdtoTPe36pXtChNZvH4c8mSB6vzSJHJ",535960.9465);
        addUnvoidsCheck("1EPwygdMJXY7wAQUMhQuBZGkXA5h47R3dn",513109.5135);
        addUnvoidsCheck("13ckSm9CJL2SPqiQVrDC2C1UxLBUV2Y1Xe",499416.5388);
        addUnvoidsCheck("1NArvaEWFrt4R6G6Uxar6XNM4s3h4sBqkw",499375.2715);
        addUnvoidsCheck("1ArturoQZAbmeYtZgJFyRnK5GsRLitcBfH",472056.4218);
        addUnvoidsCheck("15QmJV44C8Zs2AqATqBpvgp8poyCcABY53",455229.0887);
        addUnvoidsCheck("16K2qfVXgwzAJkXun933zPp1CxpqjeGLbR",430226.6626);
        addUnvoidsCheck("1BP1Ai1CurMqtpvLx7jU1NB38oe2Vrm1b6",424199.6753);
        addUnvoidsCheck("13UtpxQJuoTigEeeuq4HrkUvp1uyPxYXEu",420482.5502);
        addUnvoidsCheck("1Eq8bqMRBxADqFVW1YUP1zYyufYBp4kfs1",420045.0138);
        addUnvoidsCheck("1KDhNhA3m1mg1CoB6CT4tgzjAKBLqUrLmG",415370.721);
        addUnvoidsCheck("12knCup7U5V8oQ3FWPD6Be9L4HfFYD4MDZ",414310.1748);
        addUnvoidsCheck("1GxENbATk1HfUx2ijYaSvRDENXBymF91mw",397311.8114);
        addUnvoidsCheck("19oo6gsF6qf6C4CX1yFR7fv5wHPLm78Hjo",396270.6197);
        addUnvoidsCheck("1zuDxdhaLrEyzmSbKrBRnJputgccSY5wD",385838.4687);
        addUnvoidsCheck("1LJudB5t6sq8NTsDj2Cg7SvUq23z6Q1qju",382470.8004);
        addUnvoidsCheck("1HVPu1KhFCrjvzZLHvmU1zvmmCx35rCFbk",378115.3326);
        addUnvoidsCheck("1KyDFiDR21fo9XL4Dp76UJV8ThfdzYuqFf",371623.3521);
        addUnvoidsCheck("17cHZHiKHpUk8jmSYCKqpzT3YcaPom16Ev",366994.4096);
        addUnvoidsCheck("1FuMDoQh7fj7NTm1p6zqhgfpTXBRVsXm54",353593.0097);
        addUnvoidsCheck("1NTQfZFBN61ke2Dxb7dQ4d4oNEMsf5fq1u",351743.6841);
        addUnvoidsCheck("14erK2S3EDZ1YnKnkRJqiTopfkvvCQRXPD",350204.6005);
        addUnvoidsCheck("1LhuczTy4Ep4VVXQHraL2jAN2AZS95z1nE",347010.9069);
        addUnvoidsCheck("1AA6KycbJ32bTphqFKZTkSCYhaeh348jkE",339430.0831);
        addUnvoidsCheck("1Le5NkD6aYnzZPKCiadVAPzcbWsH6CjbYJ",328117.5903);
        addUnvoidsCheck("1Gx4nmEt8VyNeSjn675o3bHZ7VSaZjS5yk",326103.145);
        addUnvoidsCheck("1NJJs32wnaA6NAjhTGPg1KyKv5R3PrEw1Z",320757.9503);
        addUnvoidsCheck("1BgLudnKgbYGuJkvyDLzS3qgNaeJ51ngoQ",306121.1916);
        addUnvoidsCheck("1PBpVC2P5YrMyabyFD4QiS5jvhGCxCKANa",304963.319);
        addUnvoidsCheck("17hz5ebzoig5jqvNbQbLeQ5y23TNW5i8sn",297111.1297);
        addUnvoidsCheck("16y4wASazPdKEGDcoxDAL3CWrfBse3g5B6",291920.7732);
        addUnvoidsCheck("14f4hDX2z9cwkpZuSZqiK2uj3aakNFnoHx",290206.5197);
        addUnvoidsCheck("1Kk1uqZqZiWWGFzZ8onYGWWG3mRVR6P94H",289946.2744);
        addUnvoidsCheck("1ACajk8H6fknesYhJVSVw62faDQsUfxzSC",286019.9518);
        addUnvoidsCheck("14Tc6PU1kzLnmooPHXdsy4ePtPxMgoXEYj",281793.5484);
        addUnvoidsCheck("1Fwpiz9CpFJvtca1aaJP7oQKq51FSF4R1X",280924.2718);
        addUnvoidsCheck("1KeBxECUCqQ53MoJSbYvuKCe8idDGUHoxR",280736.4346);
        addUnvoidsCheck("17aVroAkjg5dS7h3fdfKav72kDgivavc4d",278295.6182);
        addUnvoidsCheck("1D8AJUBvkFLiLi7qqzLNe1SnD7sA3duVNP",257304.4819);
        addUnvoidsCheck("1LE1hYNegqX2CQaPsuw3ovQrbMKuAacbFd",255366.0958);
        addUnvoidsCheck("1MVqoPXUTJ2BgFetBFojvtauotz7TGvnka",248751.4474);
        addUnvoidsCheck("1MqE1mkEX5Jj1VBsHuEkGDiaFFpHA1E5LF",244593.1583);
        addUnvoidsCheck("1K4iZ9FFDx5CyAWKgfTH3hSCnVRtrmm1eS",241644.5634);
        addUnvoidsCheck("155VUG4hd9pGYsQeuyMvrbGgzpP7nSzJSn",229380.1464);
        addUnvoidsCheck("1friNTBnsATv3Voyujd5Fn2eDAYXcNE9S",227614.5443);
        addUnvoidsCheck("1M5xPWSfQw4qAhSibZuaSWrtukso1eMmBq",225238.6839);
        addUnvoidsCheck("19Ua5ieT7V4opixMv6aSc2pCK3Rp4sJz9T",222995.1448);
        addUnvoidsCheck("1P21GkkUFt8ME2U65SbGwutP9EZcuQoFs",215640.4445);
        addUnvoidsCheck("1nMrSrMAiEDWopZKPUFQC6fBx5EGAsEdH",208914.4855);
        addUnvoidsCheck("1BVap5sg6gTNg9VkU5eu5t4JNjbLAxEpRM",208857.6887);
        addUnvoidsCheck("17Awa5AbqkFiUmW9PxdBhYFGs9n4BWBTgf",208193.3861);
        addUnvoidsCheck("1CDz6WgBbNNXBhbtsRCLBXHvnzZdF4YMeJ",208120.2251);
        addUnvoidsCheck("3Gg6jdT85BeWTmy2nJ7J7cKBHohx96fYxS",204762.4399);
        addUnvoidsCheck("3EkQEPPyVL5FnTv3zheJeYzWWLqE4mEjXV",204762.4399);
        addUnvoidsCheck("18VD8S7Tc9u9B87u5DZAbbshzxLhmqtE9R",204762.4399);
        addUnvoidsCheck("16HLVBKgSmTWq7igbtVmxKU2YujuckpEnA",204762.4399);
        addUnvoidsCheck("1MNStNrkraqYaawKSNaZ5d6GqzJm4EGrJY",204762.4399);
        addUnvoidsCheck("13NeNTpwFW2EMeh8B6q75t6Vxj4pGBMAKW",204762.4399);
        addUnvoidsCheck("1ByBfJpqvZs6QGutzh134PEqYMpsM8Xwkq",204762.4399);
        addUnvoidsCheck("1NXscUA1e7EDDaPEDwNgqP53BF3a3yMQEh",204756.5407);
        addUnvoidsCheck("1DyHXmfFqfyX2HHt6RhqXdePDwGELwUbJx",203091.493);
        addUnvoidsCheck("1BemsM9i3gjshwmJ2txfjXXnFHfqWs8Huq",200186.6037);
        addUnvoidsCheck("19tCadLqSh3hD7v9W2AomqKj7WpoQFjJYr",198895.8774);
        addUnvoidsCheck("18brPBR9c2eNuHMtzEhr9ayTxCBmipzrk7",194891.6579);
        addUnvoidsCheck("1614UqptoSZd5SznRsvoK8tzYZuR7TUfpE",193370.9492);
        addUnvoidsCheck("1NWg1Mga4n5CWLwQPrhkQdLJ9fJdJy8zbV",192584.9205);
        addUnvoidsCheck("1B7R2yexLNxESAhTS6K8fY9epLXtzP2QN",191858.1403);
        addUnvoidsCheck("1154rigDAHKtLQKRZgS3cz9PUxouck1c9k",190882.5168);
        addUnvoidsCheck("1Ptc1krYh1NAidPiqovQRjy8YtpcYH4kdL",189313.5404);
        addUnvoidsCheck("15sqodDr9GR1JsuKa3kTU1a75w52Bw44wG",188966.6948);
        addUnvoidsCheck("19n1xVC4JWsx8GhtfoxdDfoN4Ensz3mg1N",187448.0029);
        addUnvoidsCheck("14LEPr49hNgfM9EaDU67UFTwWey8rZc3wU",182678.5895);
        addUnvoidsCheck("1CiYTFjgVWJG3GFAXehmyp38Z96uHPTqut",181410.674);
        addUnvoidsCheck("18FCNW11Bv5emmMRj7P1pek9hxXXCZqda4",180752.8592);
        addUnvoidsCheck("1DnXbLXgAoVxwKVmXtETaEPcDLxhb8HHox",179811.2834);
        addUnvoidsCheck("1PCNXireXa6mZGub7vJ8jRCMnKj5D3sVtt",179186.6);
        addUnvoidsCheck("1DEDnBdU8FCjkacRf6Ttc2CQAS7FNFrfwS",177717.5243);
        addUnvoidsCheck("13sUuLiyLU2kdxRpfsNUJXgLR1xiNdadBG",177645.0165);
        addUnvoidsCheck("133b63y8JB8zFCVnE6YhP442nnSPKj4YfF",177617.671);
        addUnvoidsCheck("15MvcgPMpWLZuRtTFHhE8LBTGP45CE7Cos",176579.9551);
        addUnvoidsCheck("1HgVAQfFqSycVMxRUtmWUc772RS1BQU2XF",175974.642);
        addUnvoidsCheck("14W27o3HBYxVRkeJj36Qj6RHHDj8ga4fPY",175777.4955);
        addUnvoidsCheck("19NL1hXyD6omKMMbomui9z1o9mgYENXKva",174170.5206);
        addUnvoidsCheck("1NkATnRy31UTDpkuzEru2o3nGzE5JYm3p8",173929.5771);
        addUnvoidsCheck("12ucKWqPxXBxctauuPcJY154rTm1bnPNDM",173641.2349);
        addUnvoidsCheck("1B3zoYVVRNXkv7TegHQ2xrGbrq4pdZyG2m",166116.7619);
        addUnvoidsCheck("1JRuH2BFgtFgiMaVtCPzvnTY1PYAmEJEG",164829.6957);
        addUnvoidsCheck("18u8n8GjQJavb6Cs9WywR2ZhfJxUKP3YJU",164238.0431);
        addUnvoidsCheck("1Du2DiSoiZKEK22jPHWvRkyAeJH7KWFckQ",160711.261);
        addUnvoidsCheck("144EvecNyPVS3wS4uuVgcQrnK6UreKFk4v",156309.5104);
        addUnvoidsCheck("1JHyxrRhvhpKmDvkcVL6m6v4pZs7iE2N87",154584.0162);
        addUnvoidsCheck("13ujSkcNMmzmNHfqr7p8ddnH9KHZc5ngx7",153398.0346);
        addUnvoidsCheck("1NMTFJ278TJHFoFPZwxV9ZyseD6inn2Zj9",152662.5065);
        addUnvoidsCheck("18ZjiPMDuMdMqvknMdhbpDKBRbBXywCNKC",152156.1883);
        addUnvoidsCheck("18eiw3eMbW7iLDvwrpyJoVRA9drL2DQeHa",149550.8786);
        addUnvoidsCheck("1Annm6pLezYsCDR8q32zFGoWqWkERDwaGu",148558.3014);
        addUnvoidsCheck("12NZvfXZ7VpuxCfWJ1GJ3A927An1vW1Uh2",148441.4169);
        addUnvoidsCheck("13Qr22PY9862o2AMwGiJjELbPFg1EVqjvY",147856.3351);
        addUnvoidsCheck("1PCmmJu8c7BgUXjnM8QqTQ1Yu5YM7MeBDa",145829.7407);
        addUnvoidsCheck("19yegjdDMdx3ArzASkxkpkpyycSpmag6sW",137654.7788);
        addUnvoidsCheck("3ALZMwXrv9CXbVotYAAcDNoPiLNuHofdN5",129492.7782);
        addUnvoidsCheck("13pCKj73DbGgGcteSvZns19vF1XywyNsJU",124905.0883);
        addUnvoidsCheck("1LvVTc9TyxYVYdPQcqKxpNoJfbJNdGNs1b",122836.9877);
        addUnvoidsCheck("1FvqVxaCtsxtNTBTYndhsS9Xa5LxqjZGoo",122497.7798);
        addUnvoidsCheck("38HAQdwt61ZvwJwCMfJ1JrDF4YUBtTjv1K",116622.1632);
        addUnvoidsCheck("1HXC9ra6x5VZy4GRSCNSaDSWw3aNjzidwi",114583.2286);
        addUnvoidsCheck("12py9NUvkLQBqbBS4De8miMREhjpUrcKZp",114302.7862);
        addUnvoidsCheck("1EQ7zx9HLdmBdfUUwkA2fnCiKF2M5bEzwd",106336.8795);
        addUnvoidsCheck("1D7w471ZDkbtp8p8QZoMGduzJ26V9u2Jdm",105876.875);
        addUnvoidsCheck("1EccR9M3dfsVTPxvrEygFWoxyLRt6r4tnZ",103771.5822);
        addUnvoidsCheck("186MNGTmgdoesFNhCvQAts43K9ys13DjRK",103653.0854);
        addUnvoidsCheck("1HoYtFhQbYnx9UY1dfWfoM3gksuEP8397m",103103.0827);
        addUnvoidsCheck("1HP81SXjHkhYoEAxuVWmJXVpxffwZWMZs5",102878.0762);
        addUnvoidsCheck("1PhcxuPuhGnDH7GeAcYTDkky1cdcZt6mZT",102463.1249);
        addUnvoidsCheck("1KHsvBy9B7vxMgoNL1n64ChfTAczbp1ASh",102430.5677);
        addUnvoidsCheck("1Cy2FFLrqDTFzeScfieawPafAXj2i8ybK3",102381.2199);
        addUnvoidsCheck("1DE6puFK3MFZxMtnFiAWcLewQNqpn7GxDV",102381.2199);
        addUnvoidsCheck("1KrJ6e325yXVrNpd6NgYcuiRfvTcbZbBAR",102381.2199);
        addUnvoidsCheck("12VZ3shGNn8wkt6tWYSDSr1ehiz2NDGBhP",102381.2199);
        addUnvoidsCheck("1CEagPKG8WL6oX5baJ16uWacD5kQRCCQvk",101551.8922);
        addUnvoidsCheck("18szmRivgPcsPVes3QNyR1Xne6ND4xj8DK",99007.2216);
        addUnvoidsCheck("16tQCYcfJMJ2n36T9chCbNUQiB4hZeCB6j",98056.096);
        addUnvoidsCheck("1PpUpmpGN848rmiVg29CHj6StP9GSFvCMe",96756.5727);
        addUnvoidsCheck("1DsTZJvGo4ztdsBMdEpeqHHMdaTcZXMd2N",96373.4331);
        addUnvoidsCheck("1D7E61eVfnbZJdk4cPWFaDYyQ9usqY8CxL",96099.9591);
        addUnvoidsCheck("1ELCSHBK1JQQwDC96MFRAY2MtYYuVF3bwG",95783.9116);
        addUnvoidsCheck("19ToAspzr9VoRW5VgiSEGwSP5xHgdM64Z",95498.5318);
        addUnvoidsCheck("19m3QXzPX9gwZ3iD3gq2RS9EuqwkP6GkdV",95008.4065);
        addUnvoidsCheck("1MS76xiUL311BSxkSmP4iT2zN74e62QyUP",94536.7329);
        addUnvoidsCheck("14T3ub4q64EFdLsKuvumRabscLUddh1aKc",94530.8081);
        addUnvoidsCheck("1LXzyxj848i1d8QTnoVegaYziSYz7z6DNi",93047.6234);
        addUnvoidsCheck("1ATMQmcUszWtFpbLs8M8iZsZhS6oZCk7bN",92886.843);
        addUnvoidsCheck("1JainHFBinv3cCELLJW7scdxucu3hU9mgk",92697.7853);
        addUnvoidsCheck("1F5C89a5s9tfMYdUxcM7JfaBdZB6i9BKDP",92551.9118);
        addUnvoidsCheck("179ConMuWNepwGCkqaiJuFfqEhCvGANGwH",92461.0643);
        addUnvoidsCheck("1EDTRFD6JFb2N6EYzAcB2RwESLLfUbT9a5",92143.0979);
        addUnvoidsCheck("1HLEZL9LxSmCeZMr3XkMsWYSUCC2EXUSaL",91978.5087);
        addUnvoidsCheck("1Q7aBEendYyjtrA4wLPY2aHMYfD3kuPBXX",91959.4279);
        addUnvoidsCheck("38fHvXodJNehazXu6Yn7bMykfdq1L8asVA",91320.2563);
        addUnvoidsCheck("19bCRByV1WE51vfRqEKtweJknfzKevm7wM",90152.3201);
        addUnvoidsCheck("1AdWkjLpaCteeKznVbCXLXFoxymJeKz2Xz",89486.8425);
        addUnvoidsCheck("1GC2SoDHDUzctaKGGn9mhhBu6j4qaWCHFN",88442.5335);
        addUnvoidsCheck("1ABpYSgv6tcUpF4ryiQarGy5Rqfy1qwC9v",87853.5144);
        addUnvoidsCheck("13StFxJgNBiHMatJqnkBGt1damZPZV4qeM",87103.0348);
        addUnvoidsCheck("1L6KsD32NSo5kcDeDAwNiYuUdpozS7mMPs",85281.8973);
        addUnvoidsCheck("16bekfRKhSKpUkz6brFCyKXwqJobzmM6mi",84902.9445);
        addUnvoidsCheck("1P4LiqFF5kP96cAuM2WoEVfyoiYYxnLhyU",82717.112);
        addUnvoidsCheck("14yZJxhDKQkgaK19PEUpguaPtBrv2hbJxe",81798.3487);
        addUnvoidsCheck("1GGaL5BZM4LMScS1mphamvC9PmkTDT9VFw",81406.855);
        addUnvoidsCheck("18RQH8yB7qMapzqRQ4Um8qdHvs1EcoKhsY",79483.2967);
        addUnvoidsCheck("1ZcgfRp7sEt3XYr6QMc4x4AkYKHfrkLAE",77555.8486);
        addUnvoidsCheck("1B84kRYre9YHXoASmAAVvaTRuEHNQ1tQCQ",76782.755);
        addUnvoidsCheck("149pNpMNVQCCzgXHwckkkd1u34efahQicQ",74880.569);
        addUnvoidsCheck("19HrkEdm6sbwdm5cGoPAaY2ccxpUYAA5mm",74546.8393);
        addUnvoidsCheck("1BUZTuymLjkwEouiQuyR2Cp2RwLFKQ8Pdc",74265.8834);
        addUnvoidsCheck("1NKbkA4KxwGayoK5cBeQzPMVXCXAbcLtdp",73082.0347);
        addUnvoidsCheck("1HsFYGTJddaCspLWCmxydg8vWpeRPMrEmF",72901.2669);
        addUnvoidsCheck("37rpxu46CDFgBgLaynbSbRj4yqTJXWad4Y",72661.8961);
        addUnvoidsCheck("1LzCmnXqvNApPN8BLcyUWnddc7x4QQ5caR",72339.9157);
        addUnvoidsCheck("15vUxBo9S8Seob94aAhdsGps7MEzBoViPa",70063.6321);
        addUnvoidsCheck("1BxVCHGP9FZA2BcvLtLma1V3pDJAuo44DK",69738.4986);
        addUnvoidsCheck("17qB4S8KJhovDj2ag8iFtQfcW2isy3ZJj",69217.9655);
        addUnvoidsCheck("1Nwg73ThKbEtTJMxL8Fmp3nXPoB6F2sANs",67086.9533);
        addUnvoidsCheck("1EePAYbyhidFDhKsu3KsfnY8BtmcTqttrb",66622.8409);
        addUnvoidsCheck("195zUtZLEHYvwDTbPNkzHBBF1SoZThDZyy",65251.2407);
        addUnvoidsCheck("1KTKP2kacPMA824UJ7BN9PxUx4fman9M6m",64647.0477);
        addUnvoidsCheck("1Cw65HfhdehZfu3MEPwpUhdcooBSpy2rjo",64098.3852);
        addUnvoidsCheck("1J5qXLjQ3o5QJYnzriQdsLhMNCnQVBGU9a",61428.732);
        addUnvoidsCheck("14HADDEQz5KY6V4VyP96War1UqQdq66W1e",60310.9204);
        addUnvoidsCheck("17dfDBiqCmFbUvPaXDACNk37TbkYTQ7bQB",59016.1375);
        addUnvoidsCheck("1CVUSDGjd3jWrmGU7Vb2VzN3z6eMaxZ5dn",58992.6356);
        addUnvoidsCheck("19FH6UgAHbRSwNr7sGJxNYW7p8NA8ZxwYp",58251.8332);
        addUnvoidsCheck("16Z2uhUBhg6VWk3eXEpgbbXVTm5Phxove9",57628.9351);
        addUnvoidsCheck("17736rfhafvjFk3usfo9zQFR3PfHsvtiYQ",57586.2092);
        addUnvoidsCheck("1Fp1oVFRGBpShbWLL5VjRsAs7sVKKHNE5y",57300.2251);
        addUnvoidsCheck("1FT3ko6cZwTsNWxgroujBpsB3wrpu4NQ2a",57119.0682);
        addUnvoidsCheck("1JEhp9E4aLfAw1HCaXTAQMUGRLGmbP7Xh4",55440.6946);
        addUnvoidsCheck("1Db9bf82bZT5hnrZjjqoagGv2nQr6X5dLV",55211.2848);
        addUnvoidsCheck("1EdmcmNLjtzxyrGcZ7TsMB7Up7aQ4qSFcr",53894.7539);
        addUnvoidsCheck("1J15wnNEE44Nn7yVH6hc5gVc4EZn6ejghM",51511.5272);
        addUnvoidsCheck("1P37KAuSLGUmoRTQYWqeXtTCRuLsvszuZp",51190.61);
        addUnvoidsCheck("1JyuxSxPsd2A9HfF2X7EbFFZfSAPZPs4M5",51190.61);
        addUnvoidsCheck("12iDQNAbfVduDhfWhFevyLCJBGCJHd7NmH",50699.8395);
        addUnvoidsCheck("16d9C5nBjh78JwasrPg9CimwUBBoGArfSr",48232.589);
        addUnvoidsCheck("326DXFoxBvQgg11qEsppzEbSvmrvMN6hJg",47655.2125);
        addUnvoidsCheck("1G2pGEM88fqnMtZdJNCLF8wh7T6YQSkWog",45735.7646);
        addUnvoidsCheck("1odajeaZaCpHrxMPKq4b37KbsKnGRkxwt",44031.0739);
        addUnvoidsCheck("1FDg5uY5SVEQexpwmvZcnLGxFRuy4q7NQp",43963.4912);
        addUnvoidsCheck("1CiBuR4aYoAfhKt1AhMKiJMGwkSCaQuWxm",43639.6145);
        addUnvoidsCheck("18YLC2tD1yGEdaNitXuaueyFPJ9yeJRwgL",43508.8586);
        addUnvoidsCheck("19fLMHcFPX3CiHXTDUnNJg3ES23yXQTYbv",43237.9733);
        addUnvoidsCheck("1H1sAkEMqJ6w2yms3V26nzSKcjtkVdzkkv",42371.167);
        addUnvoidsCheck("1HW2VtmAuomDjBPAqVVWPWfRuDx8S3qzMK",42269.0582);
        addUnvoidsCheck("1Mmm4c8PZvNttyzZLJJwyMR1QkFMA5GF5P",41775.9652);
        addUnvoidsCheck("18iYdBTcAY2hPZiKHqDz5D61YjrKgJ7gyY",41673.3434);
        addUnvoidsCheck("17gJqvqZ26i22p3sKaKRqAb12sZF64meFY",41581.5084);
        addUnvoidsCheck("18Z18VmxKiXRz3Zd45ejRv8vtkFUa7e9GS",41068.3545);
        addUnvoidsCheck("17q6DdCN4wnURrsBVduMjkpAGygmiwaoHg",40982.5072);
        addUnvoidsCheck("1Kf6ggJHdRS8oeyjDSx4MouG9Bdzr61EUf",40932.8308);
        addUnvoidsCheck("1MhLm4VaZ7XWPPsFrZnqUfUUehNhG84Ww2",40354.4742);
        addUnvoidsCheck("1Fd6r9JrvB1W2MgU4n9vkjzG2NJ5Rx8b2A",39970.129);
        addUnvoidsCheck("1CQ6koPvnna4d5ENNhaEzRHmLEDegMaP2S",39777.3949);
        addUnvoidsCheck("1HLFG8EuA6vsfUi5tw37PcUCnASqA4x1w3",39729.0087);
        addUnvoidsCheck("1KcXadtYBJgavHKNfE3VfmTJyVZdXUpCmN",38756.8216);
        addUnvoidsCheck("12wspBHpbn3UvZvmNUFQ4tWephVcx9Nt6Y",38163.3376);
        addUnvoidsCheck("36GHpNCyDfuGXuMMVv15PDiUi3UzhTfGT8",38042.6968);
        addUnvoidsCheck("1CbhHTFFaUjmDwwFYucSLKRmD6pUtKbVNN",37908.3056);
        addUnvoidsCheck("16oCBjNuxStiQq5RbvdgkCgCnkSpewCr8N",36907.6872);
        addUnvoidsCheck("1AZ52RzySPbdbTQZwxkUnAuYCnrQgQ1UHD",36524.6582);
        addUnvoidsCheck("16E4kUAf739pK7c48J5uJpcdB3fUN39Fcb",36183.3875);
        addUnvoidsCheck("122R8pEGWT4ABeh9BWsz4DPhJ8xyYMq9TQ",35673.8513);
        addUnvoidsCheck("19Zd1HCUXyveUvj7QwHEqWiF3To7wuv5PQ",34703.1257);
        addUnvoidsCheck("1J8kXzvDLHuQQTTNt7bNmMKUz25RPJC8wz",32271.3861);
        addUnvoidsCheck("16LUs7scDuJvGk4A4LaR24DwHiXuwsZuVf",32071.1541);
        addUnvoidsCheck("16YBpNdzKe58KmwPA3cBaf6zhqXCdTHiGp",31912.1499);
        addUnvoidsCheck("1LmJ7z3WezMRWyKx1ZHYdTgg9cX23bHNHJ",31658.5617);
        addUnvoidsCheck("13aKeNqzEzNrdFV1SfbZ4Spg8x8QqM1VpW",30714.366);
        addUnvoidsCheck("18dz62bLpfMepyg59nCnkBrufrQAz2H5gt",30556.4973);
        addUnvoidsCheck("15E1CktEoJGbwm4XNS9aKk7ijP93jbWNuV",30459.5979);
        addUnvoidsCheck("1MneNPGDvscZdn1aGcVKcRvLoauCBTRfaB",29676.9267);
        addUnvoidsCheck("135uZKXDhYYhiLisdCvyx3fLhKS1mbp8nE",29369.8225);
        addUnvoidsCheck("1DksYUW6J9wGuArxM6n1eMhjG86mqxnmA8",29315.9921);
        addUnvoidsCheck("1CkH38kWDAf6d9EwVU38s6fjvAShu6tfvg",28909.788);
        addUnvoidsCheck("13Qnwu6UUHrPfa2bo89v4kqKkUrDW8Z2Cf",28845.0792);
        addUnvoidsCheck("17KWUsihUPxeiVYxFZqTJxFtno5rxvV89g",28420.2706);
        addUnvoidsCheck("1KKV9aNR1TnWnHNzqpX17Jio8NMoXLycCt",28268.5924);
        addUnvoidsCheck("1K44nMoCmDEf5Wa1v9Y2UFUiM8Qai99RQj",28031.7075);
        addUnvoidsCheck("1EwBpn9NNPGE174NvkSjhtdAsFdUCiHdk7",27964.8614);
        addUnvoidsCheck("1Q4P6QW5TmtB8u5QsSoo8FihD7AphHML1w",27815.1447);
        addUnvoidsCheck("1Mo2U2did2AFKX45sFg1tbK2z8K8LuHWyu",27631.7962);
        addUnvoidsCheck("17SnP5CDEUKPZBa8yXTUqrvtnM3VHB2VkV",26973.3562);
        addUnvoidsCheck("1GK62XAceZtLQmikjNUVCQrrLRgkAN8GfJ",26885.3084);
        addUnvoidsCheck("1DHKTB55ZPEQ4Ng1rxwe39i5jiF5ebmYHf",26701.275);
        addUnvoidsCheck("1JW3wHLApAgwpFTYg5KTMc8MJe4b2E1vtd",26470.2062);
        addUnvoidsCheck("1GV6FRbvNuunDxRdJ5UfT7oHefwB6ugcZd",26461.0117);
        addUnvoidsCheck("1DLbmALgy7saTfpEsbweP1es9CmVVzHpts",26378.1737);
        addUnvoidsCheck("14FPA9qD3hFDtLjZHDy2JUDkixFtEXDiwx",26175.8997);
        addUnvoidsCheck("1JJuVWjefhYk7VfFi5D7UovVdA19X7hCv5",26156.9221);
        addUnvoidsCheck("1LBKdycznxbx5egJBqz19ijugwHcHFY1jm",25595.305);
        addUnvoidsCheck("15imAyVyDtsuwUdLccY52epJt7Kakg7ZnP",25307.9503);
        addUnvoidsCheck("1Voidf1WWAwqMd2uMLr5uXisRyjPnWRA7",24448.6353);
        addUnvoidsCheck("169R7xxSRxZXmxNX3YoKNTtaYQwHLNZNao",24201.1903);
        addUnvoidsCheck("1D3miCgxVoV3yuJDPG4RAtXu8qnLdRE6j5",24041.2766);
        addUnvoidsCheck("1Naira51gy4pc6hnSeokQ2mCm7tQ5eZyZU",23794.4206);
        addUnvoidsCheck("1BEbASKFwGuJfwLHFh1jSTWSr6RMviN9hs",23503.8368);
        addUnvoidsCheck("1LZNS7UNiL3Q74RuGTzY593jMP8t2hKfDb",23436.9577);
        addUnvoidsCheck("1M5VHFAvwrTgeFVASZHeiAbETMrbaUWHqE",23429.1231);
        addUnvoidsCheck("1FNqi66JVzE3kou4QQX4SHDAxvYLVpDg2U",23231.0572);
        addUnvoidsCheck("12S34uZ2HDiVgdsY2sV8nZpQZHwsENiqoH",23060.6382);
        addUnvoidsCheck("32kYzc5ehbSAeAoxj2qcrfgqPpBYDBQ2cL",22933.3933);
        addUnvoidsCheck("17ASKQx5PxCM42Ym8MEvaeAPjRwCP3asXK",22906.8397);
        addUnvoidsCheck("1Kr5JqiaCGo5SugDPdFSd8KJ5GofVhzhqW",22536.499);
        addUnvoidsCheck("1GzikhiugNF4uHxsWaLHgLBsD4GUeMQiDy",22152.2506);
        addUnvoidsCheck("1MsaU3qFkz84Lid6RfiACSf5qqLbDgdDwz",21065.568);
        addUnvoidsCheck("18fjqZuCQ3ZM5XoGjAKfus5PsRfoZmf4EH",20483.5564);
        addUnvoidsCheck("196fbmwTBkFm3xjUisMBSggmV4xyR5642W",20476.244);
        addUnvoidsCheck("1KF6tLemSoVjgtQ7WiFNseqKdnf1HZh27V",20464.3637);
        addUnvoidsCheck("1B9f9HChAAonTUxJPNU9PkUu2zRLVA1sLv",20360.3936);
        addUnvoidsCheck("1BLrpCrTXa194ZszTNYoXiT6VESR4nyo8M",20034.251);
        addUnvoidsCheck("17MDYNKvWD7JzB1aoxX7pnCiPYUrC8LQgn",19942.2185);
        addUnvoidsCheck("1JPVXfTmC6xwbYW2RgSeM6iKZP3NsMBByT",19716.2846);
        addUnvoidsCheck("1DMHGapZs43By6siPrzZUgWxCFt95V65nk",19694.1652);
        addUnvoidsCheck("1GVSLaHWsaMHkEqFYdrFbraHxLx8Mi5RX",19662.5661);
        addUnvoidsCheck("1hRHm9hxrvFuyUugH64iQxCdyxeFThWUf",19533.0096);
        addUnvoidsCheck("1CcYJtqKDTMPtsPfiuYJjsJoL9nj5f3b3s",19488.3758);
        addUnvoidsCheck("1DxKXSnBxYcfrMqRtzrX9WbFYPktSRa5E3",19378.1738);
        addUnvoidsCheck("1MqjkKzZjTTvXHvuNZzEqSSPhidhu8H44Y",19252.5672);
        addUnvoidsCheck("1GHkwabM8z66PiTfcNCTTYkNnvdF22SQwY",18881.5321);
        addUnvoidsCheck("1EmrdgNEfsqDwfGF4LygEFyowTTZTPV5Uh",18573.9756);
        addUnvoidsCheck("1Ha2sMvSMgow6Dz6bqhXLfx1d73JNPHn8y",18454.6099);
        addUnvoidsCheck("18jbntdbaQLEkc8sknUed5QyJ2AgAy3a5V",18180.9613);
        addUnvoidsCheck("1GQjLN8jEVWLEPxVXgYszbwhhDurmxar9r",17900.5189);
        addUnvoidsCheck("1pr62K6d6MYddi6LfY5oPoaY5AEfn7Utx",17693.5445);
        addUnvoidsCheck("16wNqcVCy57GdtTKcPfQaQmtmkyGZtMcBA",17664.4078);
        addUnvoidsCheck("1Lu7yvwyfsPno1rdA4mu2reGsC6FKHnmso",17609.3628);
        addUnvoidsCheck("13tLKf7nhd7t1rgnT5mVMNpEDyQbbbiABJ",17372.8133);
        addUnvoidsCheck("1EAesPzaxxiffDVHAvHJZWxqNNyH1yq2YZ",17300.5302);
        addUnvoidsCheck("1AMbK69tF4rpjotkAekQoEiFiA9xZaRZ1J",16934.1712);
        addUnvoidsCheck("3JfS6y9txPp55rQTDYAsY8LQtvQLuogpmP",16318.3499);
        addUnvoidsCheck("14WMdsXApZjz1ybye79nA3JJf6h2R5T2SH",16151.1114);
        addUnvoidsCheck("1EQx6f1zYK8LpUa7mWBPTEJ9Ny34jTvio",16124.4088);
        addUnvoidsCheck("14G3vYt7hSHkpf8e8A8c3nTvbegTSv7GzP",15594.3661);
        addUnvoidsCheck("12XRgca2NLFWTEsmm9gg3FqfDSYhbnCXcD",15101.2299);
        addUnvoidsCheck("1MzNRG1WUtLgKTkpxMnUAh3F257Wkg1JQw",14398.939);
        addUnvoidsCheck("196PUcEJfiNYMdDaXhnqgqzzA3cjHpLmSt",14352.7253);
        addUnvoidsCheck("1ELZkisGYyocYdSsd6MTsbXAyHWwwPBuGz",14333.3708);
        addUnvoidsCheck("16SxRuDSeUQp1mDnaLTHBMjWsUtPXup1dZ",13934.3921);
        addUnvoidsCheck("13rUkZv3mmTqSpRMaPmKhnBJ3f139ncAeA",13761.3592);
        addUnvoidsCheck("14d1PAGLCdLA4t5KHzPhCAf5V6ekYhDCME",13087.9696);
        addUnvoidsCheck("1KVcWB6VZhmAJgaWez6pgETEoX8nmAjzmE",12765.4214);
        addUnvoidsCheck("176bEJsG2tHEo4bsA17AguZN9jMU6LmYBX",12582.1958);
        addUnvoidsCheck("1KW1eim7pRcduC2EaTBCPQHy9jCBp4BsX2",12408.904);
        addUnvoidsCheck("14ndafdPdqB2wLQddXT1N4kaVzvgdgi9eY",12349.3397);
        addUnvoidsCheck("1BZCTgfergVvwwUx5kMDFPHYKjrk3fnf5W",11873.7015);
        addUnvoidsCheck("1PuntP7sziEBVfDHWsPXiDEN2ZQo1YibwD",11490.4206);
        addUnvoidsCheck("1FBSjuR2vFqCuSW5QbfSG6uz3T7RVEwhGB",11229.2039);
        addUnvoidsCheck("1EY7UYP49CdEwVWExmekAKGUwGPppepV1R",11148.4617);
        addUnvoidsCheck("1GR4CaxvHwBgDVyBXroPDPkjNTj4HRkKkH",11129.4548);
        addUnvoidsCheck("18q3cUrw6g7CGs1mPYuxcQ5XaBFqU4sR27",10804.6374);
        addUnvoidsCheck("129yMeagKaaEEyQQz88zAfuaeY8YYz26wL",10575.2362);
        addUnvoidsCheck("1HhRfRPDLcUk8J1zpURFug1xSpacaeXCtL",10258.1148);
        addUnvoidsCheck("1NU9xzCWnrZCXHa3rRZtdi1QqvNpRMRhvH",10238.122);
        addUnvoidsCheck("1LtDqWvBoFDF4aoAYzBMmsgGQ4g4HsmYJo",10203.7579);
        addUnvoidsCheck("1JFAUpkrjwU6cn8Af2npwaQ3c8b4eMsbSw",10162.8765);
        addUnvoidsCheck("1FTqLnbjCUiF84cbQMxaguQ8sU9rikKhwW",10152.8043);
        addUnvoidsCheck("17MLkiXDu4iFoaUoMPhaUrvjVSSMuBkJhH",10128.9075);
        addUnvoidsCheck("3De41NGxSHuDgEcGXq8757HUK6m5tJfvdP", 10126.6165); // added manually
        addUnvoidsCheck("1Gc1Zz8VTrnTrq5DLa4wZ8CmD2AaSaUJ4t",9744.5829);
        addUnvoidsCheck("1B5WmWZi8twk83iaN3YbjmZEYgvQRU5D2P",9713.5762);
        addUnvoidsCheck("1815FyZMLvjTKiTSmxVhnw81imJZeScdtm",9600.9013);
        addUnvoidsCheck("17o4Dyfc7GMbwMgaeXhQQ74nqDcdmP7RVM",9540.5709);
        addUnvoidsCheck("1FndprLxopAaZFMkWDmACEhmS2W9s5CVfH",9530.5263);
        addUnvoidsCheck("3932M2K8vKTE8tZXPjUNvTN9tQyB4X2NVq",9061.112);
        addUnvoidsCheck("172aiWHKukWFGE11vUFniZVU7kp6ZRUZyU",8929.7398);
        addUnvoidsCheck("1D3rqNoj9CjYFqSv7nzL54BFaZMUGkQ29A",8883.9028);
        addUnvoidsCheck("1K414ZEozoqWr5zjMpFjYuKtGTXSAJb6QE",8668.4425);
        addUnvoidsCheck("14skxeEv8cniqDm2TxjPumM5sJpghqZo1b",8199.4907);
        addUnvoidsCheck("32jPMzNzHKE2f6u6V1Ch9qxXXf456D83F5",8190.4976);
        addUnvoidsCheck("1CmpnV341LScP1iakDSJqsf6EGinJjuP6G",7885.0919);
        addUnvoidsCheck("1AmmLPhnrTQAse2dWzTPnV6mHYjhYVCr3j",7010.1116);
        addUnvoidsCheck("1uCjf49oZSLaduGNF8nPgCAeEQDzYqEgC",7001.4983);
        addUnvoidsCheck("3EKnAJNhQwmERKZPHJXG8dkpLxiiPfRkEi",6824.4124);
        addUnvoidsCheck("1LM4KFSY6gT9HAyQFzKhkXK3A2LafLax1y",6781.6165);
        addUnvoidsCheck("1B5nabx9sJs6somo3PhN2zC65iVG8uFTiQ",6699.4131);
        addUnvoidsCheck("1L4kRvtiQvgBj4J79GhLyop1VNoYkFaN1J",6635.8198);
        addUnvoidsCheck("17hdJnw7J5jvWx75nPenQP8hfstQSnw7uG",6502.7262);
        addUnvoidsCheck("1MaL859XbZ6AKrzCj8Q73QTGA5fVZ3mhYm",6453.7298);
        addUnvoidsCheck("12cNxny1B7mmMMNTZgrsXGiqk5hCZCJnGu",6339.0733);
        addUnvoidsCheck("1KeFywuPFoZVUkmzZC2qgNco8qU9rPVTvR",6288.9432);
        addUnvoidsCheck("14F2JkzcJBhkGFExfPQLsWxzUtZaME3JSs",6163.3494);
        addUnvoidsCheck("1FzxRgEPRcsjzQ3X2tbZPUkfa5ktH9vG1Q",6142.8732);
        addUnvoidsCheck("12v4d15AuopsVzudct7J8LJMm7qH95SxA1",5941.8237);
        addUnvoidsCheck("1Nqf2fnLHEt1qLxGHFDmVspy2c4b36FeFo",5687.7271);
        addUnvoidsCheck("18GfHvyRd3UWRB8iVQpKJg1trVAbL8vq5G",5451.247);
        addUnvoidsCheck("1Fm6b2qFJvp5HiTtBJn8vwsgynm18yD2Ru",5448.2846);
        addUnvoidsCheck("1DuQF15bDFp64qsxEDJXYabshnkudvfWar",5323.8234);
        addUnvoidsCheck("1CM64CTCd5FCCX7kb29g3mghM8GkT5c6UX",5298.6745);
        addUnvoidsCheck("1GtTeRLhsvyFjS65Jcs3fL1R3Myhxi6S2o",5269.1569);
        addUnvoidsCheck("13UCGZ4qnGwMiP4PqqUx4Qi6tqQfvcqt2m",5144.4189);
        addUnvoidsCheck("13bgUr8JugfD2rb5PjYe9CzTPTGPNdBaTT",5138.0896);
        addUnvoidsCheck("1JMGMnkv4skseqfkcSizUFdT1JuN9wVUke",5110.8551);
        addUnvoidsCheck("3EmtSeEEJEGaX7JKpmuupf5N2F4pQ7vDuM",4837.0703);
        addUnvoidsCheck("1C9Hg66TbbQtoaWP3Y31MCQp2JC2TmVPd5",4616.3633);
        addUnvoidsCheck("18jsbdMm2AyiAv54rrFC5ncnQiLTh8FzoP",4427.5335);
        addUnvoidsCheck("1M7rCmfjBGxwGRtZXv5K8Nzdy3A7v4QTPm",4377.9268);
        addUnvoidsCheck("115docnYkRJopTHTeEgTAii7Jv7N5wBhSz",3961.2684);
        addUnvoidsCheck("3QA7QDkb5zgRLbkeiZzGp22hD14Qf2Myc8",3946.7328);
        addUnvoidsCheck("3CujQgooeyG47Fhwc1q1srKeoEhDEggeKh",3932.9872);
        addUnvoidsCheck("1QEEd5GjiWaeE6ZdgB7UPteyDQpMs6PRSp",3890.4864);
        addUnvoidsCheck("1HJn55Y6wbtwpSh2rauTy8Un5Cduk12gaT",3884.3245);
        addUnvoidsCheck("1KazZQVFXT1LdTAKPh4b36N1ETwJSJv4Rq",3882.2453);
        addUnvoidsCheck("12bycbUapdamVGhrnvUJiooqVQQ95Mei4S",3808.5656);
        addUnvoidsCheck("3C1w8g9GESf5H4g2TNKocN8BeD73gubT7q",3604.364);
        addUnvoidsCheck("1JmFmpbKq7S36J7w2zpXPhJHFFj9Zc4Gbr",3558.6811);
        addUnvoidsCheck("14CpY8dRsjfLBzussdADgAQwFKbeWuz8j3",3543.8845);
        addUnvoidsCheck("1J23z9TRWiZRDHyE6xQTsTnkA2g8cPp3UY",3379.9098);
        addUnvoidsCheck("19AKeSTDAWT4VB9WUAK3aiw5KSfLKooTKy",3313.8652);
        addUnvoidsCheck("1MrEo9uD8tAgHML5JLzo9biUkKH7ggcsp",3156.8333);
        addUnvoidsCheck("18LLinoGvUvSSQXLovt3fDWXwMSfmS1UDG",2991.3328);
        addUnvoidsCheck("17hGzZMUzMB3r1MCeHefohyYkfxXPKfKN1",2733.3258);
        addUnvoidsCheck("1GmY61vTsBjvY9cqvQY6LS9BBy6JSqjBYm",2552.0257);
        addUnvoidsCheck("1BCC9SkVuBmnWr1gocQgCzTZEjdvupjK7T",2416.1968);
        addUnvoidsCheck("12LDkCjLBJJSXpfrTcR8dMFVMtrvmF2Prr",2371.5156);
        addUnvoidsCheck("1BH2KAuUpiuhkszMA9fenviQRbn6pHgGE",2344.5608);
        addUnvoidsCheck("1G82zewBq8NzpC1gFHVsPRC6K67mFNon6a",2229.735);
        addUnvoidsCheck("1McnyFoBSC1eeBaXPA876MTP7PEQGrNAWc",2196.766);
        addUnvoidsCheck("1G1tyHcpFQzQyxMFxna8mB99kPfaQjdLPW",2109.0531);
        addUnvoidsCheck("17kGd5dKGSssNn477Rjd8kmJdsrUSM626Q", 2082.1611); // added manually
        addUnvoidsCheck("1AokP9W85pRrH1TdvvfyAVuMvPUpi3Jyie",2057.1294);
        addUnvoidsCheck("1JuTYh6yGiSgdD5HebFYAgDrhzXN2vioR1",2047.6244);
        addUnvoidsCheck("1KFVdnGMVPQ2gGJJZNKWQq5pJP1yVjUcLb",2047.6244);
        addUnvoidsCheck("19ZFUNJ2Jdwm4yR2Y2uQ4MRNdE6L3fQFZH",2047.6244);
        addUnvoidsCheck("1JuW2mEzofGwKrYfVx5wW67hrPvpBAJd43",2047.0714);
        addUnvoidsCheck("1JAnM7Xy1xJ7Ufzv46CNiVwrRfV2esVotL",2024.399);
        addUnvoidsCheck("1GL6C71jDeDsszFnkdYdQsYLxtJL2arGWm",2023.0995);
        addUnvoidsCheck("1GVdqzA4ULCctJJZRwKX1mRpYdHfEL1WwY",2005.1631);
        addUnvoidsCheck("128XumH2QsZ4BGo6jEuTDjShcretUxDqLi",1984.6236);
        addUnvoidsCheck("34tLwmnGzH3gnnZZ9p2GMduXDmxr6tKEkV",1980.1602);
        addUnvoidsCheck("3LsVnWTCR2hmv6kYQF1grsdNUUEpscUAdE",1979.8837);
        addUnvoidsCheck("17gvkFpYV9842qZriAtN9S2L28sZnJx4oE",1935.3084);
        addUnvoidsCheck("1HcpjzYFoonrhFYDs3AdUxbS7Qu6fS1XYN",1929.2461);
        addUnvoidsCheck("1K1eaDW9TvBeVxo5xafQRoY5BGhKMXsRnn",1858.1085);
        addUnvoidsCheck("1Gd4niapnmcn6UJpByhBJ2c6uxkpy1k1MJ",1857.8716);
        addUnvoidsCheck("1LcEW8ycTnjcSdQRZ1o5fz1c3GEFSwZtxQ",1829.2743);
        addUnvoidsCheck("1DsBd2BSefpZ7NWKJYgjCWHem6VXSip2PL",1501.9322);
        addUnvoidsCheck("1594FrR5ZbS2npVC3irmtNfKV3n92nQ8Zq",1479.0768);
        addUnvoidsCheck("1BDt2KN7GcBAvzKgTLUDTogg1zZiWKmhaW",1409.7787);
        addUnvoidsCheck("1HNPGJmb1bSUk6tsbJgsFjVc8iDJ2uNdgN",1262.6227);

        // Add all exceptions here
        // Old address, new address, reason for change - if new address is empty, entry gets removed

        addUnvoidsCheckExceptionWithMessage("1Cy2FFLrqDTFzeScfieawPafAXj2i8ybK3", "1ADgeJbeVaML6PyCTEXigJq2rLLhoTtxu1", "notsofast: Someone sometime promised to rescue this screwed coinbase owned address (wasn't me, but looks legit)");
        addUnvoidsCheckExceptionWithMessage("1HW2VtmAuomDjBPAqVVWPWfRuDx8S3qzMK","1Co9JKogApt7EfCr9n6GVxnEAL7nXT65Q2","Manual change request: 1Co9JKogApt7EfCr9n6GVxnEAL7nXT65Q2, signed \"Cryptodv\": HL5G1AGgEIMoSNChv5wgmOaSlBYULbNJQaVHAVPxc0Y1ZJcprYOn9QIj2kGrPpohwcgPy1KSuSNfdxV");
        addUnvoidsCheckExceptionWithMessage("1P4LiqFF5kP96cAuM2WoEVfyoiYYxnLhyU","","Refunded https://blockchain.info/de/tx/5312f91d3839125ac2fedcb205b9ae1939edc457f36b6509a909cc9d8cfd8f08, sent donation too late and changed his mind");
        addUnvoidsCheckExceptionWithMessage("15Z5BggJhi1cgv4CV9TFGZ5WNbH7aF7hGf","1BexuDc3UUpMRuxE9YV6TqVWTv9dhEvucF"," I, the owner of address 15Z5BggJhi1cgv4CV9TFGZ5WNbH7aF7hGf with pubkey 0217e6fb61c54b5e8c9e81d99230bae79edb0430cbaa05f5d73382347f727d84b4, hereby agree to transfer my entire XEL holdings (7321,966975309) to pubkey 036755eb2c89cbfb4bf66e118f3ed166c17357c4163cf7c9c04d07cf3d81dddba6 which belongs to 1BexuDc3UUpMRuxE9YV6TqVWTv9dhEvucF. This transaction is irreversible.\n" +
                "        Signature:   HzsStG9Jh9XIPp0YlPK7ajYgjwnN00rg9S7DxhE8VTIGaoay94juawjjATRWe+D8OHuXilMiRvOhUvm6mom3pVM=");


        boolean failedAtLeastOne = false;


        // check if unvoids elements are in Redeem class
        Set<String> keySet = amounts.keySet();
        Iterator<String> it = keySet.iterator();
        while(it.hasNext()){
            String addr = it.next();
            boolean found = false;
            for(int i=0;i<Redeem.listOfAddressesMainNet.length;++i)
                if(Redeem.listOfAddressesMainNet[i].contains(addr))
                    found=true;

                if(found == false){
                    System.out.println("Failed to forward-locate address: " + addr + ", it is not in Redeem class");
                    failedAtLeastOne = true;
                }
        }


        // Check if Redeem class elements are in unvoids list
        for(int i=0;i<Redeem.listOfAddressesMainNet.length; ++i){
            String addr = Redeem.listOfAddressesMainNet[i];
            if(addr.contains(";")){
                addr = addr.split(";")[1];
            }

            if(amounts.containsKey(addr) == false){
                System.out.println("Failed to locate address: " + addr + ", it is not in unvoid's list");
                failedAtLeastOne = true;
            }else {
                // Now check amount
                Double dbl = amounts.get(addr);
                dbl = dbl * Constants.ONE_NXT;
                Long doit = dbl.longValue();

                // check for error greater than 1 XEL (weird, unvoids script produces inaccurate results so we can only check them to <1XEL precision. Should be fine as a test though)
                if (Math.abs(doit - Redeem.amountsMainNet[i]) > Constants.ONE_NXT) {
                    System.out.println("Wrong amount: " + addr + " should be " + doit + " but was " + Redeem.amountsMainNet[i] + " [error " + (Math.abs(doit - Redeem.amountsMainNet[i])) + "]");
                    failedAtLeastOne = true;
                }
            }
        }

        // Unvoids list and Redeem class list should have same lengths
        Assert.assertEquals(Redeem.amountsMainNet.length, amounts.size());

        // And make sure nothing went wrong before
        Assert.assertFalse(failedAtLeastOne);
    }

}
