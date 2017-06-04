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

import nxt.Account;
import nxt.Attachment;
import nxt.Redeem;
import nxt.crypto.Crypto;
import nxt.util.Logger;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import static nxt.helpers.TransactionBuilder.make;

public class RedeemFunctions {
    public static boolean redeem(String address_entry, String secretPhrase, String[] bitcoin_privkeys)
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
            make(attachment, secretPhrase, Account.getId(Crypto.getPublicKey(secretPhrase)), Redeem.getClaimableAmount(address_entry), true);
            success = true;
        } catch (Exception e) {
            Logger.logErrorMessage(e.getMessage());
        }
        return success;
    }
}
