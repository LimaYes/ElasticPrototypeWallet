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
    public static final byte[] CREATOR_PUBLIC_KEY = { (byte)0x87, (byte)0xe3, (byte)0xa4, (byte)0xb7, (byte)0x54, (byte)0x25, (byte)0x93, (byte)0x4b, (byte)0xbe, (byte)0x65, (byte)0x92, (byte)0x4e, (byte)0x5a, (byte)0x82, (byte)0x08, (byte)0x3a, (byte)0x9d, (byte)0xee, (byte)0x87, (byte)0xd3, (byte)0x77, (byte)0x26, (byte)0xf0, (byte)0xdc, (byte)0x74, (byte)0x68, (byte)0x28, (byte)0x2f, (byte)0x8d, (byte)0x73, (byte)0x78, (byte)0x36, };
    public static final long CREATOR_ID = 3823739230626990176L;
    public static final byte[][] GENESIS_SIGNATURES = new byte[][] {
            {18 ,-41 ,96 ,-99 ,38 ,85 ,-41 ,-8 ,30 ,-57 ,-116 ,-113 ,-45 ,-117 ,-21 ,82 ,-55 ,-67 ,31 ,21 ,102 ,53 ,118 ,93 ,-10 ,-112 ,64 ,71 ,15 ,104 ,79 ,10 ,96 ,58 ,-7 ,-83 ,39 ,-48 ,-47 ,60 ,-64 ,-76 ,-68 ,50 ,110 ,-120 ,15 ,-118 ,89 ,92 ,13 ,34 ,107 ,-41 ,-98 ,20 ,-2 ,-63 ,67 ,-41 ,-68 ,-60 ,-64 ,-44},
    }
            ;
    public static final long GENESIS_BLOCK_ID = 7861135490416963987L;
    public static final byte[] GENESIS_BLOCK_SIGNATURE = { (byte)0xc0, (byte)0xaf, (byte)0x81, (byte)0xdb, (byte)0xee, (byte)0x1a, (byte)0xfd, (byte)0xd5, (byte)0x30, (byte)0x76, (byte)0xab, (byte)0x82, (byte)0x46, (byte)0x4b, (byte)0x1f, (byte)0x9f, (byte)0x53, (byte)0x56, (byte)0xec, (byte)0x8c, (byte)0xb6, (byte)0x60, (byte)0xb4, (byte)0xea, (byte)0x95, (byte)0xdd, (byte)0x90, (byte)0x5c, (byte)0xd0, (byte)0xbc, (byte)0x59, (byte)0x04, (byte)0x1f, (byte)0x37, (byte)0x65, (byte)0x4f, (byte)0xdd, (byte)0x29, (byte)0x35, (byte)0x8c, (byte)0x8e, (byte)0xe7, (byte)0x72, (byte)0x6e, (byte)0x71, (byte)0xeb, (byte)0xdd, (byte)0x66, (byte)0x81, (byte)0x02, (byte)0x06, (byte)0x86, (byte)0x6d, (byte)0xa2, (byte)0x02, (byte)0xba, (byte)0x7f, (byte)0x0c, (byte)0x85, (byte)0xaa, (byte)0xa6, (byte)0xdd, (byte)0x30, (byte)0xd0, };
    public static final String REDEEM_ID_PUBKEY = "c52c96e414d0b624f8e5594407e6e3bb1584bc6c7287f5cd54371eb3b4e2cf7c";
    public static final long REDEEM_ID = 5409094869359815324L;
    public static final long[] GENESIS_RECIPIENTS = new long[]{Genesis.REDEEM_ID};
    public static final int[] GENESIS_AMOUNTS = new int[]{100000000};
    /* VOLATILE PART END */


    private Genesis() {} // never

}
