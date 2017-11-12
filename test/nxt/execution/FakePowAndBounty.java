package nxt.execution;

import nxt.IPowAndBounty;
import nxt.computation.ComputationConstants;
import nxt.computation.GravityWaveRetargeter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

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

public class FakePowAndBounty implements IPowAndBounty {

    private IPowAndBounty previousPow = null;
    public int height = 0;
    public int timestamp = 0;
    private BigInteger powTarget = null;

    private void updatePow(){
        int blkt = 0;
        double fct = 0;

        if(this.previousPow==null){
            this.powTarget = GravityWaveRetargeter.calculate(null);
            this.timestamp = (int)(System.currentTimeMillis() / 1000L);
        }else{
            this.powTarget = GravityWaveRetargeter.calculate(this.previousPow);
            BigDecimal p = new BigDecimal(this.powTarget);
            //BigDecimal m = new BigDecimal(new BigInteger("0000000AFFFFFFFFFFFFFFFFFFFFFFFF", 16));
            BigDecimal m = new BigDecimal(ComputationConstants.MAXIMAL_WORK_TARGET.divide(BigInteger.ONE.add(BigInteger.ONE)));
            double tgt = 20;
            BigDecimal factor = p.divide(m, 2, RoundingMode.HALF_UP);
            fct = factor.doubleValue();
            tgt = tgt / fct;
            this.timestamp = this.getPreviousPow().getTimestampReceived() + (int)tgt;
            blkt = (int)tgt;
        }
        System.out.println("#" + this.height + ": blk-time = " + (blkt) + ", currently we are " + fct + "x too fast, tgt = " + this.powTarget.toString(16));
    }

    public FakePowAndBounty(IPowAndBounty previousPow, int height) {
        this.previousPow = previousPow;
        this.height = height;
        updatePow();
    }

    public FakePowAndBounty(IPowAndBounty previousPow) {
        this.previousPow = previousPow;
        if(previousPow!= null)
            this.height = previousPow.getPowChainHeight() + 1;
        else
            this.height = 0;
        updatePow();
    }

    @Override
    public IPowAndBounty getPreviousPow() {
        return this.previousPow;
    }

    @Override
    public int getPowChainHeight() {
        return this.height;
    }

    @Override
    public BigInteger myCurrentTarget() {
        return this.powTarget;
    }

    @Override
    public int getTimestampReceived() {
        return this.timestamp;
    }
}
