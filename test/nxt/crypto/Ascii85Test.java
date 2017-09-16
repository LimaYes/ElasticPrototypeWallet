package nxt.crypto;

import nxt.util.Ascii85;
import org.junit.Assert;
import org.junit.Test;

public class Ascii85Test {

    private static String code = "array_uint   1000;\n" +
            "submit_sz 32;\n" +
            "submit_idx 10;\n" +
            "\n" +
            "\n" +
            "function shit {\n" +
            "    u[99]=8;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "function main {\n" +
            "    u[1]=u[1]*132;\n" +
            "    u[2]=u[1]*54;\n" +
            "    verify();\n" +
            "}\n" +
            "\n" +
            "function verify {\n" +
            "    u[5]=0;\n" +
            "    u[1]=2;\n" +
            "    u[1]=m[1];\n" +
            "    shit();\n" +
            "    verify_bty (s[0]%1000==0);\n" +
            "    verify_pow (u[0],u[1],u[2],u[3]);\n" +
            "}";
    @Test
    public void testAvailabilityOfLibrary() {
        String enc = Ascii85.encode(code.getBytes());
        System.out.println("ENC: " + enc);

        String dec = new String(Ascii85.decode(enc));
        System.out.println("DEC: " + dec);

        Assert.assertEquals(code, dec);
    }

}
