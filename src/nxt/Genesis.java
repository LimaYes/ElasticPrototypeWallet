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
            {22 ,69 ,116 ,22 ,-13 ,106 ,-83 ,-121 ,-59 ,-112 ,-43 ,-10 ,42 ,-94 ,-100 ,43 ,-125 ,-118 ,77 ,-54 ,-12 ,104 ,11 ,-97 ,-11 ,-38 ,26 ,-77 ,75 ,54 ,-64 ,3 ,-29 ,117 ,93 ,58 ,98 ,-15 ,-91 ,-48 ,-70 ,-55 ,-97 ,-43 ,-86 ,-120 ,-94 ,30 ,23 ,-121 ,104 ,29 ,30 ,-111 ,-58 ,14 ,98 ,22 ,127 ,120 ,14 ,116 ,-100 ,80},
    }
            ;
/*
{"totalFeeNQT":0,"payloadLength":128,"previousBlock":"0","totalAmountNQT":10000000000000000,"generationSignature":"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000","generatorPublicKey":"1c5493abf09ba9e56f47782d7c8a50ecf868ad88895983794559b15bbc140314","payloadHash":"2788c370b8c83fde56f51d3a519def1a53a2911d05f0d76c50bded3b874a32fd","blockSignature":"0be949a41181b762d0c6e875bf1fd29806889f97f0e4a863caca640ecca59b02dfbafc3f430f0ffc6e1aef5c23bc87fd21c184294f05fa53a1cddf23db0862d8","transactions":[{"senderPublicKey":"1c5493abf09ba9e56f47782d7c8a50ecf868ad88895983794559b15bbc140314","signature":"16457416f36aad87c590d5f62aa29c2b838a4dcaf4680b9ff5da1ab34b36c003e3755d3a62f1a5d0bac99fd5aa88a21e1787681d1e91c60e62167f780e749c50","feeNQT":0,"type":0,"version":0,"ecBlockId":"0","attachment":{"version.OrdinaryPayment":0},"subtype":0,"amountNQT":10000000000000000,"recipient":"5434816099057631245","ecBlockHeight":0,"deadline":0,"timestamp":0}],"version":-1,"timestamp":0}
*/

    public static final long GENESIS_BLOCK_ID = -4677322122371699590L;
    public static final byte[] GENESIS_BLOCK_SIGNATURE = { (byte)0x0b, (byte)0xe9, (byte)0x49, (byte)0xa4, (byte)0x11, (byte)0x81, (byte)0xb7, (byte)0x62, (byte)0xd0, (byte)0xc6, (byte)0xe8, (byte)0x75, (byte)0xbf, (byte)0x1f, (byte)0xd2, (byte)0x98, (byte)0x06, (byte)0x88, (byte)0x9f, (byte)0x97, (byte)0xf0, (byte)0xe4, (byte)0xa8, (byte)0x63, (byte)0xca, (byte)0xca, (byte)0x64, (byte)0x0e, (byte)0xcc, (byte)0xa5, (byte)0x9b, (byte)0x02, (byte)0xdf, (byte)0xba, (byte)0xfc, (byte)0x3f, (byte)0x43, (byte)0x0f, (byte)0x0f, (byte)0xfc, (byte)0x6e, (byte)0x1a, (byte)0xef, (byte)0x5c, (byte)0x23, (byte)0xbc, (byte)0x87, (byte)0xfd, (byte)0x21, (byte)0xc1, (byte)0x84, (byte)0x29, (byte)0x4f, (byte)0x05, (byte)0xfa, (byte)0x53, (byte)0xa1, (byte)0xcd, (byte)0xdf, (byte)0x23, (byte)0xdb, (byte)0x08, (byte)0x62, (byte)0xd8, };
    public static final String REDEEM_ID_PUBKEY = "bff397b6d0c491424bad98bcdfc9714aac858ce0accfaa792146ed40b8028c56";
    public static final long REDEEM_ID = 5434816099057631245L;
    public static final long[] GENESIS_RECIPIENTS = new long[]{Genesis.REDEEM_ID};
    public static final int[] GENESIS_AMOUNTS = new int[]{100000000};
    /* VOLATILE PART END */


    private Genesis() {} // never

}
