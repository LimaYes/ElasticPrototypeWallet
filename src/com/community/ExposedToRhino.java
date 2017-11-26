package com.community;

import nxt.util.Convert;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

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
public class ExposedToRhino {

    public static byte[] lastCalculatedPowHash = null;

    public static final byte[] intToByteArray(int value)
    {
        return new byte[]  { (byte)(value&0xff), (byte)((value >> 8) & 0xff), (byte)((value >> 16) & 0xff), (byte)((value >>24) & 0xff) };
    }

    public double check_pow(int v0,int v1,int v2,int v3, int[] m, int[] target){

        System.out.println("GOT ARGUMENTS TO CHECKPOW: " + Integer.toHexString(v0) + ", " + Integer.toHexString(v1) + ", " + Integer.toHexString(v2) + ", " + Integer.toHexString(v3) + ", " + ", ...");
        System.out.println("FIRST M HEXS: " + Integer.toHexString(m[0]) + ", " + Integer.toHexString(m[1]) + ", " + Integer.toHexString(m[2]) + ", " + Integer.toHexString(m[3]) + ", " + ", ...");
        System.out.println("FIRST M INTS: " + Integer.toString(m[0]) + ", " + Integer.toString(m[1]) + ", " + Integer.toString(m[2]) + ", " + Integer.toString(m[3]) + ", " + ", ...");

        System.out.println("FIRST TGT INTS: " + target[0] + ", " + target[1] + ", " + target[2] + ", " + target[3] + ", " + ", ...");
        if(target.length!=4) return 0.0; // Disallow crap here

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // todo: check if it matches with xel_compiler.c:102 and if off + len are correct
            baos.write(intToByteArray(v0));
            baos.write(intToByteArray(v1));
            baos.write(intToByteArray(v2));
            baos.write(intToByteArray(v3));


            for (int i = 0; i < 8; i++) {
                baos.write(intToByteArray(m[i]));
            }
            byte[] fullByteArray = baos.toByteArray();

            // TODO: SOMEHOW THIS ROUTINE ALLOWS MULTIPLE SUBMISSIONS WITH THE SAME POW HASH! THIS SHOULD BE AVOIDED AT ALL COSTS!!!
            System.out.println("INPUT FOR HASH CALCULATION:");

            byte[] ret = MessageDigest.getInstance("MD5").digest(fullByteArray);

            System.out.println("MD5 Debug:");
            System.out.println("===================================");
            System.out.println("Inp: " + Convert.toHexString(fullByteArray));
            System.out.println("Out: " + Convert.toHexString(ret));

            lastCalculatedPowHash = ret;

            int[] hash32 = Convert.byte2int(ret);
            // We need to swap that to match xel_miner which uses some strange endianness swap on its pow_hash
            for (int i = 0; i < 4; i++)
                hash32[i] = Convert.swap(hash32[i]);

            System.out.println("POW-Hash: " + Convert.toHexString(Convert.int2byte(hash32)) + " (swapped version as seen on xel_miner)");

            // todo: remove for production
            System.out.println("\nDifficulty Checks (hash vs target):");
            System.out.println("===================================");
            for (int i = 0; i < 4; i++)
                System.out.println(Integer.toHexString(hash32[i]) + "\t" + Integer.toHexString(target[i]));
            System.out.println("\n");



            for (int i = 0; i < 4; i++) {
                int res = Integer.compareUnsigned(hash32[i], target[i]);
                if (res > 0)
                    return 0;
                else if (res < 0)
                    return 1;    // POW Solution Found
            }
        }
        catch(Exception e){
            e.printStackTrace(); // todo: remove for production
            return 0.0; // assume it failed
        }
        return 0.0; // If the unlikely case occurs, that hash = target then just return 0. I know, this is a false negative, but this cas will never happen anyway!
    }
}
