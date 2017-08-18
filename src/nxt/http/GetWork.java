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

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Account;
import nxt.NxtException;
import nxt.Work;

public final class GetWork extends APIServlet.APIRequestHandler {

    static final GetWork instance = new GetWork();

    private GetWork() {
        super(new APITag[]{APITag.MESSAGES}, "account", "with_finished", "work_id");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {

        long just_account;
        try {
            final String readParam = ParameterParser.getParameterMultipart(req, "account");
            final BigInteger b = new BigInteger(readParam);
            just_account = b.longValue();
        } catch (final Exception e) {
            just_account = 0;
        }

        long wid_filter;
        try {
            final String readParam = ParameterParser.getParameterMultipart(req, "work_id");
            final BigInteger b = new BigInteger(readParam);
            wid_filter = b.longValue();
        } catch (final Exception e) {
            wid_filter = 0;
        }

        long storage_slot;
        try {
            final String readParam = ParameterParser.getParameterMultipart(req, "storage_id");
            final BigInteger b = new BigInteger(readParam);
            storage_slot = b.longValue();
        } catch (final Exception e) {
            storage_slot = -1;
        }


        boolean include_finished = false;
        try {
            final String readParam = ParameterParser.getParameterMultipart(req, "with_finished");
            final BigInteger b = new BigInteger(readParam);
            int res = b.intValue();
            if(res!=0){
                include_finished = true;
            }
        } catch (final Exception e) {
            include_finished = false;
        }

        boolean with_source = false;
        try {
            final String readParam = ParameterParser.getParameterMultipart(req, "with_source");
            final BigInteger b = new BigInteger(readParam);
            int res = b.intValue();
            if(res!=0){
                with_source = true;
            }
        } catch (final Exception e) {
            with_source = false;
        }

        final int firstIndex = ParameterParser.getFirstIndex(req);
        final int lastIndex = ParameterParser.getLastIndex(req);

        final List<Work> work = Work.getWork(just_account, include_finished, firstIndex, lastIndex, wid_filter);
        JSONArray work_packages = null;

        if(wid_filter == 0)
            work_packages = work.stream().map(Work::toJson).collect(Collectors.toCollection(JSONArray::new));
        else
            if(storage_slot == -1) {
                boolean finalWith_source = with_source;
                work_packages = work.stream().map((Work work2) -> Work.toJsonWithSource(work2, finalWith_source)).collect(Collectors.toCollection(JSONArray::new));
            }
            else {
                int finalStorage_slot = (int) storage_slot;
                boolean finalWith_source1 = with_source;
                work_packages = work.stream().map((Work work1) -> Work.toJsonWithStorage(work1, finalStorage_slot, finalWith_source1)).collect(Collectors.toCollection(JSONArray::new));
            }

        final JSONObject response = new JSONObject();
        response.put("work_packages", work_packages);
        return response;
    }
}
