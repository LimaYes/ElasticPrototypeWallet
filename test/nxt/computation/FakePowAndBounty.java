package nxt.computation;

import nxt.IPowAndBounty;

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
public class FakePowAndBounty implements IPowAndBounty {
    @Override
    public IPowAndBounty getPreviousPow() {
        return null;
    }

    @Override
    public int getPowChainHeight() {
        return 0;
    }

    @Override
    public BigInteger myCurrentTarget() {
        return null;
    }

    @Override
    public int getTimestampReceived() {
        return 0;
    }
}
