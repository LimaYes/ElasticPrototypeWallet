package nxt.enigmatests;

import com.community.EnigmaProgram;
import com.community.EnigmaVM;
import com.community.EnigmaStackElement;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Test;

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
public class EnigmaTests {

    @Test
    public void packTests(){
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString("445566"), 4));
            Assert.assertEquals(x1, "445566");
            System.out.println(x1);
        }
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString("0000445566"), 4));
            Assert.assertEquals(x1, "445566");
            System.out.println(x1);
        }
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString("00445566"), 4));
            Assert.assertEquals(x1, "445566");
            System.out.println(x1);
        }
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString("445566778899"), 4));
            Assert.assertEquals(x1, "66778899");
            System.out.println(x1);
        }
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString("0000"), 4));
            Assert.assertEquals(x1, "");
            System.out.println(x1);
        }
        {
            String x1 = Convert.toHexString(Convert.nullToEmptyPacked(Convert.parseHexString(""), 4));
            Assert.assertEquals(x1, "");
            System.out.println(x1);
        }

    }

    @Test
    public void Enigma1(){
        // 130101:       push 1
        // 130401020304: push 0102030405
        // 0b:           ul[1] = 0102030405
        // 130102:       push 2
        // 130101:       push 1
        // 01:           remove 1, and push ul[1]
        // 08:           u[2] = ul[1] & 0xFFFFFF
        String opcode = "130101130501020304050b1301021301010408";
        byte[] bytecode = Convert.parseHexString(opcode);
        EnigmaProgram p = new EnigmaProgram(bytecode, new int[10]);
        EnigmaVM.execute(p, true);

        // TODO: Add assert
    }

    @Test
    public void Enigma_Add_Two_Small_Unsigned(){
        // 1404020001:     push 1 as unsigned long
        // 14040102:     push 2 as unsigned long
        // 23:           add
        String opcode = "14040201001404010223";
        byte[] bytecode = Convert.parseHexString(opcode);
        EnigmaProgram p = new EnigmaProgram(bytecode, new int[10]);
        EnigmaVM.execute(p, true);
        EnigmaStackElement res = p.stackPop();
        Assert.assertTrue(res.getType() == EnigmaProgram.MEM_TARGET_STORE.UL && res.getLong() == 3);
    }

    @Test
    // From C Reference: 1.f + 20000001 (int) should result in 20000000.00
    public void Enigma_Strange_Float_Add_Test(){
        // 1405040000803f:         push 1 as float (float bytes are 3f800000, and 0000803f in BIG ENDIAN notation)
        // 140104012d3101:     push 20000001 as int (bytes 012d3101 in BIG ENDIAN)
        // 23:           add
        String opcode = "1405040000803f140104012d310123";
        byte[] bytecode = Convert.parseHexString(opcode);
        EnigmaProgram p = new EnigmaProgram(bytecode, new int[10]);
        EnigmaVM.execute(p, true);
        EnigmaStackElement res = p.stackPop();
        Assert.assertTrue(res.getType() == EnigmaProgram.MEM_TARGET_STORE.F && res.getFloat() == 20000.0f);
    }
}
