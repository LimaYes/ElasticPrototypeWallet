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

import javax.servlet.http.HttpServletRequest;

import nxt.computation.CommandCancelWork;
import nxt.computation.MessageEncoder;
import org.json.simple.JSONStreamAware;
import nxt.NxtException;

import java.io.IOException;

public final class CancelWork extends CreateTransaction {

    static final CancelWork instance = new CancelWork();

    private CancelWork() {
        super(new APITag[] { APITag.CREATE_TRANSACTION }, "work_id");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {

        final long workId = ParameterParser.getUnsignedLong(req, "work_id", true);

        CommandCancelWork work = new CommandCancelWork(workId);

        try {
            MessageEncoder.push(work, ParameterParser.getSecretPhrase(req, true));
            return JSONResponses.EVERYTHING_ALRIGHT;
        } catch (IOException e) {
            return JSONResponses.ERROR_INCORRECT_REQUEST;
        }
    }

}
