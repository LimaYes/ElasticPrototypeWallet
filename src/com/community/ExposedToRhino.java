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

    public static final byte[] intToByteArray(int value)
    {
        return new byte[]  { (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
    }

    public double check_pow(int v0,int v1,int v2,int v3, int[] m, int[] target){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.write(intToByteArray(v0), 0, 4);
            baos.write(intToByteArray(v1), 0, 4);
            baos.write(intToByteArray(v2), 0, 4);
            baos.write(intToByteArray(v3), 0, 4);
            for (int i = 0; i < 10; i++) {
                baos.write(intToByteArray(m[i]), 0, 4);
            }
            byte[] fullByteArray = baos.toByteArray();

            byte[] ret = MessageDigest.getInstance("MD5").digest(fullByteArray);

            int[] hash32 = Convert.byte2int(ret);
            for (int i = 0; i < 4; i++) {
                if (hash32[i] > target[i])
                    return 0;
                else if (hash32[i] < target[i])
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
