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
import nxt.Redeem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class GetUnclaimedRedeems extends APIServlet.APIRequestHandler {

    static final GetUnclaimedRedeems instance = new GetUnclaimedRedeems();

    private GetUnclaimedRedeems() {
        super(new APITag[] { APITag.ACCOUNTS, APITag.TRANSACTIONS }, "account", "timestamp", "type", "subtype", "firstIndex",
                "lastIndex", "numberOfConfirmations", "withMessage");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {

        final JSONArray redeems = new JSONArray();
        int bound = Redeem.listOfAddresses.length;
        for (int i = 0; i < bound; i++) {
            if (!Redeem.isAlreadyRedeemed(Redeem.listOfAddresses[i])) {
                String l = new String(String.valueOf(i) + "," + Redeem.listOfAddresses[i] + ","
                        + String.valueOf(Redeem.amounts[i]).replace("L", ""));
                redeems.add(l);
            }
        }

        final JSONObject response = new JSONObject();
        response.put("redeems", redeems);
        return response;

    }

}