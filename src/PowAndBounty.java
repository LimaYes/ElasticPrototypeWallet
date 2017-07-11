
package nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import nxt.computation.CommandPowBty;
import org.json.simple.JSONObject;

import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;

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


public final class PowAndBounty {

    public enum Event {
        POW_SUBMITTED, BOUNTY_SUBMITTED
    }

    private static final Listeners<PowAndBounty, Event> listeners = new Listeners<>();

    private static final DbKey.LongKeyFactory<PowAndBounty> powAndBountyDbKeyFactory = new DbKey.LongKeyFactory<PowAndBounty>(
            "id") {

        @Override
        public DbKey newKey(final PowAndBounty participant) {
            return participant.dbKey;
        }

    };

    private static final VersionedEntityDbTable<PowAndBounty> powAndBountyTable = new VersionedEntityDbTable<PowAndBounty>(
            "pow_and_bounty", PowAndBounty.powAndBountyDbKeyFactory) {

        @Override
        protected PowAndBounty load(final Connection con, final ResultSet rs, final DbKey dbKey) throws SQLException {
            return new PowAndBounty(rs, dbKey);
        }

        @Override
        protected void save(final Connection con, final PowAndBounty participant) throws SQLException {
            participant.save(con);
        }

    };


    static PowAndBounty addPowBty(final Transaction transaction, final CommandPowBty attachment) {
        PowAndBounty shuffling = new PowAndBounty(transaction, attachment);
        PowAndBounty.powAndBountyTable.insert(shuffling);
        PowAndBounty.listeners.notify(shuffling, (attachment.isIs_proof_of_work())?Event.POW_SUBMITTED:Event.BOUNTY_SUBMITTED);
    }




    public static boolean addListener(final Listener<PowAndBounty> listener, final Event eventType) {
        return PowAndBounty.listeners.addListener(listener, eventType);
    }


    public static DbIterator<PowAndBounty> getBounties(final long wid) {
        return PowAndBounty.powAndBountyTable.getManyBy(new DbClause.LongClause("work_id", wid)
                        .and(new DbClause.BooleanClause("is_pow", false)).and(new DbClause.BooleanClause("latest", true)), 0,
                -1, "");
    }

    public static DbIterator<PowAndBounty> getBounties(final long wid, final long aid) {
        return PowAndBounty.powAndBountyTable.getManyBy(new DbClause.LongClause("work_id", wid)
                .and(new DbClause.BooleanClause("is_pow", false)).and(new DbClause.LongClause("account_id", aid))
                .and(new DbClause.BooleanClause("latest", true)), 0, -1, "");
    }

    public static DbIterator<PowAndBounty> getLastBountiesRelevantForStorageGeneration(final long wid){
        // "Should" return the last X bounties (from the last repetition only)
        return PowAndBounty.powAndBountyTable.getManyBy(new DbClause.LongClause("work_id", wid)
                        .and(new DbClause.BooleanClause("is_pow", false)).and(new DbClause.BooleanClause("latest", true)), 0,
                Work.getWork(wid).getBounty_limit_per_iteration()-1, " ORDER BY height DESC");
    }


    static int getBountyCount(final long wid) {
        return PowAndBounty.powAndBountyTable
                .getCount(new DbClause.LongClause("work_id", wid).and(new DbClause.BooleanClause("is_pow", false)));
    }

    static int getPowCount(final long wid) {
        return PowAndBounty.powAndBountyTable
                .getCount(new DbClause.LongClause("work_id", wid).and(new DbClause.BooleanClause("is_pow", true)));
    }

    public static PowAndBounty getPowOrBountyById(final long id) {
        return PowAndBounty.powAndBountyTable.get(PowAndBounty.powAndBountyDbKeyFactory.newKey(id));
    }

    public static DbIterator<PowAndBounty> getPows(final long wid) {
        return PowAndBounty.powAndBountyTable.getManyBy(new DbClause.LongClause("work_id", wid)
                        .and(new DbClause.BooleanClause("is_pow", true)).and(new DbClause.BooleanClause("latest", true)), 0, -1,
                "");
    }

    public static DbIterator<PowAndBounty> getPows(final long wid, final long aid) {
        return PowAndBounty.powAndBountyTable.getManyBy(new DbClause.LongClause("work_id", wid)
                .and(new DbClause.BooleanClause("is_pow", true)).and(new DbClause.LongClause("account_id", aid))
                .and(new DbClause.BooleanClause("latest", true)), 0, -1, "");
    }

    static boolean hasHash(final long workId, final byte[] hash) {
        return PowAndBounty.powAndBountyTable
                .getCount(new DbClause.BytesClause("hash", hash).and(new DbClause.LongClause("work_id", workId))) > 0;
    }

    static void init() {
    }

    public static boolean removeListener(final Listener<PowAndBounty> listener, final Event eventType) {
        return PowAndBounty.listeners.removeListener(listener, eventType);
    }

    private final long id;
    private final long referenced_storage_height;
    private final boolean is_pow;
    private boolean too_late;
    private final long work_id;
    private final long accountId;
    private final DbKey dbKey;


    private final byte[] hash;
    private final byte[] storage_hash;

    public long getWork_id() {
        return work_id;
    }

    public long getId() {
        return id;
    }

    private PowAndBounty(final ResultSet rs, final DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.work_id = rs.getLong("work_id");
        this.referenced_storage_height = rs.getLong("referenced_storage_height");
        this.accountId = rs.getLong("account_id");
        this.is_pow = rs.getBoolean("is_pow");
        this.dbKey = dbKey;
        this.too_late = rs.getBoolean("too_late");
        this.hash = rs.getBytes("hash");
        this.storage_hash = rs.getBytes("storage_hash");
    }

    private PowAndBounty(final Transaction transaction, final CommandPowBty attachment) {
        this.id = transaction.getId();
        this.work_id = attachment.getWork_id();
        this.referenced_storage_height = attachment.getReferenced_storage_height();
        this.accountId = transaction.getSenderId();
        this.dbKey = PowAndBounty.powAndBountyDbKeyFactory.newKey(this.id);
        this.is_pow = attachment.isIs_proof_of_work();
        this.hash = attachment.getHash(); // FIXME TODO
        this.storage_hash = attachment.getStorage_hash();
        this.too_late = false;
    }

    public long getAccountId() {
        return this.accountId;
    }


    private void save(final Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement(
                "MERGE INTO pow_and_bounty (id, referenced_storage_height, too_late, work_id, hash, storage_hash, account_id, is_pow, "
                        + " height, latest) " + "KEY (id, height) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setLong(++i, this.referenced_storage_height);
            pstmt.setBoolean(++i, this.too_late);
            pstmt.setLong(++i, this.work_id);
            DbUtils.setBytes(pstmt, ++i, this.hash);
            DbUtils.setBytes(pstmt, ++i, this.storage_hash);
            pstmt.setLong(++i, this.accountId);
            pstmt.setBoolean(++i, this.is_pow);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public JSONObject toJsonObject() {
        final JSONObject response = new JSONObject();
        response.put("id", Convert.toUnsignedLong(this.id));
        final Transaction t = TransactionDb.findTransaction(this.id);
        response.put("storage_hash", Convert.toString(this.storage_hash, false));
        if (t != null) {
            response.put("date", Convert.toUnsignedLong(t.getTimestamp()));
            response.put("referenced_storage_height", Convert.toUnsignedLong(this.referenced_storage_height));
        } else response.put("error", "Transaction not found");
        return response;
    }



}