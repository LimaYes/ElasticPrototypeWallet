package nxt.http;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.NxtException;
import nxt.Work;
import nxt.db.DbIterator;

public final class GetMineableWork extends APIServlet.APIRequestHandler {

	static final GetMineableWork instance = new GetMineableWork();

	private GetMineableWork() {
		super(new APITag[] { APITag.MESSAGES });
	}

	@Override
	protected JSONStreamAware processRequest(final HttpServletRequest req) throws NxtException {
		int n = ParameterParser.getInt(req,"n",1,10000, true);

		final JSONArray work_packages = new JSONArray();

		try (DbIterator<? extends Work> iterator = Work.getActiveWork(0, n)) {
			while (iterator.hasNext()) {
				final Work transaction = iterator.next();
				work_packages.add(Work.toJsonWithSource(transaction));
			}
		}

		final JSONObject response = new JSONObject();
		response.put("work_packages", work_packages);

		return response;

	}

}
