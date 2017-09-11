package com.community;
import nxt.Appendix;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.Pair;
import org.mozilla.javascript.NativeArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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


    public static Object jsObj = new ExposedToRhino();
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
        CodeConverter.convert_verify(t.state);

        String result = "";
        for(int i=0;i<t.state.stack_code.size();++i){
            result += t.state.stack_code.get(i);
        }

        // Add global variables and functions
        String pre_code = "var pow_found = 0;\nvar bounty_found = 0;\n";
        pre_code += "function rotr32(word, shift) {\n" +
                "  return word << 32 - shift | word >>> shift;\n" +
                "}\n";

        pre_code += "function rotl32(word, shift) {\n" +
                "  return word << shift | word >>> 32 - shift;\n" +
                "}\n";

        // todo, make this better (and more correct)

        return pre_code + "\n" + result;
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

        final byte[] b1 = new byte[16];
        for (int i = 0; i < 8; ++i) b1[i] = (byte) (workId >> ((8 - i - 1) << 3));
        for (int i = 0; i < 8; ++i) b1[i + 8] = (byte) (blockId >> ((8 - i - 1) << 3));

        dig.update(b1);

        byte[] digest = dig.digest();
        int ln = digest.length;
        if (ln == 0) {
            throw new Exception("Bad digest calculation");
        }

        int[] multi32 = Convert.byte2int(multiplicator);

        for (int i = 0; i < 10; ++i) {
            int got = toInt(digest, (i * 4) % ln);
            if (i > 4) got = got ^ stream[i - 3];
            stream[i] = got;

        }
        stream[10] = multi32[1];
        stream[11] = multi32[2];

        return stream;
    }



    public static CODE_RESULT executeCode(final byte[] publicKey, final long blockId, final long workId, String
            verifyCode, byte[] multiplier, int[] storage, int[] validator, int validator_offset_index, boolean verify_pow, int[]
            target, byte[] pow_hash){

        // TODO: IMPLEMENT POW_HASH CHECK
        CODE_RESULT result = new CODE_RESULT();
        result.bty = false;
        result.pow = false;
        result.error = false;
        try {
            delight.rhinosandox.RhinoSandbox sandbox = delight.rhinosandox.RhinoSandboxes.create();
            sandbox.setInstructionLimit(Constants.INSTRUCTION_LIMIT);
            sandbox.setMaxDuration(Constants.SAFE_TIME_LIMIT);
            sandbox.allow(ExposedToRhino.class);
            sandbox.inject("s", storage); // todo, add extra elements to S[] as coralreefer proposed
            sandbox.inject("target", target);
            sandbox.inject("verify_pow", verify_pow?1:0);

            // Inject temp arrays
            int[] m = personalizedIntStream(publicKey, blockId, multiplier, workId);
            int[] u = new int[10000];

            // now, fill the validator uints! (also called "data" in xelminer)
            for(int i=0;i<validator.length;++i){
                u[validator_offset_index+i] = validator[i];
            }

            int[] i = new int[10000];
            float[] f = new float[10000];
            double[] d = new double[10000];
            sandbox.inject("u", u);
            sandbox.inject("m", i);
            sandbox.inject("f", f);
            sandbox.inject("d", d);


            // Add native java object for Rhino exposed POW functions
            sandbox.inject("ExposedToRhino", jsObj);

            String vcode = verifyCode + " verify(); function res(){ return [pow_found, " +
                    "bounty_found]; } " +
                    "res();";

            org.mozilla.javascript.NativeArray array = (NativeArray) sandbox.eval("epl", vcode);


            // todo: here is a lot debug stuff
            System.out.println("SZ: " + String.valueOf(storage.length) + ", VALIDATION_IDX: " + String.valueOf(validator_offset_index));
            System.out.println("Last POW Hash:");
            System.out.println("--------------------------");
            System.out.println(Convert.toHexString(ExposedToRhino.lastCalculatedPowHash));
            System.out.println("M Personalized Int Stream:");
            System.out.println("--------------------------");
            System.out.println(Arrays.toString(m));
            System.out.println("Storage Array (S Array):");
            System.out.println("------------------------");
            System.out.println(Arrays.toString(storage));
            System.out.println("Validator/Data Array:");
            System.out.println("---------------------");
            System.out.println(Arrays.toString(validator));
            System.out.println("\n\n");
            System.out.println(vcode); // todo, comment in to see what code is being executed, remove for production

            double p = (double) array.get(0);
            double b = (double) array.get(1);
            System.out.println(p + ", " + b);

            result.bty = b==1.0;
            result.pow = p==1.0;

            return result;
        }catch(Exception e){
            e.printStackTrace(); // todo, remove for production
            result.pow = false;
            result.bty = false;
            result.error = true;
            return result; // Failed execution (reason does not matter)
        }
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
        return new Pair<Integer, Integer>(t.state.ast_submit_sz, t.state.ast_submit_idx);
    }

    public static class CODE_RESULT {
        public boolean pow;
        public boolean bty;
        public boolean error;
    }
}
