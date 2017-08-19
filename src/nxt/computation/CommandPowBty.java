package nxt.computation;

import com.community.Executor;
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
    private byte[] storage;
    private byte[] verificator;
    private boolean validated = false;
    private boolean isValid = false;

    public CommandPowBty(long work_id, boolean is_proof_of_work, byte[] multiplier, byte[] storage, byte[] verificator) {
        super();
        this.work_id = work_id;
        this.is_proof_of_work = is_proof_of_work;
        this.multiplier = multiplier;
        this.storage = storage;
        this.verificator = verificator;
    }

    CommandPowBty(ByteBuffer buffer) {
        super(buffer);
        try {
            this.work_id = buffer.getLong();
            this.is_proof_of_work = (buffer.get() == (byte) 0x01) ? true : false;

            // First read in the multiplicator
            short readsize = buffer.getShort();
            if (readsize != ComputationConstants.MULTIPLIER_LENGTH * 4) {
                throw new NxtException.NotValidException("Wrong Parameters");
            }
            multiplier = new byte[readsize];
            buffer.get(multiplier);

            // Then, read the storage ints
            readsize = buffer.getShort();
            if (!this.is_proof_of_work && readsize > ComputationConstants.BOUNTY_STORAGE_INTS * 4) {
                throw new NxtException.NotValidException("Wrong Parameters: too many storage ints");
            }
            if (this.is_proof_of_work && readsize != 0) {
                throw new NxtException.NotValidException("Wrong Parameters: PoW must not contain storage elements");
            }
            storage = new byte[readsize];
            buffer.get(storage);

            // And finally, read the verificator
            readsize = buffer.getShort();
            if (readsize > ComputationConstants.VERIFICATOR_INTS * 4) {
                throw new NxtException.NotValidException("Wrong Parameters");
            }
            verificator = new byte[readsize];
            buffer.get(verificator);
        } catch (Exception e) {
            e.printStackTrace(); // todo: remove for production
            // pass through any error
            this.work_id = 0;
            this.is_proof_of_work = false;
            this.multiplier = new byte[0];
            this.verificator = new byte[0];
            this.storage = new byte[0];
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
        return 8 + 1 + 2 + 2 + 2 + this.multiplier.length + this.verificator.length + this.storage.length;
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
        buffer.putShort((short)this.storage.length);
        buffer.put(this.storage);
        buffer.putShort((short)this.verificator.length);
        buffer.put(this.verificator);
    }

    public byte[] getMultiplier() {
        return multiplier;
    }

    public byte[] getStorage() {
        return storage;
    }

    public byte[] getVerificator() {
        return verificator;
    }

    private boolean validatePow(String vcode){
        int[] storage_array = Convert.byte2int(this.getStorage());
        int[] multiplier_array = Convert.byte2int(this.getStorage());
        int[] verificator_array = Convert.byte2int(this.getStorage());

        Executor.CODE_RESULT result = Executor.executeCode(vcode, multiplier_array, storage_array, verificator_array, true, new int[]{0,0});
        return result.pow;
    }
    private boolean validateBty(String vcode){
        int[] storage_array = Convert.byte2int(this.getStorage());
        int[] multiplier_array = Convert.byte2int(this.getStorage());
        int[] verificator_array = Convert.byte2int(this.getStorage());

        Executor.CODE_RESULT result = Executor.executeCode(vcode, multiplier_array, storage_array, verificator_array, false, new int[]{0,0});
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

        // Now check for duplicate entry (I guess storage hash is enough, isn't it?)
        byte[] myHash = this.getVerificatorHash();
        if(PowAndBounty.hasVerificatorHash(w.getId(), myHash))
            return false;

        byte[] myMultiplier = this.getMultiplier();
        if(PowAndBounty.hasMultiplier(w.getId(), myMultiplier))
            return false;


        if (multiplier.length != ComputationConstants.MULTIPLIER_LENGTH * 4) {
            return false;
        }
        if (!this.is_proof_of_work && storage.length > ComputationConstants.BOUNTY_STORAGE_INTS * 4) {
            return false;
        }
        if (this.is_proof_of_work && storage.length != 0) {
            return false;
        }

        // TODO: fix the following
        /*
        if (!this.is_proof_of_work && storage.length/4 != w.getStorage_size()) {
            return false;
        }*/

        if (verificator.length > ComputationConstants.VERIFICATOR_INTS * 4) {
            return false;
        }

        // TODO: Fix the following one
        /*if (verificator.length/4 != w.getVerificatorSize()) {
            return false;
        }*/

        // Validate code-level
        if (this.is_proof_of_work && !validatePow(w.getVerifyFunction())) {
            return false;
        }
        if (!this.is_proof_of_work && !validateBty(w.getVerifyFunction())) {
            return false;
        }

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
            dos.write(this.storage);
            dos.close();
        } catch (final IOException ignored) {

        }
        byte[] longBytes = baos.toByteArray();
        if (longBytes == null) longBytes = new byte[0];
        dig.update(longBytes);
        return dig.digest();
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
