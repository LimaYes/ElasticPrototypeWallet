package nxt.execution;

import com.community.CTypeInts.Int32_t;
import com.community.CTypeInts.Uint32_t;
import com.community.CTypeInts.Uint64_t;
import com.community.Constants;
import com.community.Executor;
import com.community.Primitives;
import com.community.TokenManager;
import org.h2.schema.Constant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


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
public class ExecutionEngineTests {

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    delight.rhinosandox.RhinoSandbox sandbox = delight.rhinosandox.RhinoSandboxes.create();

    @Before
    public void initEngine() {
        sandbox.setInstructionLimit(10000000);
        sandbox.setMaxDuration(15 * 1000);
        sandbox.allow(Uint32_t.class);
        sandbox.allow(Int32_t.class);

        sandbox.allow(Uint64_t.class);
        sandbox.allow(java.lang.String.class);
    }

    @Test
    public void notAllowedAccess() {
        boolean aborted = false;
        try {
            sandbox.eval("epl", "java.lang.System.out.println('hello');");
        }catch(Exception e){
            aborted=true;
        }
        Assert.assertTrue(aborted);
    }

    @Test
    public void injectedObjectDisallowed() {
        boolean aborted = false;
        try {
            sandbox.inject("fromJava", new Object());
            sandbox.eval("epl", "fromJava.getClass();");
        }catch(Exception e){
            aborted=true;
        }
        Assert.assertTrue(aborted);

    }

    @Test
    public void intArrayInjection() {
        int[] u = new int[]{4, 6};
        sandbox.inject("u", u);
        Object res = sandbox.eval("epl", "function sum(a, b) { return a + b; } sum(u[0],u[1]);");
        System.out.println("injecting storage, did it work? " + res);
        Assert.assertEquals(res, new Double(10));
    }

    @Test
    public void overriding_global_variables_in_functions() {
        int[] u = new int[]{4, 6};
        sandbox.inject("u", u);
        Object res = sandbox.eval("epl", "function screw(){ u[0]=1; u[1]=1;} function sum(a, b) { screw(); return u[1] + u[0]; } sum();");
        System.out.println("manipulating global storage, did it work? " + res);
        Assert.assertEquals(res, new Double(2));
    }


    public int called_val = 0;
    public double expose1(int v0){
        System.out.println("Called from Exposed Function: " + v0);
        called_val = v0;
        return v0;
    }
    public double expose2(int[] v0){
        int c = v0[0]+v0[1];
        System.out.println("Called from Exposed Function: " + c);
        called_val = c;
        return c;
    }
    @Test
    public void intArrayInjectionAndRhinoExposure() {
        int[] u = new int[]{4, 6};
        sandbox.allow(ExecutionEngineTests.class);
        sandbox.inject("ExposedToRhino", this);
        sandbox.inject("u", u);
        Object res = sandbox.eval("epl", "function sum(a, b) { ExposedToRhino.expose1(a+b); return a + b; } sum(u[0],u[1]);");
        System.out.println("injecting storage, did it work? " + res);
        Assert.assertEquals(res, new Double(10));
        Assert.assertEquals(new Integer(called_val), new Integer(10));
    }
    @Test
    public void intArrayInjectionAndRhinoExposureUseARRAY() {
        int[] u = new int[]{4, 6};
        sandbox.allow(ExecutionEngineTests.class);
        sandbox.inject("ExposedToRhino", this);
        sandbox.inject("u", u);
        Object res = sandbox.eval("epl", "function sum(a, b) { ExposedToRhino.expose2(u); return a + b; } sum(u[0],u[1]);");
        System.out.println("injecting storage, did it work? " + res);
        Assert.assertEquals(res, new Double(10));
        Assert.assertEquals(new Integer(called_val), new Integer(10));
    }

    @Test
    public void doubleInjectionFailure() {
        boolean aborted = false;
        try {
            int[] u = new int[]{4, 6};
            sandbox.inject("u", u);
            Object res = sandbox.eval("epl", "function sum(a, b) { return a + b; } sum(u[0],u[1]);");
            System.out.println("injecting storage, did it work? " + res);
            Assert.assertEquals(res, new Double(10));
        } catch (Exception e) {
            e.printStackTrace();
            aborted = true;
        }
        Assert.assertTrue(aborted);
    }

    @Test
    public void executeCodeSimple() {
        Object res = sandbox.eval("epl", "function sum(a, b) { return a + b; } sum(1, 2);");
        Assert.assertEquals(res, new Double(3));
    }

    @Test
    public void syntaxError() {
        boolean aborted = false;
        try {
            sandbox.eval("epl", "syn;tax-error");
        } catch (Exception e) {
            aborted = true;
        }
        Assert.assertTrue(aborted);
    }

    @Test
    public void infiniteLoopAbortion() {
        boolean aborted = false;
        try {
            sandbox.eval("epl", "function loop_forever() { var a=1; while(true){a*a+1;} return a; } loop_forever();");
        } catch (Exception e) {
            e.printStackTrace();
            aborted = true;
        }
        Assert.assertTrue(aborted);
    }


    private Primitives.STATE st(){
        Primitives.STATE state = new Primitives.STATE();
        state.ast_vm_longs = 100;
        state.ast_vm_doubles = 100;
        state.ast_vm_floats = 100;
        state.ast_vm_uints = 100;
        state.ast_vm_ints = 100;
        state.ast_vm_ulongs = 100;

        return state;
    }
    @Test
    public void testCompile(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/btc.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);

            byte[] pubkey = new byte[32];
            long blockid = 4821857325L;
            long workid = 875385923335L;

            byte[] m = new byte[32];
            int[] v = new int[]{};
            int[] s = new int[]{9000,4,3,1,4,5,5,5,5};

            byte[] pow_hash = new byte[16];
            int validator_index = 0;


            Executor.CODE_RESULT cd = Executor.executeCode(pubkey, blockid, workid, epl, m, s, v, validator_index, true, new int[]{0,0}, pow_hash, st());
            Assert.assertFalse(cd.error);

            System.out.println("Result:\nbty\t" + cd.bty);
            System.out.println("pow\t" + cd.pow);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testCompileAndExecute2(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/bountytest.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
            byte[] pubkey = new byte[32];
            long blockid = 4821857325L;
            long workid = 875385923335L;

            byte[] m = new byte[32];
            int[] v = new int[]{};
            int[] s = new int[]{9000,4,3,1,4,5,5,5,5};
            byte[] pow_hash = new byte[16];
            int validator_index = 0;


            Executor.CODE_RESULT cd = Executor.executeCode(pubkey, blockid, workid, epl, m, s, v, validator_index,true, new int[]{0,0}, pow_hash, st());
            Assert.assertTrue(cd.bty);
            Assert.assertFalse(cd.error);
            System.out.println("Result:\nbty\t" + cd.bty);
            System.out.println("pow\t" + cd.pow);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testCompileAndExecute(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/btc.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
        } catch (Exception e) {
            e.printStackTrace();
            threw_exception = true;
        }
        Assert.assertFalse(threw_exception);
    }

    @Test
    public void testCompileErrorDueToSyntaxErrorInEPL(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/epl_with_syntax_error.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
        } catch (Exception e) {
            e.printStackTrace();
            threw_exception = true;
        }
        Assert.assertTrue(threw_exception);
    }

    @Test
    public void testCompileErrorDueToHighWCETInEPL(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/epl_with_too_high_wcet.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
        } catch (Exception e) {
            e.printStackTrace();
            threw_exception = true;
        }
        Assert.assertTrue(threw_exception);
    }

    @Test
    public void testFailUnsignedSignedConversio1(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/signed_unsigned_failure_1.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
            byte[] pubkey = new byte[32];
            long blockid = 4821857325L;
            long workid = 875385923335L;

            byte[] m = new byte[32];
            int[] v = new int[]{};
            int[] s = new int[]{9000,4,3,1,4,5,5,5,5};

            byte[] pow_hash = new byte[16];
            int validator_index = 0;

            Executor.CODE_RESULT cd = Executor.executeCode(pubkey, blockid, workid, epl, m, s, v, validator_index, true, new int[]{0,0}, pow_hash, st());
            Assert.assertFalse(cd.error);
            Assert.assertFalse(cd.bty);
            System.out.println("Result:\nbty\t" + cd.bty);
            System.out.println("pow\t" + cd.pow);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testOP1(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/OP1.epl", Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String epl = Executor.checkCodeAndReturnVerify(code);
            System.out.println(epl);
        } catch (Exception e) {
            e.printStackTrace();
            threw_exception = true;
        }
        Assert.assertFalse(threw_exception);
    }



    @Test
    public void uint_objects(){

        long test1 = 3147483647L;
        int test2 = (int)test1;
        int test3 = (int)(test1 & 0xFFFFFFFFL);
        System.out.println(test2 + " ==? " + test3);
        Assert.assertEquals(test2,test3);

        Object res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Uint32_t(a); } test1(\"-1\").half();");
        System.out.println("Test 1: 2147483647 !== " + res);
        Assert.assertEquals(res.toString(), "2147483647"); // Halving unsigned int

        res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Int32_t(a); } test1(\"-1\").half();");
        System.out.println("Test 2: 0 !== " + res);
        Assert.assertEquals(res.toString(), "0"); // halfing -1 with rounding down

        res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Uint64_t(a); } test1(\"-1\").half();");
        System.out.println("Test 3: 9223372036854775807 !== " + res);
        Assert.assertEquals(res.toString(), "9223372036854775807"); // Halving unsigned long

        res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Int64_t(a); } test1(\"-1\").half();");
        System.out.println("Test 4: 0 !== " + res);
        Assert.assertEquals(res.toString(), "0"); // halfing -1 as signed long with rounding down

        res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Uint32_t(a); } test1(\"-268435456\").shl(6);");
        System.out.println("Test 5: 0 !== " + res);
        Assert.assertEquals(res.toString(), "0"); // int32 shl overflow

        res = sandbox.eval("epl", "function test1(a) { return new com.community.CTypeInts.Int64_t(a); } test1(\"-268435456\").shl(6);");
        System.out.println("Test 6: -17179869184 !== " + res);
        Assert.assertEquals(res.toString(), "-17179869184"); // int64 shl no overflow


        for(int i = 0; i< Constants.epl_token.length; ++i){
            Primitives.EXP_TOKEN_LIST t = Constants.epl_token[i];
            System.out.println(t.type.toString().replace("TOKEN_","ENIGMA_") + "(0x00, " + t.len + ", " + t.inputs +
                    ", " + t
                    .prec + ", \"" + t.str + "\"),");
        }
    }
}
