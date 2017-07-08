/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

package nxt;

import nxt.db.DbIterator;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DebugTrace {

    static final String QUOTE = Nxt.getStringProperty("nxt.debugTraceQuote", "\"");
    static final String SEPARATOR = Nxt.getStringProperty("nxt.debugTraceSeparator", "\t");
    static final boolean LOG_UNCONFIRMED = Nxt.getBooleanProperty("nxt.debugLogUnconfirmed");

    static void init() {
        List<String> accountIdStrings = Nxt.getStringListProperty("nxt.debugTraceAccounts");
        String logName = Nxt.getStringProperty("nxt.debugTraceLog");
        if (accountIdStrings.isEmpty() || logName == null) {
            return;
        }
        Set<Long> accountIds = new HashSet<>();
        for (String accountId : accountIdStrings) {
            if ("*".equals(accountId)) {
                accountIds.clear();
                break;
            }
            accountIds.add(Convert.parseAccountId(accountId));
        }
        final DebugTrace debugTrace = addDebugTrace(accountIds, logName);
        Nxt.getBlockchainProcessor().addListener(block -> debugTrace.resetLog(), BlockchainProcessor.Event.RESCAN_BEGIN);
        Logger.logDebugMessage("Debug tracing of " + (accountIdStrings.contains("*") ? "ALL"
                : String.valueOf(accountIds.size())) + " accounts enabled");
    }

    public static DebugTrace addDebugTrace(Set<Long> accountIds, String logName) {
        final DebugTrace debugTrace = new DebugTrace(accountIds, logName);
        Account.addListener(account -> debugTrace.trace(account, false), Account.Event.BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addListener(account -> debugTrace.trace(account, true), Account.Event.UNCONFIRMED_BALANCE);
        }
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, true), Account.Event.LEASE_STARTED);
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, false), Account.Event.LEASE_ENDED);
        Nxt.getBlockchainProcessor().addListener(debugTrace::traceBeforeAccept, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT);
        Nxt.getBlockchainProcessor().addListener(debugTrace::trace, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY);
        Nxt.getTransactionProcessor().addListener(transactions -> debugTrace.traceRelease(transactions.get(0)), TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        return debugTrace;
    }

    //NOTE: first and last columns should not have a blank entry in any row, otherwise VerifyTrace fails to parse the line
    private static final String[] columns = {"height", "event", "account", "asset", "currency", "balance", "unconfirmed balance",
            "asset balance", "unconfirmed asset balance", "currency balance", "unconfirmed currency balance",
            "transaction amount", "transaction fee", "generation fee", "effective balance", "dividend",
            "order", "order price", "order quantity", "order cost",
            "offer", "buy rate", "sell rate", "buy units", "sell units", "buy cost", "sell cost",
            "trade price", "trade quantity", "trade cost",
            "exchange rate", "exchange quantity", "exchange cost", "currency cost",
            "crowdfunding", "claim", "mint",
            "asset quantity", "currency units", "transaction", "lessee", "lessor guaranteed balance",
            "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund",
            "shuffling",
            "sender", "recipient", "block", "timestamp"};

    private static final Map<String,String> headers = new HashMap<>();
    static {
        for (String entry : columns) {
            headers.put(entry, entry);
        }
    }

    private final Set<Long> accountIds;
    private final String logName;
    private PrintWriter log;

    private DebugTrace(Set<Long> accountIds, String logName) {
        this.accountIds = accountIds;
        this.logName = logName;
        resetLog();
    }

    void resetLog() {
        if (log != null) {
            log.close();
        }
        try {
            log = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logName)))), true);
        } catch (IOException e) {
            Logger.logDebugMessage("Debug tracing to " + logName + " not possible", e);
            throw new RuntimeException(e);
        }
        this.log(headers);
    }

    private boolean include(long accountId) {
        return accountId != 0 && (accountIds.isEmpty() || accountIds.contains(accountId));
    }


    private void trace(Account account, boolean unconfirmed) {
        if (include(account.getId())) {
            log(getValues(account.getId(), unconfirmed));
        }
    }

    private void trace(Account.AccountLease accountLease, boolean start) {
        if (! include(accountLease.getCurrentLesseeId()) && ! include(accountLease.getLessorId())) {
            return;
        }
        log(getValues(accountLease.getLessorId(), accountLease, start));
    }

    private void traceBeforeAccept(Block block) {
        long generatorId = block.getGeneratorId();
        if (include(generatorId)) {
            log(getValues(generatorId, block));
        }
        for (long accountId : accountIds) {
            Account account = Account.getAccount(accountId);
            if (account != null) {
                try (DbIterator<Account> lessors = account.getLessors()) {
                    while (lessors.hasNext()) {
                        log(lessorGuaranteedBalance(lessors.next(), accountId));
                    }
                }
            }
        }
    }

    private void trace(Block block) {
        for (Transaction transaction : block.getTransactions()) {
            long senderId = transaction.getSenderId();
            if (((TransactionImpl)transaction).attachmentIsPhased()) {
                if (include(senderId)) {
                    log(getValues(senderId, transaction, false, true, false));
                }
                continue;
            }
            if (include(senderId)) {
                log(getValues(senderId, transaction, false, true, true));
                log(getValues(senderId, transaction, transaction.getAttachment(), false));
            }
            long recipientId = transaction.getRecipientId();
            if (transaction.getAmountNQT() > 0 && recipientId == 0) {
                recipientId = Genesis.CREATOR_ID;
            }
            if (include(recipientId)) {
                log(getValues(recipientId, transaction, true, true, true));
                log(getValues(recipientId, transaction, transaction.getAttachment(), true));
            }
        }
    }

    private void traceRelease(Transaction transaction) {
        long senderId = transaction.getSenderId();
        if (include(senderId)) {
            log(getValues(senderId, transaction, false, false, true));
            log(getValues(senderId, transaction, transaction.getAttachment(), false));
        }
        long recipientId = transaction.getRecipientId();
        if (include(recipientId)) {
            log(getValues(recipientId, transaction, true, false, true));
            log(getValues(recipientId, transaction, transaction.getAttachment(), true));
        }
    }

    private Map<String,String> lessorGuaranteedBalance(Account account, long lesseeId) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(account.getId()));
        map.put("lessor guaranteed balance", String.valueOf(account.getGuaranteedBalanceNQT()));
        map.put("lessee", Long.toUnsignedString(lesseeId));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "lessor guaranteed balance");
        return map;
    }


    private Map<String,String> getValues(long accountId, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        Account account = Account.getAccount(accountId);
        map.put("balance", String.valueOf(account != null ? account.getBalanceNQT() : 0));
        map.put("unconfirmed balance", String.valueOf(account != null ? account.getUnconfirmedBalanceNQT() : 0));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", unconfirmed ? "unconfirmed balance" : "balance");
        return map;
    }


    private Map<String,String> getValues(long accountId, Transaction transaction, boolean isRecipient, boolean logFee, boolean logAmount) {
        long amount = transaction.getAmountNQT();
        long fee = transaction.getFeeNQT();
        if (isRecipient) {
            fee = 0; // fee doesn't affect recipient account
        } else {
            // for sender the amounts are subtracted
            amount = - amount;
            fee = - fee;
        }
        if (fee == 0 && amount == 0) {
            return Collections.emptyMap();
        }
        Map<String,String> map = getValues(accountId, false);
        if (logAmount) {
            map.put("transaction amount", String.valueOf(amount));
        }
        if (logFee) {
            map.put("transaction fee", String.valueOf(fee));
        }
        map.put("transaction", transaction.getStringId());
        if (isRecipient) {
            map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
        } else {
            map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
        }
        map.put("event", "transaction");
        return map;
    }

    private Map<String,String> getValues(long accountId, Block block) {
        long fee = block.getTotalFeeNQT();
        if (fee == 0) {
            return Collections.emptyMap();
        }
        long totalBackFees = 0;
        if (block.getHeight() > Constants.SHUFFLING_BLOCK) {
            long[] backFees = new long[3];
            for (Transaction transaction : block.getTransactions()) {
                long[] fees = ((TransactionImpl)transaction).getBackFees();
                for (int i = 0; i < fees.length; i++) {
                    backFees[i] += fees[i];
                }
            }
            for (int i = 0; i < backFees.length; i++) {
                if (backFees[i] == 0) {
                    break;
                }
                totalBackFees += backFees[i];
                long previousGeneratorId = BlockDb.findBlockAtHeight(block.getHeight() - i - 1).getGeneratorId();
                if (include(previousGeneratorId)) {
                    Map<String,String> map = getValues(previousGeneratorId, false);
                    map.put("effective balance", String.valueOf(Account.getAccount(previousGeneratorId).getEffectiveBalanceNXT()));
                    map.put("generation fee", String.valueOf(backFees[i]));
                    map.put("block", block.getStringId());
                    map.put("event", "block");
                    map.put("timestamp", String.valueOf(block.getTimestamp()));
                    map.put("height", String.valueOf(block.getHeight()));
                    log(map);
                }
            }
        }
        Map<String,String> map = getValues(accountId, false);
        map.put("effective balance", String.valueOf(Account.getAccount(accountId).getEffectiveBalanceNXT()));
        map.put("generation fee", String.valueOf(fee - totalBackFees));
        map.put("block", block.getStringId());
        map.put("event", "block");
        map.put("timestamp", String.valueOf(block.getTimestamp()));
        map.put("height", String.valueOf(block.getHeight()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountLease accountLease, boolean start) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("event", start ? "lease begin" : "lease end");
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("lessee", Long.toUnsignedString(accountLease.getCurrentLesseeId()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Transaction transaction, Attachment attachment, boolean isRecipient) {
        Map<String,String> map = getValues(accountId, false);
        if (attachment == Attachment.ARBITRARY_MESSAGE) {
            map = new HashMap<>();
            map.put("account", Long.toUnsignedString(accountId));
            map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
            map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
            map.put("event", attachment == Attachment.ARBITRARY_MESSAGE ? "message" : "encrypted message");
            if (isRecipient) {
                map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
            } else {
                map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
            }
        } else {
            return Collections.emptyMap();
        }
        return map;
    }

    private void log(Map<String,String> map) {
        if (map.isEmpty()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        for (String column : columns) {
            if (!LOG_UNCONFIRMED && column.startsWith("unconfirmed")) {
                continue;
            }
            String value = map.get(column);
            if (value != null) {
                buf.append(QUOTE).append(value).append(QUOTE);
            }
            buf.append(SEPARATOR);
        }
        log.println(buf.toString());
    }

}
