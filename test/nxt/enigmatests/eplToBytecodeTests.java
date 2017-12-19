package nxt.enigmatests;

import com.community.Executor;
import nxt.helpers.FileReader;
import org.junit.Test;
import java.nio.charset.Charset;

public class eplToBytecodeTests {
    @Test
    public void conversionTest() throws Exception {
        String code = FileReader.readFile("test/testfiles/conversion_test_1.epl", Charset.forName("UTF-8"));
        String epl = Executor.checkCodeAndReturnVerify(code);
        System.out.println(epl);
    }
}
