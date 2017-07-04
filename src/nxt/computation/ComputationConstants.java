package nxt.computation;

import nxt.Constants;

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

public class ComputationConstants {

    // maximum number of block for each open work package
    public static final int DEADLINE_MIN = 3;
    public static final int DEADLINE_MAX = 1440;


    public static final long XEL_POW_MIN = 1;
    public static final long XEL_POW_MAX = Constants.MAX_CURRENCY_TOTAL_SUPPLY;
    public static final long XEL_BTY_MIN = 1;
    public static final long XEL_BTY_MAX = Constants.MAX_CURRENCY_TOTAL_SUPPLY;
    public static final long BTY_PER_ITER_MIN = 1;
    public static final long BTY_PER_ITER_MAX = 10;
    public static final int ITER_MIN = 1;
    public static final int ITER_MAX = 100;
}
