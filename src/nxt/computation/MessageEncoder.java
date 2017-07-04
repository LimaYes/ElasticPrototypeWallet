package nxt.computation;

import nxt.Appendix;

import java.nio.ByteBuffer;
import java.util.Arrays;

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


public class MessageEncoder {

    static byte[] MAGIC = {(byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};

    public static byte[] encodeAttachment(){
        return null;
    }

    static byte[] merge(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static Appendix.PrunablePlainMessage encodeAttachment(IComputationAttachment att){
        try {
            Appendix.PrunablePlainMessage pl = new Appendix.PrunablePlainMessage(merge(MAGIC,att.getByteArray()));
            return pl;
        }catch(Exception e){
            return null;
        }
    }

    public static IComputationAttachment decodeAttachment(Appendix.PrunablePlainMessage m){
        try {
            if (!MessageEncoder.checkMessageForPiggyback(m)) return null;

            byte[] msg = m.getMessage();
            byte[] relevance = Arrays.copyOfRange(msg, MessageEncoder.MAGIC.length, msg.length);

            if (relevance.length == 0) return null; // safe guard
            byte messageType = relevance[0];
            relevance = Arrays.copyOfRange(relevance, 1, relevance.length);

            if(messageType == CommandsEnum.CREATE_NEW_WORK.getCode()){
                return new CommandNewWork(ByteBuffer.wrap(relevance));
            }
            else{
                return null;
            }
        }catch(Exception e){
            return null; // failed parsing
        }
    }

    public static boolean checkMessageForPiggyback(Appendix.PrunablePlainMessage plainMessage){

        try {
            if (plainMessage.isText())
                return false;

            byte[] msg = plainMessage.getMessage();
            if (msg.length < MAGIC.length) return false;

            for (int i = 0; i < MAGIC.length; ++i) {
                if (msg[i] != MAGIC[i]) return false;
            }

            return true;
        }catch(Exception e){
            return false;
        }
    }
}
