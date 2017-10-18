package nxt.computation;

import com.community.Executor;
import com.community.ExposedToRhino;
import nxt.NxtException;
import nxt.PowAndBounty;
import nxt.Transaction;
import nxt.Work;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

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
    private byte[] multiplier;
    private byte[] hash;
    private byte[] verificator;
    private boolean validated = false;
    private boolean isValid = false;
    private int storage_bucket;

    public CommandPowBty(long work_id, boolean is_proof_of_work, byte[] multiplier, byte[] hash,  byte[]
            verificator, int storage_bucket) {
        super();
        this.work_id = work_id;
        this.is_proof_of_work = is_proof_of_work;
        this.multiplier = multiplier;
        this.hash = hash;
        this.storage_bucket = storage_bucket;
        this.verificator = verificator;
    }

    CommandPowBty(ByteBuffer buffer) {
        super(buffer);
        try {

            /* you will notice, that here only "upper bounds" are checked, not the exact correct storate/m/submit length.
            The reason for this is, that we not yet know the work id nor do we want any db access for Work retrieval here.
            The correct lengths will later be checked in verify, for now just stop some memory exhaustion attacks using simple upper bounds checks
             */

            this.work_id = buffer.getLong();

            this.is_proof_of_work = (buffer.get() == (byte) 0x01) ? true : false;

            // First read in the multiplicator
            short readsize = buffer.getShort();
            if (readsize != ComputationConstants.MULTIPLIER_LENGTH) {
                throw new NxtException.NotValidException("Wrong Parameters, your multiplier was " + readsize + " but " +
                        "should be " + ComputationConstants.MULTIPLIER_LENGTH);
            }
            multiplier = new byte[readsize];
            buffer.get(multiplier);


            // Then, read the pow_hash, must be empty for POW and MD5LEN for BTY
            readsize = buffer.getShort();
            if (!this.is_proof_of_work && readsize != ComputationConstants.MD5LEN) {
                throw new NxtException.NotValidException("Wrong Parameters: pow_hash must be MD5LEN size");
            }
            if (this.is_proof_of_work && readsize != 0) {
                throw new NxtException.NotValidException("Wrong Parameters: pow_hash must be empty");
            }
            hash = new byte[readsize];
            buffer.get(hash);


           this.storage_bucket = buffer.getInt();

            // And finally, read the verificator
            readsize = buffer.getShort();
            if (readsize > ComputationConstants.VERIFICATOR_INTS * 4) {
                throw new NxtException.NotValidException("Wrong Parameters: verificator/data length is too large");
            }

            verificator = new byte[readsize];
            buffer.get(verificator);
            System.out.println("POWBTY - About to decode " + this.storage_bucket);


        } catch (Exception e) {
            e.printStackTrace(); // todo: remove for production
            // pass through any error
            this.work_id = 0;
            this.is_proof_of_work = false;
            this.multiplier = new byte[0];
            this.verificator = new byte[0];
            this.hash = new byte[0];
            this.storage_bucket = 0;
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
        return "CommandPowBty";
    }

    @Override
    int getMySize() {
        return 8 + 1 + 2 + 2 + 2 + this.multiplier.length + this.verificator.length  + this.hash.length  + 4 /*storage bucket in t */;
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
        buffer.put((this.is_proof_of_work == true) ? (byte) 0x01 : (byte) 0x00);
        // Now put the "triade"
        buffer.putShort((short)this.multiplier.length);
        buffer.put(this.multiplier);
        buffer.putShort((short)this.hash.length);
        buffer.put(this.hash);
        buffer.putInt(this.storage_bucket);
        buffer.putShort((short)this.verificator.length);
        buffer.put(this.verificator);

        System.out.println("POWBTY - About to encode " + this.storage_bucket);
    }

    public byte[] getMultiplier() {
        return multiplier;
    }

    /*
    public byte[] getStorage() {
        return storage;
    }*/

    public int getStorage_bucket() {
        return storage_bucket;
    }

    public byte[] getVerificator() {
        return verificator;
    }

    private boolean validatePow(byte[] pubkey, long blockid, long workId, String vcode, int[] target){
        byte[] hash_array = this.getPowHash();
        byte[] multiplier_array = this.getMultiplier();
        int[] verificator_array = Convert.byte2int(this.getVerificator());

        Work w = Work.getWorkById(workId);
        int[] storage_array = null;
        if(this.storage_bucket != -1){
            storage_array = Work.getStorage(w, this.storage_bucket);
        }
        int validation_offset = w.getVerification_idx();

        Executor.CODE_RESULT result = Executor.executeCode(pubkey, blockid, workId, vcode, multiplier_array,
                 storage_array, verificator_array, validation_offset, true, target, hash_array);
        return result.pow;
    }
    private boolean validateBty(byte[] pubkey, long blockid, long workId, String vcode, int[] target){
        byte[] hash_array = this.getPowHash();
        byte[] multiplier_array = this.getMultiplier();
        int[] verificator_array = Convert.byte2int(this.getVerificator());

        Work w = Work.getWorkById(workId);
        int[] storage_array = null;
        if(this.storage_bucket != -1){
            storage_array = Work.getStorage(w, this.storage_bucket);
        }
        int validation_offset = w.getVerification_idx();

        Executor.CODE_RESULT result = Executor.executeCode(pubkey, blockid, workId, vcode, multiplier_array,
                storage_array, verificator_array, validation_offset, false, target, hash_array);
        return result.bty;
    }

    @Override
    boolean validate(Transaction transaction) {

        // This construction avoids multiple code-evaluations which are not really required
        if(validated) return isValid;
        validated = true;
        if (this.work_id == 0) return false;
        Work w = Work.getWork(this.work_id);
        if (w == null) return false;
        if (w.isClosed() == true) return false;

        // todo, check if double spending prevention has to rely on verificator hash as well? But if yes ... what do we do with same hashes (hash len = 0 is always the same)

        // Now check for duplicate entry (I guess verificator hash is enough, isn't it?)
        /*byte[] myHash = this.getVerificatorHash();
        if(PowAndBounty.hasVerificatorHash(w.getId(), myHash))
            return false;
        */

        byte[] myMultiplier = this.getMultiplier();
        if(PowAndBounty.hasMultiplier(w.getId(), myMultiplier)) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: multiplier already in database.");
            return false;
        }


        // checking multiplicator length requirements
        if (multiplier.length != ComputationConstants.MULTIPLIER_LENGTH) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: multiplier length is incorrect.");
            return false;
        }


        // checking pow_hash length requirements once again
        if (!this.is_proof_of_work && hash.length != ComputationConstants.MD5LEN) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: pow_hash length is incorrect");
            return false;
        }
        if (this.is_proof_of_work && hash.length != 0) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: pow_hash provided for a proof of work submission.");
            return false;
        }

        // todo: check if > or >= ... depending on whether bucket id 0 exists or not!

        // !! if storage size is larger than 0 this indicates the presence of a storage. Therefore, storage bucket must be in a valid range
        if((w.getStorage_size()>0) && (this.storage_bucket >= w.getBounty_limit_per_iteration() || this.storage_bucket < 0)) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: storage_bucket index exceeds bounds: got " + this.storage_bucket + " but limits were [0, " + w.getBounty_limit_per_iteration() + "].");
            return false;
        }

        // !! otherwise, if storage_size == 0, then no storage is there and storage_bucket must be -1
        if(w.getStorage_size()==0 && this.storage_bucket != -1) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: storage_bucket index must be -1 because there simply is no storage.");
            return false;
        }


        if (verificator.length/4 != w.getStorage_size()) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: the verificator / data length does not match the configured storage size (" + String.valueOf(verificator.length/4) + " != " + String.valueOf(w.getStorage_size()) + ").");
            return false;
        }

        int[] target = new int[]{-1,-1,-1,-1};

        // reset last pow cache so we safely can detect a faulty execution / abortion
        ExposedToRhino.lastCalculatedPowHash = null;

        // Validate code-level
        if (this.is_proof_of_work && !validatePow(transaction.getSenderPublicKey(), transaction.getBlockId(),
                work_id, w.getVerifyFunction(), target)) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: proof of work checks in code execution failed.");
            return false;
        }
        if (!this.is_proof_of_work && !validateBty(transaction.getSenderPublicKey(), transaction.getBlockId(),
                work_id, w.getVerifyFunction(), target)) {
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: bounty checks in code execution failed.");
            return false;
        }

        // At this point we have already called the "Exposed to Rhino function" which made sure t hat the POW hash is in the temporary static value. See if it matches
        // in case of a bounty submission
        if(this.is_proof_of_work==false && ExposedToRhino.lastCalculatedPowHash == null){
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: the pow hash could not be calculated in the last code execution run.");
            return false; // this should not happen at all!
        }
        if(this.is_proof_of_work==false && Arrays.equals(ExposedToRhino.lastCalculatedPowHash, this.hash) == false){
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification failed: supplied pow hash does not match the real one (" + Convert.toHexString(this.hash) + " != " + Convert.toHexString(ExposedToRhino.lastCalculatedPowHash) + ").");
            return false; // return false if the POW Hash does not match
        }


        if(this.is_proof_of_work)
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification succeeded: pow submission passed all checks.");
        else
            Logger.logInfoMessage("Work " + String.valueOf(w.getId()) + " verification succeeded: bty submission passed all checks.");


        isValid = true;
        return true;
    }

    @Override
    void apply(Transaction transaction) {
        if (!validate(transaction))
            return;
        // Here, apply the actual package
        Logger.logInfoMessage("processing pow-or-bty for work: id=" + Long.toUnsignedString(this.work_id));
        PowAndBounty.addPowBty(transaction, this);
    }

    public byte[] getVerificatorHash() {
        final MessageDigest dig = Crypto.sha256();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.write(this.verificator);
            dos.close();
        } catch (final IOException ignored) {

        }
        byte[] longBytes = baos.toByteArray();
        if (longBytes == null) longBytes = new byte[0];
        dig.update(longBytes);
        return dig.digest();
    }

    public byte[] getPowHash() {
        return this.hash;
    }
    public byte[] getHash() {
        final MessageDigest dig = Crypto.sha256();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeLong(this.work_id);
            dos.writeBoolean(this.is_proof_of_work); // distinguish between pow and bounty
            dos.write(getVerificatorHash());
            dos.close();
        } catch (final IOException ignored) {

        }
        byte[] longBytes = baos.toByteArray();
        if (longBytes == null) longBytes = new byte[0];
        dig.update(longBytes);
        return dig.digest();
    }
}
