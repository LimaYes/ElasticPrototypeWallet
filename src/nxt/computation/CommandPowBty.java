package nxt.computation;

import nxt.Transaction;
import nxt.Work;
import nxt.util.Logger;

import java.nio.ByteBuffer;

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

public class CommandPowBty extends IComputationAttachment {

    private long work_id;
    private boolean is_proof_of_work;

    public CommandPowBty(long work_id, boolean is_proof_of_work){
        super();
        this.work_id = work_id;
        this.is_proof_of_work = is_proof_of_work;
    }

    CommandPowBty(ByteBuffer buffer) {
        super(buffer);
        try {
            this.work_id = buffer.getLong();
            this.is_proof_of_work = (buffer.get()==(byte)0x01)?true:false;
        }catch(Exception e){
            // pass through any error
            this.work_id = 0;
            this.is_proof_of_work = false;
        }
    }

    public long getWork_id() {
        return work_id;
    }

    public boolean isIs_proof_of_work() {
        return is_proof_of_work;
    }

    @Override
    String getAppendixName() {
        return "CommandCancelWork";
    }

    @Override
    int getMySize() {
        return 8 + 1;
    }

    @Override
    byte getMyMessageIdentifier() {
        return CommandsEnum.POWBTY.getCode();
    }

    @Override
    public String toString() {
        return "Pow-or-bty for id " + Long.toUnsignedString(this.work_id);
    }

    @Override
    void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(this.work_id);
        buffer.put((this.is_proof_of_work==true)?(byte)0x01:(byte)0x00);
    }

    @Override
    boolean validate(Transaction transaction) {

        if (this.work_id == 0) return false;
        Work w = Work.getWork(this.work_id);
        if(w == null) return false;
        if(w.isClosed() == true) return false;

        // more validation

        return true;
    }

    @Override
    void apply(Transaction transaction) {
        if(!validate(transaction))
            return;
        // Here, apply the actual package
        Logger.logInfoMessage("processing pow-or-bty for work: id=" + Long.toUnsignedString(this.work_id));
        Work w = Work.getWork(this.work_id);
        if(w!=null && w.isClosed() == false){

            // Apply the Pow or BTY here

        }
    }
}
