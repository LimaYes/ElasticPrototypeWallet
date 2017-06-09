package nxt;

import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

public class GenesisMiner {

    private static BlockImpl getGenesis(String genesisSecretKey, long toWhom) throws Exception {
        byte[] genesisAccount = Crypto.getPublicKey(genesisSecretKey);
        long id = Account.getId(genesisAccount);
        System.out.print("public static final byte[] CREATOR_PUBLIC_KEY = { ");
        for (byte c : genesisAccount) System.out.format("(byte)0x%02x, ", c);
        System.out.println("};");

        System.out.println("public static final long CREATOR_ID = " + Long.toString(id) + "L;");


        final List<TransactionImpl> transactions = new ArrayList<>();
        TransactionImpl transaction = new TransactionImpl.BuilderImpl((byte) 0, genesisAccount,
                Constants.MAX_BALANCE_NQT, 0, (short) 0,
                Attachment.ORDINARY_PAYMENT)
                .timestamp(0)
                .recipientId(Genesis.REDEEM_ID)
                .height(0)
                .ecBlockHeight(0)
                .ecBlockId(0)
                .build(genesisSecretKey);
        transactions.add(transaction);
        transactions.sort(Comparator.comparingLong(Transaction::getId));

        String signatures = "{\n";
        for (int i = 0; i < transactions.size(); i++) {

            signatures += "{";
            for (int s = 0; s < transactions.get(i).getSignature().length; ++s) {
                signatures += String.valueOf((int) transactions.get(i)
                        .getSignature()[s]);
                if (s < transactions.get(i).getSignature().length - 1) signatures += " ,";
            }
            signatures += "},\n";

        }
        signatures += "}\n";
        System.out.println("public static final byte[][] GENESIS_SIGNATURES = new byte[][] " + signatures + ";");

        transactions.sort(Comparator.comparingLong((transaction1) -> transaction1.getId()));
        MessageDigest digest = Crypto.sha256();
        for (Transaction transaction_iter : transactions) digest.update(transaction_iter.getBytes());

        BlockImpl genesisBlock = new BlockImpl(-1, 0, 0, Constants.MAX_BALANCE_NQT, 0, transactions.size() * 128, digest.digest(),
                genesisAccount, new byte[64],  null, transactions, genesisSecretKey);

        if(!genesisBlock.verifyBlockSignatureDebug()){
            throw new Exception("FAILED GENERATING!");
        }
        return genesisBlock;

    }


    public static void mineGenesis() throws Exception {
        try{
            String genesisSecretKey = "";
            String redeemAccountSecretKey = "";

            BlockImpl genesisBlock = getGenesis(genesisSecretKey, Account.getId(Crypto.getPublicKey(redeemAccountSecretKey)));
            System.out.println("public static final long GENESIS_BLOCK_ID = " + Long.toString(genesisBlock.getId()) + "L;");
            System.out.print("public static final byte[] GENESIS_BLOCK_SIGNATURE = { ");
            for(byte c :  genesisBlock.getBlockSignature()) System.out.format("(byte)0x%02x, ", c);
            System.out.println("};");
            System.out.println("public static final String REDEEM_ID_PUBKEY = \"" + Convert.toHexString(Crypto.getPublicKey(redeemAccountSecretKey)) + "\";");

            System.out.println("public static final long REDEEM_ID = " + Long.toString(Account.getId(Crypto.getPublicKey(redeemAccountSecretKey))) + "L;");
            System.out.println("public static final long[] GENESIS_RECIPIENTS = new long[]{Genesis.REDEEM_ID};");
            System.out.println("public static final int[] GENESIS_AMOUNTS = new int[]{" + Constants.MAX_BALANCE_NXT + "};");
            /*byte[] checksum = genesisBlock.getChecksum(0,0);
            System.out.print("CHECKSUM: { ");
            for(byte c :  checksum) System.out.format("(byte)0x%02x, ", c);
            System.out.println("};");*/
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static void main(String [] args)
    {
        try {
            mineGenesis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
