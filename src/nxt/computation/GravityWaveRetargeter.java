package nxt.computation;

import nxt.IPowAndBounty;
import nxt.PowAndBounty;

import java.math.BigInteger;

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
public class GravityWaveRetargeter {
    // Give the previous POW (which is the current POW prior adding a new POW) to get the target for the new pow!

    public static BigInteger calculate(IPowAndBounty prev) {
        /* original work done by evan duffield, modified for java and XEL POW packages */
        
        IPowAndBounty previousBlock = prev;
        int blockTime = ComputationConstants.TIME_PER_POW_TARGET_IN_SECONDS; //
        int nActualTimespan = 0;
        int lastBlockTime = 0;
        int blockCount = 0;
        BigInteger sumTargets = BigInteger.ZERO;

        if (previousBlock.getPowChainHeight() == 0 || previousBlock.getPowChainHeight() < 24) {
            // This is the first block or the height is < PastBlocksMin
            // Return minimal required work. (1e0ffff0)
            return ComputationConstants.MINIMAL_WORK_TARGET;
        }

        IPowAndBounty currentBlock = previousBlock;
        // loop over the past n blocks, where n == PastBlocksMax
        for (blockCount = 1; currentBlock != null && currentBlock.getPowChainHeight() > 0 && blockCount <= 24;
             blockCount++) {
            // Calculate average difficulty based on the blocks we iterate over in this for loop
            if (blockCount <= 24) {
                BigInteger currentTarget = currentBlock.myCurrentTarget();
                if (blockCount == 1) {
                    sumTargets = currentTarget;
                    sumTargets = sumTargets.add(currentTarget);
                } else {
                    sumTargets = sumTargets.add(currentTarget);
                }
            }

            // If this is the second iteration (LastBlockTime was set)
            if (lastBlockTime > 0) {
                // Calculate time difference between previous block and current block
                int currentBlockTime = currentBlock.getTimestampReceived();
                int diff = ((lastBlockTime) - (currentBlockTime));
                // Increment the actual timespan
                nActualTimespan += diff;
            }
            // Set lastBlockTime to the block time for the block in current iteration
            lastBlockTime = currentBlock.getTimestampReceived();
            currentBlock = currentBlock.getPreviousPow();
        }

        // darkTarget is the difficulty
        BigInteger darkTarget = sumTargets.divide(BigInteger.valueOf(blockCount));

        // nTargetTimespan is the time that the CountBlocks should have taken to be generated.
        int nTargetTimespan = (blockCount - 1) * 60 * blockTime;

        // Limit the re-adjustment to 3x or 0.33x
        // We don't want to increase/decrease diff too much.
        if (nActualTimespan < nTargetTimespan / 3.0)
            nActualTimespan = (int) ((double) nTargetTimespan / 3.0);
        if (nActualTimespan > nTargetTimespan * 3.0)
            nActualTimespan = (int) ((double) nTargetTimespan * 3.0);

        // Calculate the new difficulty based on actual and target timespan.
        BigInteger wew = darkTarget.multiply(BigInteger.valueOf(nActualTimespan));
        BigInteger aas = wew.divide(BigInteger.valueOf(nTargetTimespan));
        darkTarget = darkTarget.multiply(BigInteger.valueOf(nActualTimespan)).divide(BigInteger.valueOf(nTargetTimespan));


        // Return the new diff.
        return darkTarget;
    }
}
