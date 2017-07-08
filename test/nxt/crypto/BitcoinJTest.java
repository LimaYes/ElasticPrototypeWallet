package nxt.crypto;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.KeyCrypter;
import org.junit.Test;

import java.math.BigInteger;
import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.assertTrue;

/**
 * Created by anonymous on 26.05.17.
 */


public class BitcoinJTest {

    private static CharSequence PASSWORD1 = "my hovercraft has eels";
    private KeyCrypter keyCrypter;

    @Test
    public void testAvailabilityOfLibrary() {
        BigInteger privkey = new BigInteger(1, HEX.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
        ECKey key = ECKey.fromPrivate(privkey);
        byte[] output = key.sign(Sha256Hash.ZERO_HASH).encodeToDER();
        System.out.println("Zerosignature: " + HEX.encode(output));
        assertTrue(key.verify(Sha256Hash.ZERO_HASH.getBytes(), output));
    }
}
