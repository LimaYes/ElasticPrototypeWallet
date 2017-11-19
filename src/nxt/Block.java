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

import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.List;

public interface Block {

    void setLocallyProcessed();

    int getVersion();

    long getId();

    String getStringId();

    byte[] getChecksum(int fromHeight, int toHeight);

    int getHeight();

    int getTimestamp();

    long getGeneratorId();

    byte[] getGeneratorPublicKey();

    long getPreviousBlockId();

    byte[] getPreviousBlockHash();

    long getNextBlockId();

    long getTotalAmountNQT();

    long getTotalFeeNQT();

    int getPayloadLength();

    byte[] getPayloadHash();

    List<? extends Transaction> getTransactions();

    byte[] getGenerationSignature();

    byte[] getBlockSignature();

    long getBaseTarget();

    long getPowTarget();

    int getPowLastMass();

    int getPowMass();

    long getTargetLastMass();

    long getTargetMass();

    void calculatePowTarget(int powCounter);

    BigInteger getCumulativeDifficulty();

    byte[] getBytes();

    JSONObject getJSONObject();

    long getPreviousBlockPowTarget();
}
