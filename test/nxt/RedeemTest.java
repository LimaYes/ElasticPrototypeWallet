package nxt;

import nxt.helpers.RedeemFunctions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

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

}
