package nxt.computation;

import nxt.*;
import nxt.crypto.Crypto;
import nxt.http.JSONData;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import static nxt.computation.ComputationConstants.WORK_MESSAGE_RECEIVER_ACCOUNT;
import static nxt.http.JSONResponses.*;
import static nxt.http.JSONResponses.FEATURE_NOT_AVAILABLE;

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

class Pair<K, V> {

    private final K element0;
    private final V element1;

    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public K getElement0() {
        return element0;
    }

    public V getElement1() {
        return element1;
    }

}
public class CustomTransactionBuilder {

    final static Pair<JSONStreamAware, String> createTransaction(Appendix.PrunablePlainMessage work_rel_message, String secretPhraseOrPublicKey) throws NxtException {
        return CustomTransactionBuilder.createTransaction(work_rel_message, secretPhraseOrPublicKey, null);
    }

    final static Pair<JSONStreamAware, String> createTransaction(Appendix.PrunablePlainMessage work_rel_message, String secretPhrase, String referencedTransactionFullHash) throws NxtException {

        Appendix.PrunablePlainMessage prunablePlainMessage = work_rel_message;

        if (secretPhrase == null) {
            throw new NxtException.NotValidException("No passphrase given");
        }

        short deadline = ComputationConstants.WORK_TRANSACTION_DEADLINE_VALUE;

        JSONObject response = new JSONObject();
        byte[] publicKey = Crypto.getPublicKey(secretPhrase);

        Transaction.Builder builder = Nxt.newTransactionBuilder(publicKey, 0, 0,
                deadline, Attachment.ARBITRARY_MESSAGE).referencedTransactionFullHash(referencedTransactionFullHash).recipientId(WORK_MESSAGE_RECEIVER_ACCOUNT);

        builder.appendix(prunablePlainMessage);

        Transaction transaction = builder.build(secretPhrase);

        JSONObject transactionJSON = JSONData.unconfirmedTransaction(transaction);
        response.put("transactionJSON", transactionJSON);
        response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
        response.put("transaction", transaction.getStringId());
        response.put("fullHash", transactionJSON.get("fullHash"));
        response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
        response.put("signatureHash", transactionJSON.get("signatureHash"));

        transaction.validate();


        return new Pair<>(transactionJSON, transaction.getFullHash());
    }
}
