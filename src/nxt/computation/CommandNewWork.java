package nxt.computation;

import nxt.*;
import nxt.util.Convert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

public class CommandNewWork extends IComputationAttachment {

    private final short deadline;

    private final long xelPerPow;
    private final long xelPerBounty;

    private final int bountiesPerIteration;
    private final int numberOfIterations;

    private byte[] sourceCode;
    private byte[] sourceCodeCompressed;

    boolean validated = false;

    private boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }


    public CommandNewWork(short deadline, long xelPerPow, long xelPerBounty, int bountiesPerIteration, int numberOfIterations, byte[] sourceCode){
        super();
        this.deadline = deadline;
        this.xelPerPow = xelPerPow;
        this.xelPerBounty = xelPerBounty;
        this.bountiesPerIteration = bountiesPerIteration;
        this.numberOfIterations = numberOfIterations;
        this.sourceCode = sourceCode;

        try {
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(sourceCode);
            gzip.flush();
            gzip.close();
            sourceCodeCompressed = obj.toByteArray();
        }catch(Exception e){
            sourceCodeCompressed = null;
        }
    }

    public CommandNewWork(short deadline, long xelPerPow, long xelPerBounty, int bountiesPerIteration, int numberOfIterations, String sourceCode){
        super();
        this.deadline = deadline;
        this.xelPerPow = xelPerPow;
        this.xelPerBounty = xelPerBounty;
        this.bountiesPerIteration = bountiesPerIteration;
        this.numberOfIterations = numberOfIterations;
        this.sourceCode = Convert.toBytes(sourceCode);
        try {
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(sourceCode.getBytes("UTF-8"));
            gzip.flush();
            gzip.close();
            sourceCodeCompressed = obj.toByteArray();
        }catch(Exception e){
            sourceCodeCompressed = null;
        }
    }

    CommandNewWork(ByteBuffer buffer) {
        super(buffer);

        byte compressed_or_not = buffer.get();

        this.deadline = buffer.getShort();
        this.xelPerPow = buffer.getLong();
        this.xelPerBounty = buffer.getLong();
        this.bountiesPerIteration = buffer.getInt();
        this.numberOfIterations = buffer.getInt();

        if(compressed_or_not == 0) {
            this.sourceCode = new byte[buffer.getShort()];
            buffer.get(this.sourceCode);
            try {
                ByteArrayOutputStream obj = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(obj);
                gzip.write(sourceCode);
                gzip.flush();
                gzip.close();
                sourceCodeCompressed = obj.toByteArray();
            }catch(Exception e){
                sourceCodeCompressed = null;
            }
        }else{
            this.sourceCodeCompressed = new byte[buffer.getShort()];
            buffer.get(this.sourceCodeCompressed);
            if ((this.sourceCodeCompressed == null) || (this.sourceCodeCompressed.length == 0)) return;
            try {
                if (isCompressed(this.sourceCodeCompressed)) {
                    GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(this.sourceCodeCompressed));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
                    String outStr = "";
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        outStr += line;
                    }
                    this.sourceCode = outStr.getBytes("UTF-8");
                }
            }catch(Exception e){
                this.sourceCode = null;
            }
        }
    }

    @Override
    String getAppendixName() {
        return "CommandNewWork";
    }

    @Override
    int getMySize() {
        int min = 0;
        if(sourceCodeCompressed==null)
            min = this.sourceCode.length;
        else
            min = (this.sourceCode.length<this.sourceCodeCompressed.length)?this.sourceCode.length:this.sourceCodeCompressed.length;
        return 1+2+8+8+4+4+2+min;
    }

    @Override
    byte getMyMessageIdentifier() {
        return CommandsEnum.CREATE_NEW_WORK.getCode();
    }

    @Override
    public String toString() {
        return "Test (DL: " + this.deadline + "): " + new String(sourceCode);
    }

    @Override
    void putMyBytes(ByteBuffer buffer) {

        boolean compressed = false;
        if(this.sourceCodeCompressed != null && this.sourceCodeCompressed.length<this.sourceCode.length){
            compressed = true;
        }
        buffer.put((byte) (compressed ? 0x01 : 0x00));
        buffer.putShort(this.deadline);
        buffer.putLong(this.xelPerPow);
        buffer.putLong(this.xelPerBounty);
        buffer.putInt(this.bountiesPerIteration);
        buffer.putInt(this.numberOfIterations);

        if(compressed){
            buffer.putShort((short) this.sourceCodeCompressed.length);
            buffer.put(this.sourceCodeCompressed);
        }else {
            buffer.putShort((short) this.sourceCode.length);
            buffer.put(this.sourceCode);
        }
    }

    @Override
    boolean validate(Transaction transaction) {

        if ((this.sourceCode == null) || (this.sourceCode.length == 0)) return false;

        if(!Commons.checkRange(ComputationConstants.DEADLINE_MIN, ComputationConstants.DEADLINE_MAX, this.deadline))
            return false;

        if(!Commons.checkRange(ComputationConstants.XEL_POW_MIN, ComputationConstants.XEL_POW_MAX, this.xelPerPow))
            return false;

        if(!Commons.checkRange(ComputationConstants.XEL_BTY_MIN, ComputationConstants.XEL_BTY_MAX, this.xelPerBounty))
            return false;

        if(!Commons.checkRange(ComputationConstants.BTY_PER_ITER_MIN, ComputationConstants.BTY_PER_ITER_MAX, this.bountiesPerIteration))
            return false;

        if(!Commons.checkRange(ComputationConstants.ITER_MIN, ComputationConstants.ITER_MAX, this.numberOfIterations))
            return false;

        validated = true;

        return true;
    }

    @Override
    void apply(Transaction transaction, Account senderAccount) {
        if ((this.sourceCode == null) || (this.sourceCode.length == 0)) return;

        if(!validated){
            if(!validate(transaction))
                return;
        }

        // Here, apply the actual package
    }
}
