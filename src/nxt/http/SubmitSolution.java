package nxt.http;

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

import nxt.NxtException;
import nxt.computation.CommandCancelWork;
import nxt.computation.CommandPowBty;
import nxt.computation.MessageEncoder;
import org.json.simple.JSONStreamAware;
import org.spongycastle.crypto.digests.SkeinEngine;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public final class SubmitSolution extends CreateTransaction {

    static final SubmitSolution instance = new SubmitSolution();

    private SubmitSolution() {
        super(new APITag[] { APITag.CREATE_TRANSACTION }, "work_id", "data","multiplicator","storage_id","is_pow","hash");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {

        final long workId = ParameterParser.getUnsignedLong(req, "work_id", true);
        final byte[] data = ParameterParser.getBytes(req, "data", true);
        final byte[] multiplicator = ParameterParser.getBytes(req, "multiplicator", true);
        final int storageId = ParameterParser.getInt(req, "storage_id",0,Integer.MAX_VALUE, true);
        final boolean is_pow = ParameterParser.getBooleanByString(req, "is_pow", true);
        byte[] hash = ParameterParser.getBytes(req, "hash", false);

        if(is_pow == true)
            hash = new byte[0];

        CommandPowBty work = new CommandPowBty(workId, is_pow, multiplicator, hash, data, storageId);

        try {
            MessageEncoder.push(work, ParameterParser.getSecretPhrase(req, true));
            return JSONResponses.EVERYTHING_ALRIGHT;
        } catch (IOException e) {
            return JSONResponses.ERROR_INCORRECT_REQUEST;
        }
    }

}
