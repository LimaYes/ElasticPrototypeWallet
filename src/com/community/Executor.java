package com.community;
import nxt.util.Convert;
import nxt.util.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.community.Constants.MAX_SOURCE_SIZE;
import static java.security.MessageDigest.getInstance;

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
public class Executor {


    public static MessageDigest dig = null;

    static {
        try {
            dig = getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // Should always work
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String checkCodeAndReturnVerify(String elasticPL) throws Exception{
        if(elasticPL.length()>MAX_SOURCE_SIZE) throw new Exceptions.SyntaxErrorException("Code length exceeded");
        TokenManager t = new TokenManager();
        t.build_token_list(elasticPL);
        //t.dump_token_list();
        ASTBuilder.parse_token_list(t.state);
        int wcet = WCETCalculator.calc_wcet(t.state);
        int verify_wcet = WCETCalculator.get_verify_wcet(t.state);
        //System.out.println("Debug: WCETS " + wcet + ", verify " + verify_wcet);
        if(wcet > Constants.ABSOLUTELY_MAXIMUM_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_WCET + " exceeded: your script has a WCET of " + wcet + ".");
        }
        if(verify_wcet < 0){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET has a strange value.");
        }
        if(verify_wcet > Constants.ABSOLUTELY_MAXIMUM_VERIFY_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_VERIFY_WCET + " exceeded: your script has a verify function WCET of " + wcet + ".");
        }
        ByteCodeCompiler.build_bytecode(t.state);

        String result = "";
        for(int i=0;i<t.state.stack_code.size();++i){
            result += t.state.stack_code.get(i);
        }
        return result;
    }

    public static int toInt(final byte[] bytes, final int offset) {
        int ret = 0;
        for (int i = 0; (i < 4) && ((i + offset) < bytes.length); i++) {
            ret <<= 8;
            ret |= bytes[i + offset] & 0xFF;
        }
        return ret;
    }

    public static int[] personalizedIntStream(final byte[] publicKey, final long blockId, final byte[] multiplicator, final long workId) throws Exception {
        final int[] stream = new int[12];

        dig.reset();
        dig.update(multiplicator);
        dig.update(publicKey);

        System.out.println("Calculating Personalized Int Stream");
        System.out.println("Multiplicator: " + Convert.toHexString(multiplicator));
        System.out.println("PublicKey: " + Convert.toHexString(publicKey));

        final byte[] b1 = new byte[16];
        for (int i = 0; i < 8; ++i) b1[i] = (byte) (workId >> ((8 - i - 1) << 3));
        for (int i = 0; i < 8; ++i) b1[i + 8] = (byte) (blockId >> ((8 - i - 1) << 3));

        dig.update(b1);
        System.out.println("TotalBytes: " + (16+multiplicator.length+publicKey.length));

        System.out.println("b1: " + Convert.toHexString(b1));

        byte[] digest = dig.digest();

        System.out.println("Digest: " + Convert.toHexString(digest));

        int ln = digest.length;
        if (ln == 0) {
            throw new Exception("Bad digest calculation");
        }

        int[] multi32 = Convert.byte2int(multiplicator);
        System.out.println("Resultierende Ints");

        for (int i = 0; i < 10; ++i) {
            int got = toInt(digest, (i * 4) % ln);
            if (i > 4) got = got ^ stream[i - 3];
            stream[i] = got;
            System.out.println(i + ": " + Integer.toHexString(stream[i]));

        }
        stream[10] = multi32[1];
        stream[11] = multi32[2];
        System.out.println("10" + ": " + Integer.toHexString(stream[10]));
        System.out.println("11" + ": " + Integer.toHexString(stream[11]));


        return stream;
    }



    public static CODE_RESULT executeCode(final byte[] publicKey, final long blockId, final long workId, String
            verifyCode, byte[] multiplier, int[] storage, int[] validator, int validator_offset_index, boolean verify_pow, int[]
            target, byte[] pow_hash, Primitives.STATE state){

        CODE_RESULT result = new CODE_RESULT();
        result.bty = false;
        result.pow = false;
        result.error = false;

        return result;
    }

    public static Pair<Integer, Integer> checkCodeAndReturnStorageSizeAndVERIIDX(String elasticPL) throws Exceptions.SyntaxErrorException {
        if(elasticPL.length()>MAX_SOURCE_SIZE) throw new Exceptions.SyntaxErrorException("Code length exceeded");
        TokenManager t = new TokenManager();
        t.build_token_list(elasticPL);
        ASTBuilder.parse_token_list(t.state);
        int wcet = WCETCalculator.calc_wcet(t.state);
        int verify_wcet = WCETCalculator.get_verify_wcet(t.state);
        //System.out.println("Debug: WCETS " + wcet + ", verify " + verify_wcet);
        if(wcet > Constants.ABSOLUTELY_MAXIMUM_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_WCET + " exceeded: your script has a WCET of " + wcet + ".");
        }
        if(verify_wcet < 0){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET has a strange value.");
        }
        if(verify_wcet > Constants.ABSOLUTELY_MAXIMUM_VERIFY_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_VERIFY_WCET + " exceeded: your script has a verify function WCET of " + wcet + ".");
        }
        return new Pair<>(t.state.ast_submit_sz, t.state.ast_submit_idx);
    }

    public static class CODE_RESULT {
        public boolean pow;
        public boolean bty;
        public boolean error;
    }
}
