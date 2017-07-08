package nxt.computation;

import nxt.Appendix;
import nxt.Constants;
import nxt.NxtException;
import nxt.Transaction;
import org.json.simple.JSONStreamAware;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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


    /*
    the reason that both are present is, that messages can be split up in multiple chunks. Each chunk must be identifies as a NON message, but the last chunk only triggers the message parsing
     warning: both must be of same length */
    static byte[] MAGIC = {(byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};
    static byte[] MAGIC_INTERMEDIATE = {(byte)0xef, (byte)0xbe, (byte)0xad, (byte)0xde, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};


    public static JSONStreamAware[] encodeTransactions(Appendix.PrunablePlainMessage[] msgs, String passphraseOrPubkey, boolean isPassphrase) throws NxtException {
        ArrayList<JSONStreamAware> array_tx = new ArrayList<>(msgs.length);

        // Transactions have to be created from "end to start" to get the "referenced tx hashes" chained up correctly
        for(int i=msgs.length-1; i>=0; --i){
            JSONStreamAware t = CustomTransactionBuilder.createTransaction(msgs[i], passphraseOrPubkey, isPassphrase);
            array_tx.add(t);
        }

        return array_tx.toArray(new JSONStreamAware[msgs.length]);
    }

    public static Appendix.PrunablePlainMessage[] encodeAttachment(IComputationAttachment att){
        try {
            ArrayList<Appendix.PrunablePlainMessage> preparation = new ArrayList<>();
            byte[] to_encode = att.getByteArray();
            int pos_counter=0;

            while(pos_counter<to_encode.length){
                int maximum_read = Math.min(Constants.MAX_PRUNABLE_MESSAGE_LENGTH - MAGIC.length, to_encode.length - pos_counter);
                byte[] msg = new byte[maximum_read + MAGIC.length];
                pos_counter += maximum_read;

                // now, depending on pos_counter decide whether MAGIC or MAGIC_INTERMEDIATE is appended
                if(pos_counter<to_encode.length)
                    System.arraycopy(MessageEncoder.MAGIC, 0, msg, 0, MessageEncoder.MAGIC.length);
                else
                    System.arraycopy(MessageEncoder.MAGIC_INTERMEDIATE, 0, msg, 0, MessageEncoder.MAGIC_INTERMEDIATE.length);

                System.arraycopy(to_encode, pos_counter-maximum_read, msg, MessageEncoder.MAGIC.length, maximum_read);
                Appendix.PrunablePlainMessage pl = new Appendix.PrunablePlainMessage(msg);
                preparation.add(pl);
            }

            return preparation.toArray(new Appendix.PrunablePlainMessage[preparation.size()]);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static IComputationAttachment decodeAttachment(Appendix.PrunablePlainMessage[] m){
        try {

            int total_length = 0;
            for(int i=0;i<m.length;++i) {
                if (!MessageEncoder.checkMessageForPiggyback(m[i])) return null;
                total_length = total_length + (m[i].getMessage().length - MessageEncoder.MAGIC.length);
            }

            byte[] work_package = new byte[total_length];
            int last_pos = 0;

            for(int i=0;i<m.length;++i) {
                byte[] msg = m[i].getMessage();
                System.arraycopy(msg, MessageEncoder.MAGIC.length, work_package, last_pos, msg.length-MessageEncoder.MAGIC.length);
                last_pos += msg.length;
            }

            if (work_package.length == 0) return null; // safe guard

            ByteBuffer wp_bb = ByteBuffer.wrap(work_package);

            byte messageType = wp_bb.get();
            if(messageType == CommandsEnum.CREATE_NEW_WORK.getCode()){
                return new CommandNewWork(wp_bb);
            }
            else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkMessageForPiggyback(Appendix.PrunablePlainMessage plainMessage){
        return checkMessageForPiggyback(plainMessage, false);
    }

    public static boolean checkMessageForPiggyback(Appendix.PrunablePlainMessage plainMessage, boolean onlyFinalMessageOfChain){

        try {
            if (plainMessage.isText())
                return false;

            byte[] msg = plainMessage.getMessage();
            if (msg.length < MAGIC.length) return false;

            boolean returned = true;
            for (int i = 0; i < MAGIC.length; ++i) {
                if (msg[i] != MAGIC[i]){
                    returned = false;
                    break;
                }
            }
            if(!returned && !onlyFinalMessageOfChain)
            {
                returned = true;
                for (int i = 0; i < MAGIC_INTERMEDIATE.length; ++i) {
                    if (msg[i] != MAGIC_INTERMEDIATE[i]){
                        returned = false;
                        break;
                    }
                }
            }
            return returned;
        }catch(Exception e){
            return false;
        }
    }
}
