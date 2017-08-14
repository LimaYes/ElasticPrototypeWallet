/*
 * Copyright © 2017 The XEL Core Developers                                  ~
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

package nxt.http;

import javax.servlet.http.HttpServletRequest;
import nxt.*;
import org.json.simple.JSONStreamAware;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Redeem extends CreateTransaction {

    static final Redeem instance = new Redeem();

    private Redeem() {
        super(new APITag[] { APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION }, "name", "description");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {

        String address = ParameterParser.getParameterMultipart(req, "redeem_address");
        final String secp_signatures = ParameterParser.getParameterMultipart(req, "secp_signatures");

        final long account_to = ParameterParser.getAccountId(req, "receiver_id", true);

        if (address == null) return JSONResponses.MISSING_FIELDS_REDEEM;
        else if (secp_signatures == null) return JSONResponses.MISSING_FIELDS_REDEEM;

        final String[] parts = address.split(",");
        if (parts.length == 3) address = parts[1];

        if (!nxt.Redeem.hasAddress(address)) return JSONResponses.MISSING_FIELDS_REDEEM;
        // More boundary checks
        final long amountlong = ParameterParser.getAmountNQT(req);

        final Attachment attachment = new Attachment.RedeemAttachment(address, secp_signatures);
        final Account fake_from = Account.getAccount(Genesis.REDEEM_ID);
        return createTransaction(req, fake_from, account_to, amountlong, attachment);

    }


    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}