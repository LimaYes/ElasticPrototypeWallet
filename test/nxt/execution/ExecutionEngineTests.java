package nxt.execution;

import com.community.Executor;
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

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    delight.rhinosandox.RhinoSandbox sandbox = delight.rhinosandox.RhinoSandboxes.create();

    @Before
    public void initEngine() {
        sandbox.setInstructionLimit(10000000);
        sandbox.setMaxDuration(15 * 1000);
    }

    @Test
    public void notAllowedAccess() {
        sandbox.eval("epl", "java.lang.System.out.println('hello');");
    }

    @Test
    public void injectedObjectDisallowed() {
        sandbox.inject("fromJava", new Object());
        sandbox.eval("epl", "fromJava.getClass();");
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
    public void doubleInjectionFailure() {
        boolean aborted = false;
        try {
            int[] u = new int[]{4, 6};
            sandbox.inject("u", u);
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

    @Test
    public void testLiveImplementation(){
        String code = "function verify() {\treturn ((s[1]) == (0) != 0 ? 1 : 0);}";
        int[] s = new int[]{9,0,3,1,4,5,5,5,5};
        Assert.assertTrue(Executor.executeCode(code, s));

        code = "function verify() {\treturn ((s[1]) == (0) != 0 ? 1 : 0);}";
        s = new int[]{9,4,3,1,4,5,5,5,5};
        Assert.assertFalse(Executor.executeCode(code, s));
    }

    @Test
    public void testCompile(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/test2.epl", Charset.defaultCharset());
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
    public void testCompileAndExecute(){
        String code = null;
        boolean threw_exception = false;
        try {
            code = readFile("test/testfiles/test2.epl", Charset.defaultCharset());
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
}
