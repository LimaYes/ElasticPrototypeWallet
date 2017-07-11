package nxt.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by anonymous on 04.06.17.
 */
public class JUnitEnvironment {

    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }


}
