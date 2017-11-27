package nxt;


import com.community.*;
import com.community.Constants;
import nxt.computation.ComputationConstants;
import nxt.util.Convert;
import nxt.util.Logger;
import org.mozilla.javascript.NativeArray;

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


    public static int[] getIntArray(Object x){
        NativeArray arr = (NativeArray) x;
        int [] array = new int[(int) arr.getLength()];
        for (Object o : arr.getIds()) {
            int index = (Integer) o;
            array[index] = (int) arr.get(index, null);
        }
        return array;
    }
    public static float[] getFloatArray(Object x){
        NativeArray arr = (NativeArray) x;
        float [] array = new float[(int) arr.getLength()];
        for (Object o : arr.getIds()) {
            int index = (Integer) o;
            array[index] = (int) arr.get(index, null);
        }
        return array;
    }
    public static double[] getDoubleArray(Object x){
        NativeArray arr = (NativeArray) x;
        double [] array = new double[(int) arr.getLength()];
        for (Object o : arr.getIds()) {
            int index = (Integer) o;
            array[index] = (int) arr.get(index, null);
        }
        return array;
    }
    public static long[] getLongArray(Object x){
        NativeArray arr = (NativeArray) x;
        long [] array = new long[(int) arr.getLength()];
        for (Object o : arr.getIds()) {
            int index = (Integer) o;
            array[index] = (int) arr.get(index, null);
        }
        return array;
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

            // Add global variables and functions
            String pre_code = "var pow_found = 0;\nvar bounty_found = 0;\n";
            pre_code += "function rotr32(word, shift) {\n" +
                    "  return word << 32 - shift | word >>> shift;\n" +
                    "}\n";

            pre_code += "function rotl32(word, shift) {\n" +
                    "  return word << shift | word >>> 32 - shift;\n" +
                    "}\n";
            pre_code += "function gcd(a, b) {\n" +
                    "    if ( ! b) {\n" +
                    "        return a;\n" +
                    "    }\n" +
                    "\n" +
                    "    return gcd(b, a % b);\n" +
                    "};";

            String code = pre_code + "\n" + result;
            if (dumpCode) {
                Logger.logMessage("Dumping generated source code");
                System.out.flush();
                System.err.flush();
                System.out.println("--BEGIN CODE\n" + code + "\n--END CODE");
            }

            if (exec) {

                int validator_offset_index = 0;
                Logger.logMessage("We will now execute the code");

                delight.rhinosandox.RhinoSandbox sandbox = delight.rhinosandox.RhinoSandboxes.create();
                sandbox.setInstructionLimit(com.community.Constants.INSTRUCTION_LIMIT);
                sandbox.setMaxDuration(Constants.SAFE_TIME_LIMIT); // TODO: Good idea?
                sandbox.allow(ExposedToRhino.class);

                // Add some dummy storae
                int[] s = new int[10000];
                sandbox.inject("s", s); // todo, add extra elements to S[] as coralreefer proposed

                int[] target = Convert.bigintToInts(ComputationConstants.TESTVM_WORK_TARGET, 4);
                sandbox.inject("target", target);
                sandbox.inject("verify_pow", false); // always go for the bounty check here

                // Inject temp arrays
                int[] m = fakeInts();

                int[] u = new int[t.state.ast_vm_uints];
                float[] f = new float[t.state.ast_vm_floats];
                double[] d = new double[t.state.ast_vm_doubles];
                long[] l = new long[t.state.ast_vm_longs];
                long[] ul = new long[t.state.ast_vm_ulongs];
                int[] i = new int[t.state.ast_vm_ints];

                // now, fill the validator uints! (also called "data" in xelminer)
                    /*
                    leave this out for now
                    for (int i = 0; i < validator.length; ++i) {
                        u[validator_offset_index + i] = validator[i];
                    }*/

                // TO CHECK; WE GIVE I ARRAY AS M-VARIABLE TO JS, BUT IT DOES NOT CONTAIN M IN ANY CASE! PLEASE ELABORATE ON THIS, THIS MIGHT BE THE ERROR! PUT I IN M AND YOURE POSSIBLY DONE


                // fill beginning of i (or better say m array) with deterministic stuff
                for (int pyx = 0; pyx < m.length; ++pyx)
                    i[pyx] = m[pyx];


                sandbox.inject("u", u);
                sandbox.inject("m", m);
                sandbox.inject("i", i);
                sandbox.inject("l", l);
                sandbox.inject("ul", ul);
                sandbox.inject("f", f);
                sandbox.inject("d", d);


                // Add native java object for Rhino exposed POW functions
                sandbox.inject("ExposedToRhino", Executor.jsObj);

                code = code + " verify(); function res(){ return [pow_found, " +
                        "bounty_found]; } " +
                        "res();";

                org.mozilla.javascript.NativeArray array = (NativeArray) sandbox.eval("epl", code);

                u = (int[]) sandbox.getGlobalScopeObject("u");
                m = (int[]) sandbox.getGlobalScopeObject("m");
                i = (int[]) sandbox.getGlobalScopeObject("i");
                l = (long[]) sandbox.getGlobalScopeObject("l");
                ul = (long[]) sandbox.getGlobalScopeObject("ul");
                f = (float[]) sandbox.getGlobalScopeObject("f");
                d = (double[]) sandbox.getGlobalScopeObject("d");



                // todo: here is a lot debug stuff
                System.out.println("Storagesize: " + String.valueOf(s.length) + ", VALIDATION_IDX: " + String.valueOf(validator_offset_index));
                System.out.println("\nM Personalized Int Stream:");
                System.out.println("--------------------------");
                System.out.println("MARR: " + Arrays.toString(m));

                if(s != null) {
                    System.out.println("\nStorage Array (S Array):");
                    System.out.println("------------------------");
                    System.out.println("ARR-S: " + Arrays.toString(s));
                }

                if(u.length>0){
                    System.out.println("\nu Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-U: " + Arrays.toString(u));
                }
                if(f.length>0){
                    System.out.println("\nf Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-F: " + Arrays.toString(f));
                }

                if(d.length>0){
                    System.out.println("\nd Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-D: " + Arrays.toString(d));
                }
                if(i.length>0){
                    System.out.println("\ni Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-I: " + Arrays.toString(i));
                }
                if(l.length>0){
                    System.out.println("\nl Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-L: " + Arrays.toString(l));
                }
                if(ul.length>0){
                    System.out.println("\nul Array");
                    System.out.println("------------------------");
                    System.out.println("ARR-UL: " + Arrays.toString(ul));
                }


                /*System.out.println("Validator/Data Array:");
                System.out.println("---------------------");
                System.out.println(Arrays.toString(validator));
                System.out.println("Raw Multiplicator Was::");
                System.out.println("---------------------");
                System.out.println(Convert.toHexString(multiplier));
                System.out.println("\n\n");
                System.out.println(vcode); // todo, comment in to see what code is being executed, remove for production
                */
                double p = (double) array.get(0);
                double b = (double) array.get(1);
                System.out.println("\nSolutions found?\nFound-POW:" + p + "\nFound-BTY:" + b);


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
