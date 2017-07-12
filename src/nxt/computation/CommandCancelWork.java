package nxt.computation;

import nxt.Transaction;
import nxt.Work;
import nxt.util.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static nxt.computation.ComputationConstants.MAX_UNCOMPRESSED_WORK_SIZE;

// TODO: Check the entire file for unhandled exceptions

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

public class CommandCancelWork extends IComputationAttachment {

    private long cancel_work_id;

    public CommandCancelWork(long cancel_work_id){
        super();
        this.cancel_work_id = cancel_work_id;
    }

    CommandCancelWork(ByteBuffer buffer) {
        super(buffer);
        byte compressed_or_not = 0; // assume false
        try {
            this.cancel_work_id = buffer.getLong();
        }catch(Exception e){
            // pass through any error
            this.cancel_work_id = 0;
        }
    }

    @Override
    String getAppendixName() {
        return "CommandCancelWork";
    }

    @Override
    int getMySize() {
        return 8;
    }

    @Override
    byte getMyMessageIdentifier() {
        return CommandsEnum.CANCEL_WORK.getCode();
    }

    @Override
    public String toString() {
        return "Cancel-work-request for id " + Long.toUnsignedString(this.cancel_work_id);
    }

    @Override
    void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(this.cancel_work_id);
    }

    @Override
    boolean validate(Transaction transaction) {

        if (this.cancel_work_id == 0) return false;
        Work w = Work.getWork(this.cancel_work_id);
        if(w == null) return false;
        if(w.isClosed() == true) return false;

        if(transaction.getSenderId() != w.getSender_account_id()){
            // only the author can close jobs
            return false;
        }

        return true;
    }

    @Override
    void apply(Transaction transaction) {

        if(!validate(transaction))
            return;

        // Here, apply the actual package
        Logger.logInfoMessage("cancelling work: id=" + Long.toUnsignedString(this.cancel_work_id));
        Work w = Work.getWork(this.cancel_work_id);
        if(w!=null){
            w.CloseManual(transaction.getBlock());
        }
    }
}
