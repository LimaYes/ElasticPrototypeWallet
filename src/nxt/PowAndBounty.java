package nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import nxt.computation.CommandPowBty;

import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;
import nxt.util.Logger;

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


    public static void addPowBty(final Transaction transaction, final CommandPowBty attachment) {
        PowAndBounty shuffling = new PowAndBounty(transaction, attachment);
        PowAndBounty.powAndBountyTable.insert(shuffling); // store immedeately!

        // Here check if it is counting or if it is "old"
        Work w = Work.getWork(attachment.getWork_id());
        if(w == null ||w.isClosed()){
            return; // just another line of defense
        }

        // Now the work itself has to be manipulated (and close if necessary)
        if(attachment.isIs_proof_of_work())
        {
            // logic for PoW
            w.setReceived_pows(w.getReceived_pows() + 1);
            System.out.println("Work already got " + w.getReceived_pows() + " of " + w.getCap_number_pow() + " POWS!");

            // Close work if enough bounties were received
            int cap = w.getCap_number_pow();
            if(w.getReceived_pows() == cap){
                w.setClosed(true);
            }

            w.EmitPow();
            w.JustSave();
        }
        else
        {
            // logic for BTY
            w.setReceived_bounties(w.getReceived_bounties() + 1);


            // Close work if enough bounties were received
            int cap = w.getIterations() * w.getBounty_limit_per_iteration();
            if(w.getReceived_bounties() == cap){
                w.setClosed(true);
            }


            w.EmitBty();
            w.JustSave(); // we will have to save again when storage gets consolidated in the next step! Do it more elegant later on


            // In all cases (even after close case) make sure the combined storage is updated properly!
            if(w.getReceived_bounties()%w.getBounty_limit_per_iteration()==0){

                int cntr = 0;
                String xxxx = Arrays.toString(w.getCombined_storage());
                byte[] fullstorage = new byte[w.getBounty_limit_per_iteration()*w.getStorage_size()*4];
                try(DbIterator<PowAndBounty> it = getLastBountiesRelevantForStorageGeneration(w.getId())){
                    while(it.hasNext()){
                        PowAndBounty n = it.next();
                        byte[] storage = n.multiplier_or_storage;
                        for(int i=0;i<storage.length;++i){
                            fullstorage[cntr*w.getStorage_size()*4+i] = storage[i];
                        }
                        cntr++;
                    }
                    w.setCombined_storage(Convert.byte2int(fullstorage));
                    w.JustSave();
                }



                Logger.logDebugMessage("Consolidating storage for job " + w.getId() + " after " + w.getReceived_bounties() + " bounties. [processing cntr = " + cntr + "]");
                Logger.logDebugMessage("Full Storage: before: " + xxxx + ", after: " + Arrays.toString(Convert.byte2int(fullstorage)));
            }



        }
        PowAndBounty.listeners.notify(shuffling, (shuffling.is_pow)?Event.POW_SUBMITTED:Event.BOUNTY_SUBMITTED);
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

    public static boolean hasHash(final byte[] hash) {
        return PowAndBounty.powAndBountyTable
                .getCount(new DbClause.BytesClause("hash", hash)) > 0;
    }


    // storage hash linked to wid only
    public static boolean hasStorageHash(long workId, byte[] hash) {
        return PowAndBounty.powAndBountyTable
                .getCount(new DbClause.BytesClause("storage_hash", hash).and(new DbClause.LongClause("work_id",workId))) > 0;
    }


    static void init() {
    }

    public static boolean removeListener(final Listener<PowAndBounty> listener, final Event eventType) {
        return PowAndBounty.listeners.removeListener(listener, eventType);
    }

    private final long id;
    private final boolean is_pow;
    private boolean too_late;
    private final long work_id;
    private final long accountId;
    private final DbKey dbKey;
    private final byte[] hash;
    private final byte[] storage_hash;
    private final byte[] multiplier_or_storage;


    public long getWork_id() {
        return work_id;
    }

    public long getId() {
        return id;
    }

    private PowAndBounty(final ResultSet rs, final DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.work_id = rs.getLong("work_id");
        this.accountId = rs.getLong("account_id");
        this.is_pow = rs.getBoolean("is_pow");
        this.dbKey = dbKey;
        this.too_late = rs.getBoolean("too_late");
        this.hash = rs.getBytes("hash");
        this.storage_hash = rs.getBytes("hash");
        this.multiplier_or_storage = rs.getBytes("multiplier_or_storage");
    }

    private PowAndBounty(final Transaction transaction, final CommandPowBty attachment) {
        this.id = transaction.getId();
        this.work_id = attachment.getWork_id();
        this.accountId = transaction.getSenderId();
        this.dbKey = PowAndBounty.powAndBountyDbKeyFactory.newKey(this.id);
        this.is_pow = attachment.isIs_proof_of_work();
        this.hash = attachment.getHash();
        this.storage_hash = attachment.getStorageHash();
        this.multiplier_or_storage = attachment.getMultiplier_or_storage();
        this.too_late = false;
    }

    public long getAccountId() {
        return this.accountId;
    }


    private void save(final Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement(
                "MERGE INTO pow_and_bounty (id, too_late, work_id, hash, multiplier_or_storage, account_id, is_pow, storage_hash, "
                        + " height, latest) " + "KEY (id, height) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setBoolean(++i, this.too_late);
            pstmt.setLong(++i, this.work_id);
            DbUtils.setBytes(pstmt, ++i, this.hash);
            DbUtils.setBytes(pstmt, ++i, this.multiplier_or_storage);
            pstmt.setLong(++i, this.accountId);
            pstmt.setBoolean(++i, this.is_pow);
            DbUtils.setBytes(pstmt, ++i, this.storage_hash);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }
}