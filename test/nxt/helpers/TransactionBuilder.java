/*
 * Copyright © 2017 The XEL Core Developers
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
package nxt.helpers;

import nxt.*;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.util.Objects;

public class TransactionBuilder {
    public static Transaction make(Attachment attachment, String secretPhrase, long recipientId, long amountNQT, boolean broadcast, boolean graceblock) throws Exception{

        final int ecBlockHeight = 0;
        long ecBlockId = 0;

        if (ecBlockHeight > 0) ecBlockId = Nxt.getBlockchain().getBlockIdAtHeight(ecBlockHeight);

        long feeNQT = 0;
        short deadline = 1440;

        byte[] publicKey;
        if (attachment instanceof Attachment.RedeemAttachment)
            publicKey = Convert.parseHexString(Genesis.REDEEM_ID_PUBKEY);
        else publicKey = secretPhrase != null ? Crypto.getPublicKey(secretPhrase) : null;

        Appendix.PublicKeyAnnouncement announcement = new Appendix.PublicKeyAnnouncement(Crypto.getPublicKey(secretPhrase));
        final Transaction.Builder builder = Nxt
                .newTransactionBuilder(publicKey, amountNQT, feeNQT, deadline, attachment)
                .referencedTransactionFullHash(null).appendix(announcement);
        if (attachment.getTransactionType().canHaveRecipient()) builder.recipientId(recipientId);
        if (ecBlockId != 0) {
            builder.ecBlockId(ecBlockId);
            builder.ecBlockHeight(ecBlockHeight);
        }

        Transaction transaction;
        //noinspection ConstantConditions
        if(Objects.equals(attachment.getTransactionType(), TransactionType.Payment.REDEEM))
            transaction = builder.buildTimestamped(secretPhrase, ((Attachment.RedeemAttachment) attachment).getRequiredTimestamp());
        else transaction = builder.build(secretPhrase);

        if(broadcast){
            Nxt.getTransactionProcessor().broadcast(transaction);

            // Now, if transaction was my redeem transaction, and we are below the #ALLOW_FAKE_FORGING_ON_REDEEM_UNTIL_BLOCK block threshold ... mine block immediately
            // This will help bootstrapping if no forgers are online yet
            if (graceblock && (transaction.getType() == TransactionType.Payment.REDEEM && Nxt.getBlockchain().getHeight() < Constants.ALLOW_FAKE_FORGING_ON_REDEEM_UNTIL_BLOCK)) {
                try {
                    Nxt.getBlockchainProcessor().generateBlock(secretPhrase,
                            Nxt.getEpochTime());
                } catch (final Exception e) {
                    // fall through
                }
            }

        }

        return transaction;
    }

}
