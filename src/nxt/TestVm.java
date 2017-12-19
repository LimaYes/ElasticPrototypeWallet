package nxt;


import com.community.*;
import com.community.Constants;
import nxt.computation.ComputationConstants;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static com.community.Executor.personalizedIntStream;
import static nxt.Nxt.NXT_DEFAULT_TESTVM_PROPERTIES;
import static nxt.Nxt.loadProperties;


public class TestVm {

    private static class OutputStreamCombiner extends OutputStream {
        private List<OutputStream> outputStreams;

        public OutputStreamCombiner(List<OutputStream> outputStreams) {
            this.outputStreams = outputStreams;
        }

        public void write(int b) throws IOException {
            for (OutputStream os : outputStreams) {
                os.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream os : outputStreams) {
                os.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream os : outputStreams) {
                os.close();
            }
        }
    }

    private static final Properties defaultProperties = new Properties();

    static{
        loadProperties(defaultProperties,NXT_DEFAULT_TESTVM_PROPERTIES,true);
    }

    public static String getStringProperty(String name) {
        return getStringProperty(name, null, false);
    }

    public static String getStringProperty(String name, String defaultValue) {
        return getStringProperty(name, defaultValue, false);
    }

    private static ByteArrayOutputStream baos;
    private static PrintStream previous;
    private static PrintStream previous2;

    private static boolean capturing;

    public static void start() {
        if (capturing) {
            return;
        }

        capturing = true;
        previous = System.out;
        previous2 = System.err;
        baos = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner =
                new OutputStreamCombiner(Arrays.asList(previous, baos));
        OutputStream outputStreamCombiner2 =
                new OutputStreamCombiner(Arrays.asList(previous2, baos));
        PrintStream custom = new PrintStream(outputStreamCombiner);
        PrintStream custom2 = new PrintStream(outputStreamCombiner2);

        System.setOut(custom);
        System.setErr(custom2);
    }

    public static String stop() {
        if (!capturing) {
            return "";
        }

        System.setOut(previous);
        System.setErr(previous2);
        String capturedValue = baos.toString();

        baos = null;
        previous = null;
        previous2 = null;
        capturing = false;

        return capturedValue;
    }

    public static String getStringProperty(String name, String defaultValue, boolean doNotLog) {
        String value = defaultProperties.getProperty(name);
        if (value != null && !"".equals(value)) {
            Logger.logMessage(name + " = \"" + (doNotLog ? "{not logged}" : value) + "\"");
            return value;
        } else {
            Logger.logMessage(name + " not defined");
            return defaultValue;
        }
    }

    public static Boolean getBooleanProperty(String name) {
        String value = defaultProperties.getProperty(name);
        if (Boolean.TRUE.toString().equals(value)) {
            Logger.logMessage(name + " = \"true\"");
            return true;
        } else if (Boolean.FALSE.toString().equals(value)) {
            Logger.logMessage(name + " = \"false\"");
            return false;
        }
        Logger.logMessage(name + " not defined, assuming false");
        return false;
    }

    public static void main(String[] args) {
        String file = getStringProperty("nxt.test_file");
        String content = null;
        try {
            content = new Scanner(new File(file)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        String result = exec(content);
        //System.out.println(result);
    }

    public static synchronized String exec(String content){

        start();
        boolean dumpTokens = getBooleanProperty("nxt.dump_tokens");
        boolean dumpAst = getBooleanProperty("nxt.dump_ast");
        boolean dumpCode = getBooleanProperty("nxt.dump_code");
        boolean exec = getBooleanProperty("nxt.execute_code");
        try {
            TokenManager t = new TokenManager();
            t.build_token_list(content);

            if (dumpTokens) {
                Logger.logMessage("Dumping the token list now");
                System.out.flush();
                System.err.flush();
                System.out.println("--BEGIN TOKENS");

                t.dump_token_list();
                System.out.println("--END TOKENS");

            }

            ASTBuilder.parse_token_list(t.state);

            if (dumpAst) {
                Logger.logMessage("Dumping AST");

                System.out.flush();
                System.err.flush();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("--BEGIN AST");
                ASTBuilder.dump_vm_ast(t.state);
                System.out.println("--END AST");
            }

            int wcet = WCETCalculator.calc_wcet(t.state);
            int verify_wcet = WCETCalculator.get_verify_wcet(t.state);

            Logger.logMessage("WCET: " + wcet);
            System.out.println("Verify-WCET: " + verify_wcet);
            System.out.flush();
            System.err.flush();

            CodeConverter.convert_verify(t.state);

            String result = "";
            for (int i = 0; i < t.state.stack_code.size(); ++i) {
                result += t.state.stack_code.get(i);
            }


            String code = result;

            if (dumpCode) {
                Logger.logMessage("Dumping generated source code");
                System.out.flush();
                System.err.flush();
                System.out.println("--BEGIN CODE\n" + code + "\n--END CODE");
            }

            if (exec) {

                int validator_offset_index = 0;
                Logger.logMessage("We will now execute the code");

                // TODO: Execute code

            }

        }
        catch (Exception e) {
            Logger.logErrorMessage("The following syntax error has been found");
            System.out.flush();
            System.err.flush();
            e.printStackTrace();
        }
        return stop();
    }
    private static int[] fakeInts() {
        int[] m = new int[12]; //personalizedIntStream(publicKey, 123456789, multiplier, 12345);
        return m;
    }
}
