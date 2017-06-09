/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017 The XEL Core Developers                                  ~
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

public final class Genesis {



    /* VOLATILE PART BEGIN */
    public static final byte[] CREATOR_PUBLIC_KEY = { (byte)0x1c, (byte)0x54, (byte)0x93, (byte)0xab, (byte)0xf0, (byte)0x9b, (byte)0xa9, (byte)0xe5, (byte)0x6f, (byte)0x47, (byte)0x78, (byte)0x2d, (byte)0x7c, (byte)0x8a, (byte)0x50, (byte)0xec, (byte)0xf8, (byte)0x68, (byte)0xad, (byte)0x88, (byte)0x89, (byte)0x59, (byte)0x83, (byte)0x79, (byte)0x45, (byte)0x59, (byte)0xb1, (byte)0x5b, (byte)0xbc, (byte)0x14, (byte)0x03, (byte)0x14, };
    public static final long CREATOR_ID = 7771402932088772036L;
    public static final byte[][] GENESIS_SIGNATURES = new byte[][] {
            {11 ,17 ,30 ,111 ,-50 ,-50 ,-102 ,23 ,-100 ,115 ,-5 ,27 ,-96 ,98 ,-105 ,34 ,-33 ,-87 ,-51 ,-17 ,101 ,46 ,27 ,116 ,24 ,-61 ,-81 ,-37 ,-29 ,-52 ,2 ,15 ,126 ,14 ,78 ,88 ,24 ,-27 ,-63 ,-54 ,17 ,39 ,-108 ,-127 ,-20 ,36 ,117 ,-91 ,17 ,32 ,-70 ,-64 ,-2 ,-95 ,84 ,-28 ,34 ,54 ,98 ,-25 ,-101 ,-95 ,103 ,36},
    }
            ;
    public static final long GENESIS_BLOCK_ID = 3604450856999086574L;
    public static final byte[] GENESIS_BLOCK_SIGNATURE = { (byte)0x36, (byte)0xaa, (byte)0x6b, (byte)0x40, (byte)0x8e, (byte)0x72, (byte)0x0e, (byte)0x39, (byte)0xb1, (byte)0x7d, (byte)0x60, (byte)0x1c, (byte)0x11, (byte)0x0a, (byte)0x67, (byte)0x14, (byte)0x4b, (byte)0x1f, (byte)0x94, (byte)0x41, (byte)0xeb, (byte)0x50, (byte)0xf0, (byte)0x43, (byte)0x75, (byte)0xbf, (byte)0xbe, (byte)0x37, (byte)0x20, (byte)0xaa, (byte)0x98, (byte)0x03, (byte)0x8b, (byte)0x48, (byte)0xb2, (byte)0x8f, (byte)0x90, (byte)0x19, (byte)0xaf, (byte)0x43, (byte)0x93, (byte)0x5a, (byte)0xe8, (byte)0x53, (byte)0xa8, (byte)0x75, (byte)0x2b, (byte)0x31, (byte)0xcd, (byte)0x10, (byte)0x54, (byte)0x68, (byte)0x3b, (byte)0xea, (byte)0x91, (byte)0x54, (byte)0x58, (byte)0xa3, (byte)0xd6, (byte)0xed, (byte)0x71, (byte)0xd2, (byte)0xd9, (byte)0xa5, };
    public static final String REDEEM_ID_PUBKEY = "bff397b6d0c491424bad98bcdfc9714aac858ce0accfaa792146ed40b8028c56";
    public static final long REDEEM_ID = 5434816099057631245L;
    public static final long[] GENESIS_RECIPIENTS = new long[]{Genesis.REDEEM_ID};
    public static final int[] GENESIS_AMOUNTS = new int[]{100000000};
    /* VOLATILE PART END */


    private Genesis() {} // never

}
