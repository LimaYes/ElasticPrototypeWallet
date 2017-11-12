package nxt.execution;

import nxt.PowAndBounty;
import org.junit.Test;

import java.util.ArrayList;

public class RetargetingTest {
    @Test
    public void testFakeChain(){
        ArrayList<FakePowAndBounty> p = new ArrayList<>();
        FakePowAndBounty last = null;
        for(int i=0;i<200;++i){
            FakePowAndBounty b = new FakePowAndBounty(last);
            last = b;
            p.add(b);
        }
        System.out.println("Last Target: " + last.myCurrentTarget().toString(16));
    }
}
