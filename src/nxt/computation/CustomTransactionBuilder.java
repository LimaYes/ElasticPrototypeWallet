package nxt.computation;

import nxt.*;
import nxt.crypto.Crypto;
import nxt.http.JSONData;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
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

public class CustomTransactionBuilder {

    final static JSONStreamAware createTransaction(Appendix.PrunablePlainMessage work_rel_message, String secretPhraseOrPublicKey, boolean wasSecretPhrase) throws NxtException {
        return createTransaction(work_rel_message, secretPhraseOrPublicKey, wasSecretPhrase, null);
    }

    final static JSONStreamAware createTransaction(Appendix.PrunablePlainMessage work_rel_message, String secretPhraseOrPublicKey, boolean wasSecretPhrase, String referencedTransactionFullHash) throws NxtException {

        String secretPhrase = (wasSecretPhrase) ? secretPhraseOrPublicKey : null;
        String publicKeyValue = (!wasSecretPhrase) ? secretPhraseOrPublicKey : null;
        Appendix.PrunablePlainMessage prunablePlainMessage = work_rel_message;

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE;
        }

        short deadline = ComputationConstants.WORK_TRANSACTION_DEADLINE_VALUE;

        JSONObject response = new JSONObject();
        byte[] publicKey = secretPhrase != null ? Crypto.getPublicKey(secretPhrase)
                : Convert.parseHexString(publicKeyValue);

        try {
            Transaction.Builder builder = Nxt.newTransactionBuilder(publicKey, 0, 0,
                    deadline, Attachment.ARBITRARY_MESSAGE).referencedTransactionFullHash(referencedTransactionFullHash);

            builder.appendix(prunablePlainMessage);

            Transaction transaction = builder.build(secretPhrase);

            /*try {
                if (transaction.getFeeNQT() > senderAccount.getUnconfirmedBalanceNQT()) {
                    return NOT_ENOUGH_FUNDS;
                }
            } catch (ArithmeticException e) {
                return NOT_ENOUGH_FUNDS;
            }*/

            JSONObject transactionJSON = JSONData.unconfirmedTransaction(transaction);
            response.put("transactionJSON", transactionJSON);
            try {
                response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
            } catch (NxtException.NotYetEncryptedException ignore) {
            }
            if (secretPhrase != null) {
                response.put("transaction", transaction.getStringId());
                response.put("fullHash", transactionJSON.get("fullHash"));
                response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
                response.put("signatureHash", transactionJSON.get("signatureHash"));
            }
            transaction.validate();

        } catch (NxtException.NotYetEnabledException e) {
            return FEATURE_NOT_AVAILABLE;
        } catch (NxtException.InsufficientBalanceException e) {
            throw e;
        } catch (NxtException.ValidationException e) {
            response.put("broadcasted", false);
            JSONData.putException(response, e);
        }
        return response;

    }
}
